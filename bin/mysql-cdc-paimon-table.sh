flink run \
-m yarn-cluster \
-yjm 1024 \
-ytm 1024 \
-yD execution.checkpointing.interval=5sec \
-yD execution.checkpointing.externalized-checkpoint-retention=RETAIN_ON_CANCELLATION \
-yD state.backend.type=hashmap \
-yD state.checkpoint-storage=filesystem \
-yD state.checkpoints.dir=hdfs://localhost:8020/flink-chk/mysql-table-to-paimon \
/Users/bruce/software/paimon/paimon-flink-action-0.8.2.jar \
mysql_sync_table \
--warehouse hdfs://localhost:8020/paimon \
--database default \
--table cdc_stu \
--primary-keys id \
--mysql-conf hostname=localhost \
--mysql-conf port=3306 \
--mysql-conf username=root \
--mysql-conf password=Dfw920130Q520, \
--mysql-conf database-name='data_ingestion' \
--mysql-conf table-name='stu' \
--mysql-conf server-time-zone=Asia/Shanghai \
--mysql-conf jdbc.properties.useSSL=false \
--catalog-conf type=paimon \
--table-conf bucket=2 \
--table-conf changelog-producer=input \
--table-conf sink.parallelism=2 \
-s hdfs://localhost:8020/flink-chk/mysql-table-to-paimon/b4bc1a13d938d7dfc279297c483abee7/chk-170/_metadata
# mysql-connector-java-8.0.27.jar
# paimon-flink-1.19-0.8.2.jar
# flink-sql-connector-mysql-cdc-3.1.0.jar
# flink-sql-connector-mysql-cdc-3.0.1.jar
# flink-connector-debezium-3.0.1.jar
# mysql_sync_table
# --mysql-conf database-name='data_ingestion'
# --mysql-conf table-name='stu'

# -s hdfs://localhost:8020/flink-chk/mysql-table-to-paimon/.../chk-40(最新的checkpoint)/_metadata

# 先全量同步 5 条数据, 再增量同步
# 如果Flink任务异常停止,如何重启任务(checkpoint)
# -s hdfs://localhost:8020/flink-chk/mysql-table-to-paimon/.../chk-40(最新的checkpoint)/_metadata

# server: 0.0.0.0/0.0.0.0:8032. Already tried 9 time(s); retry policy is RetryUpToMaximumCountWithFixedSleep(maxRetries=10, sleepTime=1000 MILLISECONDS)

