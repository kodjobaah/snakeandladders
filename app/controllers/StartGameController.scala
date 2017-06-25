package controllers

import javax.inject.{Inject, Named, Singleton}

import actors.GameStateActor.{GameExist, NewGame, Start}
import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

@Singleton
class StartGameController @Inject()(
    @Named("gamestate-actor") gameStateActor: ActorRef)(
    implicit ec: ExecutionContext)
    extends Controller {

  import models.JsonFormats._

  implicit val timeout: Timeout = 5.seconds
  def start(computer: String) = Action.async {
    (gameStateActor ? Start(computer.toInt)).map { message =>
      message match {
        case NewGame(gs) => Created(Json.toJson(gs))
        case GameExist(gs) => Accepted(Json.toJson(gs))
      }
    }
  }
}
