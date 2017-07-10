package step1

import scala.language.postfixOps
import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl._
import scala.concurrent._
import scala.concurrent.duration._

object MaterializedValues extends App {

  implicit val system = ActorSystem("test-system")
  implicit val materializer = ActorMaterializer()

  val source: Source[Int, NotUsed] = Source(1 to 10).throttle(1, 1 second, 8, ThrottleMode.shaping)

  val sink: Sink[Any, Future[Done]] = Sink.foreach(println)


  val left: RunnableGraph[NotUsed] = source.toMat(sink)(Keep.left)

  val right: RunnableGraph[Future[Done]] = source.toMat(sink)(Keep.right)

  val both: RunnableGraph[(NotUsed, Future[Done])] = source.toMat(sink)(Keep.both)

  val none: RunnableGraph[NotUsed] = source.toMat(sink)(Keep.none)

  left.run()
  right.run()
  both.run()
  none.run()

}
