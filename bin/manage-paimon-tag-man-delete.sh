flink run \
/Users/bruce/software/paimon/paimon-flink-action-0.8.2.jar \
delete-tag \
--warehouse hdfs://localhost:8020/paimon \
--database default \
--table man_manage_tag \
--tag-name t-20241004 \
--catalog-conf type=paimon




