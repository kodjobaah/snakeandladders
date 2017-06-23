package actors

import actors.GameStateActor.Start
import akka.actor.{ ActorSystem, Props }
import akka.testkit.{ ImplicitSender, TestKit }
import akka.util.Timeout
import models.{ GameState, GameStateDao, Player }
import org.scalamock.scalatest.MockFactory
import org.scalatest.{ BeforeAndAfterAll, Matchers, WordSpecLike }
import service.PlaysFirstService

import scala.concurrent.Future
import scala.concurrent.duration._

class GameStateActorSpec extends TestKit(ActorSystem("MyTest")) with MockFactory with ImplicitSender with WordSpecLike with Matchers
    with BeforeAndAfterAll {

  import scala.concurrent.ExecutionContext.Implicits.global

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "GameStateActor" must {

    "return the id of the game state if it exists" in {

      val p1 = Player("player1")

      val playsFirstService = mock[PlaysFirstService]
      val gameState = mock[GameStateDao]

      val p2 = Player("player2")
      val players = List(p1, p2)
      val gs = GameState(player = players, state = true)

      (gameState.findActive _).expects().returning(Future(Option(gs)))

      val gameStartActor = system.actorOf(Props(classOf[GameStateActor], gameState, playsFirstService))
      implicit val timeout: Timeout = 5.seconds
      gameStartActor ! Start
      expectMsg(gs._id.stringify)
    }

    "create a new game state if there is not an active one and return the id" in {

      val gameState = mock[GameStateDao]

      val p1 = Player("player1")

      val playsFirstService = mock[PlaysFirstService]

      val p2 = Player("player2")
      val players = List(p1, p2)
      val gs = GameState(player = players, state = true)

      (gameState.findActive _).expects().returning(Future(None))

      (gameState.createGame _).expects(playsFirstService).returning(Future("myId"))

      val gameStartActor = system.actorOf(Props(classOf[GameStateActor], gameState, playsFirstService))
      implicit val timeout: Timeout = 5.seconds
      gameStartActor ! Start
      expectMsg("myId")
    }

  }
}