package com.bruce.paimon.rescalebucket

import org.apache.flink.api.common.RuntimeExecutionMode
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment

/**
 * 使用Flink SQL向Paimon表中写入数据
 */
object FlinkSQLWriteToPaimonForBucket_4 {
  def main(args: Array[String]): Unit = {
    // 创建执行环境
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setRuntimeMode(RuntimeExecutionMode.STREAMING)

    env.setParallelism(10) // 设置全局并行度为10,因为结果表bucket是10

    // 在流处理模式中,操作Paimon表时需要开启checkpoint
    env.enableCheckpointing(5000)
    //获取Checkpoint的配置对象
    val cpConfig = env.getCheckpointConfig
    //在任务故障和手工停止任务时都会保留之前生成的Checkpoint数据
    cpConfig.setExternalizedCheckpointCleanup(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    //设置Checkpoint后的状态数据的存储位置
    cpConfig.setCheckpointStorage("hdfs://192.168.95.129:8020/flink-chk/word_filter")

    val tEnv = StreamTableEnvironment.create(env)

    // 创建数据源表-普通表
    tEnv.executeSql(
      """
        |CREATE TABLE word_source(
        |    id BIGINT,
        |    word STRING
        |)WITH(
        |    'connector' = 'kafka',
        |    'topic' = 'paimon_word',
        |    'properties.bootstrap.servers' = '192.168.95.129:9092,192.168.95.130:9092,192.168.95.131:9092',
        |    'properties.group.id' = 'gid-paimon-1',
        |    'scan.startup.mode' = 'group-offsets',
        |    'properties.auto.offset.reset' = 'latest',
        |    'format' = 'json',
        |    'json.fail-on-missing-field' = 'false',
        |    'json.ignore-parse-errors' = 'true'
        |)
        |""".stripMargin)


    // 创建Paimon类型的Catalog
    tEnv.executeSql(
      """
        |CREATE CATALOG paimon_catalog WITH(
        |   'type' = 'paimon',
        |   'warehouse' = 'hdfs://192.168.95.129:8020/paimon'
        |)
        |""".stripMargin)

    tEnv.executeSql("USE CATALOG paimon_catalog")

    // 创建结果表-Paimon表
    tEnv.executeSql(
      """
        |CREATE TABLE IF NOT EXISTS word_filter(
        |    id BIGINT,
        |    word STRING,
        |    dt STRING,
        |    PRIMARY KEY(id,dt) NOT ENFORCED
        |) PARTITIONED BY(dt) WITH (
        |   'bucket' = '10'
        |)
        |""".stripMargin)

    // 向结果表写入数据
    tEnv.executeSql(
      """
        |INSERT INTO `paimon_catalog`.`default`.`word_filter`
        |SELECT id, word, '20230101' AS dt
        |FROM `default_catalog`.`default_database`.`word_source`
        |WHERE word <> 'hello11'
        |""".stripMargin)

  }
}
