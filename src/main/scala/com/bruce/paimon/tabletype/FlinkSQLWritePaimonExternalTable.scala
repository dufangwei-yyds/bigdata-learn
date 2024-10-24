package com.bruce.paimon.tabletype

import org.apache.flink.api.common.RuntimeExecutionMode
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment

/**
 * 使用Flink SQL向Paimon外部表中写入数据
 */
object FlinkSQLWritePaimonExternalTable {
  def main(args: Array[String]): Unit = {
    // 创建执行环境
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setRuntimeMode(RuntimeExecutionMode.STREAMING)
    val tEnv = StreamTableEnvironment.create(env)

    // 创建Paimon外部表
    tEnv.executeSql(
      """
        |CREATE TABLE IF NOT EXISTS paimon_external_user2(
        |   name STRING,
        |   age INT,
        |   PRIMARY KEY(name) NOT ENFORCED
        |) WITH(
        |   'connector' = 'paimon',
        |   'path' = 'hdfs://localhost:8020/paimon/default.db/user2',
        |   'auto-create' = 'true'
        |)
        |""".stripMargin)

    // 写入数据
    tEnv.executeSql(
      """
        |INSERT INTO paimon_external_user2
        |VALUES
        |   ('Alice', 10),
        |   ('Bob', 20),
        |   ('Cary', 30)
        |""".stripMargin)
  /**
   * 因为paimon_external_user2这个表名是存储在Flink SQL中默认的基于内存的catalog中，当任务执行结束之后，paimon_external_user2这个表名就不存在了。
   最终Paimon中存储的表名是user2，这个表名来源于path路径中的名称。
   */
  // 查询数据
  //读取Paimon表中的数据，并且打印输出结果
//    tEnv.executeSql("show catalogs").print()
        tEnv.executeSql("show tables").print()
//  tEnv.executeSql(
//      """
//        |SELECT * FROM  `paimon_catalog`.`default`.`user2`
//        |""".stripMargin)
//    .print()
      tEnv.executeSql(
          """
            |SELECT * FROM  `default_catalog`.`default_database`.`paimon_external_user2`
            |""".stripMargin)
        .print()
  }
}
