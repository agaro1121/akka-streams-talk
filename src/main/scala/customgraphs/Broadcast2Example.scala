package customgraphs

import akka.actor.{Actor, ActorSystem, PoisonPill, Props}
import akka.stream._
import akka.stream.scaladsl._

class EchoActor extends Actor {
  override def receive: Receive = {
    case msg => println(s"received = $msg")
  }
}
object EchoActor {
  def props: Props = Props(new EchoActor())
}

object Broadcast2Example extends App {

  implicit val system = ActorSystem("test-system")
  import system.dispatcher
  implicit val materializer = ActorMaterializer()

  val remoteProcessing = Sink.actorRef(system.actorOf(EchoActor.props), PoisonPill)
  val localProcessing = Sink.foreach[Int](println)

  val graph = GraphDSL.create() {
    implicit builder =>
      import GraphDSL.Implicits._

      val bcast = builder.add(Broadcast[Int](2))

      Source(List(0, 1, 2)) ~> bcast.in
      bcast.out(0) ~> remoteProcessing
      bcast.out(1) ~> localProcessing

      ClosedShape
  }

  val runnableGraph = RunnableGraph.fromGraph(graph)

  runnableGraph.run()

}
