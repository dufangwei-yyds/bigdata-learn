//package com.bruce.flink.demo;
//import org.apache.flink.api.common.RuntimeExecutionMode;
//import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
//import org.apache.flink.api.common.eventtime.WatermarkStrategy;
//import org.apache.flink.api.common.functions.FlatMapFunction;
//import org.apache.flink.api.java.tuple.Tuple2;
//import org.apache.flink.streaming.api.datastream.DataStream;
//import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
//import org.apache.flink.streaming.api.functions.source.datagen.DataGeneratorSource;
//import org.apache.flink.streaming.api.transformations.SourceTransformation;
//import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
//import org.apache.flink.util.Collector;
//import java.time.Duration;
//
///**
// * 词频统计
// * 基于最新Flink批流一体的API开发
// */
//public class WordCountJava {
//
//    public static void main(String[] args) throws Exception {
//
//        // 获取执行环境
//        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
//
//        // 指定处理模式，默认支持流处理模式，也支持批处理模式
//        /**
//         * AUTOMATIC: 根据程序中设置的数据源和算子,自动选择流处理还是批处理模式
//         * STREAMING: 强制使用流处理模式
//         * BATCH: 强制使用批处理模式
//         * 建议在客户端中使用 flink run 提交任务时通过-Dexecution.runtime-mode=BATCH指定
//         */
//        env.setRuntimeMode(RuntimeExecutionMode.AUTOMATIC);
//
//        // 指定数据源DataSource
//        DataStream<String> text = env.socketTextStream("127.0.0.1", 9999);
//        // DataStream<String> text = env.readTextFile("/Users/bruce/workspace/bigdata-learn/data/test.txt");
//
//        // 定义时间戳分配器
//        WatermarkStrategy<String> watermarkStrategy = WatermarkStrategy.<String>forBoundedOutOfOrderness(Duration.ofSeconds(5))
//                .withTimestampAssigner(new SerializableTimestampAssigner<String>() {
//                    @Override
//                    public long extractTimestamp(String element, long recordTimestamp) {
//                        return System.currentTimeMillis(); // 或者根据实际数据中的时间戳字段提取
//                    }
//                });
//
//        // 应用到数据流上
//        DataStream<String> textWithWatermark = text.assignTimestampsAndWatermarks(watermarkStrategy);
//
//        // 指定具体业务逻辑
//        DataStream<Tuple2<String, Integer>> wordCount = textWithWatermark
//                .flatMap(new FlatMapFunction<String, Tuple2<String, Integer>>() {
//                    @Override
//                    public void flatMap(String line, Collector<Tuple2<String, Integer>> out) throws Exception {
//                        for (String word : line.split(" ")) {
//                            out.collect(new Tuple2<>(word, 1));
//                        }
//                    }
//                })
//                .keyBy(data -> data.f0)
//                .window(TumblingEventTimeWindows.of(Duration.ofSeconds(5)))
//                .sum(1);
//
//        // 指定DataSink
//        wordCount.print().setParallelism(1);
//
//        // 执行程序
//        env.execute("WordCountJava");
//    }
//}
//
//
//
