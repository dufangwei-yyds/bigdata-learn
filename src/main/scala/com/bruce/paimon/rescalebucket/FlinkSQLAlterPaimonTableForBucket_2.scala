package com.bruce.paimon.rescalebucket

import org.apache.flink.api.common.RuntimeExecutionMode
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment

/**
 * 修改Paimon表属性
 */
object FlinkSQLAlterPaimonTableForBucket_2 {
  def main(args: Array[String]): Unit = {
    //创建执行环境
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setRuntimeMode(RuntimeExecutionMode.STREAMING)
    val tEnv = StreamTableEnvironment.create(env)

    //创建Paimon类型的Catalog
    tEnv.executeSql(
      """
        |CREATE CATALOG paimon_catalog WITH(
        |    'type'='paimon',
        |    'warehouse'='hdfs://192.168.95.129:8020/paimon'
        |)
        |""".stripMargin)
    tEnv.executeSql("USE CATALOG paimon_catalog")

    //修改bucket数量
    tEnv.executeSql(
      """
        |ALTER TABLE word_filter SET ('bucket' = '10')
        |""".stripMargin)

    //查看最新的表属性信息
    tEnv.executeSql(
        """
          |SHOW CREATE TABLE word_filter
          |""".stripMargin)
      .print()

  }
}

