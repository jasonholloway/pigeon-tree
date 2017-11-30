
val sharedSettings = Seq(
  organization := "com.woodpigeon",
  version := "0.1.0",
  scalaVersion := "2.12.4"
)

val jsSettings = Seq(
  libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.2"
)


lazy val root = (project in file("."))
    .aggregate(trunkJS, trunkJVM, webJS)


lazy val trunk = (crossProject in file("trunk"))
    .settings(sharedSettings ++ Seq(
      name:= "sapling.trunk"
    ))

lazy val trunkJS = trunk.js
lazy val trunkJVM = trunk.jvm

lazy val trunkShared = (project in file("trunk/shared"))
    .settings(sharedSettings)
    .dependsOn(trunkJVM)


lazy val webJS = (project in file("web"))
    .settings(sharedSettings ++ jsSettings ++ Seq(
      name := "sapling.web",
      localUrl := ("localhost", 7777),
      scalaJSUseMainModuleInitializer := true,
      mainClass in Compile := Some("com.woodpigeon.sapling.web.Main"),
      libraryDependencies ++= Seq(
        "org.scala-js" %%% "scalajs-dom" % "0.9.2"
      )
    ))
    .dependsOn(trunkJS)
    .enablePlugins(ScalaJSPlugin, WorkbenchPlugin)
