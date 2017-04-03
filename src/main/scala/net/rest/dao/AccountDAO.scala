package net.rest.dao

import java.sql._

import net.rest.config.Configuration
import net.rest.domain.{Account, Accounts, FailureType, RestFailure}
import slick.jdbc.H2Profile.api._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

import org.h2.Driver

/**
  * Provides DAL for Account entities for database.
  */
class AccountDAO extends Configuration {

  // init Database instance
  private val db = Database.forConfig("h2mem1")

  // create tables if not exist
  private val accounts = TableQuery[Accounts]
  private val setup = DBIO.seq(accounts.schema.create)
  private val setupFuture = db.run(setup)

  /**
    * Saves account entity into database.
    *
    * @param account account entity to
    * @return saved account entity
    */
  def create(account: Account): Either[RestFailure, Account] = {
    try {
      val idFuture: Future[Long] = db.run((accounts returning accounts.map(_.id)) += account)
      val id = Await.result(idFuture, Duration.Inf)
      Right(account.copy(id = Some(id)))
    } catch {
      case e: SQLException =>
        Left(databaseError(e))
    }
  }

  /**
    * Updates account entity with specified one.
    *
    * @param id      id of the account to update.
    * @param account updated account entity
    * @return updated account entity
    */
  def update(id: Long, account: Account): Either[RestFailure, Account] = {
    try {
      val accFeature = db.run(accounts.filter(_.id === id).result)
      val accs = Await.result(accFeature, Duration.Inf)
      accs.size match {
        case 0 =>
          Left(notFoundError(id))
        case _ => {
          val updateAccFeature = db.run((for {a <- accounts if a.id === id} yield a).update(account))
          val accs = Await.result(updateAccFeature, Duration.Inf)
          Right(account.copy(id = Some(id)))
        }
      }
    } catch {
      case e: SQLException =>
        Left(databaseError(e))
    }
  }

  /**
    * Send money from one account to another.
    *
    * @param id      id of the sender account.
    * @param account pair of receiver id and amount of money to send.
    * @return receiver account entity after money send.
    */
  def send(id: Long, account: Account): Either[RestFailure, Account] = {
    try {
      val senderFeature = db.run(accounts.filter(_.id === id).result)
      val sender = Await.result(senderFeature, Duration.Inf)
      val receiverFeature = db.run(accounts.filter(_.id === account.id.get).result)
      val receiver = Await.result(receiverFeature, Duration.Inf)
      sender.size match {
        case 0 =>
          Left(notFoundError(id))
        case _ => {
          receiver.size match {
            case 0 =>
              Left(notFoundError(account.id.get))
            case _ => {
              val rec = receiver.head
              val receiverAccFeature = db
                .run((for {a <- accounts if a.id === account.id.get} yield a)
                  .update(Account(rec.id, rec.funds + account.funds)))
              val receiverAcc = Await.result(receiverAccFeature, Duration.Inf)

              val sendr = sender.head
              val senderAccFeature = db
                .run((for {a <- accounts if a.id === id} yield a)
                  .update(Account(sendr.id, sendr.funds - account.funds)))
              val senderAcc = Await.result(senderAccFeature, Duration.Inf)
              Right(get(account.id.get).right.get)
            }
          }
        }
      }
    } catch {
      case e: SQLException =>
        Left(databaseError(e))
    }
  }

  /**
    * Deletes account from database.
    *
    * @param id id of the account to delete
    * @return deleted account entity
    */
  def delete(id: Long): Either[RestFailure, Account] = {
    try {
      val accFeature = db.run(accounts.filter(_.id === id).result)
      val accs = Await.result(accFeature, Duration.Inf)
      accs.size match {
        case 0 =>
          Left(notFoundError(id))
        case _ => {
          val deleteAccFeature = db.run(accounts.filter(_.id === id).result)
          val accs = Await.result(deleteAccFeature, Duration.Inf)
          Right(accs.head)
        }
      }
    } catch {
      case e: SQLException =>
        Left(databaseError(e))
    }
  }

  /**
    * Retrieves specific account from database.
    *
    * @param id id of the account to retrieve
    * @return account entity with specified id
    */
  def get(id: Long): Either[RestFailure, Account] = {
    try {
      val accFeature = db.run(accounts.filter(_.id === id).result)
      val acct = Await.result(accFeature, Duration.Inf)
      acct.headOption match {
        case Some(account: Account) =>
          Right(account.copy(id = Some(account.id.get), funds = account.funds))
        case _ =>
          Left(notFoundError(id))
      }
    } catch {
      case e: SQLException =>
        Left(databaseError(e))
    }
  }

  /**
    * Get all accounts.
    *
    * @return accounts.
    */
  def getAll(): Either[RestFailure, List[Account]] = {
    try {
      val accFeature = db.run(accounts.result)
      val acct = Await.result(accFeature, Duration.Inf)
      Right(acct.toList)
    } catch {
      case e: SQLException =>
        Left(databaseError(e))
    }
  }

  /**
    * Produce database error description.
    *
    * @param e SQL Exception.
    * @return database error description.
    */
  protected def databaseError(e: SQLException) =
    RestFailure("%d: %s".format(e.getErrorCode, e.getMessage), FailureType.DatabaseFailure)

  /**
    * Produce account not found error description.
    *
    * @param accountId id of the account.
    * @return not found error description.
    */
  protected def notFoundError(accountId: Long) =
    RestFailure("Account with id=%d does not exist".format(accountId), FailureType.NotFound)
}