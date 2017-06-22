import java.util.UUID

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._


class GatlinSpec extends Simulation {

    val httpConf = http.baseURL("http://localhost:9000")

    val readClients = scenario("Clients").exec(Index.refreshManyTimes)

    setUp(
      readClients.inject(rampUsers(1) over (1 seconds)).protocols(httpConf)
    )
  }

  object Index {

    def refreshAfterOneSecond =
      exec(http("Index")
        .post("/startGame")
        .check(bodyString.saveAs("gameId"))
        .check(status.is(200))).pause(2)
        .exec(http("MoveToken")
        .post("/moveToken/${gameId}/player1")
          .check(bodyString.saveAs("resultId")))
        .exec(session => {
          val maybeId = session.get("resultId").asOption[String]
          println(maybeId.getOrElse("COULD NOT FIND ID"))
          session
        })


    val refreshManyTimes = repeat(1) {
      refreshAfterOneSecond
    }
  }
