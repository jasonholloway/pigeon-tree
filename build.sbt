
val sharedSettings = Seq(
  organization := "com.woodpigeon",
  version := "0.1.0",
  scalaVersion := "2.12.4"
)

lazy val root = (project in file("."))
    .aggregate(trunkJS, trunkJVM, webJS)


lazy val trunk = (crossProject in file("trunk"))
    .settings(sharedSettings ++ Seq(
      name:= "sapling-trunk",
      libraryDependencies ++= Seq(
        "org.apache.thrift" % "libthrift" % "0.9.2",
        "com.twitter" %% "scrooge-core" % "17.11.0" exclude("com.twitter", "libthrift")
      )
    ))

lazy val trunkJS = trunk.js
lazy val trunkJVM = trunk.jvm


lazy val webJS = (project in file("web"))
    .settings(sharedSettings ++ Seq(
      name := "sapling-web",
      localUrl := ("localhost", 7777)
    ))
    .dependsOn(trunkJS)
    .enablePlugins(ScalaJSPlugin, WorkbenchPlugin)
