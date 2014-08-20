package com.dtlbox.play.oauth2.contrib.grants

import com.dtlbox.play.oauth2.core.{ResourceOwner, Grant}
import play.api.libs.json._
import play.api.libs.functional.syntax._

import scala.language.implicitConversions

case class SMSCodeGrant[R <: ResourceOwner](id: String,
                                            smsCode: String,
                                            clientId: String,
                                            phoneNumber: String,
                                            expiresAt: Long,
                                            resourceOwnerId: R#Id,
                                            numOfTries: Int = 0,
                                            scope: Option[String] = None,
                                            consumed: Boolean = false) extends Grant[R] {

  val grantType = SMSCodeGrant.GrantType
}

object SMSCodeGrant {

  val GrantType = "sms_code"

  val RequestToken = "request_token"
  val TokenType = "token_type"
  val ExpireIn = "expires_in"
  val RefreshToken = "refresh_token"
  val Scope = "scope"

  implicit def toParams[R <: ResourceOwner](smsCode: SMSCodeGrant[R]) = Map(
    RequestToken -> Seq(smsCode.id),
    Scope -> smsCode.scope.toSeq)

  implicit def jsonWrites[R <: ResourceOwner]: Writes[SMSCodeGrant[R]] =
    ((__ \ RequestToken).write[String] ~
     (__ \ Scope).writeNullable[String])(smsCode => (smsCode.id, smsCode.scope))

}