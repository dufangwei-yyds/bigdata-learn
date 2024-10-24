package com.bruce.paimon.mergeengine.aggregation

import org.apache.flink.api.common.RuntimeExecutionMode
import org.apache.flink.api.common.typeinfo.Types
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment
import org.apache.flink.table.api.{DataTypes, Schema}
import org.apache.flink.table.connector.ChangelogMode
import org.apache.flink.types.{Row, RowKind}

/**
 * 使用Flink DataStream向Paimon表中写入数据
 */
object FlinkDataStreamWriteToPaimonForAggregation {
  def main(args: Array[String]): Unit = {
    // 创建执行环境
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setRuntimeMode(RuntimeExecutionMode.STREAMING)
    val tEnv = StreamTableEnvironment.create(env)

    // 手动构造一个Changelog DataStream数据流
    val dataStream = env.fromElements(
//      Row.ofKind(RowKind.INSERT,"1",Double.box(3.4),Int.box(10)), //+I
      Row.ofKind(RowKind.INSERT,"1",Double.box(2.12),Int.box(15)) //+I
    )(Types.ROW_NAMED(Array("id","price","count"),Types.STRING,Types.DOUBLE,Types.INT))

    // 将DataStream转换为表
    val schema = Schema.newBuilder()
      .column("id",DataTypes.STRING().notNull())
      .column("price",DataTypes.DOUBLE())
      .column("count",DataTypes.INT())
      .primaryKey("id")
      .build()
    val table = tEnv.fromChangelogStream(dataStream,schema,ChangelogMode.all())

    // 创建Paimon类型的Catalog
    tEnv.executeSql(
      """
        |CREATE CATALOG paimon_catalog WITH(
        |   'type' = 'paimon',
        |   'warehouse' = 'hdfs://localhost:8020/paimon'
        |)
        |""".stripMargin)

    tEnv.executeSql("USE CATALOG paimon_catalog")

    // 注册临时表
    tEnv.createTemporaryView("t1",table)

    // 创建Paimon类型的表
    tEnv.executeSql(
      """
        |CREATE TABLE IF NOT EXISTS `merge_engine_aggregation`(
        |   id STRING,
        |   price DOUBLE,
        |   `count` INT,
        |   PRIMARY KEY(id) NOT ENFORCED
        |) WITH(
        |   'changelog-producer' = 'lookup',
        |   'merge-engine' = 'aggregation',
        |   'fields.price.aggregate-function' = 'max',
        |   'fields.count.aggregate-function' = 'sum',
        |   'fields.price.ignore-retract' = 'true'
        |)
        |""".stripMargin)

    // 向Paimon表中写入数据
    tEnv.executeSql(
     """
        |INSERT INTO `merge_engine_aggregation`
        |SELECT id,price,`count` FROM t1
        |""".stripMargin
    )
  }

}
