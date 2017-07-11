package customgraphs

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl._

import scala.collection.immutable
import scala.concurrent.Future

object SourceFromGraph extends App {

  implicit val system = ActorSystem("test-system")

  import system.dispatcher

  implicit val materializer = ActorMaterializer()

  val evensAndOdds: Source[(Int, Int), NotUsed] =
    Source.fromGraph(GraphDSL.create() { implicit b =>
      import GraphDSL.Implicits._

      val zip = b.add(Zip[Int, Int]())

      def ints = Source(1 to 6)

      ints.filter(_ % 2 != 0) ~> zip.in0
      ints.filter(_ % 2 == 0) ~> zip.in1

      // expose port
      SourceShape(zip.out)
    })

  val pairs: Future[immutable.Seq[(Int, Int)]] =
    evensAndOdds.runWith(Sink.seq)

  pairs.onComplete { res =>
    println(res)
    val t = system.terminate()
    t.onComplete(println)
  }

}
