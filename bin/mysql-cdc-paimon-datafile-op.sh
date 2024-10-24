flink run \
-m yarn-cluster \
-yjm 1024 \
-ytm 1024 \
-yD execution.checkpointing.interval=5sec \
/Users/bruce/software/paimon/paimon-flink-action-0.8.2.jar \
delete \
--warehouse hdfs://localhost:8020/paimon \
--database default \
--table data_file_op \
--primary-keys id \
--where "dt >= '20230503'" \
--catalog-conf type=paimon

