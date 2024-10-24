package com.bruce.paimon.manage

import org.apache.flink.api.common.RuntimeExecutionMode
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment

/**
 * 使用Flink SQL手动管理Paimon表标签
 */
object FlinkSQLManagePaimonTagV2 {
  def main(args: Array[String]): Unit = {
    // 创建执行环境
    val env = StreamExecutionEnvironment.getExecutionEnvironment
//    env.setRuntimeMode(RuntimeExecutionMode.STREAMING)
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

    // 创建Paimon表
    tEnv.executeSql(
      """
        |CREATE TABLE IF NOT EXISTS man_manage_tag(
        |    id INT,
        |    name STRING,
        |    PRIMARY KEY (id) NOT ENFORCED
        |)
        |""".stripMargin)

//      tEnv.executeSql("INSERT INTO man_manage_tag(id,name) VALUES (1,'zhangsan')")
//      tEnv.executeSql("INSERT INTO man_manage_tag(id,name) VALUES (2,'tom')")
//      tEnv.executeSql("INSERT INTO man_manage_tag(id,name) VALUES (3,'jessic')")


    tEnv.executeSql(
        """
          |SELECT * FROM `paimon_catalog`.`default`.`man_manage_tag`
          |/*+ OPTIONS('scan.tag-name' = 't-20241004') */ -- 指定想要查询的标签的名称
          |""".stripMargin)
      .print()

  }

}
