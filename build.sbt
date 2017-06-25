import sbt.Keys._
name := """snakesandladders"""
organization := "com.snakeandladders"

version := "1.0-SNAPSHOT"

lazy val GatlingTest = config("gatling") extend Test
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots" +
  ""

lazy val ItTest = config("it") extend Test

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, GatlingPlugin)
  .configs(GatlingTest)
  .configs(ItTest)
  .settings(inConfig(GatlingTest)(Defaults.testSettings): _*)
  .settings(inConfig(ItTest)(Defaults.testSettings): _*)
  .settings(
    name := """snake-and-ladders""",
    scalaSource in GatlingTest := baseDirectory.value / "/gatling/simulation",
    scalaSource in ItTest := baseDirectory.value / "/it/src"
  )

scalaVersion := "2.11.11"

libraryDependencies += filters

val reactiveMongoVer = "0.12.3"

libraryDependencies ++= Seq(
  "org.reactivemongo" %% "play2-reactivemongo" % reactiveMongoVer,
  "org.reactivemongo" %% "reactivemongo-jmx" % "0.12.3",
  "io.gatling.highcharts" % "gatling-charts-highcharts" % "2.2.5" % Test,
  "io.gatling" % "gatling-test-framework" % "2.2.5" % Test,
  "com.typesafe.akka" %% "akka-testkit" % "2.4.17" % Test,
  "org.scalatest" %% "scalatest" % "3.0.1" % Test,
  specs2 % Test,
  "org.eu.acolyte" %% "reactive-mongo" % "1.0.41-j7p" % Test,
  "de.flapdoodle.embed" % "de.flapdoodle.embed.mongo" % "2.0.0" % "test",
  "org.scalamock" %% "scalamock-scalatest-support" % "3.6.0" % Test,
  "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" % Test
)
// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.snakeandladders.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.snakeandladders.binders._"
