package customgraphs

import akka.actor.ActorSystem
import scala.language.postfixOps
import akka.stream._
import akka.stream.scaladsl._
import customgraphs.UnzipExample.system

import scala.concurrent.Future

object BroadcastExample extends App {

  implicit val system = ActorSystem("test-system")
  import system.dispatcher
  implicit val materializer = ActorMaterializer()

  val topHeadSink = Sink.head[Int]
  val bottomHeadSink = Sink.headOption[Int]
  val sharedDoubler = Flow[Int].map(_ * 2)

  val runnableGraph: RunnableGraph[(Future[Int], Future[Option[Int]])] =
    RunnableGraph.fromGraph(GraphDSL.create(topHeadSink, bottomHeadSink)((_, _)) {
      implicit builder => (topHS, bottomHS) =>
        import GraphDSL.Implicits._

        val broadcast = builder.add(Broadcast[Int](2))

        Source.single(1) ~> broadcast.in
        broadcast.out(0) ~> sharedDoubler ~> topHS.in
        broadcast.out(1) ~> sharedDoubler ~> bottomHS.in

        ClosedShape
    })

  private val (r1, r2) = runnableGraph.run()

  r1.zip(r2).onComplete{ res =>
    println(res)
    val t = system.terminate()
    t.onComplete(println)
  }

}
