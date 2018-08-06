import Settings._

lazy val aggregatedProjects: Seq[ProjectReference] = Seq(`functions`, `services`)

val `gemfireservices` = project
  .aggregate(aggregatedProjects: _*)
  .enablePlugins(DeployApp, DockerPlugin)

lazy val `services` = project
    .dependsOn(`functions`)
  .enablePlugins(DeployApp)
  .settings(
    libraryDependencies ++= Dependencies.GemfireService
  ).settings(defaultSettings: _*)

lazy val `functions` = project
  .enablePlugins(DeployApp)
  .settings(
    libraryDependencies ++= Dependencies.GemfireService
  ).settings(defaultSettings: _*)

assemblyOption in assembly := (assemblyOption in assembly).value.copy(appendContentHash = false)
assemblyJarName in assembly := "functions.jar"

assemblyMergeStrategy in assembly := {
  case x => MergeStrategy.first
}