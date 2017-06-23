package actors

import javax.inject.Inject

import actors.MoveTokenActor._
import akka.actor.Actor
import models.{ GameStateDao, Player }
import akka.pattern.pipe

import scala.concurrent.Future

object MoveTokenActor {

  case class MoveToken(streamId: String, playerId: String)

  sealed trait MoveTokenResults
  case class GameDoesNotExist() extends MoveTokenResults
  case class PlayerWon(playerId: String) extends MoveTokenResults
  case class PlayerDoesNotExist() extends MoveTokenResults
  case class NotUpdated() extends MoveTokenResults
  case class Updated(id: String) extends MoveTokenResults
}

class MoveTokenActor @Inject() (gameStateDao: GameStateDao) extends Actor {

  import scala.concurrent.ExecutionContext.Implicits.global

  val snakes = Map(12 -> 2)
  val ladders = Map(2 -> 12)

  override def receive: Receive = {

    case MoveToken(streamId, playerId) =>
      val result: Future[MoveTokenActor.MoveTokenResults with Product with Serializable] = gameStateDao.find(streamId).flatMap { gs =>
        gs match {
          case Some(gameState) =>

            val result: Future[MoveTokenActor.MoveTokenResults with Product with Serializable] = if (gameState.player.identifier == playerId) {

              def updatePlayer(player: Player): Future[Updated] = {
                val newGameState = gameState.copy(player = player)
                gameStateDao.update(newGameState).map { value =>
                  Updated(value)
                }
              }

              if (gameState.player.tokenLocation == -1) {
                updatePlayer(gameState.player.copy(tokenLocation = 1, dice = 0))
              } else {
                val newLocation = gameState.player.dice + gameState.player.tokenLocation
                if (newLocation < 100) {

                  val newPosition = snakes.get(newLocation)
                  val newLadder = ladders.get(newLocation)

                  (newPosition, newLadder) match {
                    case (None, Some(ladder)) => updatePlayer(gameState.player.copy(tokenLocation = ladder, dice = 0))
                    case (Some(snake), None) => updatePlayer(gameState.player.copy(tokenLocation = snake, dice = 0))
                    case _ => updatePlayer(gameState.player.copy(tokenLocation = newLocation, dice = 0))
                  }

                } else if (newLocation == 100) {
                  val newGameState = gameState.copy(player = gameState.player.copy(tokenLocation = newLocation, dice = 0), state = false)
                  gameStateDao.update(newGameState).map { value =>
                    PlayerWon(gameState.player.identifier)
                  }
                } else {
                  Future(NotUpdated())
                }
              }
            } else {
              Future(PlayerDoesNotExist())
            }
            result
          case None => Future(GameDoesNotExist())
        }
      }

      result pipeTo sender()
  }
}
