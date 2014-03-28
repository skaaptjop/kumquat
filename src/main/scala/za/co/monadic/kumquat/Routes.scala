package za.co.monadic.kumquat

import spray.json.DefaultJsonProtocol
import spray.routing.Directives._
import spray.httpx.marshalling.Marshaller
import spray.http.MediaTypes._
import spray.http.HttpEntity


case class Customer(name: String, no: Int)
case class Part( number: Int, name: String, length: Double)

object JsonMarshallers extends DefaultJsonProtocol {
  implicit val customerM = jsonFormat2(Customer)
  implicit val partM = jsonFormat3(Part)
}

// Custom marshalling for our text/plain
object TextMarshal {
  implicit val customerM = Marshaller.of[Customer](`text/plain`) {
    (value, content, ctx) => ctx.marshalTo(HttpEntity(content, value.toString))
  }
  implicit val partM = Marshaller.of[Part](`text/plain`) {
    (value, content, ctx) => ctx.marshalTo(HttpEntity(content, value.toString))
  }
}

trait JsonApi {
  import spray.httpx.SprayJsonSupport._
  implicit val cm = sprayJsonMarshallerConverter(JsonMarshallers.customerM)
  implicit val pm = sprayJsonMarshallerConverter(JsonMarshallers.partM)
}

trait TextApi {
  implicit val cm = TextMarshal.customerM
  implicit val pm = TextMarshal.partM
}

// Fails to compile because it lacks the needed implicits
trait Routes {
  // Need some abstract types here for the implicits
  implicit val cm: Marshaller[Customer]
  implicit val pm: Marshaller[Part]

  val customer = get { path("customer" / IntNumber / ){ id => complete(Customer("Frankie",id)) } }
  val part = get {path("stock" / IntNumber / ){ partNo => complete(Part(partNo,"Power flubber", 1.33)) } }
  val route = path("v1.0") { customer ~ part }
}

object JsonRoutes extends Routes with JsonApi {
  val r = path("api" / "json" ) { route }
}

object TextRoutes extends Routes with TextApi {
  val r = path("api" / "txt" ) { route }
}