package models

import play.api.libs.json.OFormat
import reactivemongo.bson.BSONObjectID
import reactivemongo.play.json.BSONFormats.BSONObjectIDFormat
import shapeless.ops.nat.GT.>

case class GameState(
    _id: BSONObjectID = BSONObjectID.generate,
    player: List[Player],
    computer: Int = -1,
    snakes: List[Movement] = List.empty[Movement],
    ladders: List[Movement] = List.empty[Movement],
    state: Boolean
)

case class Player(identifier: String,
                  dice: Int = -1,
                  tokenLocation: Int = -1,
                  roll: Boolean = false)

case class Movement(start: Int, end: Int)

object JsonFormats {

  import play.api.libs.json.{Json, Format}

  implicit val movement: OFormat[Movement] = Json.format[Movement]
  implicit val player: OFormat[Player] = Json.format[Player]
  implicit val gameState: OFormat[GameState] = Json.format[GameState]

}
