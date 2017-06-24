import actors.{ DiceRollActor, GameStateActor, MoveTokenActor }
import com.google.inject.AbstractModule
import models.GameStateDao
import play.api.libs.concurrent.AkkaGuiceSupport
import service.{ MovePlayerService, PlaysFirstService }

class Module extends AbstractModule with AkkaGuiceSupport {
  override def configure() = {
    bind(classOf[PlaysFirstService])
    bind(classOf[GameStateDao])
    bind(classOf[MovePlayerService])
    bindActor[GameStateActor]("gamestate-actor")
    bindActor[MoveTokenActor]("movetoken-actor")
    bindActor[DiceRollActor]("rolldice-actor")
  }
}
