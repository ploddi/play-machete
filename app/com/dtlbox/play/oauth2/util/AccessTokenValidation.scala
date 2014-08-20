package com.dtlbox.play.oauth2.util

import com.dtlbox.play.oauth2.core.{ResourceOwner, OAuth2Error, AccessToken}


trait AccessTokenValidation[R <: ResourceOwner] {

  type ValidationResult = Either[OAuth2Error, AccessToken[R]]
  type Validation = AccessToken[R] => ValidationResult

  def validate(description: => String)(predicate: AccessToken[R] => Boolean) = {
    val validation: Validation = {
      case t if predicate(t) => Right(t)
      case _ => Left(OAuth2Error.InvalidToken(Some(description)))
    }
    validation
  }
}
