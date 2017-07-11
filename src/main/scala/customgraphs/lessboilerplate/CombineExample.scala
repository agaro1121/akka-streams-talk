package customgraphs.lessboilerplate

import akka.actor.{Actor, ActorSystem, PoisonPill, Props}
import akka.stream._
import akka.stream.scaladsl._

import scala.concurrent.Future

class EchoActor extends Actor {
  override def receive: Receive = {
    case msg => println(s"received = $msg")
  }
}
object EchoActor {
  def props: Props = Props(new EchoActor())
}

object CombineExample extends App {

  implicit val system = ActorSystem("test-system")
  import system.dispatcher
  implicit val materializer = ActorMaterializer()

  /*
  *
  * Fan In
  *
  * */

  val sourceOne = Source(List(1))
  val sourceTwo = Source(List(2))
  val merged = Source.combine(sourceOne, sourceTwo)(Merge(_))

  val mergedResult: Future[Int] = merged.runWith(Sink.fold(0)(_ + _))
  mergedResult.onComplete{ res =>
    Thread.sleep(1000)
    println(s"mergedResult=$res")
    val t = system.terminate()
    t.onComplete(println)
  }

  /*
  *
  * Fan Out
  *
  * */
  val sendRmotely = Sink.actorRef(system.actorOf(EchoActor.props), PoisonPill)
  val localProcessing = Sink.foreach[Int](println)

  val sink = Sink.combine(sendRmotely, localProcessing)(Broadcast[Int](_))

  Source(List(0, 1, 2)).runWith(sink)

}
