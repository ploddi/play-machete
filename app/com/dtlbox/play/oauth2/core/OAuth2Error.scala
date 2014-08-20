package com.dtlbox.play.oauth2.core

import play.api.libs.json._
import play.api.libs.functional.syntax._
import scala.language.implicitConversions

case class OAuth2Error(code: String,
                       description: Option[String] = None,
                       uri: Option[String] = None)

object OAuth2Error {
  val Code = "error"
  val Description = "error_description"
  val Uri = "error_uri"

  implicit def toParams(error: OAuth2Error) =
    Map(OAuth2Error.Code -> Seq(error.code)) ++
    error.description.map(OAuth2Error.Description -> Seq(_)) ++
    error.uri.map(OAuth2Error.Uri -> Seq(_))

  implicit val errorJsonWrites =
    ((__ \ Code).write[String] ~
     (__ \ Description).writeNullable[String] ~
     (__ \ Uri).writeNullable[String]
    )(unlift(OAuth2Error.unapply))

  def InvalidRequest(description: Option[String] = None, uri: Option[String] = None) = OAuth2Error(OAuth2ErrorCodes.InvalidRequest, description, uri)
  def InvalidClient(description: Option[String] = None, uri: Option[String] = None) = OAuth2Error(OAuth2ErrorCodes.InvalidClient, description, uri)
  def InvalidScope(description: Option[String] = None, uri: Option[String] = None) = OAuth2Error(OAuth2ErrorCodes.InvalidScope, description, uri)
  def InvalidGrant(description: Option[String] = None, uri: Option[String] = None) = OAuth2Error(OAuth2ErrorCodes.InvalidGrant, description, uri)
  def UnauthorizedClient(description: Option[String] = None, uri: Option[String] = None) = OAuth2Error(OAuth2ErrorCodes.UnauthorizedClient, description, uri)
  def UnsupportedGrantType(description: Option[String] = None, uri: Option[String] = None) = OAuth2Error(OAuth2ErrorCodes.UnsupportedGrantType, description, uri)
  def UnsupportedResponseType(description: Option[String] = None, uri: Option[String] = None) = OAuth2Error(OAuth2ErrorCodes.UnsupportedResponseType, description, uri)
  def AccessDenied(description: Option[String] = None, uri: Option[String] = None) = OAuth2Error(OAuth2ErrorCodes.AccessDenied, description, uri)
  def ServerError(description: Option[String] = None, uri: Option[String] = None) = OAuth2Error(OAuth2ErrorCodes.ServerError, description, uri)
  def TemporarilyUnavailable(description: Option[String] = None, uri: Option[String] = None) = OAuth2Error(OAuth2ErrorCodes.TemporarilyUnavailable, description, uri)

  def InvalidToken(description: Option[String] = None, uri: Option[String] = None) = OAuth2Error(OAuth2ErrorCodes.InvalidToken, description, uri)
  def InsufficientScope(description: Option[String] = None, uri: Option[String] = None) = OAuth2Error(OAuth2ErrorCodes.InsufficientScope, description, uri)
}

object OAuth2ErrorCodes {
  val InvalidRequest = "invalid_request"
  val InvalidClient = "invalid_client"
  val InvalidScope = "invalid_scope"
  val InvalidGrant = "invalid_grant"
  val UnauthorizedClient =  "unauthorized_client"
  val UnsupportedGrantType = "unsupported_grant_type"
  val UnsupportedResponseType = "unsupported_response_type"
  val AccessDenied = "access_denied"
  val ServerError = "server_error"
  val TemporarilyUnavailable = "temporarily_unavailable"

  // RFC 6750 additions
  val InvalidToken = "invalid_token"
  val InsufficientScope = "insufficient_scope"
}