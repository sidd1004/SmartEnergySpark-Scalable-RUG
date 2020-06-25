echo "###### STARTING CASSANDRA ######"
kubectl delete -f cassandra/

echo "###### STARTING KAFKA ######"
kubectl delete -f kafka/

echo "###### STOPPING JOBS ######"
kubectl delete -f scalable/ 

echo "###### DELETING SPARK OPERATOR ######"
id=$(helm list --namespace=spark-operator|grep -oP "sparkoperator-\d\d+")
helm delete $id --namespace=spark-operator


