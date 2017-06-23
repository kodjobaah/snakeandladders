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

            val result: Future[MoveTokenActor.MoveTokenResults with Product with Serializable] = if (gameState.player.find(p => p.identifier == playerId && p.roll == true).isDefined) {

              val playerOfInterest = gameState.player.find(p => p.identifier == playerId && p.roll == true).get
              val playerTwo = gameState.player.find(p => p.identifier != playerId).get
              def updatePlayer(players: List[Player]): Future[Updated] = {
                val newGameState = gameState.copy(player = players)
                gameStateDao.update(newGameState).map { value =>
                  Updated(value)
                }
              }

              if (playerOfInterest.tokenLocation == -1) {
                val listPlayers = List(playerTwo.copy(roll = true), playerOfInterest.copy(tokenLocation = 1, dice = 0, roll = false))
                updatePlayer(listPlayers)
              } else {
                val newLocation = playerOfInterest.dice + playerOfInterest.tokenLocation
                if (newLocation < 100) {

                  val newPosition = snakes.get(newLocation)
                  val newLadder = ladders.get(newLocation)

                  (newPosition, newLadder) match {
                    case (None, Some(ladder)) =>
                      val listPlayers = List(playerTwo.copy(roll = true), playerOfInterest.copy(tokenLocation = ladder, dice = 0, roll = false))
                      updatePlayer(listPlayers)
                    case (Some(snake), None) =>
                      val listPlayers = List(playerTwo.copy(roll = true), playerOfInterest.copy(tokenLocation = snake, dice = 0, roll = false))
                      updatePlayer(listPlayers)
                    case _ =>
                      val listPlayers = List(playerTwo.copy(roll = true), playerOfInterest.copy(tokenLocation = newLocation, dice = 0, roll = false))
                      updatePlayer(listPlayers)
                  }

                } else if (newLocation == 100) {
                  val listPlayers = List(playerTwo, playerOfInterest.copy(tokenLocation = newLocation, dice = 0, roll = false))
                  val newGameState = gameState.copy(player = listPlayers, state = false)
                  gameStateDao.update(newGameState).map { value =>
                    PlayerWon(playerOfInterest.identifier)
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
