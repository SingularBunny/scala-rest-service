package net.rest.domain

import slick.jdbc.H2Profile.api._

/**
  * Table class.
  * @param tag
  */
class Accounts(tag: Tag) extends Table[Account](tag, "accounts") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def funds = column[Long]("funds")

  def * = (id.?, funds) <> (Account.tupled, Account.unapply)
}