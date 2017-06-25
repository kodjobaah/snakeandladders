package models

import play.api.inject.DefaultApplicationLifecycle
import play.api.inject.guice.GuiceApplicationBuilder
import play.modules.reactivemongo.ReactiveMongoApi

trait DaoAccess {

  val fakeApp = configuredAppBuilder

  val reactiveMongoApi = fakeApp.injector.instanceOf[ReactiveMongoApi]

  val lifecycle = fakeApp.injector.instanceOf[DefaultApplicationLifecycle]
  def stopApp = lifecycle.stop()

  def configuredAppBuilder = {
    import scala.collection.JavaConverters._

    val env = play.api.Environment.simple(mode = play.api.Mode.Test)
    val config = play.api.Configuration.load(env)
    val modules = config
      .getStringList("play.modules.enabled")
      .fold(
        List.empty[String]
      )(l => l.asScala.toList)

    new GuiceApplicationBuilder()
      .configure("play.modules.enabled" -> (modules :+
                   "play.modules.reactivemongo.ReactiveMongoModule"),
                 "mongodb.uri" -> "mongodb://localhost:12313/test")
      .build
  }
}
