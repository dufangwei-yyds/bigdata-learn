package com.bruce.flink.demo

import org.apache.flink.api.common.RuntimeExecutionMode
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment

/**
 * 基于 collection 的 source 的使用
 * 这个 source 的主要应用场景是模拟测试代码流程时使用
 */
object StreamCollectionSourceScala {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    env.setRuntimeMode(RuntimeExecutionMode.AUTOMATIC)

    import org.apache.flink.api.scala._
    val text = env.fromCollection(Array(1, 2, 3, 4, 5))
    text.print().setParallelism(1)

    env.execute("StreamCollectionSourceScala")
  }
}
