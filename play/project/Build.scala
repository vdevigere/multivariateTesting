import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "testingFramework"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    javaCore,
    javaJdbc,
    javaEbean,
    "redis.clients"%"jedis"%"2.1.0",
    "com.google.code.gson"%"gson"%"2.2.2"
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here      
  )

}
