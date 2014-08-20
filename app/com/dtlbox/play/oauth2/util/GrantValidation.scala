package com.dtlbox.play.oauth2.util

import com.dtlbox.play.oauth2.core.{ResourceOwner, Grant, OAuth2Error}

/**
 * Grant validation helpers
 */
trait GrantValidation[R <: ResourceOwner, G <: Grant[R]] {

  type ValidationResult = Either[OAuth2Error, G]
  type Validation = G => ValidationResult

  /**
   * Validates grant using predicate.
   *
   * @param errorDesc error description
   * @param predicate predicate used in validation
   * @return validation function
   */
  protected def validateGrant(errorDesc: => String)(predicate: G => Boolean): Validation = {
    val validation: Validation = {
      case grant if predicate(grant) => Right(grant)
      case _ => Left(OAuth2Error.InvalidGrant(Some(errorDesc)))
    }
    validation
  }
}
