flink run \
-m yarn-cluster \
-yjm 1024 \
-ytm 1024 \
-yD execution.checkpointing.interval=5sec \
-yD execution.checkpointing.externalized-checkpoint-retention=RETAIN_ON_CANCELLATION \
-yD state.backend.type=hashmap \
-yD state.checkpoint-storage=filesystem \
-yD state.checkpoints.dir=hdfs://localhost:8020/flink-chk/kafka-table-to-paimon \
/Users/bruce/software/paimon/paimon-flink-action-0.8.2.jar \
kafka_sync_table \
--warehouse hdfs://localhost:8020/paimon \
--database default \
--table cdc_products_test \
--primary-keys id \
--kafka-conf properties.bootstrap.servers=localhost:9092 \
--kafka-conf topic=products_test \
--kafka-conf properties.group.id=cdc002 \
--kafka-conf scan.startup.mode=latest-offset \
--kafka-conf value.format=canal-json \
--catalog-conf type=paimon \
--table-conf bucket=2 \
--table-conf changelog-producer=input \
--table-conf sink.parallelism=2
#-s hdfs://localhost:8020/flink-chk/kafka-table-to-paimon/b4bc1a13d938d7dfc279297c483abee7/chk-170/_metadata

