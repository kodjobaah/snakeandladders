package models

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.testkit.TestKit
import akka.util.Timeout
import play.api.{Application, DefaultApplication, Mode}
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.inject.{ApplicationLifecycle, DefaultApplicationLifecycle, bind}
import play.api.libs.concurrent.{ActorSystemProvider, MaterializerProvider}
import play.modules.reactivemongo.ReactiveMongoApi

import scala.concurrent.duration._

trait DaoAccess {

  val fakeApp = configuredAppBuilder

  val reactiveMongoApi = fakeApp.injector.instanceOf[ReactiveMongoApi]

  val lifecycle = fakeApp.injector.instanceOf[DefaultApplicationLifecycle]
  def stopApp = lifecycle.stop()

  def configuredAppBuilder = {
    import scala.collection.JavaConverters._

    val env = play.api.Environment.simple(mode = play.api.Mode.Test)
    val config = play.api.Configuration.load(env)
    val modules = config.getStringList("play.modules.enabled").fold(
      List.empty[String]
    )(l => l.asScala.toList)

    new GuiceApplicationBuilder().
      configure("play.modules.enabled" -> (modules :+
        "play.modules.reactivemongo.ReactiveMongoModule"),
        "mongodb.uri" -> "mongodb://localhost:12313/test").build
  }
}
