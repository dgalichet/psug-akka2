package psug.supervision

import akka.actor._
import collection.mutable
import akka.util.duration._
import akka.actor.SupervisorStrategy._
import scala.math._
import psug.utils._

/**
 * @author David Galichet.
 */

sealed trait Event
case class Message(msg: String, to: Long) extends Event
case class Get(id: Long) extends Event
case class Messages(messages: List[String]) extends Event
case class Error(msg: String) extends Event
case object Load extends Event

class ActorSupervision extends Actor with ActorLogging {

  val actorMap = mutable.HashMap.empty[Long, ActorRef]

  val storageActorRef = context.actorOf(Props[StorageActor])
  context.watch(storageActorRef)

  // After 5 restarts in a range of 10 seconds, the StorageActor will be stopped
  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 5, withinTimeRange = 10 seconds) {
    case _: StorageActor.StorageException => Restart
  }

  protected def receive = {
    case message@Message(_, to) => {
      val dest = actorMap.getOrElseUpdate(to, context.actorOf(Props(new UserActor(to))))
      dest forward message
      storageActorRef ! message
    }
    case event@Get(id) => actorMap.get(id) match {
      case Some(actorRef) => actorRef forward event
      case None => sender ! Error("Id %d doesn't exists".format(id))
    }
    case Terminated(`storageActorRef`) => {
      // ... embrace storage failure ! (alert Sysadmin, don't care, crippled mode, stop the application ...)
      log.error(redBoldMsg("Storage actor has been stopped"))
    }
  }
}

class UserActor(private val id: Long) extends Actor with ActorLogging {
  private[this] val messages = mutable.ListBuffer.empty[String]

  protected def receive = {
    case Message(msg, _) => messages.prepend(msg)
    case Get(_) => sender ! Messages(messages.toList); messages.clear()
  }
}

class StorageActor extends Actor with ActorLogging {
  import StorageActor._

  override def preStart() {
    log.info(redMsg("initializing database connection"))
  }


  override def preRestart(reason: Throwable, message: Option[Any]) {
    log.info(redMsg("Actor has been restarted [%s] due to exception %s".format(message.getOrElse(""), reason.getMessage)))
  }

  protected def receive = {
    case msg:Message => handleNewMessage(msg) //...
    case Get(id) => log.info(greenMsg("dropping messages for " + id)) //...
    // case Load => ...
  }

  def handleNewMessage(msg: Message) {
    if (random > 0.8) throw new StorageException("Connection to database lost ...")
    log.info(greenMsg("storing message..."))
    //...
  }

  override def postStop() {
    log.info(redMsg("stopping database connection...")) //...
  }

  override def postRestart(reason: Throwable) {
    log.info(redMsg("post restart hook...")) //...
  }
}

object StorageActor {
  class StorageException(msg: String) extends Exception(msg)
}

object TestSupervision extends App {

  val system = ActorSystem("TestSupervision")
  val actorSupervision = system.actorOf(Props[ActorSupervision], name = "supervisor")
  val messages = (1 to 200) map { i => Thread.sleep(10); Message("Welcome user " + i, i) }
  messages foreach( actorSupervision ! _ )
  Thread.sleep(2000)
  system.shutdown()
}
