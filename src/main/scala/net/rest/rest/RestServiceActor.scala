package net.rest.rest

import akka.actor.Actor
import akka.event.slf4j.SLF4JLogging
import net.rest.domain._
import java.text.{ParseException, SimpleDateFormat}
import java.util.Date

import net.liftweb.json.Serialization._
import net.liftweb.json.{DateFormat, Formats}
import net.rest.dao.AccountDAO

import scala.Some
import spray.http._
import spray.httpx.unmarshalling._
import spray.routing._

/**
 * REST Service actor.
 */
class RestServiceActor extends Actor with RestService {

  implicit def actorRefFactory = context

  def receive = runRoute(rest)
}

