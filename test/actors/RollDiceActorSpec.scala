package actors

import actors.DiceRollActor.{ DiceRoll, DiceRollGood, RollDice }
import akka.actor.Status.Success
import akka.pattern.ask
import akka.actor.{ ActorSystem, Props }
import akka.testkit.{ ImplicitSender, TestKit }
import akka.util.Timeout
import models.{ GameState, GameStateDao, Player }
import org.scalamock.scalatest.MockFactory
import org.scalatest.{ BeforeAndAfterAll, Matchers, WordSpecLike }

import scala.concurrent.Future
import scala.concurrent.duration._

class RollDiceActorSpec extends TestKit(ActorSystem("MyTest")) with MockFactory with ImplicitSender with WordSpecLike with Matchers
    with BeforeAndAfterAll {

  import scala.concurrent.ExecutionContext.Implicits.global

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "RollDiceActor" must {

    "must roll the dice between 1 - 6" in {

      val gameState = mock[GameStateDao]

      val p1 = Player("player1")
      val gs = GameState(player = p1, state = true)

      (gameState.find _).expects(*).returning(Future(Option(gs)))

      var diceRoll = 0
      (gameState.update _).expects(where {
        (gameState: GameState) =>
          {
            diceRoll = gameState.player.dice
            gameState.player.dice >= 1 && gameState.player.dice <= 6
          }
      }).returning(Future(gs._id.stringify))

      val rollDiceActor = system.actorOf(Props(classOf[DiceRollActor], gameState))
      implicit val timeout: Timeout = 5.seconds

      rollDiceActor ! RollDice(gs._id.stringify, gs.player.identifier)

      expectMsgClass(classOf[DiceRollGood])
    }
  }

}
