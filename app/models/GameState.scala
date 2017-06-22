package models

import play.api.libs.json.OFormat
import reactivemongo.bson.BSONObjectID
import reactivemongo.play.json.BSONFormats.BSONObjectIDFormat

case class GameState(
  _id: BSONObjectID = BSONObjectID.generate,
  player: Player,
  state: Boolean
)

case class Player(identifier: String, dice: Int = 0, tokenLocation: Int = -1, roll: Boolean = false)

object JsonFormats {

  import play.api.libs.json.{ Json, Format }

  implicit val player: OFormat[Player] = Json.format[Player]
  implicit val gameState: OFormat[GameState] = Json.format[GameState]

}
