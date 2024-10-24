package com.bruce.paimon.manage

import org.apache.flink.api.common.RuntimeExecutionMode
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment

/**
 * 使用Flink SQL自动管理Paimon表标签
 */
object FlinkSQLManagePaimonTag {
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

    // 创建Paimon表
    tEnv.executeSql(
      """
        |CREATE TABLE IF NOT EXISTS auto_manage_tag(
        |    id INT,
        |    name STRING,
        |    PRIMARY KEY (id) NOT ENFORCED
        |) WITH (
        |    'tag.automatic-creation' = 'process-time', --指定创建模式：使用处理时间
        |    'tag.creation-period' = 'hourly', -- 指定创建频率：每小时创建1个
        |    'tag.creation-delay' = '5 m', -- 指定延迟时间：延迟5分钟生成标签
        |    'tag.num-retained-max' = '2' -- 指定允许保留的最大标签数量：2个
        |)
        |""".stripMargin)

    //假设2024-10-04 21:35执行，会触发生成一个标签，标签名称为：2023-10-01 21
//    tEnv.executeSql("INSERT INTO auto_manage_tag(id,name) VALUES (1,'zhangsan')")

    //假设21:40执行，不会触发生成标签
//    tEnv.executeSql("INSERT INTO auto_manage_tag(id,name) VALUES (2,'tom')")

    //假设22:35执行，会触发生成一个标签，标签名称为：2023-10-01 22
    tEnv.executeSql("INSERT INTO auto_manage_tag(id,name) VALUES (3,'jessic')")


    // 读取Paimon表数据
//    tEnv.executeSql(
//      """
//        |SELECT * FROM `paimon_catalog`.`default`.`auto_manage_tag`
//        |""".stripMargin
//    ).print()

    // tag信息表
    tEnv.executeSql("select * from auto_manage_tag$tags").print()

    /**
     * create external table auto_manage_tag_external
     * stored by 'org.apache.paimon.hive.PaimonStorageHandler'
     * location 'hdfs://localhost:8020/paimon/default.db/auto_manage_tag';
     *
     * set paimon.scan.tag-name = 2024-10-04 20;
     */

  }

}
