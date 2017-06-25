package controllers

import javax.inject.{ Inject, Singleton, Named }

import actors.DiceRollActor.{ DiceRoll, DiceRollGood, DiceRollNotGood, RollDice }
import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import play.api.mvc.{ Action, Controller }

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext

@Singleton
class RollDiceController @Inject() (
  @Named("rolldice-actor") rollDiceActor: ActorRef
)(
  implicit
  ec: ExecutionContext
)
    extends Controller {

  implicit val timeout: Timeout = 5.seconds
  def roll(id: String, player: String) = Action.async {
    val rollDice = RollDice(id, player)
    (rollDiceActor ? rollDice).mapTo[DiceRoll].map { message =>
      message match {
        case DiceRollGood(dr) => Ok(dr)
        case drg: DiceRollNotGood => Forbidden
      }
    }
  }
}
