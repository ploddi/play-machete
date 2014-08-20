package com.dtlbox.play.oauth2.core

import play.api.libs.json._
import play.api.libs.functional.syntax._

import scala.language.implicitConversions

case class AccessToken[R <: ResourceOwner](id: String,
                                           expires: Long,
                                           expiresIn: Long,
                                           grantInfo: GrantInfo[R],
                                           isRevoked: Boolean = false,
                                           refreshToken: Option[String] = None,
                                           scope: Option[String] = None)

object AccessToken {
  val AccessToken = "access_token"
  val TokenType = "token_type"
  val Expire = "expires"
  val ExpireIn = "expires_in"
  val RefreshToken = "refresh_token"
  val Scope = "scope"

  implicit def toParams[R <: ResourceOwner](token: AccessToken[R]) = Map(
    AccessToken -> Seq(token.id),
    Expire -> Seq(token.expires.toString),
    ExpireIn -> Seq(token.expiresIn.toString)) ++
    token.refreshToken.map(RefreshToken -> Seq(_)) ++
    token.scope.map(Scope -> Seq(_))

  implicit def jsonWrites[R <: ResourceOwner]: Writes[AccessToken[R]] =
    ((__ \ AccessToken).write[String] ~
     (__ \ Expire).write[Long] ~
     (__ \ ExpireIn).write[Long] ~
     (__ \ RefreshToken).writeNullable[String] ~
     (__ \ Scope).writeNullable[String] ~
     (__ \ TokenType).write[String])(t => (t.id, t.expires, t.expiresIn, t.refreshToken, t.scope, AccessTokenTypes.Bearer))
}

object AccessTokenTypes {
  val Bearer = "bearer"
  val Mac = "mac"
}