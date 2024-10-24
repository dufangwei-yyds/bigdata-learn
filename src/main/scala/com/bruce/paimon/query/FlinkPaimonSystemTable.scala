package com.bruce.paimon.query

import org.apache.flink.api.common.RuntimeExecutionMode
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment

/**
 * 查询Paimon中的系统表
 */
object FlinkPaimonSystemTable {
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

    // snapshot信息表,对应的其实就是hdfs中表的snapshot目录下的snapshot-*文件信息
    println("============snapshot信息表==============")
    tEnv.executeSql("select * from query_table$snapshots").print()

    // schema信息表,对应的其实就是hdfs中表的schema目录下的schema-*文件信息
    println("============schema信息表==============")
    tEnv.executeSql("select * from query_table$schemas").print()

    // manifest信息表,对应的其实就是hdfs中表的manifest目录下的manifest-*文件信息
    println("============schema信息表==============")
    tEnv.executeSql("select * from query_table$manifests").print()

    // file信息表,对应的其实就是hdfs中表的bucket-*目录下的data-*文件信息
    println("============file信息表==============")
    tEnv.executeSql("select * from query_table$files").print()

    // option信息表,对应的其实就是建表语句中 with 里面指定的参数信息,在schema-*文件中也能看到 option 信息
    println("============option信息表==============")
    tEnv.executeSql("select * from query_table$options").print()

    // consumer信息表,在查询数据的sql 语句中指定了 consumer-id
    println("============consumer信息表==============")
    tEnv.executeSql("select * from query_table$consumers").print()

    // audit log信息表,相当于是表的审核日志,可以看到表中每条数据的rowkind,也就是+I/-D/+U/-U
    println("============audit log信息表==============")
    tEnv.executeSql("select * from query_table$audit_log").print()
  }
}
