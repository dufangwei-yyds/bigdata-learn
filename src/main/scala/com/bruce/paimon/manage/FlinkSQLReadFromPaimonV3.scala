package com.bruce.paimon.manage

import org.apache.flink.api.common.RuntimeExecutionMode
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment

/**
 * 使用Flink SQL从Paimon表中读取数据
 */
object FlinkSQLReadFromPaimonV3 {
  def main(args: Array[String]): Unit = {
    // 创建执行环境
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setRuntimeMode(RuntimeExecutionMode.BATCH)
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

    tEnv.executeSql(
        """
          |SELECT * FROM `paimon_catalog`.`default`.`auto_manage_tag`
          |/*+ OPTIONS('scan.tag-name' = '2024-10-04 20') */ -- 指定想要查询的标签的名称
          |""".stripMargin)
      .print()

  }
}

