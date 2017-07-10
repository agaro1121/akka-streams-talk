package step1

import scala.language.postfixOps
import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.util.ByteString
import scala.concurrent._
import scala.concurrent.duration._
import java.nio.file.Paths

/*
* Stream imports
* */
import akka.stream._
import akka.stream.scaladsl._

object Sources extends App {

  /*
  * 1. Always need actor system
  * */
  implicit val system = ActorSystem("test-system")
  import system.dispatcher

  /*
  * 2. Always need a materializer
  * */
  implicit val materializer = ActorMaterializer()

  /*
  * Source
  *
  * 2 Param Types:
  * 1st -> Type of element stream emits
  * 2nd -> Materialized value from the stream
  * i.e in network connections, could be port#
  *     in file streams, could be a signal that file is [[Done]]
  * In this case, it is NotUsed because `1 to 100` no auxiliary
  * information is produced from this stream
  *
  *
  * */
  val source: Source[Int, NotUsed] =
    Source(1 to 10)


  val done: Future[Done] = source.runForeach(println)

  done.onComplete { d =>
    //shut down actor system
    println("pretending to shut down actor sytem...")
    println(d)
  }

  val factorials: Source[BigInt, NotUsed] =
    source.scan(BigInt(1))(_ * _)

  factorials.runForeach(println)

  val factorialWithId = factorials
    .zipWith(Source(0 to 9))((num, idx) => s"$idx! = $num")
    .throttle(1, 1 second, 5, ThrottleMode.shaping)

  factorialWithId.runForeach(println)

  // Keep whatever Materialized value you want
  factorialWithId.toMat(Sink.foreach(println))(Keep.none)


}
