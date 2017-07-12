package errorhandling

import akka.actor.ActorSystem
import scala.language.postfixOps
import akka.stream._
import akka.stream.scaladsl._
import customgraphs.UnzipExample.system

import scala.concurrent.Future


object DivideResumeExample extends App {

  implicit val system = ActorSystem("test-system")

  import system.dispatcher

  val decider: Supervision.Decider = {
    case _: ArithmeticException => Supervision.Resume //drop anything divided by zero
    case _ => Supervision.Stop
  }

  implicit val materializer =
    ActorMaterializer(
      ActorMaterializerSettings(system)
        .withSupervisionStrategy(decider)
    )

  val source = Source(-4 to 5).scan(0)((acc, elem) => acc + 100 / elem)
  val result = source.runWith(Sink.fold(0)(_ + _))

  result.onComplete {
    res =>
      println(res)
      val t = system.terminate()
      t.onComplete(println)
  }

}

object DivideResumeExample2 extends App {

  implicit val system = ActorSystem("test-system")

  import system.dispatcher

  implicit val mat = ActorMaterializer()

  val decider: Supervision.Decider = {
    case _: ArithmeticException => Supervision.Resume //drop anything divided by zero
    case _ => Supervision.Stop
  }


  val flow = Flow[Int]
    .scan(0)((acc, elem) => acc + 100 / elem)
    .withAttributes(ActorAttributes.supervisionStrategy(decider))

  val source = Source(-4 to 5)
  val result = source.via(flow).runWith(Sink.fold(0)(_ + _))

  result.onComplete {
    res =>
      println(res)
      val t = system.terminate()
      t.onComplete(println)
  }

}

/*
* State is lost on restart so final answer is different
* */
object DivideRestartExample extends App {

  implicit val system = ActorSystem("test-system")

  import system.dispatcher

  implicit val mat = ActorMaterializer()

  val decider: Supervision.Decider = {
    case _: ArithmeticException => Supervision.Restart //drop anything divided by zero
    case _ => Supervision.Stop
  }


  val flow = Flow[Int]
    .scan(0)((acc, elem) => acc + 100 / elem)
    .withAttributes(ActorAttributes.supervisionStrategy(decider))

  val source = Source(-4 to 5)
  val result = source.via(flow).runWith(Sink.fold(0)(_ + _))

  result.onComplete {
    res =>
      println(res)
      val t = system.terminate()
      t.onComplete(println)
  }

}
