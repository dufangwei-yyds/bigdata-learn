package com.bruce.paimon.tableop

import org.apache.flink.api.common.RuntimeExecutionMode
import org.apache.flink.api.common.typeinfo.Types
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment
import org.apache.flink.table.api.{DataTypes, Schema}
import org.apache.flink.table.connector.ChangelogMode
import org.apache.flink.types.{Row, RowKind}

/**
 *  动态修改Paimon表中的属性
 */
object FlinkPaimonTableOp {
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

    println("=========================创建表======================")

    // 创建Paimon类型的表
    tEnv.executeSql(
      """
        |CREATE TABLE IF NOT EXISTS table_op(
        |   name STRING,
        |   age INT,
        |   PRIMARY KEY(name) NOT ENFORCED
        |) WITH(
        |   'changelog-producer' = 'input'
        |)
        |""".stripMargin)

    // 查看表目前最新的消息
    tEnv.executeSql(
      """
        |SHOW CREATE TABLE table_op
        |""".stripMargin).print()

    println("===========表增加或修改属性==================")
    // 给已存在的表增加或修改属性
    // 如果此属性不存在,则是增加,如果此属性已存在,则是修改
    tEnv.executeSql(
      """
        |ALTER TABLE table_op SET ('write-buffer-size' = '100MB')
        |""".stripMargin
    )

    // 查看表目前最新的消息
    tEnv.executeSql(
      """
        |SHOW CREATE TABLE table_op
        |""".stripMargin).print()

    println("===========表移除属性==================")
    // 移除表中已存在的属性
    tEnv.executeSql(
      """
        |ALTER TABLE table_op RESET ('write-buffer-size')
        |""".stripMargin)

    // 查看表目前最新的消息
    tEnv.executeSql(
      """
        |SHOW CREATE TABLE table_op
        |""".stripMargin).print()
  }
}
