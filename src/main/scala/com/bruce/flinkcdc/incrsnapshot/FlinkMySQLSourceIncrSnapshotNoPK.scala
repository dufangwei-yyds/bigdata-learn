package com.bruce.flinkcdc.incrsnapshot

import com.ververica.cdc.connectors.mysql.MySqlSource
import com.ververica.cdc.connectors.mysql.table.StartupOptions
import com.ververica.cdc.debezium.JsonDebeziumDeserializationSchema
import org.apache.flink.api.common.RuntimeExecutionMode
import org.apache.flink.configuration.{Configuration, RestOptions}
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment

/**
 * Description: 表中无主键
 * Date: 2024/10/26
 *
 * @author bruce
 */
object FlinkMySQLSourceIncrSnapshotNoPK {
  def main(args: Array[String]): Unit = {
    //获取执行环境
    val conf = new Configuration()
    //指定WebUI界面的访问端口，默认就是8081
    conf.setString(RestOptions.BIND_PORT,"8081")
    //为了便于在本地通过页面观察任务执行情况，所以开启本地WebUI功能
    val env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(conf)
    //为了便于观察每个组件
    env.disableOperatorChaining()

    env.setRuntimeMode(RuntimeExecutionMode.AUTOMATIC)
    //设置全局并行度为4
    env.setParallelism(4)

    //开启Checkpoint
    //注意：在使用MySQL CDC 2.x版本时，如果不开启Checkpoint，则只能读取全量(快照)数据，无法读取增量（binlog）数据
    env.enableCheckpointing(5000)

    /*val prop = new Properties()
    //Caused by: java.security.cert.CertificateNotYetValidException:  NotBefore
    //原因是MySQL服务器的时间(2028年)和当前客户端机器的时间相差太大导致的
    prop.setProperty("useSSL","false")*/


    //借助于MySQL CDC组件指定数据源
    val mySqlSource = MySqlSource// 注意：需要导包：com.ververica.cdc.connectors.mysql.MySqlSource
      .builder[String]//String：表示MySqlSource采集的数据类型
      .hostname("127.0.0.1")//MySQL主机名或者IP
      .port(3306)//MySQL端口
      .username("root")//MySQL用户名，此用户需要对MySQL CDC监视的所有数据库都具有所需的权限
      .password("Dfw920130Q520,")//MySQL密码
      .databaseList("data")//设置需要获取的数据库，这里可以接收多个字符串参数，如果需要同步整个数据库，需要将tableList设置为 ".*"
      .tableList("data.t1_no_pk")// 设置需要获取的表，这里也可以接收多个字符串参数，表名前面需要指定数据库名称
      .startupOptions(StartupOptions.initial())//注意不要导错包：com.ververica.cdc.connectors.mysql.table.StartupOptions
      .serverId(5400)//注意：老版本只支持单任务，所以这里只需要接收一个整数类型的参数即可
      .serverTimeZone("Asia/Shanghai")//默认不需要指定，它会自动获取MySQL服务器时区。如果指定，也要和MySQL服务器时区保持一致
      .deserializer(new JsonDebeziumDeserializationSchema())//指定数据反序列化类，可以将MySqlSource输出的数据转换为指定格式
      .build()

    import org.apache.flink.api.scala._
    val text = env.addSource(mySqlSource)// 注意：此时需要使用老的API，addSource

    text.print()

    env.execute("FlinkMySQLSourceIncrSnapshotNoPK")
  }

}
