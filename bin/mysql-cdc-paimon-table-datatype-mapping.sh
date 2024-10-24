flink run \
-m yarn-cluster \
-yjm 1024 \
-ytm 1024 \
-yD execution.checkpointing.interval=5sec \
/Users/bruce/software/paimon/paimon-flink-action-0.8.2.jar \
mysql_sync_table \
--warehouse hdfs://localhost:8020/paimon \
--database default \
--table data_type_mapping \
--primary-keys id \
--type-mapping tinyint1-not-bool \
--mysql-conf hostname=localhost \
--mysql-conf port=3306 \
--mysql-conf username=root \
--mysql-conf password=Dfw920130Q520, \
--mysql-conf database-name='data_ingestion' \
--mysql-conf table-name='data_type_mapping' \
--mysql-conf server-time-zone=Asia/Shanghai \
--mysql-conf jdbc.properties.useSSL=false \
--catalog-conf type=paimon \
--table-conf bucket=2 \
--table-conf changelog-producer=input \
--table-conf sink.parallelism=2

