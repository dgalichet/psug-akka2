import akka.actor._
import akka.dispatch.Await
import org.specs2.Specification
import akka.pattern.ask
import TestUtils._

/**
 * @author David Galichet.
 */

class CounterTest extends Specification { def is =
  "A counter actor must"            ^
    "return 0 when just created"    !t01^
    "return 2 after two increments" !t02^
  end

  def t01 = useCounterActor { counter =>
    val future = counter ? GetCount
    val count = Await.result(future, waitDuration)
    count === 0
  }

  def t02 = useCounterActor { counter =>
    counter ! IncCounter
    counter ! IncCounter
    val future = counter ? GetCount
    val count = Await.result(future, waitDuration)
    count === 2
  }

  val useCounterActor = useActor(Props[Counter]) _

}
