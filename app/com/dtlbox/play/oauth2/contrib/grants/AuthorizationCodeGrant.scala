package com.dtlbox.play.oauth2.contrib.grants

import com.dtlbox.play.oauth2.core.{ResourceOwner, Grant}
import play.api.libs.json.{Writes, Json}

import scala.language.implicitConversions

case class AuthorizationCodeGrant[R <: ResourceOwner](id: String,
                                                      redirectUri: String,
                                                      clientId: String,
                                                      resourceOwnerId: R#Id,
                                                      scope: Option[String] = None,
                                                      consumed: Boolean = false)
  extends Grant[R] {

  val grantType = AuthorizationCodeGrant.GrantType

}

object AuthorizationCodeGrant {

  val GrantType = "authorization_code"

  val Code = "code"

  implicit def toParams[R <: ResourceOwner](code: AuthorizationCodeGrant[R]) = Map(
    Code -> Seq(code.id)
  )

  implicit def jsonWrites[R <: ResourceOwner] = Writes[AuthorizationCodeGrant[R]] { code =>
    Json.obj(Code -> code.id)
  }
}