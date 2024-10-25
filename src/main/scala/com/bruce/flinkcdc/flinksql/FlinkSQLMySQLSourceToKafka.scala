package com.bruce.flinkcdc.flinksql

import org.apache.flink.api.common.RuntimeExecutionMode
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment

/**
 * Description: 使用MySQL CDC实现MySQL数据采集 MySQL -> Kafka
 * Date: 2024/10/25
 *
 * @author bruce
 */
object FlinkSQLMySQLSourceToKafka {
  def main(args: Array[String]): Unit = {
    //创建执行环境
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setRuntimeMode(RuntimeExecutionMode.STREAMING)
    //设置全局并行度为4
    env.setParallelism(4)

    //开启Checkpoint
    //注意：在使用MySQL CDC 2.x版本时，如果不开启Checkpoint，则只能读取全量(快照)数据，无法读取增量（binlog）数据
    env.enableCheckpointing(5000)

    val tEnv = StreamTableEnvironment.create(env)

    //创建输入表(mysql-cdc)
    val inTableSql =
      """
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
        |  'database-name' = 'data',// 指定多个Database：(data|other)  支持正则表达式
        |  'table-name' = 'goods',//指定多个Table：(goods|other) 支持正则表达式 最终匹配的时候使用的是database-name.table-name
        |  'server-id' = '5400-5403',
        |  'server-time-zone' = 'Asia/Shanghai',
        |  'jdbc.properties.useSSL' = 'false'
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
        |  'topic' = 'goods_topic',
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
