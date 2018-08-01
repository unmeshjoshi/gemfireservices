name := "gemfireservices"

version := "0.1"

scalaVersion := "2.12.4"

resolvers += "Artima Maven Repository" at "http://repo.artima.com/releases"

// https://mvnrepository.com/artifact/org.apache.geode/geode-core
libraryDependencies += "org.apache.geode" % "geode-core" % "1.0.0-incubating"
libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.5"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test"
