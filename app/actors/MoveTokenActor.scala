package actors

import javax.inject.Inject

import actors.MoveTokenActor._
import akka.actor.Actor
import models.{GameState, GameStateDao, Player}
import akka.pattern.pipe
import service.{Dice, MovePlayerService}

import scala.concurrent.Future

object MoveTokenActor {

  case class MoveToken(streamId: String, playerId: String)

  sealed trait MoveTokenResults extends Product with Serializable
  case class GameDoesNotExist() extends MoveTokenResults
  case class NeedsToRollDice(playerId: String) extends MoveTokenResults
  case class PlayerWon(playerId: String) extends MoveTokenResults
  case class PlayerDoesNotExist() extends MoveTokenResults
  case class SkipTurn() extends MoveTokenResults
  case class Updated(id: String) extends MoveTokenResults
}

class MoveTokenActor @Inject()(gameStateDao: GameStateDao,
                               movePlayerService: MovePlayerService)
    extends Actor {

  import scala.concurrent.ExecutionContext.Implicits.global

  override def receive: Receive = {

    case MoveToken(streamId, playerId) =>
      val result: Future[MoveTokenResults] =
        gameStateDao.find(streamId).flatMap { gs =>
          gs match {
            case Some(gameState) =>
              val move = movePlayerService.movePlayer(playerId, gameState)
              if (gameState.computer > 0) {
                move.flatMap { result =>
                  result match {
                    case Updated(x) =>
                      moveComputer(streamId, playerId)
                    case s: SkipTurn =>
                      moveComputer(streamId, playerId)
                    case _ => move
                  }
                }
              } else {
                move
              }
            case None => Future(GameDoesNotExist())
          }
        }
      result pipeTo sender()
  }

  private def moveComputer(streamId: String,
                           playerId: String): Future[MoveTokenResults] = {
    gameStateDao.find(streamId).flatMap { gs =>
      gs match {
        case Some(gameState) =>
          val computer =
            gameState.player.find(p => p.identifier != playerId).get
          val player = gameState.player.find(p => p.identifier == playerId).get
          val dice = Dice.rollDice()
          val players = List(computer.copy(dice = dice, roll = true), player)
          movePlayerService.movePlayer(computer.identifier,
                                       gameState.copy(player = players))
        case None => Future(GameDoesNotExist())
      }
    }
  }
}
