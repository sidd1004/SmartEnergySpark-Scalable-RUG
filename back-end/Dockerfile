####FROM bde2020/spark-submit:2.4.5-hadoop2.7
####
####LABEL maintainer=""
####
####ARG SBT_VERSION
####ENV SBT_VERSION=${SBT_VERSION:-1.3.8}
####
####RUN wget -O - https://piccolo.link/sbt-1.3.8.tgz | gunzip | tar -x -C /usr/local
####
####ENV PATH /usr/local/sbt/bin:${PATH}
####
####WORKDIR /app
####
##### Pre-install base libraries
####ADD build.sbt /app/
####ADD project/plugins.sbt /app/project/
####ADD project/Dependencies.scala /app/project/
####RUN sbt update
####
####COPY template.sh /
####
##### ENV SPARK_APPLICATION_MAIN_CLASS Application
####
##### Copy the build.sbt first, for separate dependency resolving and downloading
####ONBUILD COPY build.sbt /app/
####ONBUILD COPY project /app/project
####ONBUILD RUN sbt update
####
##### Copy the source code and build the application
####ONBUILD COPY . /app
####ONBUILD RUN sbt clean assembly
####
####CMD ["/template.sh"]
###
###FROM bde2020/spark-scala-template
###
###MAINTAINER ""
###
###ENV SPARK_APPLICATION_MAIN_CLASS spark.appliancelc.PredictLifeCycle
####ENV SPARK_APPLICATION_ARGS ""
##
### Information
### Main Spark Docker file code: https://github.com/apache/spark/blob/master/resource-managers/kubernetes/docker/src/main/dockerfiles/spark/Dockerfile
##
##ARG SPARK_IMAGE=gcr.io/spark-operator/spark:v2.4.4
##FROM ${SPARK_IMAGE}
##
##ARG VERSION
##ARG VCS_REF
##ARG BUILD_DATE
##
##ENV SCALA_VERSION 2.12
##ENV SPARK_VERSION 2.4.4
##ENV HADOOP_VERSION 3.2.1
##ENV KUBERNETES_CLIENT_VERSION 4.6.4
##
##ENV REPO_URL http://central.maven.org/maven2
##ENV ARCHIVE_URL http://archive.apache.org/dist
##
### Adhere to opencontainers image spec https://github.com/opencontainers/image-spec/blob/master/annotations.md
##LABEL org.opencontainers.image.title="graphiq-spark-runner" \
##      org.opencontainers.image.created=${BUILD_DATE} \
##      org.opencontainers.image.description="base image for Scala  Spark jobs that need to run on kubernetes via the spark-operator" \
##      org.opencontainers.image.source="https://github.com/TomLous/medium-spark-k8s" \
##      org.opencontainers.image.documentation="https://github.com/TomLous/medium-spark-k8s/blob/master/README.md" \
##      org.opencontainers.image.revison=${VCS_REF} \
##      org.opencontainers.image.vendor="GraphIQ" \
##      org.opencontainers.image.version=${VERSION} \
##      org.opencontainers.image.authors="Tom Lous <tomlous@gmail.com>" \
##      version.scala=${SCALA_VERSION} \
##      version.spark=${SPARK_VERSION} \
##      version.hadoop=${HADOOP_VERSION} \
##      version.kubernetes-client=${KUBERNETES_CLIENT_VERSION}
##
### Install Tools
##RUN apk --no-cache add curl
##
### Hadoop Config
##ENV HADOOP_HOME "/opt/hadoop"
##RUN rm -rf ${HADOOP_HOME}/ \
##    && cd /opt \
##    && curl -sL --retry 3 "${ARCHIVE_URL}/hadoop/common/hadoop-${HADOOP_VERSION}/hadoop-${HADOOP_VERSION}.tar.gz" | tar xz  \
##    && chown -R root:root hadoop-${HADOOP_VERSION} \
##    && ln -sfn hadoop-${HADOOP_VERSION} hadoop \
##    && rm -rf ${HADOOP_HOME}/share/doc \
##    && find /opt/ -name *-sources.jar -delete
##ENV PATH="${HADOOP_HOME}/bin:${PATH}"
##ENV HADOOP_CONF_DIR "${HADOOP_HOME}/etc/hadoop"
##
### Spark Config
### Since the conf/ folder gets mounted over by the spark-operator we move the spark-env.sh to another folder to be sourced in the entrypoint.sh. No good solution exists to merge the original conf folder with the volumeMount
##RUN rm -rf ${SPARK_HOME}/ \
##    && cd /opt \
##    && curl -sL --retry 3 "${ARCHIVE_URL}/spark/spark-${SPARK_VERSION}/spark-${SPARK_VERSION}-bin-without-hadoop-scala-${SCALA_VERSION}.tgz" | tar xz  \
##    && mv spark-${SPARK_VERSION}-bin-without-hadoop-scala-${SCALA_VERSION} spark-${SPARK_VERSION} \
##    && chown -R root:root spark-${SPARK_VERSION} \
##    && ln -sfn spark-${SPARK_VERSION} spark \
##    && mkdir -p ${SPARK_HOME}/conf-org/ \
##    && mv ${SPARK_HOME}/conf/spark-env.sh.template ${SPARK_HOME}/conf-org/spark-env.sh \
##    && rm -rf ${SPARK_HOME}/examples  ${SPARK_HOME}/data ${SPARK_HOME}/tests ${SPARK_HOME}/conf  \
##    && echo 'export SPARK_DIST_CLASSPATH=$(hadoop classpath)' >> ${SPARK_HOME}/conf-org/spark-env.sh \
##    && echo 'export SPARK_EXTRA_CLASSPATH=$(hadoop classpath)' >> ${SPARK_HOME}/conf-org/spark-env.sh
##
##ENV PATH="${SPARK_HOME}/bin:${PATH}"
##
##
### Kubernetes Client  <= TODO Remove when this issue is fixed https://andygrove.io/2019/08/apache-spark-regressions-eks/
###RUN rm -rf ${SPARK_HOME}/jars/kubernetes-*.jar
###ADD ${REPO_URL}/io/fabric8/kubernetes-client/${KUBERNETES_CLIENT_VERSION}/kubernetes-client-${KUBERNETES_CLIENT_VERSION}.jar ${SPARK_HOME}/jars
###ADD ${REPO_URL}/io/fabric8/kubernetes-model/${KUBERNETES_CLIENT_VERSION}/kubernetes-model-${KUBERNETES_CLIENT_VERSION}.jar ${SPARK_HOME}/jars
###ADD ${REPO_URL}/io/fabric8/kubernetes-model-common/${KUBERNETES_CLIENT_VERSION}/kubernetes-model-common-${KUBERNETES_CLIENT_VERSION}.jar ${SPARK_HOME}/jars
##
### Edit entrypoint to source spark-env.sh before running spark-submit
##RUN sed -i '30i #CUSTOM\n' /opt/entrypoint.sh \
##    && sed -i '/#CUSTOM/a source ${SPARK_HOME}/conf-org/spark-env.sh\n' /opt/entrypoint.sh
##
##ENTRYPOINT ["/opt/entrypoint.sh"]
#
#ARG SPARK_IMAGE=gcr.io/spark-operator/spark:v2.4.5
#
##FROM golang:1.14.0-alpine as builder
##ARG DEP_VERSION="0.5.4"
##RUN apk add --no-cache bash git
##ADD https://github.com/golang/dep/releases/download/v${DEP_VERSION}/dep-linux-amd64 /usr/bin/dep
##RUN chmod +x /usr/bin/dep
##
##WORKDIR ${GOPATH}/src/github.com/GoogleCloudPlatform/spark-on-k8s-operator
##COPY Gopkg.toml Gopkg.lock ./
##RUN dep ensure
###-vendor-only
##COPY . ./
##RUN go generate && CGO_ENABLED=0 GOOS=linux go build -o /usr/bin/spark-operator
#
#FROM openjdk:8-alpine
#
#ARG spark_jars=jars
#ARG img_path=kubernetes/dockerfiles
#ARG k8s_tests=kubernetes/tests
#
## Before building the docker image, first build and make a Spark distribution following
## the instructions in http://spark.apache.org/docs/latest/building-spark.html.
## If this docker file is being used in the context of building your images from a Spark
## distribution, the docker build command should be invoked from the top level directory
## of the Spark distribution. E.g.:
## docker build -t spark:latest -f kubernetes/dockerfiles/spark/Dockerfile .
#
#RUN set -ex && \
#    apk upgrade --no-cache && \
#    apk add --no-cache bash tini libc6-compat linux-pam && \
#    mkdir -p /opt/spark && \
#    mkdir -p /opt/spark/work-dir && \
#    touch /opt/spark/RELEASE && \
#    rm /bin/sh && \
#    ln -sv /bin/bash /bin/sh && \
#    echo "auth required pam_wheel.so use_uid" >> /etc/pam.d/su && \
#    chgrp root /etc/passwd && chmod ug+rw /etc/passwd
#
#COPY ${spark_jars} /opt/spark/jars
#COPY bin /opt/spark/bin
#COPY sbin /opt/spark/sbin
#COPY ${img_path}/spark/entrypoint.sh /opt/
#COPY examples /opt/spark/examples
#COPY ${k8s_tests} /opt/spark/tests
#COPY data /opt/spark/data
#
#ENV SPARK_HOME /opt/spark
#
#WORKDIR /opt/spark/work-dir
#
#ENTRYPOINT [ "/opt/entrypoint.sh" ]

#FROM openjdk:8-alpine
#
#RUN mkdir -p /app && \
#    mkdir -p /app/inputs \
#    sbt assembly
#
#COPY target/scala-2.11/back-end-assembly-0.1.jar /app
#COPY src/main/resources/output /app
#COPY src/main/resources/data/Home_data.csv /app
#COPY entrypoint.sh /app
#
##ENTRYPOINT [ "/app/" ]
#CMD []
#



FROM openjdk:8-jdk-slim

ARG SPARK_DIR=/spark-2.4.5-bin-hadoop2.7
ARG spark_jars=jars
ARG img_path=kubernetes/dockerfiles
ARG k8s_tests=kubernetes/tests

# Before building the docker image, first build and make a Spark distribution following
# the instructions in http://spark.apache.org/docs/latest/building-spark.html.
# If this docker file is being used in the context of building your images from a Spark
# distribution, the docker build command should be invoked from the top level directory
# of the Spark distribution. E.g.:
# docker build -t spark:latest -f kubernetes/dockerfiles/spark/Dockerfile .
ARG SBT_VERSION=1.3.8
#RUN \
#  apt-get update; apt-get install curl \
#  curl sbt-$SBT_VERSION.deb https://dl.bintray.com/sbt/debian/sbt-$SBT_VERSION.deb && \
#  dpkg -i sbt-$SBT_VERSION.deb && \
#  rm sbt-$SBT_VERSION.deb && \
#  apt-get update && \
#  apt-get install sbt && \
#  sbt sbtVersion

RUN set -ex && \
    apt-get update && \
    ln -s /lib /lib64 && \
    apt install -y bash tini libc6 libpam-modules libnss3 && \
    mkdir -p /opt/spark && \
    mkdir -p /opt/spark/work-dir && \
    mkdir -p /opt/spark/input && \
    mkdir -p /opt/spark/inputJson && \
    touch /opt/spark/RELEASE && \
    rm /bin/sh && \
    ln -sv /bin/bash /bin/sh && \
    echo "auth required pam_wheel.so use_uid" >> /etc/pam.d/su && \
    chgrp root /etc/passwd && chmod ug+rw /etc/passwd && \
    rm -rf /var/cache/apt/*


COPY ${SPARK_DIR}/jars /opt/spark/jars
#COPY target/scala-2.11/ /opt/spark/jars
COPY ${SPARK_DIR}/bin /opt/spark/bin
COPY ${SPARK_DIR}/sbin /opt/spark/sbin
COPY ${SPARK_DIR}/${img_path}/spark/entrypoint.sh /opt/
COPY target/scala-2.11/back-end_2.11-0.1.jar /opt/spark/jars
COPY src/main/resources/output /opt/spark/input
COPY src/main/resources/streamoutputs /opt/spark/streaminput
#COPY src/main/resources/outputJson /opt/spark/inputJson
COPY src/main/resources/data/Home_data.csv /opt/spark/input/
COPY src/main/resources/data/testing_data.csv /opt/spark/input/

#COPY ${SPARK_DIR}/${k8s_tests} /opt/spark/tests
#COPY ${SPARK_DIR}/data /opt/spark/data

ENV SPARK_HOME /opt/spark

WORKDIR /opt/spark/work-dir

ENTRYPOINT [ "/opt/entrypoint.sh" ]