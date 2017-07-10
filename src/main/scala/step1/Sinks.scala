package step1

import scala.language.postfixOps
import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ThrottleMode}
import akka.stream.scaladsl._
import scala.concurrent._
import scala.concurrent.duration._

object Sinks extends App {

  implicit val system = ActorSystem("test-system")

  import system.dispatcher

  implicit val materializer = ActorMaterializer()

  val source: Source[Int, NotUsed] = Source(1 to 10).throttle(1, 1 second, 8, ThrottleMode.shaping)
  val factorials: Source[BigInt, NotUsed] = source.scan(BigInt(1))(_ * _)

  source.runWith(Sink.ignore)
  source.runWith(Sink.foreach(println))

  val sinkFold: Sink[BigInt, Future[BigInt]] =
    Sink.fold[BigInt, BigInt](BigInt(0))(_ + _)

  val factorialSum: Future[BigInt] = factorials.runWith(sinkFold)
  factorialSum.onComplete(println)

  source.runWith(Sink.cancelled)
  val head = source.runWith(Sink.head)
  head.onComplete(println)

  val tail = source.runWith(Sink.last)
  tail.onComplete(println)

  val seq = source.runWith(Sink.seq)
  seq.onComplete(println)

  val q = source.runWith(Sink.queue())

  val one = q.pull()
  one.onComplete(println)

  val two = q.pull()
  two.onComplete(println)

  val three = q.pull()
  three.onComplete(println)


}
