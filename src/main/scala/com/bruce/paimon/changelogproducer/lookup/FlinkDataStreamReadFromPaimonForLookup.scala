package com.bruce.paimon.changelogproducer.lookup

import org.apache.flink.api.common.RuntimeExecutionMode
import org.apache.flink.configuration.{ConfigOptions, Configuration, RestOptions}
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment

/**
 * 使用Flink DataStream从Paimon表中读取数据
 */
object FlinkDataStreamReadFromPaimonForLookup {
  def main(args: Array[String]): Unit = {
    // 创建执行环境
    val conf = new Configuration()
    conf.setString("bind_port","8081")

    val env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(conf)
    env.setRuntimeMode(RuntimeExecutionMode.STREAMING)

    env.disableOperatorChaining()

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

    // 将计算结果Table转换为DataStream
    val execSql =
      """
        |select * from `changelog_lookup` --此时默认只能查到数据的最新值
        |--/*+ OPTIONS('scan.mode'='from-snapshot','scan.snapshot-id' = '1')*/
        |--通过动态表选项来指定数据扫描模式以及从哪里开始读取
        |""".stripMargin
    val table = tEnv.sqlQuery(execSql)
    table.execute().print()
  }
}
