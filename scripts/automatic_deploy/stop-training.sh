echo "###### DELETING TRAINING APPLICATION ######"

cat scalable/spark-pi-training.yaml \
  | sed 's/image: .*/image: swastikrug\/spark-app-knn/' \
  | kubectl delete -f - -n spark-app 

echo "###### TRAINING STOPPED ######"
