package backpressure

import java.nio.file.Paths

import scala.language.postfixOps
import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.event.Logging
import akka.stream._
import akka.stream.scaladsl._

import scala.concurrent._
import scala.concurrent.duration._
import akka.http.scaladsl._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.ByteString

import scala.io.StdIn

// watch -d -n 1 netstat -n -p tcp
object Server extends App {

  implicit val system = ActorSystem("test-system")
  implicit val materializer = ActorMaterializer()
  import system.dispatcher

  val printFlow: Flow[ByteString, ByteString, NotUsed] =
    Flow[ByteString]
      .log("PrintFlow", byteString => byteString.utf8String)
      .withAttributes(Attributes.logLevels(onElement = Logging.InfoLevel))
      .throttle(1, 100 milliseconds, 1, ThrottleMode.shaping) // for effect


  val source: Source[ByteString, Future[IOResult]] =
    FileIO.fromPath(Paths.get("src/main/resources/bitcoinData.csv"))

  val route: Route = path("data") {
    complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, source.via(printFlow)))
  }

  val port = 9000
  val bindingFuture: Future[Http.ServerBinding] =
    Http().bindAndHandle(route, interface = "localhost", port)

  println(s"Server online at http://localhost:$port/data\nPress RETURN to stop...")

  StdIn.readLine() // let it run until user presses return

  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done

}
