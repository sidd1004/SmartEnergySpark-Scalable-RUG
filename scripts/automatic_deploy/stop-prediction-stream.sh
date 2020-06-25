echo "###### DELETING TRAINING APPLICATION ######"

ALGORITHM=$1
porttype="NodePort" #"LoadBalancer"
if [ "kmeans" = $ALGORITHM ]
then
	cat scalable/spark-pi-predict-stream.yaml \
	  | sed 's/image: .*/image: swastikrug\/spark-app-kmeans/' \
	  | kubectl delete -f - -n spark-app
else
	cat scalable/spark-pi-predict-stream.yaml \
	  | sed 's/image: .*/image: swastikrug\/spark-app-knn/' \
	  | kubectl delete -f - -n spark-app 

fi


echo "###### TRAINING STOPPED ######"
