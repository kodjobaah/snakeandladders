import actors.{ GameStateActor, MoveTokenActor }
import com.google.inject.AbstractModule
import models.GameStateDao
import play.api.libs.concurrent.AkkaGuiceSupport

class Module extends AbstractModule with AkkaGuiceSupport {
  override def configure() = {
    bind(classOf[GameStateDao])
    bindActor[GameStateActor]("gamestate-actor")
    bindActor[MoveTokenActor]("movetoken-actor")
  }
}
