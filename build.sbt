name := "scsh"

version := "0.1.1"

scalaVersion := "2.11.7"

scalacOptions ++= Seq("-unchecked", "-deprecation")

shebang in scsh := "/bin/sh"

javaBin in scsh := "java"

javaOptions in scsh := Seq()

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % "test"

libraryDependencies += "org.scala-lang" % "scala-compiler" % "2.11.7" % "compile"

libraryDependencies += "com.github.pathikrit" %% "better-files" % "2.13.0"

libraryDependencies += "org.scala-lang.modules" %% "scala-swing" % "2.0.0-M2"

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.3"

libraryDependencies += "com.github.scopt" %% "scopt" % "3.3.0"

libraryDependencies += "org.xerial" % "sqlite-jdbc" % "3.8.11.2"

libraryDependencies += "com.github.tototoshi" %% "scala-csv" % "1.2.2"
