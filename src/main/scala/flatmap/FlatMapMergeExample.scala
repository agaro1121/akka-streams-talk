package flatmap

import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl._

object FlatMapMergeExample extends App {

  implicit val system = ActorSystem("test-system")
  implicit val materializer = ActorMaterializer()

  val source = Source(0 to 2)

  val flow =
    Flow[Int]
      .flatMapMerge(2, {
        num =>
          val plusOne = num + 1
          Source(plusOne to plusOne + 2)
      })

  val sink = Sink.foreach[Int](n => println(n))

  source via flow to sink run()

}
