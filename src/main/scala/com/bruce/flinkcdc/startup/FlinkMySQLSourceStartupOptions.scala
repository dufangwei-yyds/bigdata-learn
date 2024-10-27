package com.bruce.flinkcdc.startup

import com.ververica.cdc.connectors.mysql.source.MySqlSource
import com.ververica.cdc.connectors.mysql.table.StartupOptions
import com.ververica.cdc.debezium.JsonDebeziumDeserializationSchema
import org.apache.flink.api.common.RuntimeExecutionMode
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment

import java.util.Properties

/**
 * Description: 设置MySQL CDC任务启动模式-使用DataStream API MySQL -> Console
 * Date: 2024/10/26
 *
 * @author bruce
 */
object FlinkMySQLSourceStartupOptions {
  def main(args: Array[String]): Unit = {
    //获取执行环境
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setRuntimeMode(RuntimeExecutionMode.AUTOMATIC)
    //设置全局并行度为4
    env.setParallelism(4)

    //开启Checkpoint
    //注意：在使用MySQL CDC 2.x版本时，如果不开启Checkpoint，则只能读取全量(快照)数据，无法读取增量（binlog）数据
    env.enableCheckpointing(5000)

    val prop = new Properties()
    //Caused by: java.security.cert.CertificateNotYetValidException:  NotBefore
    //原因是MySQL服务器的时间(2028年)和当前客户端机器的时间相差太大导致的
    prop.setProperty("useSSL","false")

    //借助于MySQL CDC组件指定数据源
    val mySqlSource = MySqlSource
      .builder[String]//String：表示MySqlSource采集的数据类型
      .hostname("10.1.102.122")//MySQL主机名或者IP
      .port(3306)//MySQL端口
      .username("root")//MySQL用户名，此用户需要对MySQL CDC监视的所有数据库都具有所需的权限
      .password("Dfw920130Q520,")//MySQL密码
      .databaseList("data")//设置需要获取的数据库，这里可以接收多个字符串参数，如果需要同步整个数据库，需要将tableList设置为 ".*"
      .tableList("data.goods")// 设置需要获取的表，这里也可以接收多个字符串参数，表名前面需要指定数据库名称
      /*
       * initial()：默认模式，表示任务在第一次启动时会对表中的数据进行全量快照读取，然后继续读取最新的Binlog数据实现增量读取，可以保证不漏数据，也不重复读数据
       *            后续重新启动任务时可以借助于之前Checkpoint保存的位置信息继续接着读取增量数据。
       * earliest()：表示跳过全量快照读取阶段，从最早可以读取的Binlog文件开头处开始读取数据（注意：Binlog日志文件可能会有多个，超时的日志文件会过期被删除）
       * latest()：表示跳过全量快照读取阶段，从最新的Binlog文件末尾处开始读取，只能读取任务启动之后Binlog中的新增数据
       * specificOffset(...)：表示跳过全量快照读取阶段，从指定的Binlog位点处开始读取。（位点可以通过Binlog文件名和偏移量指定）
       * timestamp(...)：表示跳过全量快照读取阶段，从指定的时间戳开始读取Binlog中的数据。
       */
//      .startupOptions(StartupOptions.initial())
//      .startupOptions(StartupOptions.earliest())
      .startupOptions(StartupOptions.latest())
      .jdbcProperties(prop)//在这里可以指定JDBC URL中的一些扩展参数
      .serverId("5400-5403")//serverId需要全局唯一，默认会在5400-6400之间生成一个随机数。
      //建议用户手工指定，可以指定一个具体的整数或者是一个整数范围，但是要保证该整数范围必须大于等于Source的并行度
      //例如：Source并行度为4，则整数范围内至少要包含4个数值，5400-5403
      .serverTimeZone("Asia/Shanghai")//默认不需要指定，它会自动获取MySQL服务器时区。如果指定，也要和MySQL服务器时区保持一致
      .deserializer(new JsonDebeziumDeserializationSchema())//指定数据反序列化类，可以将MySqlSource采集到的数据转换为指定格式
      .build()

    import org.apache.flink.api.scala._
    val text = env.fromSource(mySqlSource, WatermarkStrategy.noWatermarks(), "MySQL Source")

    text.print()

    env.execute("FlinkMySQLSourceStartupOptions")
  }

}
