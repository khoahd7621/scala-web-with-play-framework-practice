scalaVersion := "2.13.10"

ThisBuild / version := "1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.10"
ThisBuild / organization := "com.nashtechglobal.khoahd7621"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    name := """scala-web-with-play-framework-practice""",
    libraryDependencies ++= (appDependencies ++ testDependencies)
  )

// Dependencies
val playSilhouetteVersion = "7.0.0"
val slickVersion = "3.3.3"
val playSlickVersion = "5.0.0"
val akkaVersion = "2.8.0"
val enumeratumVersion = "1.7.2"
val enumeratumSlickVersion = "1.7.2"

val appDependencies = Seq(
  guice,
  ws,
  filters,
  // DB Access Library
  "com.typesafe.slick" %% "slick" % slickVersion,
  "com.typesafe.play" %% "play-slick" % playSlickVersion,
  "com.typesafe.play" %% "play-slick-evolutions" % playSlickVersion,
  // Postgresql JDBC Driver
  "org.postgresql" % "postgresql" % "42.5.4",
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-protobuf-v3" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-serialization-jackson" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % "10.5.0",
  // For JWT Authentication
  "com.mohiva" %% "play-silhouette" % playSilhouetteVersion,
  "com.mohiva" %% "play-silhouette-password-bcrypt" % playSilhouetteVersion,
  "com.mohiva" %% "play-silhouette-persistence" % playSilhouetteVersion,
  "com.mohiva" %% "play-silhouette-crypto-jca" % playSilhouetteVersion,
  "net.logstash.logback" % "logstash-logback-encoder" % "7.3",
  "net.codingwell" %% "scala-guice" % "5.1.1", // Scala extensions for Google Guice, Allows binding via type parameters
  // JSON Library
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.14.2",
  "org.apache.commons" % "commons-lang3" % "3.12.0"
)

val testDependencies = Seq(
  "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test
)

resolvers += Resolver.jcenterRepo
resolvers += "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
resolvers += "Atlassian's Maven Public Repository" at "https://packages.atlassian.com/maven-public/"
