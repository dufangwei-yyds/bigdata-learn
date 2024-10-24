package com.bruce.paimon.mergeengine.partialupdate

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
object FlinkDataStreamWriteToPaimonForPartialupdate {
  def main(args: Array[String]): Unit = {
    // 创建执行环境
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setRuntimeMode(RuntimeExecutionMode.STREAMING)
    val tEnv = StreamTableEnvironment.create(env)

    // 手动构造一个Changelog DataStream数据流
    val dataStream = env.fromElements(
//      Row.ofKind(RowKind.INSERT,"jack",Int.box(10),Int.box(175),null), //+I
//      Row.ofKind(RowKind.INSERT,"jack",null,null,"beijing"), //+I
      Row.ofKind(RowKind.INSERT,"jack",Int.box(11),null,null) //+I
    )(Types.ROW_NAMED(Array("name","age","height","city"),Types.STRING,Types.INT,Types.INT,Types.STRING))

    // 将DataStream转换为表
    val schema = Schema.newBuilder()
      .column("name",DataTypes.STRING().notNull())
      .column("age",DataTypes.INT())
      .column("height",DataTypes.INT())
      .column("city",DataTypes.STRING())
      .primaryKey("name")
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
        |CREATE TABLE IF NOT EXISTS `merge_engine_partialupdate`(
        |   name STRING,
        |   age INT,
        |   height INT,
        |   city STRING,
        |   PRIMARY KEY(name) NOT ENFORCED
        |) WITH(
        |   'changelog-producer' = 'lookup',
        |   'merge-engine' = 'partial-update',
        |   'partial-update.ignore-delete' = 'true'
        |)
        |""".stripMargin)

    // 向Paimon表中写入数据
    tEnv.executeSql(
     """
        |INSERT INTO `merge_engine_partialupdate`
        |SELECT name,age,height,city FROM t1
        |""".stripMargin
    )
  }

}
