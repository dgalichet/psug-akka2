package psug.remoteDeploy

import org.specs2.Specification
import akka.util.{Duration, Timeout}
import java.util.concurrent.TimeUnit
import akka.actor._
import akka.pattern._
import akka.dispatch.Await
import org.specs2.matcher.MatchResult
import com.typesafe.config.ConfigFactory
import psug.simple._

/**
 * @author David Galichet.
 */

class RemoteMasterTest extends Specification { def is =
  "Master actor must"                     ^
    "calculate sum of 1 to 10 squares"    !e01^
  end

  def e01 = useActor { master =>
    val future = (master ? Compute).mapTo[Event]
    val result = Await.result(future, waitDuration)
    result must beAnInstanceOf[Result] and
      result === Result(5050)
  }

  val waitDuration = Duration(10, TimeUnit.SECONDS)
  implicit val timeout = Timeout(waitDuration)

  def useActor(test: ActorRef => MatchResult[_]) = {
    val system = ActorSystem("root", ConfigFactory.load().getConfig("masterNode"))
    val node1 = ActorSystem("node1", ConfigFactory.load().getConfig("node1"))
    val actor = system.actorOf(Props[RemoteMaster], name = "test")
    try {
      test(actor)
    } finally {
      system.shutdown()
      node1.shutdown()
    }
  }
}
