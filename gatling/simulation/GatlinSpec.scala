import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._

/**
  * Example of how to play against the computer
  */
class GatlinSpec extends Simulation {

  val httpConf = http.baseURL("http://localhost:9000")

  val readClients = scenario("Clients").exec(Index.refreshManyTimes)

  setUp(
    readClients.inject(rampUsers(1) over (1 seconds)).protocols(httpConf)
  )
}

object Index {

  def refreshAfterOneSecond =
    exec(
      http("Index")
        .post("/startGame/4")
        .check(bodyString.saveAs("gameId"))
        .check(status.is(201)))
      .pause(2)
      .exec(http("DiceRoll")
        .post("/diceRoll/${gameId}/player1")
        .check(status.is(200))
        .check(bodyString.saveAs("resultId")))
      .pause(2)
      .exec(http("MakeFirstMove")
        .post("/moveToken/${gameId}/player1")
        .check(status.is(202))
        .check(bodyString.saveAs("diceRollId")))
      .pause(2)
      .exec(http("diceRoll")
        .post("/diceRoll/${gameId}/player1")
        .check(status.is(200))
        .check(bodyString.saveAs("resultId")))
      .exec(session => {
        val maybeId = session.get("diceRollId").asOption[String]
        println(maybeId.getOrElse("COULD NOT FIND ID"))
        session
      })

  val refreshManyTimes = repeat(1) {
    refreshAfterOneSecond
  }
}
