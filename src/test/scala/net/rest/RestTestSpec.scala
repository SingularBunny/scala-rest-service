package net.rest

import net.liftweb.json.Serialization.write
import net.rest.domain.Account
import net.rest.rest.RestService
import org.specs2.mutable.Specification
import spray.http.{HttpCharsets, HttpEntity, MediaTypes}
import spray.httpx.marshalling.Marshaller
import spray.httpx.unmarshalling.Unmarshaller
import spray.testkit.Specs2RouteTest
import spray.routing.HttpService

class RestTestSpec extends Specification with Specs2RouteTest with RestService {
  def actorRefFactory = system // connect the DSL to the test ActorSystem

  implicit val accountMarshaller =
    Marshaller.of[Account](MediaTypes.`application/json`) { (value, contentType, ctx) =>
      ctx.marshalTo(HttpEntity(contentType, write(value)))
    }

  var account1 = Account(Option(1L), 100L)
  var account2 = Account(Option(2L), 200L)
  "Rest service" should {

    "return an created account for POST requests to the accounts path" in {
      Post("/accounts", account1) ~> rest ~> check {
        responseAs[Account].funds mustEqual 100L
      }

      Post("/accounts", account2) ~> rest ~> check {
        responseAs[Account].funds mustEqual 200L
      }

      "send money and return an receiver account for POST requests to the account/account_id path" in {
        var tempAccount = Account(account2.id, 50L)
        Post("/account/1", tempAccount) ~> rest ~> check {
          responseAs[Account].id.get mustEqual account2.id.get
          responseAs[Account].funds mustEqual 250L
        }

        "return an account for GET requests to the account/account_id path" in {
          Get("/account/1") ~> rest ~> check {
            responseAs[Account].id.get mustEqual account1.id.get
            responseAs[Account].funds mustEqual 50L
          }
        }
      }
    }
  }
}
