ThisBuild / version := "1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.10"
ThisBuild / organization := "com.nashtechglobal.khoahd7621"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    name := """scala-web-with-play-framework-practice""",
    libraryDependencies ++= (appDependencies ++ testDependencies)
  )

scalaVersion := "2.13.10"

val appDependencies = Seq(
  guice,

  // DB Access Library
  "com.typesafe.slick" %% "slick" % "3.4.1",
  "com.typesafe.play" %% "play-slick" % "5.1.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "5.1.0",
  "org.postgresql" % "postgresql" % "42.5.4", // PostgreSQL JDBC Driver
)

val testDependencies = Seq(
  "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test
)
