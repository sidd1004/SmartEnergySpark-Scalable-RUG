#!/bin/bash

set -e

#TAG=2.4.5-hadoop2.7
TAG=latest

build() {
    NAME=$1
    IMAGE=swastikrug/spark-$NAME:$TAG
    cd $([ -z "$2" ] && echo "./$NAME" || echo "$2")
    echo '--------------------------' building $IMAGE in $(pwd)
    docker build -t $IMAGE .
    cd -
}

build base
build master
build worker
build submit
build scala-app ../../back-end

docker push swastikrug/spark-base:latest
docker push swastikrug/spark-master:latest
docker push swastikrug/spark-worker:latest
docker push swastikrug/spark-scala-app:latest
