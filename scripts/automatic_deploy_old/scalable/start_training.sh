kubectl apply -f spark-pi-training.yaml
kubectl patch svc spark-pi-training-ui-svc --type='json' -p '[{"op":"replace","path":"/spec/type","value":"NodePort"}]' --namespace=spark-app
minikube service spark-pi-training-ui-svc --namespace=spark-app
