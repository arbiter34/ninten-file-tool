name := "ninten-file-tool"

lazy val baseSettings = Seq(
  version := "0.4",
  organization := "com.arbiter34",
  autoScalaLibrary := false,
  crossPaths := false,
  javacOptions in compile ++= Seq("-g:lines,vars,source", "-deprecation"),
  javacOptions in doc += "-Xdoclint:none",
  run := {}
)

lazy val baseDependencies = Seq(
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.6.3"
)

lazy val `file-util` = project
  .settings(name := "file-util")
  .settings(baseSettings)

lazy val `byml-editor-lib` = project
  .dependsOn(`file-util`)
  .settings(name := "byml-editor-lib")
  .settings(libraryDependencies ++= baseDependencies)
  .settings(baseSettings)

lazy val `prod-editor-lib` = project
  .dependsOn(`file-util`)
  .settings(name := "prod-editor-lib")
  .settings(libraryDependencies ++= baseDependencies)
  .settings(baseSettings)

lazy val `ninten-file-tool` = project
  .dependsOn(`byml-editor-lib`, `prod-editor-lib`)
  .settings(name := "ninten-file-tool")
  .settings(mainClass in assembly := Some("Main"))
  .settings(baseSettings)