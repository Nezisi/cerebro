name := "cerebro"
maintainer := "Leonardo Menezes <leonardo.menezes@xing.com>"
packageSummary := "Elasticsearch web admin tool"
packageDescription := """cerebro is an open source(MIT License) elasticsearch web admin tool built
  using Scala, Play Framework, AngularJS and Bootstrap."""
rpmVendor := "lmenezes"
rpmLicense := Some("MIT")
rpmUrl := Some("http://github.com/lmenezes/cerebro")
scalaVersion := "2.12.10"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play"                    % "2.8.1",
  "com.typesafe.play" %% "play-json"               % "2.8.1",
  "com.typesafe.play" %% "play-slick"              % "5.0.0",
  "com.typesafe.play" %% "play-slick-evolutions"   % "5.0.0",
  "org.xerial"        %  "sqlite-jdbc"             % "3.30.1",
  "org.specs2"        %% "specs2-junit"  % "4.8.3" % "test",
  "org.specs2"        %% "specs2-core"   % "4.8.3" % "test",
  "org.specs2"        %% "specs2-mock"   % "4.8.3" % "test"
)

libraryDependencies += filters
libraryDependencies += ws
libraryDependencies += guice

lazy val root = (project in file(".")).
  enablePlugins(GitVersioning, PlayScala, BuildInfoPlugin, LauncherJarPlugin, JDebPackaging, RpmPlugin).
  settings(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "models"
  )

sources in (Compile, doc) := Seq.empty

enablePlugins(JavaServerAppPackaging)
enablePlugins(SystemdPlugin)

pipelineStages := Seq(digest, gzip)

serverLoading := Some(ServerLoader.Systemd)
systemdSuccessExitStatus in Debian += "143"
systemdSuccessExitStatus in Rpm += "143"
linuxPackageMappings += packageTemplateMapping(s"/var/lib/${packageName.value}")() withUser((daemonUser in Linux).value) withGroup((daemonGroup in Linux).value)
