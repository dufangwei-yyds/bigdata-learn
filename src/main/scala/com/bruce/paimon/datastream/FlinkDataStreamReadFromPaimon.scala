package com.bruce.paimon.datastream

import org.apache.flink.api.common.RuntimeExecutionMode
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment

/**
 * 使用Flink DataStream从Paimon表中读取数据
 */
object FlinkDataStreamReadFromPaimon {
  def main(args: Array[String]): Unit = {
    // 创建执行环境
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setRuntimeMode(RuntimeExecutionMode.STREAMING)
    val tEnv = StreamTableEnvironment.create(env)

    // 创建Paimon类型的Catalog
    tEnv.executeSql(
      """
        |CREATE CATALOG paimon_catalog WITH(
        |   'type' = 'paimon',
        |   'warehouse' = 'hdfs://localhost:8020/paimon'
        |)
        |""".stripMargin)

    tEnv.executeSql("USE CATALOG paimon_catalog")

    // 将计算结果Table转换为DataStream
    val execSql =
      """
        |select * from `user` --此时默认只能查到数据的最新值
        |/*+ OPTIONS('scan.mode'='from-snapshot','scan.snapshot-id' = '1')*/
        |--通过动态表选项来指定数据扫描模式以及从哪里开始读取
        |""".stripMargin

    val table = tEnv.sqlQuery(execSql)

    // 将结果数据转换为Changelog DataStream数据流
    val dataStream = tEnv.toChangelogStream(table)

    // 将DataStream中的数据输出打印到控制台
    dataStream.print().setParallelism(1)

    // 执行任务
    env.execute("FlinkDataStreamReadFromPaimon")


    dataStream.print()

    env.execute("FlinkDataStreamReadFromPaimon")

  }
}
