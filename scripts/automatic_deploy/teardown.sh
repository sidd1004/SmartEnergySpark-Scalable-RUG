echo "###### STOPPING JOBS ######"
kubectl delete -f scalable/ 

echo "###### STOPING SPARK OPERATOR ######"
id=$(helm list --namespace=spark-operator|grep -oP "sparkoperator-\d\d+")
helm delete $id --namespace=spark-operator

echo "###### STOPING CASSANDRA ######"
kubectl delete -f cassandra/

echo "###### STOPING KAFKA ######"
kubectl delete -f kafka_operator/kafka-persistent-single.yaml -n spark-app 

cat kafka_operator/strimzi-cluster-operator-0.17.0.yaml \
  | sed 's/namespace: .*/namespace: spark-app/' \
  | kubectl delete -f - -n spark-app 

echo "###### FINISHED ######"


