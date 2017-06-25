package service

import actors.MoveTokenActor
import actors.MoveTokenActor._
import models.{GameState, GameStateDao, Player}
import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.Inside._
import org.scalatest.{AsyncFlatSpecLike, Matchers}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
class MovePlayerServiceSpec
    extends AsyncFlatSpecLike
    with Matchers
    with AsyncMockFactory {

  "movePlayer" should "Token is placed on the board. Then the token is on square 1" in {

    val gameStateDao = mock[GameStateDao]

    val movePlayerService = new MovePlayerService(gameStateDao)

    val p1 = Player("player1", roll = true)
    val p2 = Player("player2")
    val players = List(p1, p2)
    val gs = GameState(player = players, state = true, computer = 0)

    (gameStateDao.update _)
      .expects(where { (gameState: GameState) =>
        val p_1 = gameState.player.find(p => p.identifier == p1.identifier).get
        val p_2 = gameState.player.find(p => p.identifier != p1.identifier).get

        p_1.tokenLocation == 1 && p_1.roll == false && p_2.roll == true
      })
      .returning(Future(gs))

    val result: Future[MoveTokenActor.MoveTokenResults] =
      movePlayerService.movePlayer(p1.identifier, gs)

    result.map { result =>
      inside(result) {
        case Updated(gamestate) =>
          gamestate._id.stringify should be(gs._id.stringify)
      }
    }
  }

  it should "Move token to square 4 if token is on square 3 and it is move 3 spaces" in {

    val gameStateDao = mock[GameStateDao]
    val movePlayerService = new MovePlayerService(gameStateDao)

    val p1 = Player("player1", dice = 3, tokenLocation = 1, roll = true)
    val p2 = Player("player2")
    val players = List(p1, p2)
    val gs = GameState(player = players, state = true, computer = 0)

    (gameStateDao.update _)
      .expects(where { (gameState: GameState) =>
        val p_1 = gameState.player.find(p => p.identifier == p1.identifier).get
        val p_2 = gameState.player.find(p => p.identifier != p1.identifier).get
        p_1.roll == false && p_1.tokenLocation == 4 && p_2.roll == true
      })
      .returning(Future(gs))

    val result: Future[MoveTokenActor.MoveTokenResults] =
      movePlayerService.movePlayer(p1.identifier, gs)

    result.map { result =>
      inside(result) {
        case Updated(gamestate) =>
          gamestate._id.stringify should be(gs._id.stringify)
      }
    }
  }

  it should "Move token to square 8 if token is on square 4 and it is move 4 spaces" in {

    val gameStateDao = mock[GameStateDao]
    val movePlayerService = new MovePlayerService(gameStateDao)

    val p1 = Player("player1", dice = 4, tokenLocation = 4, roll = true)
    val p2 = Player("player2")
    val players = List(p1, p2)
    val gs = GameState(player = players, state = true, computer = 0)

    (gameStateDao.update _)
      .expects(where { (gameState: GameState) =>
        val p_1 = gameState.player.find(p => p.identifier == p1.identifier).get
        val p_2 = gameState.player.find(p => p.identifier != p1.identifier).get
        p_1.roll == false && p_1.tokenLocation == 8 && p_2.roll == true
      })
      .returning(Future(gs))

    val result: Future[MoveTokenActor.MoveTokenResults] =
      movePlayerService.movePlayer(p1.identifier, gs)

    result.map { result =>
      inside(result) {
        case Updated(gamestate) =>
          gamestate._id.stringify should be(gs._id.stringify)
      }
    }

  }

  it should "not update if player id is not valid" in {

    val gameStateDao = mock[GameStateDao]
    val movePlayerService = new MovePlayerService(gameStateDao)

    val p1 = Player("player1", dice = 4, tokenLocation = 4, roll = true)
    val p2 = Player("player2")
    val players = List(p1, p2)
    val gs = GameState(player = players, state = true, computer = 0)
    val result: Future[MoveTokenActor.MoveTokenResults] =
      movePlayerService.movePlayer("playerDoesNotExist", gs)

    result.map { result =>
      result should be(PlayerDoesNotExist())
    }
  }

  it should "not move player if dice not thrown" in {

    val gameStateDao = mock[GameStateDao]
    val movePlayerService = new MovePlayerService(gameStateDao)

    val p1 = Player("player1", dice = 0, tokenLocation = 4, roll = true)
    val p2 = Player("player2")
    val players = List(p1, p2)
    val gs = GameState(player = players, state = true, computer = 0)
    val result: Future[MoveTokenActor.MoveTokenResults] =
      movePlayerService.movePlayer(p1.identifier, gs)

    result.map { result =>
      inside(result) {
        case NeedsToRollDice(playerId) =>
          playerId should be(p1.identifier)
      }
    }
  }

  it should "win game if token is on 97 and moves 3 places to square 100" in {

    val gameStateDao = mock[GameStateDao]
    val movePlayerService = new MovePlayerService(gameStateDao)

    val p1 = Player("player1", dice = 3, tokenLocation = 97, roll = true)
    val p2 = Player("player2")
    val players = List(p1, p2)
    val gs = GameState(player = players, state = true, computer = 0)

    (gameStateDao.update _)
      .expects(where { (gameState: GameState) =>
        val player =
          gameState.player.find(p => p.identifier == p1.identifier).get
        player.tokenLocation == 100 && gameState.state == false
      })
      .returning(Future(gs))

    val result: Future[MoveTokenActor.MoveTokenResults] =
      movePlayerService.movePlayer(p1.identifier, gs)

    result.map { result =>
      inside(result) {
        case PlayerWon(playerId) =>
          playerId should be(p1.identifier)
      }
    }
  }

  it should "not win game if token is on 97 and moves 4 places" in {

    val gameStateDao = mock[GameStateDao]
    val movePlayerService = new MovePlayerService(gameStateDao)

    val p1 = Player("player1", dice = 4, tokenLocation = 97, roll = true)
    val p2 = Player("player2")
    val players = List(p1, p2)
    val gs = GameState(player = players, state = true, computer = 0)

    val result: Future[MoveTokenActor.MoveTokenResults] =
      movePlayerService.movePlayer(p1.identifier, gs)

    result.map { result =>
      result should be(SkipTurn())
    }
  }

  it should "cause token to come down if token location is 12 and there is a snake from 2 to 12" in {

    val gameStateDao = mock[GameStateDao]
    val movePlayerService = new MovePlayerService(gameStateDao)

    val p1 = Player("player1", dice = 2, tokenLocation = 10, roll = true)
    val p2 = Player("player2")
    val players = List(p1, p2)
    val gs = GameState(player = players, state = true)

    (gameStateDao.update _)
      .expects(where { (gameState: GameState) =>
        val p_1 = gameState.player.find(p => p.identifier == p1.identifier).get
        val p_2 = gameState.player.find(p => p.identifier != p1.identifier).get
        p_1.roll == false && p_1.tokenLocation == 2 && p_2.roll == true

      })
      .returning(Future(gs))

    val result: Future[MoveTokenActor.MoveTokenResults] =
      movePlayerService.movePlayer(p1.identifier, gs)

    result.map { result =>
      inside(result) {
        case Updated(gameState) =>
          gameState._id.stringify should be(gs._id.stringify)
      }
    }

  }

  it should "cause token to move up if on 2 and there is a laddder from 2 to 12" in {

    val gameStateDao = mock[GameStateDao]
    val movePlayerService = new MovePlayerService(gameStateDao)

    val p1 = Player("player1", dice = 1, tokenLocation = 1, roll = true)
    val p2 = Player("player2")
    val players = List(p1, p2)
    val gs = GameState(player = players, state = true, computer = 4)

    (gameStateDao.update _)
      .expects(where { (gameState: GameState) =>
        val p_1 = gameState.player.find(p => p.identifier == p1.identifier).get
        val p_2 = gameState.player.find(p => p.identifier != p1.identifier).get
        p_1.roll == false && p_1.tokenLocation == 12 && p_2.roll == true
      })
      .returning(Future(gs))

    val result: Future[MoveTokenActor.MoveTokenResults] =
      movePlayerService.movePlayer(p1.identifier, gs)

    result.map { result =>
      inside(result) {
        case Updated(gameState) =>
          gameState._id.stringify should be(gs._id.stringify)
      }
    }

  }

}
