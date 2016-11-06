name := """TravellingWhere"""

lazy val commonSettings = Seq(
  organization := "com.aerohitsaxena",
  version := "0.1.0",
  scalaVersion := "2.11.7"
)

resolvers += Resolver.bintrayRepo("cakesolutions", "maven")

libraryDependencies ++= Seq(
//  "net.cakesolutions" %% "scala-kafka-client" % "0.10.0.0",
  "com.typesafe.akka" %% "akka-stream-kafka" % "0.13",
  "com.typesafe.akka" %% "akka-actor" % "2.3.7",
  "com.typesafe.akka" %% "akka-remote" % "2.3.7"
)

lazy val common = project
  .in(file("common"))
  .settings(commonSettings: _*)

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(commonSettings: _*)
  .dependsOn(common)

lazy val requestsProcessor = project
  .in(file("request-processor"))
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-stream-kafka" % "0.13"
  ))
  .dependsOn(common)

lazy val informationCenter = project
  .in(file("information-center"))
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-stream-kafka" % "0.13",
    "com.typesafe.akka" %% "akka-actor" % "2.3.7",
    "com.typesafe.akka" %% "akka-remote" % "2.3.7",
    "com.typesafe.play" %% "play-json" % "2.5.9"
  ))
  .dependsOn(common)

