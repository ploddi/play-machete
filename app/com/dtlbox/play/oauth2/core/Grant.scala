package com.dtlbox.play.oauth2.core

/**
 * Represents OAuth 2.0 Grant
 */
trait Grant[R <: ResourceOwner] {

  def id: String

  def clientId: String

  def resourceOwnerId: R#Id

  def scope: Option[String]

  def grantType: String

}
