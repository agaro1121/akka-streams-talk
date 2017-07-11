package buffers

import akka.actor.ActorSystem
import scala.language.postfixOps
import akka.stream._
import akka.stream.scaladsl._
import customgraphs.UnzipExample.system
import scala.concurrent.Future
import scala.concurrent.duration._


object BuffersExample extends App {

  implicit val system = ActorSystem("test-system")
  implicit val materializer = ActorMaterializer()

  val source = Source(1 to 100)

  val flow = Flow[Int]
    .buffer(40, OverflowStrategy.dropNew)
    .throttle(1, 100 milliseconds, 1, ThrottleMode.shaping)

  val sink = Sink.foreach(println)

  source via flow to sink run
}
