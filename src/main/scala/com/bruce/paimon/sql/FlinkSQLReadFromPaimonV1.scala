package com.bruce.paimon.sql

import org.apache.flink.api.common.RuntimeExecutionMode
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment

/**
 * 使用Flink SQL从Paimon表中读取数据
 */
object FlinkSQLReadFromPaimonV1 {
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

    tEnv.executeSql("SHOW TABLES").print()

    //读取Paimon表中的数据
//    tEnv.executeSql(
//      """
//        |select * from cdc_stu
//        |""".stripMargin
//    ).print()

//    tEnv.executeSql(
//      """
//        |select * from `paimon_catalog`.`default`.`products`
//        |""".stripMargin
//    ).print()
//
//    tEnv.executeSql(
//      """
//        |select * from `paimon_catalog`.`default`.`users`
//        |""".stripMargin
//    ).print()

    tEnv.executeSql("select * from `chinese_code`").print()
  }
}

