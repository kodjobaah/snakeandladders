package controllers

import javax.inject.{Inject, Named, Singleton}

import actors.GameStateActor.{GameExist, NewGame, Start}
import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller, Result}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

@Singleton
class StartGameController @Inject()(
    @Named("gamestate-actor") gameStateActor: ActorRef)(
    implicit ec: ExecutionContext)
    extends Controller {

  import models.JsonFormats._

  implicit val timeout: Timeout = 5.seconds
  def start(computer: String) = Action.async {

    val result: Future[Result] = try {

      val userOrComp = computer.toInt
      (gameStateActor ? Start(userOrComp)).map { message =>
        message match {
          case NewGame(gs) => Created(Json.toJson(gs))
          case GameExist(gs) => Accepted(Json.toJson(gs))
          case _ => BadRequest
        }
      }
    } catch {
      case nfe:java.lang.NumberFormatException => Future(BadRequest)
    }

    result
  }
}
