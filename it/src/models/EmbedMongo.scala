package models

import de.flapdoodle.embed.mongo.config.{MongodConfigBuilder, Net}
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.mongo.{
  MongodExecutable,
  MongodProcess,
  MongodStarter
}
import de.flapdoodle.embed.process.runtime.Network
import reactivemongo.api.MongoConnection.ParsedURI
import reactivemongo.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, ExecutionContext}

trait EmbeddedMongo {
  val testMongoHosts = "localhost"
  val testMongoPort = 12313
  private var mongodExe: MongodExecutable = _
  private var mongodProcess: MongodProcess = _
  var db: DefaultDB = _

  def startDB(dbName: String)(implicit ctx: ExecutionContext): Unit = {
    start()
    db = createDb(dbName)
  }

  def stopDB(): Unit = {
    import scala.concurrent.duration._
    val longTime = Long.MaxValue nanos

    val connection = db.connection
    connection.close()

    Await.result(connection.actorSystem.terminate(), longTime)
    Await.result(connection.actorSystem.whenTerminated, longTime)
    stop()
  }

  def start(): Unit = {
    mongodExe = prepareExe()
    mongodProcess = mongodExe.start()
  }

  def stop(): Unit = {
    mongodProcess.stop()
    mongodExe.stop()
  }

  def createDb(dbName: String): DefaultDB = {
    val connection = createConnection()
    connection(dbName)
  }

  private def createConnection(): MongoConnection = {
    val driver = new MongoDriver
    driver.connection(
      ParsedURI(
        hosts = List((testMongoHosts, testMongoPort)),
        options = MongoConnectionOptions(),
        ignoredOptions = List.empty[String],
        db = None,
        authenticate = None
      ))
  }

  private def prepareExe(): MongodExecutable =
    MongodStarter.getDefaultInstance.prepare(
      new MongodConfigBuilder()
        .version(Version.Main.PRODUCTION)
        .net(new Net(testMongoHosts, testMongoPort, Network.localhostIsIPv6()))
        .build())

}
