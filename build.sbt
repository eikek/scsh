name := "scsh"

version := "0.2.0"

scalaVersion := "2.11.8"

scalacOptions ++= Seq("-unchecked", "-deprecation")

shebang in scsh := "/bin/sh"

javaBin in scsh := "java"

javaOptions in scsh := Seq()

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.6" % "test",
  "org.scala-lang" % "scala-compiler" % "2.11.8" % "compile",
  "com.github.pathikrit" %% "better-files" % "2.16.0",
  "org.scala-lang.modules" %% "scala-swing" % "2.0.0-M2",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.4.0",
  "ch.qos.logback" % "logback-classic" % "1.1.7",
  "com.github.scopt" %% "scopt" % "3.5.0",
  "org.xerial" % "sqlite-jdbc" % "3.8.11.2",
  "com.github.tototoshi" %% "scala-csv" % "1.3.3",
  "com.typesafe" % "config" % "1.3.0",
  "com.sksamuel.scrimage" %% "scrimage-core" % "2.1.6",
  "com.sksamuel.scrimage" %% "scrimage-io-extra" % "2.1.6"
)
