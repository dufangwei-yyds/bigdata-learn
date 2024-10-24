flink run \
-m yarn-cluster \
-yjm 1024 \
-ytm 1024 \
/Users/bruce/software/paimon/paimon-flink-action-0.8.2.jar \
mysql_sync_database \
--warehouse hdfs://localhost:8020/paimon \
--database default \
--mysql-conf hostname=localhost \
--mysql-conf port=3306 \
--mysql-conf username=root \
--mysql-conf password=Dfw920130Q520, \
--mysql-conf database-name='data_ingestion' \
--mysql-conf server-time-zone=Asia/Shanghai \
--mysql-conf jdbc.properties.useSSL=false \
--catalog-conf type=paimon \
--table-conf bucket=2 \
--table-conf changelog-producer=input \
--table-conf sink.parallelism=2  \
--mode combined \
-yD execution.checkpointing.interval=5sec \
-yD execution.checkpointing.externalized-checkpoint-retention=RETAIN_ON_CANCELLATION \
-yD state.backend.type=hashmap \
-yD state.checkpoint-storage=filesystem \
-yD state.checkpoints.dir=hdfs://localhost:8020/flink-chk/mysql-table-to-paimon-database
# -s hdfs://localhost:8020/flink-chk/mysql-table-to-paimon/b4bc1a13d938d7dfc279297c483abee7/chk-170/_metadata

# 先全量同步 5 条数据, 再增量同步
# 如果Flink任务异常停止,如何重启任务(checkpoint)
# -s hdfs://localhost:8020/flink-chk/mysql-table-to-paimon-database/.../chk-40(最新的checkpoint)/_metadata




