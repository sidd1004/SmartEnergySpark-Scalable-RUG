statusCassandra=$(kubectl get pods --namespace=spark-app|grep "cassandra-0"|wc -l)
echo "Cassandra "$statusCassandra
until [ $statusCassandra -eq '1' ]
do
  echo "### Waiting for Cassandra"
  statusCassandra=`kubectl get pods --namespace=spark-app`|grep "cassandra-0"|wc -l
  sleep 15
done
echo "### CASSANDRA IS UP...."
