package customgraphs

import scala.language.postfixOps
import java.nio.file.Paths
import akka.{Done, NotUsed, stream}
import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl._
import akka.util.ByteString
import scala.concurrent._
import scala.concurrent.duration._

object UnzipExample extends App {

  implicit val system = ActorSystem("test-system")
  import system.dispatcher
  implicit val materializer = ActorMaterializer()

  val fileContents: Source[ByteString, Future[IOResult]] =
    FileIO.fromPath(Paths.get("src/main/resources/bitcoinData.csv"))

  val splitItemAndProps: Flow[ByteString, (String, String), NotUsed] =
    Flow[ByteString]
      .map(_.utf8String)
      .map { str =>
        val head :: tail = str.split(",").toList
        (head, tail.mkString(","))
      }

  val printer = Flow[(String, String)]
    .map { case (item, props) =>
      println(s"item=$item and props=$props")
      (item, props)
    }

  val back2ByteString = Flow[String].map(ByteString(_))


  val sinkHead: Sink[ByteString, Future[IOResult]] = FileIO.toPath(Paths.get("ItemIds.txt"))
  val sinkTail: Sink[ByteString, Future[IOResult]] = FileIO.toPath(Paths.get("ItemProps.csv"))

  val customGraph: Graph[ClosedShape, (Future[IOResult], Future[IOResult])] =
    GraphDSL.create(sinkHead, sinkTail)((_,_)) {
    implicit builder => (sinkH, sinkT) =>
      import GraphDSL.Implicits._

      val unzipper = builder.add(Unzip[String, String])

      fileContents ~> splitItemAndProps ~> printer ~> unzipper.in
      unzipper.out0 ~> back2ByteString ~> sinkH
      unzipper.out1 ~> back2ByteString ~> sinkT

      ClosedShape
  }

  val (shIO, stIO) = RunnableGraph.fromGraph(customGraph).run()

  shIO.zip(stIO)
    .onComplete{ res =>
      println(res)
      val t = system.terminate()
      t.onComplete(println)
    }

}
