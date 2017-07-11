package customgraphs

import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl._

object PartialGraphExample extends App {

  implicit val system = ActorSystem("test-system")
  import system.dispatcher
  implicit val materializer = ActorMaterializer()

  val pickMaxOfTwoPartialGraph = GraphDSL.create() { implicit b =>
    import GraphDSL.Implicits._

    val zip = b.add(ZipWith[Int, Int, Int](math.max))

    UniformFanInShape(zip.out, zip.in0, zip.in1)
  }

  val resultSink = Sink.head[Int]

  /*
  * No compile-time safety if all things are connected
  * Checks are done at runtime
  * */
  val graph = GraphDSL.create(resultSink) { implicit b => sink =>
    import GraphDSL.Implicits._

    // importing the partial graph will return its shape (inlets & outlets)
    val pm2 = b.add(pickMaxOfTwoPartialGraph)

    Source.single(1) ~> pm2.in(0)
    Source.single(5) ~> pm2.in(1)

    pm2.out ~> sink.in

    ClosedShape
  }

  val runnableGraph = RunnableGraph.fromGraph(graph)

  val result = runnableGraph.run()

  result.onComplete{ res =>
    println(res)
    val t = system.terminate()
    t.onComplete(println)
  }

}
