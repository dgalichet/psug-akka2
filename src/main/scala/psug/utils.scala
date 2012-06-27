package psug
import scala.Console._

/**
 * @author David Galichet.
 */

object utils {

  def redMsg(msg: String) = RED + msg + RESET

  def redBoldMsg(msg: String) = RED + BOLD + msg + RESET

  def blueMsg(msg: String) = BLUE + msg + RESET

  def greenMsg(msg: String) = GREEN + msg + RESET
}
