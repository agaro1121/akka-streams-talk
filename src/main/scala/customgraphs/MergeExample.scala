package customgraphs

import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl._

object MergeExample extends App {

  implicit val system = ActorSystem("test-system")
  import system.dispatcher
  implicit val materializer = ActorMaterializer()


  val source1 = Source.single(1)
  val source2 = Source.single(2)

  val sink = Sink.foreach(println)


  val graph = GraphDSL.create(){
    implicit builder =>
      import GraphDSL.Implicits._

      val merge = builder.add(Merge[Int](2))

      source1 ~> merge.in(0)
      source2 ~> merge.in(1)
      merge.out ~> Flow[Int].fold(0)(_ + _) ~> sink

      ClosedShape
  }

  val runnableGraph = RunnableGraph.fromGraph(graph)

  val res = runnableGraph.run()

}
