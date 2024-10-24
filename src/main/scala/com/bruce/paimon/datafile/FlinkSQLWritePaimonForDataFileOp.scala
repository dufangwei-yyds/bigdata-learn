package com.bruce.paimon.datafile

import org.apache.flink.api.common.RuntimeExecutionMode
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment

/**
 * 分析Paimon 底层文件变化
 */
object FlinkSQLWritePaimonForDataFileOp {
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

    // 创建Paimon的分区表
   tEnv.executeSql(
     """
       |CREATE TABLE IF NOT EXISTS data_file_op(
       |id INT,
       |code INT,
       |dt STRING,
       |PRIMARY KEY (id,dt) NOT ENFORCED
       |) PARTITIONED BY (dt)
       |""".stripMargin)

    // 第一次执行-写入数据
//    tEnv.executeSql(
//      """
//        |INSERT INTO data_file_op(id,code,dt)
//        |VALUES
//        |(1,1001,'20230501')
//        |""".stripMargin)

//    // 第二次执行-写入数据
//    tEnv.executeSql(
//      """
//        |INSERT INTO data_file_op(id,code,dt)
//        |VALUES
//        |(99,1099,'20230501'),
//        |(2,1002,'20230502'),
//        |(3,1003,'20230503'),
//        |(4,1004,'20230504'),
//        |(5,1005,'20230505'),
//        |(6,1006,'20230506'),
//        |(7,1007,'20230507'),
//        |(8,1008,'20230508'),
//        |(9,1009,'20230509'),
//        |(10,1010,'20230510')
//        |""".stripMargin)

    // 第三次执行-删除数据
//    tEnv.executeSql(
//      """
//        | DELETE FROM data_file_op WHERE dt >= '20230503'
//        |""".stripMargin)

    // 第四次执行-万千压缩表数据(触发full-compaction)

    // 第五次执行-修改表的属性,快照过期相关属性
//    tEnv.executeSql(
//      """
//        |ALTER TABLE data_file_op SET (
//        | 'snapshot.time-retained' = '1m',
//        | 'snapshot.num-retained.min' = '1',
//        | 'snapshot.num-retained.max' = '1'
//        |)
//        |""".stripMargin)
    // 第六次执行-触发快照过期,通过写入数据触发
//    tEnv.executeSql(
//      """
//        |INSERT INTO data_file_op(id,code,dt)
//        |VALUES
//        |(11,1011,'20230511')
//        |""".stripMargin)

//    tEnv.executeSql("select * from `paimon_catalog`.`default`.`data_file_op` /*+ OPTIONS('scan.mode'='from-snapshot','scan.snapshot-id'='1')*/").print()
  }

}
