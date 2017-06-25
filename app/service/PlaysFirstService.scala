package service

import models.Player
class PlaysFirstService() {

  def whoPlayersFirst(
    player: (Player, Int),
    player2: (Player, Int)
  ): Option[Player] = {
    if (player._2 > player2._2) {
      Option(player._1)
    } else if (player2._2 > player._2) {
      Option(player2._1)
    } else {
      None
    }
  }
}
