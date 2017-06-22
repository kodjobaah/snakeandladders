package actors

import actors.MoveTokenActor._
import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActors, TestKit}
import models.{GameState, GameStateDao, Player}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import akka.util.Timeout

import scala.concurrent.duration._
import scala.concurrent.Future

class MoveTokenActorSpec extends TestKit(ActorSystem("MyTest")) with MockFactory with ImplicitSender with WordSpecLike with Matchers
    with BeforeAndAfterAll {

  import scala.concurrent.ExecutionContext.Implicits.global

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "MoveTokenActor" must {

    "Token is placed on the board. Then the token is on square 1" in {

      val gameState = mock[GameStateDao]

      val p1 = Player("player1")
      val gs = GameState(player = p1, state = true)

      (gameState.find _).expects(*).returning(Future(Option(gs)))

      (gameState.update _).expects(where {
        (gameState: GameState) => gameState.player.tokenLocation == 1
      }).returning(Future(gs._id.stringify))

      val moveTokenActor = system.actorOf(Props(classOf[MoveTokenActor], gameState))
      implicit val timeout: Timeout = 5.seconds

      moveTokenActor ! MoveToken(gs._id.stringify, gs.player.identifier)
      expectMsg(Updated(gs._id.stringify))

    }

    "Token is on square 1 When the token is moved 3 spaces. Then the token is on square 4" in {

      val gameState = mock[GameStateDao]

      val p1 = Player(identifier = "player1", dice = 3, tokenLocation = 1)
      val gs = GameState(player = p1, state = true)

      (gameState.find _).expects(*).returning(Future(Option(gs)))

      (gameState.update _).expects(where {
        (gameState: GameState) => gameState.player.tokenLocation == 4
      }).returning(Future(gs._id.stringify))

      val moveTokenActor = system.actorOf(Props(classOf[MoveTokenActor], gameState))
      implicit val timeout: Timeout = 5.seconds

      moveTokenActor ! MoveToken(gs._id.stringify, gs.player.identifier)
      expectMsg(Updated(gs._id.stringify))

    }

    "Token is on square 4 When the token is moved 4 spaces. Then the token is on square 8" in {

      val gameState = mock[GameStateDao]

      val p1 = Player(identifier = "player1", dice = 4, tokenLocation = 4)
      val gs = GameState(player = p1, state = true)

      (gameState.find _).expects(*).returning(Future(Option(gs)))

      (gameState.update _).expects(where {
        (gameState: GameState) => gameState.player.tokenLocation == 8
      }).returning(Future(gs._id.stringify))

      val moveTokenActor = system.actorOf(Props(classOf[MoveTokenActor], gameState))
      implicit val timeout: Timeout = 5.seconds

      moveTokenActor ! MoveToken(gs._id.stringify, gs.player.identifier)
      expectMsg(Updated(gs._id.stringify))

    }

    "If the game does not exist it should not update" in {

      val gameState = mock[GameStateDao]

      (gameState.find _).expects(*).returning(Future(None))

      val moveTokenActor = system.actorOf(Props(classOf[MoveTokenActor], gameState))
      implicit val timeout: Timeout = 5.seconds

      moveTokenActor ! MoveToken("not eixit", "not exist")
      expectMsg(GameDoesNotExist())
    }

    "not update if player id is not valid" in {

      val gameState = mock[GameStateDao]

      val p1 = Player(identifier = "player1", dice = 4, tokenLocation = 4)
      val gs = GameState(player = p1, state = true)

      (gameState.find _).expects(*).returning(Future(Option(gs)))

      val moveTokenActor = system.actorOf(Props(classOf[MoveTokenActor], gameState))
      implicit val timeout: Timeout = 5.seconds

      moveTokenActor ! MoveToken(gs._id.stringify, "invalid player")
      expectMsg(PlayerDoesNotExist())

    }

    "win game if token is on 97 and moves 3 places to square 100" in {

      val gameState = mock[GameStateDao]

      val p1 = Player(identifier = "player1", dice = 3, tokenLocation = 97)
      val gs = GameState(player = p1, state = true)

      (gameState.find _).expects(*).returning(Future(Option(gs)))

      (gameState.update _).expects(where {
        (gameState: GameState) => gameState.player.tokenLocation == 100 && gameState.state == false
      }).returning(Future(gs._id.stringify))

      val moveTokenActor = system.actorOf(Props(classOf[MoveTokenActor], gameState))
      implicit val timeout: Timeout = 5.seconds

      moveTokenActor ! MoveToken(gs._id.stringify, gs.player.identifier)
      expectMsg(PlayerWon(gs.player.identifier))

    }

    "not win game if token is on 97 and moves 4 places" in {

      val gameState = mock[GameStateDao]

      val p1 = Player(identifier = "player1", dice = 4, tokenLocation = 97)
      val gs = GameState(player = p1, state = true)

      (gameState.find _).expects(*).returning(Future(Option(gs)))
      val moveTokenActor = system.actorOf(Props(classOf[MoveTokenActor], gameState))
      implicit val timeout: Timeout = 5.seconds

      moveTokenActor ! MoveToken(gs._id.stringify, gs.player.identifier)
      expectMsg(NotUpdated())

    }
  }

}
