echo "###### STOPPING STREAMER APPLICATION ######"

cat scalable/spark-pi-streamer.yaml \
  | sed 's/image: .*/image: swastikrug\/spark-app-knn/' \
  | kubectl delete -f - -n spark-app 

echo "###### STREAMER STOPPED ######"
