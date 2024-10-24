package com.bruce.paimon.rescalebucket

import org.apache.flink.api.common.RuntimeExecutionMode
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment

/**
 * 使用FlinkSQL从Paimon表中读取数据
 * Created by dufangwei
 */
object FlinkSQLReadFromPaimon {
  def main(args: Array[String]): Unit = {
    //创建执行环境
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setRuntimeMode(RuntimeExecutionMode.STREAMING)
    val tEnv = StreamTableEnvironment.create(env)

    // 从配置文件中读取HDFS地址
    val hdfsAddress = System.getenv("HDFS_ADDRESS")
    if (hdfsAddress == null) {
      throw new IllegalArgumentException("Please set the HDFS_ADDRESS environment variable.")
    }

    //创建Paimon类型的Catalog hdfs://192.168.95.129:8020/paimon
    tEnv.executeSql(
      """
        |CREATE CATALOG paimon_catalog WITH(
        |    'type'='paimon',
        |    'warehouse'='$hdfsAddress'
        |)
       """.stripMargin)

    tEnv.executeSql("USE CATALOG paimon_catalog")

    //读取Paimon表中的数据，并且打印输出结果
      tEnv.executeSql(
          """
            |SELECT * FROM `paimon_catalog`.`default`.`word_filter`
            |""".stripMargin)
        .print()

  }
}
