package actors

import actors.MoveTokenActor._
import akka.actor.{ ActorSystem, Props }
import akka.testkit.{ ImplicitSender, TestActors, TestKit }
import models.{ GameState, GameStateDao, Player }
import org.scalamock.scalatest.MockFactory
import org.scalatest.{ BeforeAndAfterAll, Matchers, WordSpecLike }
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

      val p1 = Player("player1", roll = true)
      val p2 = Player("player2")
      val players = List(p1, p2)
      val gs = GameState(player = players, state = true)

      (gameState.find _).expects(*).returning(Future(Option(gs)))

      (gameState.update _).expects(where {
        (gameState: GameState) =>
          val p_1 = gameState.player.find(p => p.identifier == p1.identifier).get
          val p_2 = gameState.player.find(p => p.identifier != p1.identifier).get

          p_1.tokenLocation == 1 && p_1.roll == false && p_2.roll == true
      }).returning(Future(gs._id.stringify))

      val moveTokenActor = system.actorOf(Props(classOf[MoveTokenActor], gameState))
      implicit val timeout: Timeout = 5.seconds

      moveTokenActor ! MoveToken(gs._id.stringify, p1.identifier)
      expectMsg(Updated(gs._id.stringify))

    }

    "Token is on square 1 When the token is moved 3 spaces. Then the token is on square 4" in {

      val gameState = mock[GameStateDao]

      val p1 = Player("player1", dice = 3, tokenLocation = 1, roll = true)
      val p2 = Player("player2")
      val players = List(p1, p2)
      val gs = GameState(player = players, state = true)

      (gameState.find _).expects(*).returning(Future(Option(gs)))

      (gameState.update _).expects(where {
        (gameState: GameState) =>
          val p_1 = gameState.player.find(p => p.identifier == p1.identifier).get
          val p_2 = gameState.player.find(p => p.identifier != p1.identifier).get
          p_1.roll == false && p_1.tokenLocation == 4 && p_2.roll == true
      }).returning(Future(gs._id.stringify))

      val moveTokenActor = system.actorOf(Props(classOf[MoveTokenActor], gameState))
      implicit val timeout: Timeout = 5.seconds

      moveTokenActor ! MoveToken(gs._id.stringify, p1.identifier)
      expectMsg(Updated(gs._id.stringify))

    }

    "Token is on square 4 When the token is moved 4 spaces. Then the token is on square 8" in {

      val gameState = mock[GameStateDao]

      val p1 = Player("player1", dice = 4, tokenLocation = 4, roll = true)
      val p2 = Player("player2")
      val players = List(p1, p2)
      val gs = GameState(player = players, state = true)

      (gameState.find _).expects(*).returning(Future(Option(gs)))

      (gameState.update _).expects(where {
        (gameState: GameState) =>

          val p_1 = gameState.player.find(p => p.identifier == p1.identifier).get
          val p_2 = gameState.player.find(p => p.identifier != p1.identifier).get
          p_1.roll == false && p_1.tokenLocation == 8 && p_2.roll == true
      }).returning(Future(gs._id.stringify))

      val moveTokenActor = system.actorOf(Props(classOf[MoveTokenActor], gameState))
      implicit val timeout: Timeout = 5.seconds

      moveTokenActor ! MoveToken(gs._id.stringify, p1.identifier)
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

      val p1 = Player("player1", dice = 4, tokenLocation = 4, roll = true)
      val p2 = Player("player2")
      val players = List(p1, p2)
      val gs = GameState(player = players, state = true)

      (gameState.find _).expects(*).returning(Future(Option(gs)))

      val moveTokenActor = system.actorOf(Props(classOf[MoveTokenActor], gameState))
      implicit val timeout: Timeout = 5.seconds

      moveTokenActor ! MoveToken(gs._id.stringify, "invalid player")
      expectMsg(PlayerDoesNotExist())

    }

    "win game if token is on 97 and moves 3 places to square 100" in {

      val gameState = mock[GameStateDao]

      val p1 = Player("player1", dice = 3, tokenLocation = 97, roll = true)
      val p2 = Player("player2")
      val players = List(p1, p2)
      val gs = GameState(player = players, state = true)

      (gameState.find _).expects(*).returning(Future(Option(gs)))

      (gameState.update _).expects(where {

        (gameState: GameState) =>
          val player = gameState.player.find(p => p.identifier == p1.identifier).get
          player.tokenLocation == 100 && gameState.state == false
      }).returning(Future(gs._id.stringify))

      val moveTokenActor = system.actorOf(Props(classOf[MoveTokenActor], gameState))
      implicit val timeout: Timeout = 5.seconds

      moveTokenActor ! MoveToken(gs._id.stringify, p1.identifier)
      expectMsg(PlayerWon(p1.identifier))

    }

    "not win game if token is on 97 and moves 4 places" in {

      val gameState = mock[GameStateDao]

      val p1 = Player("player1", dice = 4, tokenLocation = 97, roll = true)
      val p2 = Player("player2")
      val players = List(p1, p2)
      val gs = GameState(player = players, state = true)

      (gameState.find _).expects(*).returning(Future(Option(gs)))
      val moveTokenActor = system.actorOf(Props(classOf[MoveTokenActor], gameState))
      implicit val timeout: Timeout = 5.seconds

      moveTokenActor ! MoveToken(gs._id.stringify, p1.identifier)
      expectMsg(NotUpdated())

    }

    "cause token to come down if token location is 12 and there is a snake from 2 to 12" in {

      val gameState = mock[GameStateDao]

      val p1 = Player("player1", dice = 2, tokenLocation = 10, roll = true)
      val p2 = Player("player2")
      val players = List(p1, p2)
      val gs = GameState(player = players, state = true)

      (gameState.find _).expects(*).returning(Future(Option(gs)))

      (gameState.update _).expects(where {
        (gameState: GameState) =>

          val p_1 = gameState.player.find(p => p.identifier == p1.identifier).get
          val p_2 = gameState.player.find(p => p.identifier != p1.identifier).get
          p_1.roll == false && p_1.tokenLocation == 2 && p_2.roll == true

      }).returning(Future(gs._id.stringify))

      val moveTokenActor = system.actorOf(Props(classOf[MoveTokenActor], gameState))
      implicit val timeout: Timeout = 5.seconds

      moveTokenActor ! MoveToken(gs._id.stringify, p1.identifier)
      expectMsg(Updated(gs._id.stringify))

    }

    "cause token to move up if on 2 and there is a laddder from 2 to 12" in {

      val gameState = mock[GameStateDao]

      val p1 = Player("player1", dice = 1, tokenLocation = 1, roll = true)
      val p2 = Player("player2")
      val players = List(p1, p2)
      val gs = GameState(player = players, state = true)

      (gameState.find _).expects(*).returning(Future(Option(gs)))

      (gameState.update _).expects(where {
        (gameState: GameState) =>

          val p_1 = gameState.player.find(p => p.identifier == p1.identifier).get
          val p_2 = gameState.player.find(p => p.identifier != p1.identifier).get
          p_1.roll == false && p_1.tokenLocation == 12 && p_2.roll == true
      }).returning(Future(gs._id.stringify))

      val moveTokenActor = system.actorOf(Props(classOf[MoveTokenActor], gameState))
      implicit val timeout: Timeout = 5.seconds

      moveTokenActor ! MoveToken(gs._id.stringify, p1.identifier)
      expectMsg(Updated(gs._id.stringify))

    }
  }

}
