package com.bruce.paimon.rescalebucket

import org.apache.flink.api.common.RuntimeExecutionMode
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment

/**
 * 使用Flink SQL向Paimon表中写入数据
 */
object FlinkSQLWriteToPaimonForBucket_3 {
  def main(args: Array[String]): Unit = {
    // 创建执行环境
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setRuntimeMode(RuntimeExecutionMode.BATCH) //切换到批处理模式,覆盖当前写入的分区中的数据

    env.setParallelism(10) // 设置全局并行度为10,因为结果表bucket是10

    val tEnv = StreamTableEnvironment.create(env)

    // 创建Paimon类型的Catalog
    tEnv.executeSql(
      """
        |CREATE CATALOG paimon_catalog WITH(
        |   'type' = 'paimon',
        |   'warehouse' = 'hdfs://192.168.95.129:8020/paimon'
        |)
        |""".stripMargin)

    tEnv.executeSql("USE CATALOG paimon_catalog")

    // 向结果表写入数据
    tEnv.executeSql(
      """
        |INSERT OVERWRITE `paimon_catalog`.`default`.`word_filter` PARTITION (dt = '20230101')
        |SELECT
        |    id,
        |    word
        |FROM `paimon_catalog`.`default`.`word_filter`
        |WHERE dt = '20230101'
        |""".stripMargin)
  }
}
