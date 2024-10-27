package com.bruce.flinkcdc.metacolumn

import org.apache.flink.api.common.RuntimeExecutionMode
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment

/**
 * Description: 元数据列的使用
 * Date: 2024/10/27
 * @author bruce
 */
object FlinkSQLMySQLSourceToPrintMetaColumn {
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
        |  database_name STRING METADATA FROM 'database_name' VIRTUAL,
        |  table_name STRING METADATA FROM 'table_name' VIRTUAL,
        |  op_ts TIMESTAMP_LTZ(3) METADATA FROM 'op_ts' VIRTUAL,
        |  PRIMARY KEY (id) NOT ENFORCED
        |) WITH (
        |  'connector' = 'mysql-cdc',
        |  'hostname' = '127.0.0.1',
        |  'port' = '3306',
        |  'username' = 'root',
        |  'password' = 'Dfw920130Q520,',
        |  'database-name' = 'data',// 指定多个Database：(data|other)  支持正则表达式
        |  'table-name' = 'g.*',//指定多个Table：(goods|other) 支持正则表达式 最终匹配的时候使用的是database-name.table-name
        |  'server-id' = '5400-5403',
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
        |  description STRING,
        |  database_name STRING,
        |  table_name STRING,
        |  op_ts TIMESTAMP_LTZ(3)
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
        |  description,
        |  database_name,
        |  table_name,
        |  op_ts
        |FROM goods
        |""".stripMargin
    tEnv.executeSql(execSql)
  }

}