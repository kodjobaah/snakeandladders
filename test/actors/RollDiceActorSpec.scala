package actors

import actors.DiceRollActor.{DiceRoll, DiceRollGood, RollDice}
import akka.actor.Status.Success
import akka.pattern.ask
import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import models.{GameState, GameStateDao, Player}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.Future
import scala.concurrent.duration._

class RollDiceActorSpec
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

  "RollDiceActor" must {

    "must roll the dice between 1 - 6" in {

      val gameState = mock[GameStateDao]

      val p1 = Player("player1", dice = 0, roll = true)
      val p2 = Player("player2")
      val players = List(p1, p2)
      val gs = GameState(player = players, state = true)

      (gameState.find _).expects(*).returning(Future(Option(gs)))

      var diceRoll = 0
      (gameState.update _)
        .expects(where { (gameState: GameState) =>
          {
            val player =
              gameState.player.find(p => p.identifier == p1.identifier).get
            diceRoll = player.dice
            player.dice >= 1 && player.dice <= 6
          }
        })
        .returning(Future(gs))

      val rollDiceActor =
        system.actorOf(Props(classOf[DiceRollActor], gameState))
      implicit val timeout: Timeout = 5.seconds

      rollDiceActor ! RollDice(gs._id.stringify, p1.identifier)

      expectMsgClass(classOf[DiceRollGood])
    }
  }

}
