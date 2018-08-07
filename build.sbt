import Settings._

lazy val aggregatedProjects: Seq[ProjectReference] = Seq(`models`, `functions`, `services`)
lazy val `models` = project
  .enablePlugins(DeployApp)
  .settings(defaultSettings: _*)

lazy val `functions` = project
  .dependsOn(`models`)
  .enablePlugins(DeployApp)
  .settings(
    libraryDependencies ++= Dependencies.GemfireService
  ).settings(defaultSettings: _*)

lazy val `services` = project
  .dependsOn(`functions`, `models`)
  .enablePlugins(DeployApp)
  .settings(
    libraryDependencies ++= Dependencies.GemfireService
  ).settings(defaultSettings: _*)

val `gemfireservices` = project
  .aggregate(aggregatedProjects: _*)
  .enablePlugins(DeployApp, DockerPlugin)

assemblyOption in assembly := (assemblyOption in assembly).value.copy(appendContentHash = false)
//
//assemblyMergeStrategy in assembly := {
//  case x => MergeStrategy.first
//}