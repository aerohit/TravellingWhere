name := """TravellingWhere"""

lazy val commonSettings = Seq(
  organization := "com.aerohitsaxena",
  version := "0.1.0",
  scalaVersion := "2.11.7"
)

resolvers += Resolver.bintrayRepo("cakesolutions", "maven")

libraryDependencies ++= Seq(
  // https://mvnrepository.com/artifact/org.apache.kafka/kafka_2.11
//  libraryDependencies += "org.apache.kafka" % "kafka_2.11" % "0.10.1.0"
  "net.cakesolutions" %% "scala-kafka-client" % "0.10.0.0"
//  "org.apache.kafka" %% "kafka-clients" % "0.10.1.0"
)

lazy val root = (project in file(".")).enablePlugins(PlayScala).settings(commonSettings: _*)

lazy val requestsProcessor = project.in(file("request-processor")).settings(commonSettings: _*)

lazy val informationCenter = project.in(file("information-center")).settings(commonSettings: _*)
