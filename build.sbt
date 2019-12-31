
name := "redislimiter"

version := "0.1"

scalaVersion := "2.13.1"
libraryDependencies += "com.github.etaty" %% "rediscala" % "1.9.0"
libraryDependencies += "org.scalatest" % "scalatest_2.13" % "3.1.0" % "test"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"
// https://mvnrepository.com/artifact/com.typesafe.play/play-json
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.7.4"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"