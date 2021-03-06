package models

import com.google.inject.Inject
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.play.json.collection.JSONCollection
import play.modules.reactivemongo.json._
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.BSONObjectID._
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import service.{Dice, PlaysFirstService}

import scala.concurrent.Future
import scala.util.{Failure, Success}

class GameStateDao @Inject()(val reactiveMongoApi: ReactiveMongoApi) {

  import scala.concurrent.ExecutionContext.Implicits.global
  def gameStates =
    reactiveMongoApi.database.map(_.collection[JSONCollection]("gamestate"))

  import JsonFormats._

  def createGame(
      playsFirstService: PlaysFirstService,
      computer: Int
  ): Future[GameState] = {

    def findPlayer(p1: (Player, Int), p2: (Player, Int)): Player = {
      val result = playsFirstService.whoPlayersFirst(p1, p2)

      if (result == None) {
        findPlayer((p1._1, Dice.rollDice()), (p2._1, Dice.rollDice()))
      } else {
        result.get
      }

    }

    val p1 = Player("player1")
    val p2 = Player("player2")

    val first = findPlayer((p1, Dice.rollDice()), (p2, Dice.rollDice()))

    val players = first.identifier match {
      case "player1" =>
        if (computer < 1) {
          val p1 = Player("player1", roll = true)
          val p2 = Player("player2", roll = false)
          List(p1, p2)
        } else {
          val p1 = Player("player1", dice = 0, tokenLocation = 1, roll = true)
          val p2 = Player("player2", dice = 0, tokenLocation = 1, roll = false)
          List(p1, p2)
        }
      case "player2" =>
        if (computer < 1) {
          val p1 = Player("player1", roll = false)
          val p2 = Player("player2", roll = true)
          List(p1, p2)
        } else {
          val p1 = Player("player1", tokenLocation = 1, dice = 0, roll = true)
          val p2 = Player("player2", tokenLocation = 1, dice = 0, roll = false)
          List(p1, p2)
        }
    }

    val ladders = List(Movement(start = 2, end = 12))
    val snakes = List(Movement(start = 12, end = 2))
    val gs = GameState(player = players,
                       computer = computer,
                       state = true,
                       ladders = ladders,
                       snakes = snakes)
    val result: Future[WriteResult] = reactiveMongoApi.database.flatMap(
      _.collection[JSONCollection]("gamestate").insert(gs)
    )
    result.map { x =>
      gs
    }

  }

  def update(gs: GameState): Future[GameState] = {
    val result: Future[WriteResult] = reactiveMongoApi.database.flatMap { x =>
      x.collection[JSONCollection]("gamestate")
        .update(BSONDocument("_id" -> gs._id), gs, upsert = false)
    }
    result.map { x =>
      gs
    }

  }

  def find(gameId: String): Future[Option[GameState]] = {
    parse(gameId) match {
      case Success(id) =>
        for {
          gss <- gameStates
          gameState <- gss.find(BSONDocument("_id" -> id)).one[GameState]
        } yield gameState
      case Failure(f) => Future(None)
    }

  }

  def findActive: Future[Option[GameState]] =
    for {
      gss <- gameStates
      gameState <- gss.find(Json.obj("state" -> true)).one[GameState]
    } yield gameState

}
