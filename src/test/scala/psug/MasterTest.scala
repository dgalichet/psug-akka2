package psug

import akka.actor.Props
import akka.dispatch.Await
import org.specs2.Specification
import TestUtils._
import akka.pattern.ask

/**
 * @author David Galichet.
 */

class MasterTest extends Specification { def is =
  "Master actor must"                     ^
    "calculate sum of 1 to 10 squares"    !e01^
  end

  def e01 = useMasterActor { master =>
    val future = (master ? Compute).mapTo[Event]
    val result = Await.result(future, waitDuration)
    result must beAnInstanceOf[Result] and
    result === Result(55)
  }

  val useMasterActor = useActor(Props[Master]) _
}
