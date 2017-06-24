package controllers

import actors.DiceRollActor.{ DiceRollGood, DiceRollNotGood, RollDice }
import actors.MoveTokenActor._
import akka.testkit.TestProbe
import org.scalatestplus.play._
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test._
import play.libs.Akka

import scala.concurrent.Future

class RollDiceControllerSpec extends PlaySpec with Results with OneAppPerTest {
  import scala.concurrent.ExecutionContext.Implicits.global

  "RollDiceController#roll" should {
    "should return Ok if is successful" in {

      val testProbe = TestProbe()(Akka.system)
      val controller = new RollDiceController(testProbe.ref)
      val result: Future[Result] = controller.roll("gamestate", "playerId").apply(FakeRequest())
      testProbe.expectMsg(RollDice("gamestate", "playerId"))
      testProbe.reply(DiceRollGood("5"))

      val state: Int = status(result)
      state mustBe 200
      val bodyText: String = contentAsString(result)
      bodyText mustBe "5"
    }

    "should return Forbidden if dice not rolled" in {

      val testProbe = TestProbe()(Akka.system)
      val controller = new RollDiceController(testProbe.ref)
      val result: Future[Result] = controller.roll("gamestate", "playerId").apply(FakeRequest())
      testProbe.expectMsg(RollDice("gamestate", "playerId"))
      testProbe.reply(DiceRollNotGood())

      val state: Int = status(result)
      state mustBe 403
    }
  }

}
