package buffers

import akka.actor.ActorSystem
import scala.language.postfixOps
import akka.stream._
import akka.stream.scaladsl._
import customgraphs.UnzipExample.system
import scala.concurrent.Future
import scala.concurrent.duration._

/*
* Fast Producer
* Slow consumer
* */
object ConflateExample extends App {

  implicit val system = ActorSystem("test-system")
  import system.dispatcher
  implicit val materializer = ActorMaterializer()


  val source = Source(1 to 100)

  val flow = Flow[Int]
    .conflate(_ + _) // Producer is slow, give me the executive summary
    .throttle(1, 100 milliseconds, 1, ThrottleMode.shaping)

  val sink = Sink.foreach(println)

  source via flow to sink run

}
