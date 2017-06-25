package models

import actors.MoveTokenActor
import actors.MoveTokenActor.Updated
import akka.actor.ActorSystem
import akka.testkit.TestKit
import org.scalatest.Inside.inside
import org.scalatest.{AsyncFlatSpecLike, BeforeAndAfterAll, BeforeAndAfterEach, Matchers}
import org.scalatestplus.play.{OneAppPerTest, PlaySpec}
import play.api.libs.json.{JsArray, JsObject, Json}
import reactivemongo.api.{Cursor, ReadPreference}
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson._
import reactivemongo.play.json.collection.JSONCollection
import service.{MovePlayerService, PlaysFirstService}
import play.modules.reactivemongo.json._

import scala.concurrent.Future
import models.JsonFormats._
import org.scalatest._
import OptionValues._
import scala.Option

class GameStateDaoSpec extends AsyncFlatSpecLike with Matchers with DaoAccess with BeforeAndAfterAll with EmbeddedMongo with BeforeAndAfterEach {

  private var gameStateDao: GameStateDao = _

  "GameStateDao" should "create a new game state if one does not exist" in {

    val playsFirstService = new PlaysFirstService()

    val result: Future[String] = gameStateDao.createGame(playsFirstService, 10)
    val cursor: Future[Option[GameState]] = db.collection[JSONCollection]("gamestate").find(Json.obj()).one[GameState]

    val myout: Future[(String, Option[GameState])] = for {
      result <- gameStateDao.createGame(playsFirstService, 10)
      testData <- db.collection[JSONCollection]("gamestate").find(Json.obj()).one[GameState]
    } yield(result, testData)

    myout.flatMap { case (result, gs) =>
      result should be(gs.value._id.stringify)
    }
  }

  override def beforeEach() = {
 //   AwaitHelper.awaitResult(db.collection[JSONCollection]("gamestate").drop(failIfNotFound = false))
  }
  override def beforeAll(): Unit = {
    startDB("test")
    gameStateDao = new GameStateDao(reactiveMongoApi)

  }
  override def afterAll(): Unit  = {
    stopDB()
    stopApp
  }

}
