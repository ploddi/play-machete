package com.dtlbox.play.oauth2.core

/**
 * OAuth 2.0 Client
 */
trait Client {

  def credentials: ClientCredentials

  def id = credentials.id

  def allowedGrants: Option[Set[String]]

  def redirectUri: String

}
