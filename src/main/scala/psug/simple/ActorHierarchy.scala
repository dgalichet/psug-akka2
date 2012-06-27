package psug.simple

import akka.actor.{ActorRef, Props, Actor}

import akka.dispatch.Future
import akka.routing.RoundRobinRouter
import akka.util.{Timeout, Duration}
import java.util.concurrent.TimeUnit
import scala.math._
import akka.pattern.{ask, pipe}

/**
 * @author David Galichet.
 */

sealed trait Event
case object Compute extends Event
case class Work(i: Int) extends Event
case class Result(i: Int) extends Event
case class Error(s: String) extends Event

class Master extends Actor {

  val waitDuration = Duration(5, TimeUnit.SECONDS)
  implicit val timeout = Timeout(waitDuration)

  import context._

  val nrOfWorkers = 4

  val workerRouter = context.actorOf(Props[Worker].withRouter(RoundRobinRouter(nrOfWorkers)), name = "workerRouter")

  protected def receive = {
    case Compute => processComputation(sender)
  }

  def processComputation(replyTo: ActorRef) {
    val futures = (1 to 10).map( x => (workerRouter ? Work(x)).mapTo[Result] )
    val future = Future.sequence(futures).map( lr => lr.map(_.i).sum ).map(Result(_))
    //future.onComplete( _.fold( { e => replyTo ! Error(e.getMessage) }, { r => replyTo ! r } ) )
    future pipeTo replyTo
  }
}

class Worker extends Actor {
  protected def receive = {
    case Work(i) => sender ! Result(doWork(i))
  }

  private def doWork(i: Int) = {
    println("computing...")
    Thread.sleep(round(random * 1000))
    i^2
  }
}

