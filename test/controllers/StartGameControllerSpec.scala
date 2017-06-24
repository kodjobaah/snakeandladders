package controllers

import actors.DiceRollActor.{ DiceRollGood, RollDice }
import actors.GameStateActor.{ GameExist, NewGame, Start }
import akka.testkit.TestProbe
import org.scalatestplus.play._
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test._
import play.libs.Akka

import scala.concurrent.Future

class StartGameControllerSpec extends PlaySpec with Results with OneAppPerTest {
  import scala.concurrent.ExecutionContext.Implicits.global
  "StartGameController#start" should {
    "should return Created if a new game is created" in {

      val testProbe = TestProbe()(Akka.system)
      val controller = new StartGameController(testProbe.ref)
      val result: Future[Result] = controller.start("1").apply(FakeRequest())
      testProbe.expectMsg(Start(1))
      testProbe.reply(NewGame("gameId"))

      val state: Int = status(result)
      state mustBe 201
      val bodyText: String = contentAsString(result)
      bodyText mustBe "gameId"
    }

    "should return Accepted game all ready exist" in {

      val testProbe = TestProbe()(Akka.system)
      val controller = new StartGameController(testProbe.ref)
      val result: Future[Result] = controller.start("1").apply(FakeRequest())
      testProbe.expectMsg(Start(1))
      testProbe.reply(GameExist("gameId"))

      val state: Int = status(result)
      state mustBe 202
      val bodyText: String = contentAsString(result)
      bodyText mustBe "gameId"
    }
  }
}
