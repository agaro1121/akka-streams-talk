package hugefile

import java.nio.file.Paths
import scala.language.postfixOps
import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl._

import scala.concurrent._
import scala.concurrent.duration._


object HugeFile extends App {

  implicit val system = ActorSystem("test-system")
  implicit val materializer = ActorMaterializer()
  import system.dispatcher

  val source: Source[(String, Long), Future[IOResult]] =
    FileIO.fromPath(Paths.get("src/main/resources/bitcoinData.csv"))
        .map(_.utf8String)
    .zipWithIndex

  val (io, done): (Future[IOResult], Future[Done]) =
    source.toMat(Sink.foreach(println))(Keep.both).run()

  io.foreach(println)
  done.foreach(println)

}
