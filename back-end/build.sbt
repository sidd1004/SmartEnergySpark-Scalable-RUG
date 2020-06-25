import Dependencies._
import sbt._
import Keys._

name := "back-end"

version := "0.1"

scalaVersion := "2.11.7"
// enablePlugins(DockerPlugin)
// version in Docker := "latest"
enablePlugins(DockerPlugin)

libraryDependencies += sparkCore
libraryDependencies += sparkMLlib
libraryDependencies += sparkSql
libraryDependencies += jodaTime
libraryDependencies += typesafeConf
libraryDependencies += cassandraConnector
// libraryDependencies += cassandraDatastax
libraryDependencies += kafkaStream
libraryDependencies += sqlKafka

libraryDependencies += jacksonDep

//javaOptions in Universal ++= Seq(
//  "-Dpidfile.path=/dev/null"
//)
mainClass in assembly := Some("spark.appliancelc.PredictLifeCycle")

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}


//val targetDockerJarPath = "/opt/spark/jars"
//
//val domain = "spark-app"
//
//// For building the docker image
//lazy val dockerSettings = Seq(
//  imageNames in docker := Seq(
//    ImageName(s"$domain/${name.value}:latest"),
//    ImageName(s"$domain/${name.value}:${version.value}"),
//  ),
//  buildOptions in docker := BuildOptions(
//    cache = false,
//    removeIntermediateContainers = BuildOptions.Remove.Always,
//    pullBaseImage = BuildOptions.Pull.Always
//  ),
//  dockerfile in docker := {
//    // The assembly task generates a fat JAR file
//    val artifact: File = assembly.value
//    val artifactTargetPath = s"$targetDockerJarPath/$domain-${name.value}.jar"
//    new Dockerfile {
//      from(s"localhost:5000/spark-runner")
//    }.add(artifact, artifactTargetPath)
//  }
//)
