import sbt._

object Dependencies {

  val Version = "0.1-SNAPSHOT"
  val GemfireService = Seq(
    Libs.`junit` % Test,
    Libs.`junit-interface` % Test,
    Libs.`mockito-core` % Test,
    Libs.`scalatest` % Test,
    Libs.`geode` % Provided
  )
}