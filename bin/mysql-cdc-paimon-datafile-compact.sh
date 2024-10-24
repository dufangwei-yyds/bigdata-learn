flink run \
-m yarn-cluster \
-yjm 1024 \
-ytm 1024 \
-yD execution.checkpointing.interval=5sec \
-yD execution.runtime-mode=batch \
/Users/bruce/software/paimon/paimon-flink-action-0.8.2.jar \
compact \
--warehouse hdfs://localhost:8020/paimon \
--database default \
--table data_file_op \
--catalog-conf type=paimon

# -D classloader-check-leaked-classloader=false
# 这个参数在脚本中指定无效,需要在config.yml中配置: classloader.check-leaked-classloader: false

