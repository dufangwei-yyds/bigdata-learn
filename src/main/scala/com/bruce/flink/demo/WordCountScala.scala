//package com.bruce.flink.demo
//
//import org.apache.flink.api.common.RuntimeExecutionMode
//import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}
//import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
//import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows
//import java.time.Duration
//
///**
// * 词频统计
// * 基于最新Flink批流一体的API开发
// */
//object WordCountScala {
//  def main(args: Array[String]): Unit = {
//    // 获取执行环境
//    val env = StreamExecutionEnvironment.getExecutionEnvironment
//    // 指定处理模式,默认支持流处理模式,也支持批处理模式
//    /**
//     * AUTOMATIC: 根据程序中设置的数据源和算子,自动选择流处理还是批处理模式
//     * STREAMING: 强制使用流处理模式
//     * BATCH: 强制使用批处理模式
//     * 建议在客户端中使用 flink run 提交任务时通过-Dexecution.runtime-mode=BATCH指定
//     */
//    env.setRuntimeMode(RuntimeExecutionMode.AUTOMATIC)
//    // 指定数据源DataSource
//    val text = env.socketTextStream("127.0.0.1", 9999)
////    val text = env.readTextFile("/Users/bruce/workspace/bigdata-learn/data/test.txt")
//
//    // 定义时间戳分配器
//    val watermarkStrategy: WatermarkStrategy[String] =
//      WatermarkStrategy
//        .forBoundedOutOfOrderness[String](Duration.ofSeconds(5)) // 设置最大延迟时间为 5 秒
//        .withTimestampAssigner(new SerializableTimestampAssigner[String] {
//          override def extractTimestamp(element: String, recordTimestamp: Long): Long = {
//            // 为每个元素生成一个默认的时间戳
//            System.currentTimeMillis()
//          }
//        })
//
//    // 应用到数据流上
//    val textWithWatermark = text.assignTimestampsAndWatermarks(watermarkStrategy)
//
//    // 指定具体业务逻辑
//    import org.apache.flink.api.scala._
//    val wordCount = textWithWatermark.flatMap{_.split(" ")}
//      .map{(_,1)}
//      .keyBy(_._1)
//      .window(TumblingEventTimeWindows.of(Duration.ofSeconds(5)))
//      .sum(1)
//
//    // 指定DataSink
//    wordCount.print().setParallelism(1)
//
//    //执行程序
//    env.execute("WordCountScala")
//  }
//}
