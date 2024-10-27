package com.bruce.flinkcdc.dynamicaddtable

import com.ververica.cdc.connectors.mysql.source.MySqlSource
import com.ververica.cdc.debezium.JsonDebeziumDeserializationSchema
import org.apache.flink.api.common.RuntimeExecutionMode
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.connector.base.DeliveryGuarantee
import org.apache.flink.connector.kafka.sink.{KafkaRecordSerializationSchema, KafkaSink}
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import java.util.Properties

/**
 * Description:
 * 动态加表功能-DataStream API
 * 1：首先采集goods表中的数据，采集之后，生成savepoint保存采集的位置信息
 * 2：修改代码，增加想要采集的新表，并且开启动态加表功能
 * 3：使用之前生成的savepoint重新提交任务
 *
 * kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic t1
 * flink run -m yarn-cluster  -c com.bruce.flinkcdc.dynamicaddtable.FlinkMySQLSourceToKafkaDynamicAddTable -yjm 1024 -ytm 1024 bigdata-learn-1.0-SNAPSHOT.jar
 * flink cancel -s hdfs://bigdata01:9000/flink/sap-cdc  2ddb9cb97b93eb3a8036e13c8c4346e7 -yid application_1851558023051_0006
 * yarn application -kill application_1851558023051_0006
 *
 * .tableList("data.goods,data.g1")// 设置需要获取的表，这里也可以接收多个字符串参数，表名前面需要指定数据库名称
 * .scanNewlyAddedTableEnabled(true)//开启动态加表功能
 * mvn clean package -DskipTests
 * flink run -m yarn-cluster -s hdfs://bigdata01:9000/flink/sap-cdc/savepoint-78b0da-586a20e0a040/_metadata  -c com.imooc.scala.dynamicaddtable.FlinkMySQLSourceToKafkaDynamicAddTable -yjm 1024 -ytm 1024 db_flinkcdc-1.0-SNAPSHOT.jar
 * INSERT INTO g1 VALUES (2, 'Huawei', 'this is desc');
 *
 * Date: 2024/10/27
 *
 * @author bruce
 */
object FlinkMySQLSourceToKafkaDynamicAddTable {
  def main(args: Array[String]): Unit = {
    //获取执行环境
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setRuntimeMode(RuntimeExecutionMode.AUTOMATIC)
    //设置全局并行度为4
    env.setParallelism(4)

    //开启Checkpoint
    env.enableCheckpointing(1000*10)//为了观察方便，在这里设置为10秒执行一次
    //获取Checkpoint的配置对象
    val cpConfig = env.getCheckpointConfig
    //在任务故障和手工停止任务时都会保留之前生成的Checkpoint数据
    cpConfig.setExternalizedCheckpointCleanup(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    //设置Checkpoint后的状态数据的存储位置
    cpConfig.setCheckpointStorage("hdfs://localhost:8020/flink-chk/mysql-cdc")

    val prop = new Properties()
    //Caused by: java.security.cert.CertificateNotYetValidException:  NotBefore
    //原因是MySQL服务器的时间(2028年)和当前客户端机器的时间相差太大导致的
    prop.setProperty("useSSL","false")

    //借助于MySQL CDC组件指定数据源
    val mySqlSource = MySqlSource
      .builder[String]//String：表示MySqlSource采集的数据类型
      .hostname("127.0.0.1")//MySQL主机名或者IP
      .port(3306)//MySQL端口
      .username("root")//MySQL用户名，此用户需要对MySQL CDC监视的所有数据库都具有所需的权限
      .password("Dfw920130Q520,")//MySQL密码
      .databaseList("data")//设置需要获取的数据库，这里可以接收多个字符串参数，如果需要同步整个数据库，需要将tableList设置为 ".*"
      .tableList("data.goods")// 设置需要获取的表，这里也可以接收多个字符串参数，表名前面需要指定数据库名称
      .jdbcProperties(prop)//在这里可以指定JDBC URL中的一些扩展参数
      .serverId("5400-5403")//serverId需要全局唯一，默认会在5400-6400之间生成一个随机数。
      //建议用户手工指定，可以指定一个具体的整数或者是一个整数范围，但是要保证该整数范围必须大于等于Source的并行度
      //例如：Source并行度为4，则整数范围内至少要包含4个数值，5400-5403
      .serverTimeZone("Asia/Shanghai")//默认不需要指定，它会自动获取MySQL服务器时区。如果指定，也要和MySQL服务器时区保持一致
      .deserializer(new JsonDebeziumDeserializationSchema())//指定数据反序列化类，可以将MySqlSource采集到的数据转换为指定格式
      .build()

    import org.apache.flink.api.scala._
    val text = env.fromSource(mySqlSource, WatermarkStrategy.noWatermarks(), "MySQL Source")

    //创建KafkaSink目的地
    val kafkaSink = KafkaSink
      //[String]：指定Kafka中数据(Value)的类型
      .builder[String]
      //指定Kafka集群地址
      .setBootstrapServers("localhost:9092")
      //指定序列化器，将数据流中的元素转换为Kafka需要的数据格式
      .setRecordSerializer(KafkaRecordSerializationSchema.builder()
        .setTopic("t1")
        .setValueSerializationSchema(new SimpleStringSchema())
        .build()
      )
      //指定KafkaSink提供的容错机制AT_LEAST_ONCE 或者 EXACTLY_ONCE
      .setDeliverGuarantee(DeliveryGuarantee.EXACTLY_ONCE)
      .build

    //注意：需要使用sinkTo，不能使用addSink
    text.sinkTo(kafkaSink)

    env.execute("FlinkMySQLSourceToKafkaDynamicAddTable")
  }

}
