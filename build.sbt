name := "byml-tools"

lazy val baseSettings = Seq(
  version := "0.2",
  organization := "com.arbiter34",
  autoScalaLibrary := false,
  crossPaths := false,
  javacOptions in compile ++= Seq("-g:lines,vars,source", "-deprecation"),
  javacOptions in doc += "-Xdoclint:none",
  run := {}
)

lazy val `byml-editor` = project
 .dependsOn(`byml-editor-lib`)
 .settings(name := "byml-editor")
 .settings(mainClass in assembly := Some("Main"))
 .settings(baseSettings)

lazy val `byml-editor-lib` = project
  .settings(name := "byml-editor-lib")
  .settings(libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.6.3")
  .settings(baseSettings)
