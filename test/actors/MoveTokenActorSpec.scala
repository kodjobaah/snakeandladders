package actors

import actors.MoveTokenActor._
import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActors, TestKit}
import models.{GameState, GameStateDao, Player}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import akka.util.Timeout
import service.MovePlayerService

import scala.concurrent.duration._
import scala.concurrent.Future

class MoveTokenActorSpec
    extends TestKit(ActorSystem("MyTest"))
    with MockFactory
    with ImplicitSender
    with WordSpecLike
    with Matchers
    with BeforeAndAfterAll {

  import scala.concurrent.ExecutionContext.Implicits.global

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "MoveTokenActor" must {

    "Game exist should perform move operation for human" in {

      val gameStateDao = mock[GameStateDao]
      val movePlayerService = mock[MovePlayerService]

      val p1 = Player("player1", roll = true)
      val p2 = Player("player2")
      val players = List(p1, p2)
      val gs = GameState(player = players, state = true, computer = 0)

      (gameStateDao.find _).expects(*).returning(Future(Option(gs)))

      (movePlayerService.movePlayer _)
        .expects(p1.identifier, gs)
        .returning(Future(Updated(gs._id.stringify)))

      val moveTokenActor = system.actorOf(
        Props(classOf[MoveTokenActor], gameStateDao, movePlayerService))

      implicit val timeout: Timeout = 5.seconds

      moveTokenActor ! MoveToken(gs._id.stringify, p1.identifier)
      expectMsg(Updated(gs._id.stringify))

    }

    "Game exist should perform move operation for human and then computer" in {

      val gameStateDao = mock[GameStateDao]
      val movePlayerService = mock[MovePlayerService]

      val p1 = Player("player1", roll = true)
      val p2 = Player("player2")
      val players = List(p1, p2)
      val gs = GameState(player = players, state = true, computer = 10)

      inSequence {
        (gameStateDao.find _).expects(*).returning(Future(Option(gs)))
        (movePlayerService.movePlayer _)
          .expects(p1.identifier, *)
          .returning(Future(Updated(gs._id.stringify)))
        (gameStateDao.find _).expects(*).returning(Future(Option(gs)))
        (movePlayerService.movePlayer _)
          .expects(p2.identifier, *)
          .returning(Future(Updated(gs._id.stringify)))
      }

      val moveTokenActor = system.actorOf(
        Props(classOf[MoveTokenActor], gameStateDao, movePlayerService))

      implicit val timeout: Timeout = 5.seconds

      moveTokenActor ! MoveToken(gs._id.stringify, p1.identifier)
      expectMsg(Updated(gs._id.stringify))

    }
    "Game exist should perform move operation for computer if human is skipped" in {

      val gameStateDao = mock[GameStateDao]
      val movePlayerService = mock[MovePlayerService]

      val p1 = Player("player1", roll = true)
      val p2 = Player("player2")
      val players = List(p1, p2)
      val gs = GameState(player = players, state = true, computer = 10)

      inSequence {
        (gameStateDao.find _).expects(*).returning(Future(Option(gs)))
        (movePlayerService.movePlayer _)
          .expects(p1.identifier, *)
          .returning(Future(SkipTurn()))
        (gameStateDao.find _).expects(*).returning(Future(Option(gs)))
        (movePlayerService.movePlayer _)
          .expects(p2.identifier, *)
          .returning(Future(Updated(gs._id.stringify)))
      }

      val moveTokenActor = system.actorOf(
        Props(classOf[MoveTokenActor], gameStateDao, movePlayerService))

      implicit val timeout: Timeout = 5.seconds

      moveTokenActor ! MoveToken(gs._id.stringify, p1.identifier)
      expectMsg(Updated(gs._id.stringify))

    }
    "If the game does not exist it should not update" in {

      val gameStateDao = mock[GameStateDao]
      val movePlayerService = mock[MovePlayerService]

      (gameStateDao.find _).expects(*).returning(Future(None))

      val moveTokenActor = system.actorOf(
        Props(classOf[MoveTokenActor], gameStateDao, movePlayerService))
      implicit val timeout: Timeout = 5.seconds

      moveTokenActor ! MoveToken("not eixit", "not exist")
      expectMsg(GameDoesNotExist())
    }

  }

}
