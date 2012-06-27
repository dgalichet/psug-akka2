package psug

import akka.actor._
import akka.util.duration._
import akka.util.{Timeout, Duration}
import org.specs2.matcher.MatchResult

/**
 * @author David Galichet.
 */

object TestUtils {

  implicit val timeout = Timeout(5.seconds)

  val waitDuration = 5 second

  def useActor(props: Props)(test: ActorRef => MatchResult[_]) = {
    val system = ActorSystem("root")
    val actor = system.actorOf(props, name = "test")
    try {
      test(actor)
    } finally {
      system.shutdown()
    }
  }
}
