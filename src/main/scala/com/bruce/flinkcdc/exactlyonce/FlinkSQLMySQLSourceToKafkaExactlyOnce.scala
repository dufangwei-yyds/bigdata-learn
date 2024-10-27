package com.bruce.flinkcdc.exactlyonce

import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment
import org.apache.flink.api.common.RuntimeExecutionMode
import org.apache.flink.runtime.state.storage.FileSystemCheckpointStorage
import redis.clients.jedis.Jedis

/**
 * Description:
 * 验证MySQL CDC的仅一次语义
 * 1：第一次启动任务之后，先等待任务执行全量快照数据采集
 * 2：向MySQL中模拟产生几条增量数据，等待任务处理完
 * 3：手工停止任务
 * 4：任务停止期间再向MySQL中模拟产生几条新数据
 * 5：基于之前生成的checkpoint数据重新启动任务
 * 6：确认任务会不会重复执行全量快照数据采集，如果没有重复执行，则是对的，这样就不会重复采集全量快照数据了。
 * 7：确认任务有没有把任务停止期间MySQL产生的新数据采集到，如果采集到，则是对的，这样就不会丢数据了。
 *
 * kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic goods --property print.key=true
 * flink run -m yarn-cluster  -c com.bruce.flinkcdc.exactlyonce.FlinkSQLMySQLSourceToKafkaExactlyOnce -yjm 1024 -ytm 1024
 * 等待任务状态变成running之后，查看kafka控制台的输出结果
 *
 * 我们再向mysql中模拟产生几条数据：
 * INSERT INTO goods VALUES (default, 'test6', 'this is desc');
 * INSERT INTO goods VALUES (default, 'test7', 'this is desc');
 * INSERT INTO goods VALUES (default, 'test8', 'this is desc');
 *
 * 手工停止任务, 到HDFS中查看最终生成的checkpoint信息：
 * hdfs dfs -ls /flink-chk/mysql-cdc
 * hdfs dfs -ls /flink-chk/mysql-cdc/87b615d879f36b45b0b05004fad33dfc
 *
 * 在任务停止期间我们再向mysql中模拟产生几条数据：
 * INSERT INTO goods VALUES (default, 'test9', 'this is desc');
 * INSERT INTO goods VALUES (default, 'test10', 'this is desc');
 * INSERT INTO goods VALUES (default, 'test11', 'this is desc');
 *
 * 清空kafka控制台的输出结果，重新启动消费者，便于一会重启任务后观察：
 * kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic goods --property print.key=true
 *
 * flink run -m yarn-cluster -s hdfs://localhost:8020/flink-chk/mysql-cdc/87b615d879f36b45b0b05004fad33dfc/chk-11/_metadata  -c com.bruce.flinkcdc.exactlyonce.FlinkSQLMySQLSourceToKafkaExactlyOnce -yjm 1024 -ytm 1024 .jar
 * 等待任务状态变成running之后，查看kafka控制台的输出结果
 *
 * Date: 2024/10/26
 *
 * @author bruce
 */
object FlinkSQLMySQLSourceToKafkaExactlyOnce {
  def main(args: Array[String]): Unit = {
    // 创建执行环境
//    val config = new Configuration()
//    // 设置 checkpoint 存储类型和存储目录
//    config.set(CheckpointingOptions.CHECKPOINT_STORAGE, "filesystem")
//    config.set(CheckpointingOptions.CHECKPOINTS_DIRECTORY, "hdfs://localhost:8020/flink-chk/mysql-cdc")

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setRuntimeMode(RuntimeExecutionMode.STREAMING)
    //设置全局并行度为4
    env.setParallelism(4)

    //开启Checkpoint
    env.enableCheckpointing(1000*10)//为了观察方便，在这里设置为10秒执行一次
    //获取Checkpoint的配置对象
    val cpConfig = env.getCheckpointConfig
    //在任务故障和手工停止任务时都会保留之前生成的Checkpoint数据
    cpConfig.setExternalizedCheckpointCleanup(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)

    //设置Checkpoint后的状态数据的存储位置
    cpConfig.setCheckpointStorage(new FileSystemCheckpointStorage("hdfs://localhost:8020/flink-chk/mysql-cdc"))

    // 设置 savepoint 的存储位置
    //    env.setDefaultSavepointDirectory("hdfs://localhost:8020/flink-chk/mysql-cdc/87b615d879f36b45b0b05004fad33dfc/chk-11/_metadata")

    val tEnv = StreamTableEnvironment.create(env)

    // 使用 Redis 检查首次运行状态
    val redis = new Jedis("localhost", 6379) // 假设 Redis 在本地运行
    val isFirstRunKey = "flink:mysql-cdc:firstRun"
    var scanStartupMode = "initial"

    if (redis.exists(isFirstRunKey)) {
      scanStartupMode = "latest-offset" // 非首次运行，使用增量
    } else {
      redis.set(isFirstRunKey, "false") // 首次运行后将状态存储到 Redis
    }
    redis.close() // 关闭 Redis 连接

    // 动态设置 MySQL CDC 表的 scan.startup.mode
    val inTableSql =
      s"""
         |CREATE TABLE goods (
         |  id INT,
         |  name STRING,
         |  description STRING,
         |  PRIMARY KEY (id) NOT ENFORCED
         |) WITH (
         |  'connector' = 'mysql-cdc',
         |  'hostname' = '127.0.0.1',
         |  'port' = '3306',
         |  'username' = 'root',
         |  'password' = 'Dfw920130Q520,',
         |  'database-name' = 'data',
         |  'table-name' = 'goods',
         |  'server-id' = '5400-5403',
         |  'server-time-zone' = 'Asia/Shanghai',
         |  'jdbc.properties.useSSL' = 'false',
         |  'scan.startup.mode' = '$scanStartupMode'
         |);
         |""".stripMargin
    tEnv.executeSql(inTableSql)

    //创建输出表
    val outTableSql =
      """
        |CREATE TABLE kafka_sink(
        |  id INT,
        |  name STRING,
        |  description STRING,
        |  PRIMARY KEY(id) NOT ENFORCED
        |)WITH(
        |  'connector' = 'upsert-kafka',
        |  'topic' = 'goods',
        |  'properties.bootstrap.servers' = 'localhost:9092',
        |  'key.format' = 'json',
        |  'value.format' = 'json'
        |)
        |""".stripMargin
    tEnv.executeSql(outTableSql)


    //业务逻辑
    val execSql =
      """
        |INSERT INTO kafka_sink
        |SELECT
        |  id,
        |  name,
        |  description
        |FROM goods
        |""".stripMargin
    tEnv.executeSql(execSql)
  }
}
