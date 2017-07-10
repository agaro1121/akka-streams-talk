package step1

import scala.language.postfixOps
import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl._
import scala.concurrent._
import scala.concurrent.duration._

object Flows extends App {

  implicit val system = ActorSystem("test-system")
  implicit val materializer = ActorMaterializer()

  val source: Source[Int, NotUsed] = Source(1 to 10)

  val sink: Sink[Any, Future[Done]] = Sink.foreach(println)


  val slowFlow: Flow[Int, Int, NotUsed] =
    Flow[Int].throttle(1, 1 second, 8, ThrottleMode.shaping)

  source.via(slowFlow).to(sink).run()

  val idFlow: Flow[Int, (Int, Int), NotUsed] =
    Flow[Int].zip(Source(0 to 9))
      .map(_.swap)

  idFlow.runWith(source, sink)

  val plusFlow = Flow.fromFunction((i: Int) => i + 1)
  source.via(plusFlow).to(sink).run()

  val all = Flow.apply[Int]
  source.via(all).to(sink).run()

}
