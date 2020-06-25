echo "###### STARTING PREDICTION STREAM QUERY APPLICATION ######"

ALGORITHM=$1
if [ "kmeans" = $ALGORITHM ]
then
	cat scalable/spark-pi-predict-stream.yaml \
	  | sed 's/image: .*/image: swastikrug\/spark-app-kmeans/' \
	  | kubectl apply -f - -n spark-app

echo "->>> KMEANS Batch query deployed"
else
	cat scalable/spark-pi-predict-stream.yaml \
	  | sed 's/image: .*/image: swastikrug\/spark-app-knn/' \
	  | kubectl apply -f - -n spark-app 

echo "->>> KNN Batch query deployed"
fi

service_ready=$(kubectl describe service spark-pi-predict-stream-ui-svc -n spark-app|grep "spark-pi-predict-stream-ui-svc"|wc -l) 2>/dev/null

while [ $service_ready = 0 ]
do
sleep 5
service_ready=$(kubectl describe service spark-pi-predict-stream-ui-svc -n spark-app|grep "spark-pi-predict-stream-ui-svc"|wc -l) 2>/dev/null
done

echo "Service Ready: spark-pi-predict-stream-ui-svc"

kubectl patch svc spark-pi-predict-stream-ui-svc --type='json' -p '[{"op":"replace","path":"/spec/type","value":"LoadBalancer"}]' --namespace=spark-app

# Local
#kubectl patch svc spark-pi-predict-stream-ui-svc --type='json' -p '[{"op":"replace","path":"/spec/type","value":"NodePort"}]' --namespace=spark-app
# sleep 5
# minikube service spark-pi-predict-stream-ui-svc --namespace=spark-app

echo "###### PREDICTION STREAM QUERY DEPLOYED ######"
