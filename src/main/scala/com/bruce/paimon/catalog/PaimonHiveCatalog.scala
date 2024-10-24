package com.bruce.paimon.catalog

import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment

/**
 * Paimon使用Hive Catalog
 * 运行之前需要首先启动 hive 的 metastore 服务,
 * 如果使用 hive 查询 paimon 表中数据,需要添加依
 *
 */
object PaimonHiveCatalog {
  def main(args: Array[String]): Unit = {
     //创建执行环境
     val env = StreamExecutionEnvironment.getExecutionEnvironment
    val tEnv = StreamTableEnvironment.create(env)

    // 创建Paimon类型的Catalog--使用Hive Catalog
    tEnv.executeSql(
      """
        |CREATE CATALOG paimon_hive_catalog WITH (
        |  'type' = 'paimon',
        |  'metastore' = 'hive',
        |  'uri' = 'thrift://localhost:9083',
        |  'warehouse' = 'hdfs://localhost:8020/paimon'
        |)
        |""".stripMargin
      )

    tEnv.executeSql("USE CATALOG paimon_hive_catalog")

    // 创建Paimon表
    tEnv.executeSql(
    """
      |CREATE TABLE IF NOT EXISTS p_h_t1 (
      |  name STRING,
      |  age INT,
      |  PRIMARY KEY (name) NOT ENFORCED
      |)
      |""".stripMargin
    )

    // 向表中插入数据
    tEnv.executeSql(
    """
      |INSERT INTO p_h_t1 (name,age)
      |VALUES ('jack', 18),('tom', 20)
      |""".stripMargin)

  }
}
