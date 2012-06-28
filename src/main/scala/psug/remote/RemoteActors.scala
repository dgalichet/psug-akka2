package psug.remote

import akka.actor._
import psug.simple._
import akka.kernel.Bootable
import com.typesafe.config.ConfigFactory
import akka.dispatch.{Await, Future}
import akka.pattern._
import akka.util.{Duration, Timeout}
import java.util.concurrent.TimeUnit

/**
 * @author David Galichet.
 */


class WorkerApplication(val port: Int) extends Bootable {

  val portConfig = ConfigFactory.parseString("akka.remote.netty.port = " + port)
  val config = ConfigFactory.load().getConfig("remoteWorker").withFallback(portConfig)

  val system = ActorSystem("RemoteWorker", config)
  system.actorOf(Props[Worker], "worker")

  def startup() {}

  def shutdown() { system.shutdown() }
}

object WorkerApplication {

  // run with xsbt 'run xxx' to override port number

  def main(args: Array[String]) {
    val port = args.headOption.map(_.toInt).getOrElse(2552)
    println("Starting worker on port " + port)
    new WorkerApplication(port)
    println("waiting for work ...")
  }
}

class RemoteMaster extends Actor {
  import context._

  val waitDuration = Duration(10, TimeUnit.SECONDS)
  implicit val timeout = Timeout(waitDuration)

  val worker = context.actorFor("akka://RemoteWorker@127.0.0.1:2552/user/worker")

  protected def receive = {
    case Compute => processComputation(sender)
  }

  def processComputation(replyTo: ActorRef) {
    val futures = (1 to 10).map( x => (worker ? Work(x)).mapTo[Result] )
    val future = Future.sequence(futures).map( lr => lr.map(_.i).sum ).map(Result(_))
    future pipeTo replyTo
  }
}

class MasterApplication extends Bootable {

  val waitDuration = Duration(10, TimeUnit.SECONDS)
  implicit val timeout = Timeout(waitDuration)

  val config = ConfigFactory.load().getConfig("remoteMaster")
  val system = ActorSystem("RemoteMaster", config)
  val master = system.actorOf(Props[RemoteMaster])

  val future = (master ? Compute).mapTo[Result]
  println("result = " + Await.result(future, waitDuration))

  def startup() {}

  def shutdown() { system.shutdown() }
}

object MasterApplication extends App {
  new MasterApplication
}
