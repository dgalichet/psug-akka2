package psug.supervision

import org.specs2.Specification
import psug.TestUtils._
import akka.actor.Props
import akka.pattern.ask
import akka.dispatch.Await

/**
 * @author David Galichet.
 */

class ActorSupervisionTest extends Specification { def is =
  "Actors must "                          ^
    "send and retrieve messages"          !a01^
    "receive an error when using bad id"  !a02^
  end

  def a01 = useSupervisionActor { actorRef =>
    actorRef ! Message("hello world", 1)
    val resultF = (actorRef ? Get(1)).mapTo[Messages]
    Await.result(resultF, waitDuration) === Messages(List("hello world"))
  }

  def a02 = useSupervisionActor { actorRef =>
    val resultF = actorRef ? Get(5)
    Await.result(resultF, waitDuration) must beAnInstanceOf[Error]
  }

  val useSupervisionActor = useActor(Props[ActorSupervision]) _
}
