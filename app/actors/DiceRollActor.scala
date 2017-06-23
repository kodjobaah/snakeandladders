package actors

import javax.inject.Inject

import actors.DiceRollActor.{ DiceRollGood, DiceRollNotGood, RollDice }
import akka.actor.Actor
import models.GameStateDao
import akka.pattern.pipe
import scala.concurrent.Future

object DiceRollActor {

  case class RollDice(gameId: String, playerId: String)

  sealed trait DiceRoll

  case class DiceRollGood(value: String) extends DiceRoll
  case class DiceRollNotGood() extends DiceRoll

}

class DiceRollActor @Inject() (val gameStateDao: GameStateDao) extends Actor {

  import scala.concurrent.ExecutionContext.Implicits.global

  override def receive: Receive = {

    case RollDice(gameId, playerId) =>
      val result: Future[DiceRollActor.DiceRoll with Product with Serializable] = gameStateDao.find(gameId).flatMap { gameState =>

        gameState match {
          case None => Future(DiceRollNotGood())
          case Some(gs) =>
            val dice = rollDice()
            gameStateDao.update(gs.copy(player = gs.player.copy(dice = dice))).map { result =>
              DiceRollGood(dice.toString)
            }
        }
      }
      result pipeTo sender()

  }

  def rollDice(): Int = {
    val rnd = new scala.util.Random
    val range = 1 to 6
    range(rnd.nextInt(range length))
  }
}
