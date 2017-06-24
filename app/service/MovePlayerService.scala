package service

import javax.inject.Inject

import actors.MoveTokenActor._
import models.{ GameState, GameStateDao, Player }

import scala.concurrent.Future

class MovePlayerService @Inject() (gameStateDao: GameStateDao) {

  import scala.concurrent.ExecutionContext.Implicits.global

  val snakes = Map(12 -> 2)
  val ladders = Map(2 -> 12)

  def movePlayer(playerId: String, gameState: GameState): Future[MoveTokenResults] = {

    val player = gameState.player.find(p => p.identifier == playerId && p.roll == true)

    val result: Future[MoveTokenResults] = if (player.isDefined && player.get.dice != 0) {

      val playerOfInterest = gameState.player.find(p => p.identifier == playerId && p.roll == true).get
      val playerTwo = gameState.player.find(p => p.identifier != playerId).get

      if (playerOfInterest.tokenLocation == -1) {
        val listPlayers = List(playerTwo.copy(roll = true), playerOfInterest.copy(tokenLocation = 1, dice = 0, roll = false))
        updatePlayer(listPlayers, gameState)
      } else {
        val newLocation = playerOfInterest.dice + playerOfInterest.tokenLocation
        if (newLocation < 100) {

          val newPosition = snakes.get(newLocation)
          val newLadder = ladders.get(newLocation)

          (newPosition, newLadder) match {
            case (None, Some(ladder)) =>
              val listPlayers = List(playerTwo.copy(roll = true), playerOfInterest.copy(tokenLocation = ladder, dice = 0, roll = false))
              updatePlayer(listPlayers, gameState)
            case (Some(snake), None) =>
              val listPlayers = List(playerTwo.copy(roll = true), playerOfInterest.copy(tokenLocation = snake, dice = 0, roll = false))
              updatePlayer(listPlayers, gameState)
            case _ =>
              val listPlayers = List(playerTwo.copy(roll = true), playerOfInterest.copy(tokenLocation = newLocation, dice = 0, roll = false))
              updatePlayer(listPlayers, gameState)
          }

        } else if (newLocation == 100) {
          val listPlayers = List(playerTwo, playerOfInterest.copy(tokenLocation = newLocation, dice = 0, roll = false))
          val newGameState = gameState.copy(player = listPlayers, state = false)
          gameStateDao.update(newGameState).map { value =>
            PlayerWon(playerOfInterest.identifier)
          }
        } else {
          Future(SkipTurn())
        }
      }
    } else if (player.isDefined && player.get.dice == 0) {
      Future(NeedsToRollDice(player.get.identifier))
    } else {
      Future(PlayerDoesNotExist())
    }
    result
  }

  private def updatePlayer(players: List[Player], gameState: GameState): Future[Updated] = {
    val newGameState = gameState.copy(player = players)
    gameStateDao.update(newGameState).map { value =>
      Updated(value)
    }
  }

}
