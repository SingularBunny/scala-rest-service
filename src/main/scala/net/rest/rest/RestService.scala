package net.rest.rest

import akka.event.slf4j.SLF4JLogging
import net.liftweb.json.Serialization.{write, read}
import net.rest.dao.AccountDAO
import net.rest.domain.{Account, RestFailure}
import spray.http._
import spray.httpx.unmarshalling.Unmarshaller
import spray.routing.{HttpService, RejectionHandler, RequestContext, Route}
/**
  * REST Service
  */
trait RestService extends HttpService with SLF4JLogging {

  val accountService = new AccountDAO

  implicit val executionContext = actorRefFactory.dispatcher

  implicit val liftJsonFormats = net.liftweb.json.DefaultFormats

  implicit val customRejectionHandler = RejectionHandler {
    case rejections => mapHttpResponse {
      response =>
        response.withEntity(HttpEntity(ContentType(MediaTypes.`application/json`),
          write(Map("error" -> response.entity.asString))))
    } {
      RejectionHandler.Default(rejections)
    }
  }

  implicit val accountUnmarshaller: Unmarshaller[Account] =
    Unmarshaller[Account](MediaTypes.`application/json`) {
      case httpEntity: HttpEntity =>
        read[Account](httpEntity.asString(HttpCharsets.`UTF-8`))
    }

  val rest: Route = respondWithMediaType(MediaTypes.`application/json`) {
    path("accounts") {
      post {
        entity(as[Account]) {
          account: Account =>
            ctx: RequestContext =>
              handleRequest(ctx, StatusCodes.Created) {
                log.debug("Creating account: %s".format(account))
                accountService.create(account)
              }
        }
      } ~
        get {
          ctx: RequestContext =>
            handleRequest(ctx) {
              log.debug("Searching for accounts")
              accountService.getAll()
            }
        }
    } ~
      path("account" / LongNumber) {
        accountId =>
          put {
            entity(as[Account]) {
              account: Account =>
                ctx: RequestContext =>
                  handleRequest(ctx) {
                    log.debug("Updating account with id %d: %s".format(accountId, account))
                    accountService.update(accountId, account)
                  }
            }
          } ~
            delete {
              ctx: RequestContext =>
                handleRequest(ctx) {
                  log.debug("Deleting account with id %d".format(accountId))
                  accountService.delete(accountId)
                }
            } ~
            get {
              ctx: RequestContext =>
                handleRequest(ctx) {
                  log.debug("Retrieving account with id %d".format(accountId))
                  accountService.get(accountId)
                }
            } ~
            post {
              entity(as[Account]) {
                account: Account =>
                  ctx: RequestContext =>
                    handleRequest(ctx, StatusCodes.OK) {
                      log.debug("Money send to account: %s".format(account))
                      accountService.send(accountId, account)
                    }
              }
            }
      }
  }

  /**
    * Handles an incoming request and create valid response for it.
    *
    * @param ctx         request context
    * @param successCode HTTP Status code for success
    * @param action      action to perform
    */
  protected def handleRequest(ctx: RequestContext, successCode: StatusCode = StatusCodes.OK)(action: => Either[RestFailure, _]) {
    action match {
      case Right(result: Object) =>
        ctx.complete(successCode, write(result))
      case Left(error: RestFailure) =>
        ctx.complete(error.getStatusCode, net.liftweb.json.Serialization.write(Map("error" -> error.message)))
      case _ =>
        ctx.complete(StatusCodes.InternalServerError)
    }
  }
}