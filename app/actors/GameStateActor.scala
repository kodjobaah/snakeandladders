package actors

import javax.inject.Inject

import actors.GameStateActor.Start
import akka.actor.Actor
import models.GameStateDao
import akka.pattern.pipe
import service.PlaysFirstService

import scala.concurrent.Future

object GameStateActor {

  case class Start(computer: Int)

}

class GameStateActor @Inject() (val gameStateDao: GameStateDao, playsFirstService: PlaysFirstService) extends Actor {

  import scala.concurrent.ExecutionContext.Implicits.global

  override def receive: Receive = {
    case Start(computer) =>

      val result: Future[String] = gameStateDao.findActive.flatMap { gameState =>
        gameState match {
          case None => gameStateDao.createGame(playsFirstService, computer).map { gs => gs }
          case Some(gs) => Future(gs._id.stringify)
        }

      }
      result pipeTo sender()
  }

}
