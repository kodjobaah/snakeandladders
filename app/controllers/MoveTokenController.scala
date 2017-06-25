package controllers

import javax.inject.{ Inject, Named, Singleton }

import actors.MoveTokenActor._
import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import play.api.http.Status.{ BAD_REQUEST, UNAUTHORIZED }
import play.api.mvc.{ Action, Controller }

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext

@Singleton
class MoveTokenController @Inject() (
  @Named("movetoken-actor") moveTokenActor: ActorRef
)(
  implicit
  ec: ExecutionContext
)
    extends Controller {

  implicit val timeout: Timeout = 5.seconds
  def move(id: String, player: String) = Action.async {
    val moveToken = MoveToken(id, player)
    (moveTokenActor ? moveToken).mapTo[MoveTokenResults].map { message =>
      message match {
        case gme: GameDoesNotExist => BadRequest("game does not exist")
        case PlayerWon(playerId) => Ok(s"player won ${playerId}")
        case playerNotExist: PlayerDoesNotExist =>
          Unauthorized("player does not exist")
        case Updated(id) => Accepted(id)
        case nu: SkipTurn => NotModified
        case NeedsToRollDice(playerId) => MethodNotAllowed(playerId)
      }
    }
  }
}
