echo "###### STARTING TRAINING APPLICATION ######"
kubectl apply -f scalable/spark-pi-streaming.yaml
sleep 15
kubectl patch svc spark-pi-ui-svc --type='json' -p '[{"op":"replace","path":"/spec/type","value":"NodePort"}]' --namespace=spark-app
minikube service spark-pi-ui-svc --namespace=spark-app
echo "###### TRAINING DEPLOYED ######"
