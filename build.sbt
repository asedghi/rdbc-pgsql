import de.heikoseeberger.sbtheader.license.Apache2_0
import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._

lazy val commonSettings = Seq(
  organization := "io.rdbc.pgsql",
  scalaVersion := "2.12.1",
  crossScalaVersions := Vector("2.11.8"),
  scalacOptions ++= Vector(
    "-unchecked",
    "-deprecation",
    "-language:_",
    "-target:jvm-1.8",
    "-encoding", "UTF-8"
  ),
  licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html")),
  bintrayOrganization := Some("rdbc"),
  headers := Map(
    "scala" -> Apache2_0("2016-2017", "Krzysztof Pado")
  ),
  resolvers += Resolver.bintrayRepo("rdbc", "maven"),
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    setNextVersion,
    commitNextVersion,
    pushChanges
  ),
  buildInfoKeys := Vector(version, scalaVersion, git.gitHeadCommit, BuildInfoKey.action("buildTime") {
    java.time.Instant.now()
  }),
  scalastyleFailOnError := true
)

lazy val rdbcPgsql = (project in file("."))
  .settings(commonSettings: _*)
  .settings(
    publishArtifact := false,
    bintrayReleaseOnPublish := false
  )
  .aggregate(core, scodec)

lazy val core = (project in file("rdbc-pgsql-core"))
  .enablePlugins(BuildInfoPlugin)
  .settings(commonSettings: _*)
  .settings(
    name := "pgsql-core",
    libraryDependencies ++= Vector(
      Library.rdbcScalaApi,
      Library.rdbcTypeconv,
      Library.rdbcImplbase,
      Library.rdbcUtil,
      Library.typesafeConfig,
      Library.scalaLogging,
      Library.akkaStream,
      Library.sourcecode,
      Library.scodecBits
    ),
    buildInfoPackage := "io.rdbc.pgsql.core"
  )

lazy val scodec = (project in file("rdbc-pgsql-codec-scodec"))
  .enablePlugins(BuildInfoPlugin)
  .settings(commonSettings: _*)
  .settings(
    name := "pgsql-codec-scodec",
    libraryDependencies ++= Vector(
      Library.scodecBits,
      Library.scodecCore
    ),
    buildInfoPackage := "io.rdbc.pgsql.scodec"
  )
  .dependsOn(core)
