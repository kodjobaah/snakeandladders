package controllers

import actors.GameStateActor.{GameExist, NewGame, Start}
import akka.testkit.TestProbe
import models.JsonFormats._
import models.{GameState, Player}
import org.scalatestplus.play._
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test._

import scala.concurrent.Future

class StartGameControllerSpec
    extends PlaySpec
    with Results
    with OneAppPerTest {
  import scala.concurrent.ExecutionContext.Implicits.global
  "StartGameController#start" should {
    "should return Created if a new game is created" in {

      val testProbe = TestProbe()(app.actorSystem)
      val gs = GameState(player = List.empty[Player], state = true)
      val controller = new StartGameController(testProbe.ref)
      val result: Future[Result] = controller.start("1").apply(FakeRequest())
      testProbe.expectMsg(Start(1))
      testProbe.reply(NewGame(gs))

      val state: Int = status(result)
      state mustBe 201
      val bodyText: GameState = contentAsJson(result).validate[GameState].get
      bodyText._id.stringify mustBe gs._id.stringify
    }

    "should return Accepted game all ready exist" in {

      val testProbe = TestProbe()(app.actorSystem)
      val gs = GameState(player = List.empty[Player], state = true)
      val controller = new StartGameController(testProbe.ref)
      val result: Future[Result] = controller.start("1").apply(FakeRequest())
      testProbe.expectMsg(Start(1))
      testProbe.reply(GameExist(gs))

      val state: Int = status(result)
      state mustBe 202
      val bodyText: GameState = contentAsJson(result).validate[GameState].get
      bodyText._id.stringify mustBe gs._id.stringify
    }
  }
}
