name := "byml-editor"
version := "1.0-SNAPSHOT"

lazy val `byml-editor` = (project in file("."))
  .settings(crossPaths := false)
  .settings(libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.6.3")
 .settings(mainClass in assembly := Some("Main"))
