package net.rest.domain

/**
  * Allowed failure types.
  */
object FailureType extends Enumeration {
  type Failure = Value

  val BadRequest = Value("bad_request")
  val NotFound = Value("not_found")
  val Duplicate = Value("entity_exists")
  val DatabaseFailure = Value("database_error")
  val InternalError = Value("internal_error")
}
