algorithm=$1
sbt clean package
#docker build --no-cache -t swastikrug/spark-app .
docker build -t swastikrug/spark-app-${algorithm} .
docker push swastikrug/spark-app-${algorithm}:latest