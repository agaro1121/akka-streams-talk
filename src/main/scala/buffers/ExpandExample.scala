package buffers

import akka.actor.ActorSystem
import scala.language.postfixOps
import akka.stream._
import akka.stream.scaladsl._
import customgraphs.UnzipExample.system
import scala.concurrent.Future
import scala.concurrent.duration._

/*
* Slow producer
* Fas consumer
* */
object ExpandExample extends App {

  implicit val system = ActorSystem("test-system")
  import system.dispatcher
  implicit val materializer = ActorMaterializer()

  val source = Source(1 to 5).throttle(1, 300 milliseconds, 1, ThrottleMode.shaping)

  val flow = Flow[Int]
      .expand(Iterator.continually(_)) //fill the void with the last element

  val sink = Sink.foreach(println)

  source via flow to sink run

}
