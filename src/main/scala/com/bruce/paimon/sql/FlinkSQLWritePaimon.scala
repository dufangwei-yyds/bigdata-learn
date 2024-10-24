package com.bruce.paimon.sql

import org.apache.flink.api.common.RuntimeExecutionMode
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment

/**
 * 使用Flink SQL向Paimon表中写入数据
 */
object FlinkSQLWritePaimon {
  def main(args: Array[String]): Unit = {
    // 创建执行环境
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setRuntimeMode(RuntimeExecutionMode.STREAMING)
    val tEnv = StreamTableEnvironment.create(env)

    // 在流处理模式中,操作Paimon表时需要开启checkpoint
    env.enableCheckpointing(5000)

    // 创建数据源表-普通表
    tEnv.executeSql(
      """
        |CREATE TABLE IF NOT EXISTS word_source(
        |    word STRING
        |)WITH(
        |   'connector' = 'datagen',
        |   'fields.word.length' = '1',
        |   'rows-per-second' = '1'
        |)
        |""".stripMargin)

    // 创建Paimon类型的Catalog
    tEnv.executeSql(
      """
        |CREATE CATALOG paimon_catalog WITH(
        |   'type' = 'paimon',
        |   'warehouse' = 'hdfs://localhost:8020/paimon'
        |)
        |""".stripMargin)

    tEnv.executeSql("USE CATALOG paimon_catalog")

    // 创建结果表-Paimon表
    tEnv.executeSql(
      """
        |CREATE TABLE IF NOT EXISTS wc_sink_sql(
        |    word STRING,
        |    cnt BIGINT,
        |    PRIMARY KEY(word) NOT ENFORCED
        |)
        |""".stripMargin)

    // 向结果表写入数据
    tEnv.executeSql(
      """
        |INSERT INTO `paimon_catalog`.`default`.`wc_sink_sql`
        |SELECT word, COUNT(1) AS cnt
        |FROM `default_catalog`.`default_database`.`word_source`
        |GROUP BY word
        |""".stripMargin).print()

  }
}
