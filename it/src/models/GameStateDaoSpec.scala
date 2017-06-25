package models

import models.JsonFormats._
import org.scalatest.OptionValues._
import org.scalatest.{
  AsyncFlatSpecLike,
  BeforeAndAfterAll,
  BeforeAndAfterEach,
  Matchers
}
import play.api.libs.json.{Json, OFormat}
import play.modules.reactivemongo.json._
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json.collection.JSONCollection
import service.PlaysFirstService

import scala.concurrent.Future

class GameStateDaoSpec
    extends AsyncFlatSpecLike
    with Matchers
    with DaoAccess
    with BeforeAndAfterAll
    with EmbeddedMongo
    with BeforeAndAfterEach {

  "GameStateDao#createGame" should "create a new game state" in {

    val playsFirstService = new PlaysFirstService()
    var gameStateDao = new GameStateDao(reactiveMongoApi)
    val myout: Future[(GameState, Option[GameState])] = for {
      result <- gameStateDao.createGame(playsFirstService, 10)
      testData <- db
        .collection[JSONCollection]("gamestate")
        .find(Json.obj())
        .one[GameState]
    } yield (result, testData)

    myout.flatMap {
      case (result, gs) =>
        result._id.stringify should be(gs.value._id.stringify)
    }
  }

  "GameStateDao#update" should "update an existing game state" in {

    val newState = false
    val newGameState: GameState = createGameState
    val updatedGames = newGameState.copy(state = newState)
    var gameStateDao = new GameStateDao(reactiveMongoApi)
    val myout: Future[(WriteResult, GameState, Option[GameState])] = for {
      initialInsert <- db
        .collection[JSONCollection]("gamestate")
        .insert(newGameState)
      result <- gameStateDao.update(updatedGames)
      testData <- db
        .collection[JSONCollection]("gamestate")
        .find(BSONDocument("_id" -> newGameState._id))
        .one[GameState]
    } yield (initialInsert, result, testData)

    myout.map {
      case (writeResult, result, gs) =>
        newState should be(gs.value.state)
    }
  }

  "GameStateDao#findActive" should "find the active game" in {

    val newGameState: GameState = createGameState.copy(state = false)
    val activeGameState: GameState = createGameState

    var gameStateDao = new GameStateDao(reactiveMongoApi)
    val myout: Future[(WriteResult, WriteResult, Option[GameState])] = for {
      insertInActive <- db
        .collection[JSONCollection]("gamestate")
        .insert(newGameState)
      insertActive <- db
        .collection[JSONCollection]("gamestate")
        .insert(activeGameState)
      result <- gameStateDao.findActive
    } yield (insertInActive, insertActive, result)

    myout.map {
      case (inactive, active, result) =>
        activeGameState._id.stringify should be(result.value._id.stringify)
    }

  }

  "GameStateDao#find" should "find the game given the gameId" in {

    val activeGameState: GameState = createGameState

    var gameStateDao = new GameStateDao(reactiveMongoApi)
    val myout: Future[(WriteResult, Option[GameState])] = for {
      insertActive <- db
        .collection[JSONCollection]("gamestate")
        .insert(activeGameState)
      result <- gameStateDao.find(activeGameState._id.stringify)
    } yield (insertActive, result)

    myout.map {
      case (active, result) =>
        activeGameState._id.stringify should be(result.value._id.stringify)
    }

  }

  private def createGameState = {
    val p1 = Player("player1", roll = true, tokenLocation = 1)
    val p2 = Player("player2")
    val players = List(p1, p2)
    GameState(player = players, state = true)
  }

  override def beforeEach() = {
    val drop: Future[Boolean] =
      db.collection[JSONCollection]("gamestate").drop(failIfNotFound = false)
    drop.map { result =>
      println("Collection dropped[" + result + "]")

    }
  }
  override def beforeAll(): Unit = {
    startDB("test")

  }
  override def afterAll(): Unit = {
    stopDB()
    stopApp
  }

}
