package com.bruce.flink.demo;

import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.api.connector.source.Source;
import org.apache.flink.api.connector.source.SourceSplit;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import java.util.Arrays;

/**
 * 基于 collection 的 source 的使用
 * 这个 source 的主要应用场景是模拟测试代码流程时使用
 */
public class StreamCollectionSourceJava {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
//        StreamExecutionEnvironment.createLocalEnvironment();
//        StreamExecutionEnvironment.createRemoteEnvironment("127.0.0.1",8888);
//        StreamExecutionEnvironment.createLocalEnvironmentWithWebUI();



        env.setRuntimeMode(RuntimeExecutionMode.AUTOMATIC);

        //DataStreamSource<Integer> text = env.fromData(Arrays.asList(1, 2, 3, 4, 5));
        DataStreamSource<Long> text = env.fromSequence(1, 5);

        text.print().setParallelism(1);

        env.execute("StreamCollectionSourceJava");
    }
}
