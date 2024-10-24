package com.bruce.flinkcdc

import com.ververica.cdc.connectors.mysql.source.MySqlSource
import com.ververica.cdc.connectors.mysql.table.StartupOptions
import com.ververica.cdc.debezium.JsonDebeziumDeserializationSchema
import org.apache.flink.api.common.RuntimeExecutionMode
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.api.scala._
import org.apache.flink.connector.base.DeliveryGuarantee
import org.apache.flink.connector.kafka.sink.{KafkaRecordSerializationSchema, KafkaSink}
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import java.util.Properties
import org.apache.kafka.clients.consumer.ConsumerConfig

/**
 * 使用MySQL CDC实现MySQL数据采集
 * MySQL -> Kafka
 * kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic goods_topic
 */

object MySQLCDCToKafkaScala {
  def main(args: Array[String]): Unit = {
    // 创建 Flink 执行环境
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    env.setRuntimeMode(RuntimeExecutionMode.AUTOMATIC)
    //设置全局并行度为4
    env.setParallelism(4)

    //开启Checkpoint
    //注意：在使用MySQL CDC 2.x版本时，如果不开启Checkpoint，则只能读取全量(快照)数据，无法读取增量（binlog）数据
    env.enableCheckpointing(5000)

    val prop = new Properties()
    prop.setProperty("useSSL","false")

    // 配置 MySQL CDC Source
    val mySqlSource = MySqlSource.builder[String]()
      .hostname("localhost")
      .port(3306)
      .databaseList("data") // 监控的数据库
      .tableList("data.goods") // 监控的表
      .username("root")
      .password("Dfw920130Q520,")
      .tableList("data.goods") // 设置需要获取的表
      .serverId("5400-5403") //serverId需要全局唯一，默认会在5400-6400之间生成一个随机数。
      //建议用户手工指定，可以指定一个具体的整数或者是一个整数范围，但是要保证该整数范围必须大于等于Source的并行度
      //例如：Source并行度为4，则整数范围内至少要包含4个数值，5400-5403
      .serverTimeZone("Asia/Shanghai") //默认不需要指定，它会自动获取MySQL服务器时区。如果指定，也要和MySQL服务器时区保持一致
      .jdbcProperties(prop)//在这里可以指定JDBC URL中的一些扩展参数
      .deserializer(new JsonDebeziumDeserializationSchema()) // 自定义反序列化器，处理为 JSON 字符串
      .startupOptions(StartupOptions.initial()) // 从初始位置读取数据
      .build()

    // 读取 MySQL 数据流
    val mySqlDataStream = env.fromSource(
      mySqlSource,
      WatermarkStrategy.noWatermarks(),
      "MySQL CDC Source"
    )

    // Kafka 生产者配置
    val kafkaBroker = "localhost:9092"
    val kafkaTopic = "goods_topic"

    val properties = new Properties()
    properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBroker)
//    properties.setProperty(ProducerConfig.TRANSACTION_TIMEOUT_CONFIG, "90000")  // 设置事务超时时间为 90 秒

    // 配置 Kafka Sink
    val kafkaSink = KafkaSink.builder[String]()
      .setBootstrapServers(kafkaBroker)
      .setRecordSerializer(KafkaRecordSerializationSchema.builder()
        .setTopic(kafkaTopic)
        .setValueSerializationSchema(new SimpleStringSchema())
        .build())
      .setDeliveryGuarantee(DeliveryGuarantee.EXACTLY_ONCE) // 可以选择 AT_LEAST_ONCE 或 EXACTLY_ONCE
      .build()

    // 将数据写入 Kafka
    mySqlDataStream.sinkTo(kafkaSink)

    // 执行作业
    env.execute("MySQL CDC to Kafka")
  }
}
