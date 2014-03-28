package za.co.monadic.kumquat

import spray.testkit.ScalatestRouteTest
import org.scalatest.{FunSpec, Matchers}

/**
 *
 */
class RouteTest extends FunSpec with ScalatestRouteTest with Matchers{

  import JsonRoutes._
  import spray.json._
  import JsonMarshallers._

  val foo = Customer("a",1).toJson

  describe("Our routes should") {
   it("gets our customer") {
      Get("/customer/233/") ~>  customer  ~> check {
        responseAs[String] should equal(Customer("Frankie",233).toJson.prettyPrint)
      }
    }
    it("gets our part details") {
      Get("/stock/23/") ~> part ~> check {
        responseAs[String] should equal(Part(23,"Power flubber", 1.33).toJson.prettyPrint)
      }
    }
  }
}
