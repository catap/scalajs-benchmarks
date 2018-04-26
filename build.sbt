import org.scalajs.linker.CheckedBehavior.Unchecked
import sbtcrossproject.CrossProject

val createHTMLRunner = taskKey[File]("Create the HTML runner for this benchmark")

val projectSettings: Seq[Setting[_]] = Seq(
  organization := "scalajs-benchmarks",
  version := "0.1-SNAPSHOT"
)

scalaJSLinkerConfig in Global := {
  org.scalajs.linker.StandardLinker.Config()
    .withSemantics(_.withAsInstanceOfs(Unchecked).withArrayIndexOutOfBounds(Unchecked))
}

val defaultSettings: Seq[Setting[_]] = projectSettings ++ Seq(
  scalaVersion := "2.12.5",
  scalacOptions ++= Seq(
      "-deprecation",
      "-unchecked",
      "-feature",
      "-encoding", "utf8"
  )
)

val defaultJVMSettings: Seq[Setting[_]] = Seq(
  fork in run := !scala.sys.env.get("TRAVIS").exists(_ == "true")
)

val defaultJSSettings: Seq[Setting[_]] = Def.settings(
  scalaJSLinkerConfig := (scalaJSLinkerConfig in ThisBuild).value,
  scalaJSUseMainModuleInitializer := true,

  inConfig(Compile)(Def.settings(
    createHTMLRunner := {
      val jsFile = fastOptJS.value.data
      val jsFileName = jsFile.getName
      val htmlFile = jsFile.getParentFile / (jsFileName.stripSuffix(".js") + ".html")
      val title = name.value
      val mainClassName = mainClass.value.getOrElse {
        throw new Exception("Oops, no main class")
      }
      val content = s"""
        |<!DOCTYPE html>
        |<html>
        |  <head>
        |    <title>$title</title>
        |    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        |  </head>
        |  <body>
        |    <script type="text/javascript" src="$jsFileName"></script>
        |    <script type="text/javascript">
        |      setupHTMLBenchmark("$mainClassName");
        |    </script>
        |  </body>
        |</html>
      """.stripMargin
      IO.write(htmlFile, content)
      streams.value.log.info(htmlFile.toURI.toASCIIString)
      htmlFile
    }
  ))
)

lazy val parent = project.in(file(".")).
  settings(projectSettings: _*).
  settings(
    name := "scalajs-benchmarks",
    publishArtifact in Compile := false
  )

lazy val common = crossProject(JSPlatform, JVMPlatform).
  settings(defaultSettings: _*).
  settings(
    name := "scalajs-benchmarks-common",
    moduleName := "common"
  )

lazy val commonJVM = common.jvm
lazy val commonJS = common.js

def autoConfigFull(cp: CrossProject): CrossProject = {
  cp.settings(defaultSettings: _*)
    .jvmSettings(defaultJVMSettings: _*)
    .jsSettings(defaultJSSettings: _*)
    .settings(
      name := "scalajs-benchmark-" + name.value,
      moduleName := name.value.stripPrefix("scalajs-benchmark-")
    )
    .dependsOn(common)
}

def autoConfig(cp: CrossProject.Builder): CrossProject = {
  autoConfigFull(cp.crossType(CrossType.Pure))
}

lazy val deltablue = autoConfig(crossProject(JSPlatform, JVMPlatform))
lazy val deltablueJVM = deltablue.jvm
lazy val deltablueJS = deltablue.js

lazy val richards = autoConfig(crossProject(JSPlatform, JVMPlatform))
lazy val richardsJVM = richards.jvm
lazy val richardsJS = richards.js

lazy val sudoku = autoConfig(crossProject(JSPlatform, JVMPlatform))
lazy val sudokuJVM = sudoku.jvm
lazy val sudokuJS = sudoku.js

lazy val tracer = autoConfigFull(crossProject(JSPlatform, JVMPlatform))
lazy val tracerJVM = tracer.jvm
lazy val tracerJS = tracer.js

lazy val sha512 = autoConfig(crossProject(JSPlatform, JVMPlatform))
lazy val sha512JVM = sha512.jvm
lazy val sha512JS = sha512.js

lazy val sha512Int = autoConfig(crossProject(JSPlatform, JVMPlatform))
lazy val sha512IntJVM = sha512Int.jvm
lazy val sha512IntJS = sha512Int.js

lazy val longMicro = autoConfig(crossProject(JSPlatform, JVMPlatform))
  .settings(
    mainClass in Compile := Some("org.scalajs.benchmark.longmicro.LongMicroAll")
  )
lazy val longMicroJVM = longMicro.jvm
lazy val longMicroJS = longMicro.js

lazy val kmeans = autoConfig(crossProject(JSPlatform, JVMPlatform))
lazy val kmeansJVM = kmeans.jvm
lazy val kmeansJS = kmeans.js
