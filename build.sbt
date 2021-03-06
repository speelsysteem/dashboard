import ReleaseTransformations._

name := "inschrijven-cloud-dashboard"

lazy val playVersion = "2.6.7"

javaOptions in Test += "-Dconfig.file=conf/application.testing.conf" // use different config file when testing

lazy val commonSettings = Seq(
  scalaVersion := "2.11.11",
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),
  scalacOptions in Compile ++= Seq("-Xmax-classfile-name", "128"),
  coverageExcludedPackages := """controllers\..*Reverse.*;router.Routes.*;controllers.javascript*;controllers.ref*;router*""",
  dockerRepository := Some("thomastoye"),
  dockerUpdateLatest := true
)

lazy val root = (project in file("."))
  .settings(commonSettings)
  .settings(
    packageName in Docker := "inschrijven-cloud-dashboard"
  )
  .enablePlugins(PlayScala)
  .dependsOn(dataAccess)
  .aggregate(dataAccess)

scalaVersion := "2.11.11"

coverageEnabled in Test := true

scalafmtOnCompile in ThisBuild := true

wartremoverErrors in (Compile, compile) ++= Warts.unsafe

wartremoverExcluded ++= routes.in(Compile).value

libraryDependencies ++= Seq(
  filters,
  guice,
  "com.typesafe.play" %% "play-json" % playVersion,
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.0.0" % "test",
  "org.scalamock" %% "scalamock-scalatest-support" % "3.6.0" % "test"
)

routesGenerator := InjectedRoutesGenerator

lazy val models = project
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.play" %% "play-json" % playVersion,
      "org.scalatest" %% "scalatest" % "3.0.3" % "test"
    ),
    publishLocal in Docker := {},
    publish in Docker := {}
  )

lazy val dataAccess = Project("data-access", file("data-access"))
  .settings(commonSettings)
  .enablePlugins(PlayScala)
  .settings(
    libraryDependencies ++= Seq(
      ws,
      ehcache,
      "io.waylay.influxdb" %% "influxdb-scala" % "2.0.1",
      "com.ibm" %% "couchdb-scala" % "0.7.2",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2",
      "com.norbitltd" %% "spoiwo" % "1.3.1",
      "io.lemonlabs" %% "scala-uri" % "0.5.0",
      "io.verizon.delorean" %% "core" % "1.2.40-scalaz-7.2",
      "com.pauldijou" %% "jwt-core" % "0.14.1",
      "org.scalatest" %% "scalatest" % "3.0.3" % "test",
      "org.scalamock" %% "scalamock-scalatest-support" % "3.6.0" % "test"
    ),
    publishLocal in Docker := {},
    publish in Docker := {}
  )
  .dependsOn(models)
  .aggregate(models)

enablePlugins(GhpagesPlugin)
enablePlugins(SiteScaladocPlugin)

siteSourceDirectory := file("app/site")

val publishScalaDoc = (ref: ProjectRef) =>
  ReleaseStep(
    action = releaseStepTaskAggregated(ghpagesPushSite in ref) // publish scaladoc
)

releaseProcess := (thisProjectRef apply { ref =>
  import sbtrelease.ReleaseStateTransformations._

  Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    //publishArtifacts,
    publishScalaDoc(ref),
    setNextVersion,
    commitNextVersion,
    pushChanges
  )
}).value

git.remoteRepo := "git@github.com:inschrijven-cloud/dashboard.git"

releaseIgnoreUntrackedFiles := true
