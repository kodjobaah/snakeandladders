package service

import models.Player
import org.scalatest.{ Matchers, WordSpec }

class PlaysFirstServiceSpec extends WordSpec with Matchers {

  "PlaysFirstService" must {

    val playFirst = new PlaysFirstService()

    "let Player1 play if dice roll is higher" in {

      val player1 = Player(identifier = "player1")
      val player2 = Player(identifier = "player2")

      val result = playFirst.whoPlayersFirst((player1, 3), (player2, 1))
      result.get.identifier should be("player1")
    }

    "let Player2 play if dice roll is higher" in {

      val player1 = Player(identifier = "player1")
      val player2 = Player(identifier = "player2")

      val result = playFirst.whoPlayersFirst((player1, 2), (player2, 3))
      result.get.identifier should be("player2")
    }

    "No one should play if there are the same" in {

      val player1 = Player(identifier = "player1")
      val player2 = Player(identifier = "player2")

      val result = playFirst.whoPlayersFirst((player1, 2), (player2, 2))
      result should be(None)
    }

  }
}
