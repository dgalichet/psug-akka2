package psug.remoteDeploy

import akka.actor._
import akka.pattern._
import psug.simple._
import akka.dispatch.Future
import akka.util.{Timeout, Duration}
import java.util.concurrent.TimeUnit
import akka.kernel.Bootable
import com.typesafe.config.ConfigFactory
import akka.routing.FromConfig

/**
 * @author David Galichet.
 */

class RemoteMaster extends Actor {

  val waitDuration = Duration(10, TimeUnit.SECONDS)
  implicit val timeout = Timeout(waitDuration)
  import context._

  val worker = context.actorOf(Props[Worker], "worker")

  protected def receive = {
    case Compute => processComputation(sender)
  }

  def processComputation(replyTo: ActorRef) {
    val futures = (1 to 100).map( x => (worker ? Work(x)).mapTo[Result] )
    val future = Future.sequence(futures).map( lr => lr.map(_.i).sum ).map(Result(_))
    future pipeTo replyTo
  }
}

class RemoteMasterApp(private val name: String) extends Bootable {

  val config = ConfigFactory.load().getConfig(name)
  val system = ActorSystem(name, config)

  def startup() { }

  def shutdown() {
    system.shutdown()
  }
}

object RemoteMasterApp extends App {
  val name = args.headOption.getOrElse("node1")
  new RemoteMasterApp(name)
}
