import org.scalatest.{FunSpec, Matchers}
import spray.http.HttpHeaders.Accept
import spray.http.MediaRange.One
import spray.http.{MediaType, HttpHeader, HttpEntity}
import spray.http.MediaTypes._
import spray.httpx.marshalling.Marshaller
import spray.json._
import spray.routing.Directives._
import spray.routing.HttpService.sealRoute
import spray.testkit.ScalatestRouteTest
import spray.httpx.SprayJsonSupport._

// All the fuss is about this...
case class Order(name: String, no: Int)

// Json marshaller
object JsonMarshal extends DefaultJsonProtocol {
  implicit val impOrder = jsonFormat2(Order)
}

// Custom marshalling for our text/plain
object TextMarshal {
  implicit val orderMarshaller = Marshaller.of[Order](`text/plain`) {
    (value, content, ctx) => ctx.marshalTo(HttpEntity(content, value.toString))
  }
}

class TestMarshalByAccept extends FunSpec with ScalatestRouteTest with Matchers {

  def getAccept(headers: List[HttpHeader]): Option[MediaType] = {
    headers.collectFirst({ case Accept(media) => media }) match {
      case Some(a) => Some(a(0).asInstanceOf[One].mediaType) // This took a little detective work
      case None => None
    }
  }

  // Marshals response according to the Accept header media type
  val fetchOrder = path("order" / IntNumber) {
    id => get { ctx =>
      getAccept(ctx.request.headers) match {
        case Some(`application/json`) | None =>
          ctx.complete(Order("Bender", id))(sprayJsonMarshallerConverter(JsonMarshal.impOrder))
        case Some(`text/plain`) =>
          ctx.complete(Order("Bender", id))(TextMarshal.orderMarshaller)
        case _ =>
          ctx.complete(406, "Invalid media type requested")
      }
    }
  }

  describe("Our route should") {
    import JsonMarshal._

    it("return a json if requested") {
      Get("/order/1234").withHeaders(Accept(`application/json`)) ~> fetchOrder ~> check {
        contentType.mediaType.toString() should equal(Accept(`application/json`).value)
        responseAs[String] should equal(Order("Bender", 1234).toJson.prettyPrint)
      }
    }

    it("return text if requested") {
      Get("/order/1235").withHeaders(Accept(`text/plain`)) ~> fetchOrder ~> check {
        contentType.mediaType.toString should equal(Accept(`text/plain`).value)
        responseAs[String] should equal(Order("Bender", 1235).toString)
      }
    }

    it("fail with unknown media types") {
      Get("/order/1234").withHeaders(Accept(`text/html`)) ~> sealRoute(fetchOrder) ~> check {
        status.intValue should equal(406)
      }
    }

    it("give us json with no Accept header") {
      Get("/order/1234") ~> sealRoute(fetchOrder) ~> check {
        contentType.mediaType.toString should equal(Accept(`application/json`).value)
        responseAs[String] should equal(Order("Bender", 1234).toJson.prettyPrint)
      }
    }
  }
}
