flink run \
/Users/bruce/software/paimon/paimon-flink-action-0.8.2.jar \
create-tag \
--warehouse hdfs://localhost:8020/paimon \
--database default \
--table man_manage_tag \
--tag-name t-20241004 \
--snapshot 2 \
--catalog-conf type=paimon




