organization := "dhs3000"
name := "todo-service"
version := "0.2-SNAPSHOT"

scalaVersion := "2.12.10"
scalacOptions := Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-feature",
  "-language:higherKinds",
  "-Ypartial-unification"
)

lazy val root = (project in file("."))
  .settings(
    libraryDependencies ++= Dependencies.all
  )
