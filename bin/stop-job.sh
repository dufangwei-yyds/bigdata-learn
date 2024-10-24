
  #_1
	flink stop --savepointPath hdfs://192.168.95.129:8020/flink/sap 8ac4eaabc394817501c89a4a552b9382 -yid application_1728706667197_0001
	# yarn application -kill application_1728706667197_0001

  #_3
  # yarn application -kill application_1728706667197_0002

  #_4
  # flink stop --savepointPath hdfs://192.168.95.129:8020/flink/sap 834c174ffcc13315f9c990d2a008f1a1 -yid application_1728706667197_0003
  # yarn application -kill application_1728706667197_0003
