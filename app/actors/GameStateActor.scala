package actors

import javax.inject.Inject

import actors.GameStateActor.{ GameCreation, GameExist, NewGame, Start }
import akka.actor.Actor
import models.GameStateDao
import akka.pattern.pipe
import service.PlaysFirstService

import scala.concurrent.Future

object GameStateActor {

  case class Start(computer: Int)

  sealed trait GameCreation extends Product with Serializable
  case class NewGame(gameId: String) extends GameCreation
  case class GameExist(gameId: String) extends GameCreation

}

class GameStateActor @Inject() (
  val gameStateDao: GameStateDao,
  playsFirstService: PlaysFirstService
)
    extends Actor {

  import scala.concurrent.ExecutionContext.Implicits.global

  override def receive: Receive = {
    case Start(computer) =>
      val result: Future[GameCreation] = gameStateDao.findActive.flatMap {
        gameState =>
          gameState match {
            case None =>
              gameStateDao.createGame(playsFirstService, computer).map { gs =>
                NewGame(gs)
              }
            case Some(gs) => Future(GameExist(gs._id.stringify))
          }

      }
      result pipeTo sender()
  }

}
