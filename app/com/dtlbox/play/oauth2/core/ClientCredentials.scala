package com.dtlbox.play.oauth2.core

import play.api.libs.Crypto

/**
 * Client Credentials
 *
 * @param id Unique client identifier
 * @param secret Client secret
 */
case class ClientCredentials(id: String, secret: String) {

  private def secureEquals(a: ClientCredentials) = a.id == this.id && Crypto.constantTimeEquals(a.secret, this.secret)

  override def equals(a: Any) = a.isInstanceOf[ClientCredentials] && secureEquals(a.asInstanceOf[ClientCredentials])

}
