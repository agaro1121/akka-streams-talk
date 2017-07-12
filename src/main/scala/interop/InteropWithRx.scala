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

  /*
  * Rx Observable
  * */
  val o: Observable[Int] = Observable.from(1 to 10)

  /*
  * Reactive Streams Publisher
  * */
  val publisher = RxReactiveStreams.toPublisher(toJavaObservable(o))

  /*
  * Akka Streams Source from
  * Reactive Streams Publisher
  * */
  val source = Source.fromPublisher(publisher)
  source.runWith(Sink.foreach(println))

}
