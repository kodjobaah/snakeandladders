package actors

import javax.inject.Inject

import actors.GameStateActor.Start
import akka.actor.Actor
import models.GameStateDao
import akka.pattern.pipe

import scala.concurrent.Future

object GameStateActor {

  case object Start

}

class GameStateActor @Inject() (val gameStateDao: GameStateDao) extends Actor {

  import scala.concurrent.ExecutionContext.Implicits.global

  override def receive: Receive = {
    case Start =>

      val result: Future[String] = gameStateDao.findActive.flatMap { gameState =>
        gameState match {
          case None => gameStateDao.createGame().map { gs => gs }
          case Some(gs) => Future(gs._id.stringify)
        }

      }
      result pipeTo sender()
  }

}