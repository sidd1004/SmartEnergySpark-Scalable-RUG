echo "###### STARTING DATA STREAMER APPLICATION ######"

cat scalable/spark-pi-streamer.yaml \
  | sed 's/image: .*/image: swastikrug\/spark-app-kmeans/' \
  | kubectl apply -f - -n spark-app

service_ready=$(kubectl describe service spark-pi-streamer-ui-svc -n spark-app|grep "spark-pi-streamer-ui-svc"|wc -l) 2>/dev/null

while [ $service_ready = 0 ]
do
sleep 5
service_ready=$(kubectl describe service spark-pi-streamer-ui-svc -n spark-app|grep "spark-pi-streamer-ui-svc"|wc -l) 2>/dev/null
done

echo "Service Ready: spark-pi-streamer-ui-svc"

kubectl patch svc spark-pi-streamer-ui-svc --type='json' -p '[{"op":"replace","path":"/spec/type","value":"LoadBalancer"}]' --namespace=spark-app

# Local
#kubectl patch svc spark-pi-streamer-ui-svc --type='json' -p '[{"op":"replace","path":"/spec/type","value":"NodePort"}]' --namespace=spark-app
#sleep 5
#minikube service spark-pi-streamer-ui-svc --namespace=spark-app

echo "###### STREAMER DEPLOYED ######"
