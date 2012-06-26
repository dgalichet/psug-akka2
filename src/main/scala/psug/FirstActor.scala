/**
 * @author David Galichet.
 */

package psug

import akka.actor._
import akka.util.Duration
import akka.util.duration._

sealed trait Message

case object IncCounter extends Message
case object GetCount extends Message

class Counter extends Actor {

  var counter = 0

  protected def receive = {
    case IncCounter => counter += 1
    case GetCount => sender ! counter
  }
}
