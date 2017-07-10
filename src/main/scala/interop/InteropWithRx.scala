package interop


import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import rx.RxReactiveStreams
import rx.lang.scala.JavaConversions.toJavaObservable
import rx.lang.scala.Observable

import scala.language.postfixOps

object InteropWithRx extends App {

  implicit val system = ActorSystem("test-system")
  implicit val materializer = ActorMaterializer()

  val o: Observable[Int] = Observable.from(1 to 10)
  val publisher = RxReactiveStreams.toPublisher(toJavaObservable(o))
  val source = Source.fromPublisher(publisher)

  source.runWith(Sink.foreach(println))

}
