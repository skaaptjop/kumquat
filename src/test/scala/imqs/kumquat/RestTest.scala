package imqs.kumquat

import org.scalatest.{Matchers, FunSpec}
import spray.http._
import HttpMethods._
import akka.pattern.ask
import MediaTypes._
import akka.io.IO
import scala.io.Source
import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.{ActorRef, Props, ActorSystem}
import spray.can.Http
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}


/**
 * User: weber
 * Date: 2013/11/03
 * Time: 12:08 PM
 */
class RestTest  extends FunSpec with Matchers {

  implicit val timeout = Timeout(5 seconds)
  implicit val system = ActorSystem()
  val alarmProcessor: ActorRef = system.actorOf(Props[PacketHandler])
  val restProp = system.actorOf(Props(classOf[DemoService],alarmProcessor), "rest_service")
  IO(Http) ! Http.Bind(restProp, interface = "localhost", port = 9111)


  describe("deliver packets") {

    it("delivers single packets") {
      val packets = Source.fromFile("src/test/scala/imqs/kumquat/test.csv","UTF-8").getLines()
      def getBody(t:String) = HttpEntity(`text/csv`,t)
      def post(body:String ) = HttpRequest(method = PUT, uri = "http://localhost:9111/api/telco/v1_0/uploadraw", entity = getBody(body) )

      val futures = for(p <- packets) yield (IO(Http) ? post(p)).mapTo[HttpResponse]
      val result = Await.result(Future.sequence(futures),10 seconds)

      for (r <- result) {
        assert(r.status == StatusCode.int2StatusCode(200))
      }
    }
  }
}
