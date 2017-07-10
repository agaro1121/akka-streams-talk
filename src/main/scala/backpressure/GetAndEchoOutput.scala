package backpressure

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpRequest}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.{ActorMaterializer, IOResult, ThrottleMode}
import akka.stream.scaladsl.{FileIO, Flow, Framing, Source}
import akka.util.ByteString

import scala.concurrent.Future
import scala.io.StdIn
import scala.concurrent.duration._
import scala.language.postfixOps

object GetAndEchoOutput extends App {

  implicit val system = ActorSystem("test-system")
  implicit val materializer = ActorMaterializer()

  import system.dispatcher

  val printFlow: Flow[ByteString, ByteString, NotUsed] =
    Flow[ByteString].map{
      bs =>
        println(bs.utf8String)
        bs
    }

  val slowerFlow: Flow[ByteString, ByteString, NotUsed] =
    Flow[ByteString].throttle(1, 200 milliseconds, 1, ThrottleMode.shaping)

  val source: () => Future[Source[ByteString, Any]] = () =>
    Http()
      .singleRequest(HttpRequest(uri = "http://localhost:9000/data"))
      .map { resp =>
        resp.entity.dataBytes
      }


  val route: Route =
    get {
      onSuccess(source()) { stream =>
        complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, stream.via(printFlow)))
      }
    }

  val port = 9001
  val bindingFuture: Future[Http.ServerBinding] =
    Http().bindAndHandle(route, interface = "localhost", port)

  println(s"Server online at http://localhost:$port/\nPress RETURN to stop...")

  StdIn.readLine() // let it run until user presses return

  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done

}
