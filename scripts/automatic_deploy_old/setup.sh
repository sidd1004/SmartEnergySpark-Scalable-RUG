echo "###### SETTING UP SPARK OPERATOR ######"
kubectl create namespace spark-app
kubectl create namespace spark-operator
helm repo add incubator https://kubernetes-charts-incubator.storage.googleapis.com
helm install incubator/sparkoperator --version 0.6.11 --generate-name --namespace spark-operator --set sparkJobNamespace=spark-app --set serviceAccounts.spark.name=spark --set serviceAccounts.spark.name=spark --set ingressUrlFormat="\{\{\$appName\}\}.ingress.cluster.com" --set service.type=NodePort

echo "###### STARTING CASSANDRA ######"
kubectl apply -f cassandra/

#echo "###### STARTING KAFKA ######"
#kubectl apply -f kafka/

statusCassandra=$(kubectl get pods --namespace=spark-app|grep "cassandra-0"|wc -l)
#statusKafka=$(kubectl get pods --namespace=spark-app|grep "kafka-2"|wc -l)
until [ $statusCassandra -eq 1 ]
do
  echo "### Waiting for Cassandra"
  sleep 15
  statusCassandra=$(kubectl get pods --namespace=spark-app|grep "cassandra-0"|wc -l)
done
echo "### CASSANDRA IS UP...."

#until [ $statusKafka -eq 1 ]
#do
#  echo "### Waiting for Kafka"
#  sleep 15
#  statusKafka=$(kubectl get pods --namespace=spark-app|grep "kafka-2"|wc -l)
#done
#echo "### KAFKA IS UP...."

kubectl apply -f scalable/spark-rbac.yaml


echo "###### STARTING KAFKA ######"

cat kafka_operator/strimzi-cluster-operator-0.17.0.yaml \
  | sed 's/namespace: .*/namespace: spark-app/' \
  | kubectl apply -f - -n spark-app 

kubectl apply -f kafka_operator/kafka-persistent-single.yaml -n spark-app 

kubectl wait kafka/my-cluster --for=condition=Ready --timeout=300s -n spark-app 

echo "###### KAFKA UP ######"
