package com.bruce.flinkcdc.incrsnapshot

import org.apache.flink.api.common.RuntimeExecutionMode
import org.apache.flink.configuration.{Configuration, RestOptions}
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment

/**
 * Description: 表中无主键
 * Date: 2024/10/26
 *
 * @author bruce
 */
object FlinkSQLMySQLSourceIncrSnapshotNoPK {
  def main(args: Array[String]): Unit = {
    //获取执行环境
    val conf = new Configuration()
    //指定WebUI界面的访问端口，默认就是8081
    conf.setString(RestOptions.BIND_PORT,"8081")
    //为了便于在本地通过页面观察任务执行情况，所以开启本地WebUI功能
    val env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(conf)
    //为了便于观察每个组件
    env.disableOperatorChaining()

    env.setRuntimeMode(RuntimeExecutionMode.STREAMING)
    //设置全局并行度为4
    env.setParallelism(4)// -- TODO 注意：此时并行度设置失效，默认并行度为1

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
        |  'table-name' = 't1_no_pk',//指定多个Table：(goods|other) 支持正则表达式 最终匹配的时候使用的是database-name.table-name
        |  'scan.startup.mode' = 'initial', -- 默认
        |  -- 'scan.startup.mode' = 'earliest-offset', -- 从最早的Binlog位点处开始读取
        |  -- 'scan.startup.mode' = 'latest-offset', -- 从最新的Binlog位点处开始读取
        |  'scan.incremental.snapshot.enabled' = 'false', -- 禁用增量快照数据读取算法
        |  'server-id' = '5400',-- TODO 注意：此时只能设置一个serverid
        |  'server-time-zone' = 'Asia/Shanghai',
        |  'jdbc.properties.useSSL' = 'false'
        |);
        |""".stripMargin
    tEnv.executeSql(inTableSql)

    //创建输出表
    val outTableSql =
      """
        |CREATE TABLE print_sink(
        |  id INT,
        |  name STRING,
        |  description STRING
        |)WITH(
        |  'connector' = 'print'
        |)
        |""".stripMargin
    tEnv.executeSql(outTableSql)


    //业务逻辑
    val execSql =
      """
        |INSERT INTO print_sink
        |SELECT
        |  id,
        |  name,
        |  description
        |FROM goods
        |group by id,name,description
        |""".stripMargin
    tEnv.executeSql(execSql)
  }
}
