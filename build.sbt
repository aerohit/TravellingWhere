name := """TravellingWhere"""

lazy val commonSettings = Seq(
  organization := "com.aerohitsaxena",
  version := "0.1.0",
  scalaVersion := "2.11.7"
)

lazy val root = (project in file(".")).enablePlugins(PlayScala).settings(commonSettings: _*)

libraryDependencies ++= Seq(
)

lazy val requestsProcessor = project.in(file("request-processor")).settings(commonSettings: _*)

lazy val informationCenter = project.in(file("information-center")).settings(commonSettings: _*)
