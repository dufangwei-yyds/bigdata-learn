flink run \
/Users/bruce/software/paimon/paimon-flink-action-0.8.2.jar \
rollback-to \
--warehouse hdfs://localhost:8020/paimon \
--database default \
--table man_manage_tag \
--version t-20241004 \
--catalog-conf type=paimon




