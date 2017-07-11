package parallel

import java.time.Instant
import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl._
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps


object ParallelExample extends App {

  implicit val system = ActorSystem("test-system")
  import system.dispatcher
  implicit val mat = ActorMaterializer()

  val source = Source(1 to 100)

  val flow = Flow[Int]
    .mapAsync(parallelism = 32)(i =>
      Future{
        Thread.sleep(1000) //some expensive operation
        i
      })

  val sink = Sink.foreach(println)

  val start = Instant.now.toEpochMilli
  val res = source.via(flow).toMat(sink)(Keep.right).run

  res.onComplete { _ =>
    val end = Instant.now.toEpochMilli
    println(s"time to complete = ${end - start} milliseconds")
    val t = system.terminate()
    t.onComplete(println)
  }


}
object ParallelExample2 extends App {

  implicit val system = ActorSystem("test-system")
  import system.dispatcher
  implicit val mat = ActorMaterializer()

  val source = Source(1 to 100)

  val flow = Flow[Int]
    .mapAsyncUnordered(parallelism = 32)(i =>
      Future{
        Thread.sleep(1000) //some expensive operation
        i
      })

  val sink = Sink.foreach(println)

  val start = Instant.now.toEpochMilli
  val res = source.via(flow).toMat(sink)(Keep.right).run

  res.onComplete { _ =>
    val end = Instant.now.toEpochMilli
    println(s"time to complete = ${end - start} milliseconds")
    val t = system.terminate()
    t.onComplete(println)
  }


}

