package service

object Dice {

  def rollDice(): Int = {
    val rnd = new scala.util.Random
    val range = 1 to 6
    range(rnd.nextInt(range length))
  }
}
