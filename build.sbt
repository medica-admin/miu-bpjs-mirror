name := """flexbpjsantrean"""
organization := "io.github.franzgranlund"

version := "1.0.0"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.12.2"

libraryDependencies += guice
libraryDependencies += ws
libraryDependencies += "com.auth0" % "java-jwt" % "3.3.0"
libraryDependencies += "com.github.tommyettinger" % "blazingchain" % "1.4.4.4"
javaOptions in Test ++= Seq("-Dlogger.resource=logback-test.xml")
