echo "###### DELETING PREDICTION BATCH QUERY APPLICATION ######"

ALGORITHM=$1
if [ "kmeans" = $ALGORITHM ]
then
	cat scalable/spark-pi-predict-batch.yaml \
	  | sed 's/image: .*/image: swastikrug\/spark-app-kmeans/' \
	  | kubectl delete -f - -n spark-app

echo "->>> KMEANS Batch query stopped"
else
	cat scalable/spark-pi-predict-batch.yaml \
	  | sed 's/image: .*/image: swastikrug\/spark-app-knn/' \
	  | kubectl delete -f - -n spark-app 

echo "->>> KNN Batch query stopped"
fi


echo "###### PREDICTION BATCH QUERY STOPPED ######"
