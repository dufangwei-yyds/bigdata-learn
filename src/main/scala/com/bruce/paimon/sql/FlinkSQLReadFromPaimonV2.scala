package com.bruce.paimon.sql

import org.apache.flink.api.common.RuntimeExecutionMode
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment

/**
 * 使用Flink SQL从Paimon表中读取数据
 */
object FlinkSQLReadFromPaimonV2 {
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

    tEnv.executeSql("SHOW CREATE TABLE p_h_t2").print()

    tEnv.executeSql("alter table p_h_t2 reset('scan.snapshot-id')").print()
    tEnv.executeSql("SHOW CREATE TABLE p_h_t2").print()
  }
}

