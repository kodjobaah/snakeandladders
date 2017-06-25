package controllers

import actors.MoveTokenActor._
import models.JsonFormats._
import akka.testkit.TestProbe
import models.{GameState, Player}
import org.scalatestplus.play._
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test._
import play.libs.Akka

import scala.concurrent.Future

class MoveTokenControllerSpec
    extends PlaySpec
    with Results
    with OneAppPerTest {
  import scala.concurrent.ExecutionContext.Implicits.global
  "MoveTokenController#move" should {
    "should return Accepted if user was updated" in {

      val testProbe = TestProbe()(Akka.system)
      val gs = GameState(player = List.empty[Player], state = true)

      val controller = new MoveTokenController(testProbe.ref)
      val result: Future[Result] =
        controller.move("gamestate", "playerId").apply(FakeRequest())
      testProbe.expectMsg(MoveToken("gamestate", "playerId"))
      testProbe.reply(Updated(gs))

      val state: Int = status(result)
      state mustBe 202
      val bodyText: GameState = contentAsJson(result).validate[GameState].get
      bodyText._id.stringify mustBe gs._id.stringify
    }

    "should return BadRequest if game does not exist" in {

      val testProbe = TestProbe()(Akka.system)

      val controller = new MoveTokenController(testProbe.ref)
      val result: Future[Result] =
        controller.move("gamestate", "playerId").apply(FakeRequest())
      testProbe.expectMsg(MoveToken("gamestate", "playerId"))
      testProbe.reply(GameDoesNotExist())

      val state: Int = status(result)
      state mustBe 400

    }

    "should return Unauthorised if player does not exist" in {

      val testProbe = TestProbe()(Akka.system)

      val controller = new MoveTokenController(testProbe.ref)
      val result: Future[Result] =
        controller.move("gamestate", "playerId").apply(FakeRequest())
      testProbe.expectMsg(MoveToken("gamestate", "playerId"))
      testProbe.reply(PlayerDoesNotExist())

      val state: Int = status(result)
      state mustBe 401

    }

    "should return Ok if player has won" in {

      val testProbe = TestProbe()(Akka.system)

      val controller = new MoveTokenController(testProbe.ref)
      val result: Future[Result] =
        controller.move("gamestate", "playerId").apply(FakeRequest())
      testProbe.expectMsg(MoveToken("gamestate", "playerId"))
      testProbe.reply(PlayerWon("playerId"))

      val state: Int = status(result)
      state mustBe 200
      val bodyText: String = contentAsString(result)
      bodyText mustBe "player won playerId"

    }

    "should return NotModified if player move was skipped" in {

      val testProbe = TestProbe()(Akka.system)

      val controller = new MoveTokenController(testProbe.ref)
      val result: Future[Result] =
        controller.move("gamestate", "playerId").apply(FakeRequest())
      testProbe.expectMsg(MoveToken("gamestate", "playerId"))
      testProbe.reply(SkipTurn())

      val state: Int = status(result)
      state mustBe 304

    }

    "should return MethodNotAllowed if player needs to roll a dice" in {

      val testProbe = TestProbe()(Akka.system)

      val controller = new MoveTokenController(testProbe.ref)
      val result: Future[Result] =
        controller.move("gamestate", "playerId").apply(FakeRequest())
      testProbe.expectMsg(MoveToken("gamestate", "playerId"))
      testProbe.reply(NeedsToRollDice("playerId"))

      val state: Int = status(result)
      state mustBe 405
      val bodyText: String = contentAsString(result)
      bodyText mustBe "playerId"

    }
  }

}
