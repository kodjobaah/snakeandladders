package actors

import javax.inject.Inject

import actors.DiceRollActor.{ DiceRollGood, DiceRollNotGood, RollDice }
import akka.actor.Actor
import models.GameStateDao
import akka.pattern.pipe
import service.Dice

import scala.concurrent.Future

object DiceRollActor {

  case class RollDice(gameId: String, playerId: String)

  sealed trait DiceRoll extends Product with Serializable

  case class DiceRollGood(value: String) extends DiceRoll
  case class DiceRollNotGood() extends DiceRoll

}

class DiceRollActor @Inject() (val gameStateDao: GameStateDao) extends Actor {

  import scala.concurrent.ExecutionContext.Implicits.global

  override def receive: Receive = {

    case RollDice(gameId, playerId) =>
      val result: Future[DiceRollActor.DiceRoll] = gameStateDao.find(gameId).flatMap { gameState =>

        gameState match {
          case None => Future(DiceRollNotGood())
          case Some(gs) =>

            if (gs.player.find(p => p.identifier == playerId && p.roll == true && p.dice == 0).isDefined) {
              val playerOfInterest = gs.player.find(p => p.identifier == playerId && p.roll == true && p.dice == 0).get
              val playerTwo = gs.player.find(p => p.identifier != playerId).get
              val dice = Dice.rollDice()
              val listPlayers = List(playerTwo, playerOfInterest.copy(dice = dice))
              gameStateDao.update(gs.copy(player = listPlayers)).map { result =>
                DiceRollGood(dice.toString)
              }
            } else {
              Future(DiceRollNotGood())
            }
        }
      }
      result pipeTo sender()

  }

}
