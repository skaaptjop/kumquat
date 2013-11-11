package imqs.kumquat

import akka.io.IO
import spray.can.Http
import akka.actor.{ActorRef, Actor, Props, ActorSystem}
import java.sql.Timestamp
import spray.routing.HttpService

object Main extends App {

    implicit val system = ActorSystem()
    val alarmProcessor: ActorRef = system.actorOf(Props[PacketHandler])
    val restProp = system.actorOf(Props(classOf[DemoService],alarmProcessor), "rest_service")
    IO(Http) ! Http.Bind(restProp, interface = "localhost", port = 9111)
  }

class DemoService(handler: ActorRef) extends Actor with HttpService {
  def actorRefFactory = context
  implicit def executionContext = actorRefFactory.dispatcher

  def receive = runRoute(demoRoute)

  val demoRoute = {
    pathPrefix("api" / "telco" / "v1_0") {
      path("uploadraw") {
        put { req =>
          handler ! req.request.entity.asString
          req.complete("Success")
        }
      }
    }
  }
}

class PacketHandler extends Actor {
  case class SiteInfo(event: Long, s:String, t: Timestamp)
  var statusMap = Map[Int,SiteInfo]()
  def receive = {
    case s: String =>
      val site = s.split(',')(1).toInt
      val m = SiteInfo(0l,s,new Timestamp(System.currentTimeMillis()))
      statusMap = statusMap + (site -> m)
  }
}

