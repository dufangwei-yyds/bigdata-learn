flink run \
/Users/bruce/software/paimon/paimon-flink-action-0.8.2.jar \
drop-partition \
--warehouse hdfs://localhost:8020/paimon \
--database default \
--table man_manage_par \
--partition dt=20230501,hh=10 \
--partition dt=20230502,hh=12 \
--catalog-conf type=paimon




