package com.bruce.paimon.manage

import org.apache.flink.api.common.RuntimeExecutionMode
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment

/**
 * 使用Flink SQL管理Paimon分区
 */
object FlinkSQLManagePaimonPatitionTable {
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

    // 创建Paimon的分区表
   tEnv.executeSql(
     """
       |CREATE TABLE IF NOT EXISTS man_manage_par(
       |id INT,
       |name STRING,
       |dt STRING,
       |hh STRING,
       |PRIMARY KEY (id,dt,hh) NOT ENFORCED
       |) PARTITIONED BY (dt, hh)
       |""".stripMargin)

    // 向Paimon分区表中写入数据
//    tEnv.executeSql(
//      """
//        |INSERT INTO man_manage_par(id,name,dt,hh)
//        |VALUES
//        |(1,'zhangsan','20230501','08'),
//        |(2,'lisi','20230501','09'),
//        |(3,'wangwu','20230502','10'),
//        |(4,'zhaoliu','20230502','11')
//        |""".stripMargin)

    // 读取Paimon分区表数据
    tEnv.executeSql(
      """
        |SELECT * FROM `paimon_catalog`.`default`.`man_manage_par`
        |""".stripMargin
    ).print()

  }

}
