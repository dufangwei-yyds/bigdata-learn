package com.bruce.paimon.bucket

import org.apache.flink.api.common.RuntimeExecutionMode
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment

/**
 * 验证Bucket特性
 */
object BucketDemo {
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

    // 创建Paimon表
    tEnv.executeSql(
      """
        |CREATE TABLE IF NOT EXISTS bucket_test(
        |word STRING,
        |cnt BIGINT,
        |PRIMARY KEY (word) NOT ENFORCED
        |) WITH (
        |  'bucket' = '2' -- 手动指定 bucket 的值,默认是 1
        |)
        |""".stripMargin)

    // 查看最完整的建表语句
    tEnv.executeSql("SHOW CREATE TABLE bucket_test").print()

    // 向表中添加数据
    tEnv.executeSql(
      """
        |INSERT INTO bucket_test (word,cnt)
        |VALUES ('hello', 1), ('world', 2), ('hello', 3), ('world', 4)
        |""".stripMargin)
  }
}
