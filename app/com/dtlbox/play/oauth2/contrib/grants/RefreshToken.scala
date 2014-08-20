package com.dtlbox.play.oauth2.contrib.grants

import com.dtlbox.play.oauth2.core.{ResourceOwner, Grant}

case class RefreshToken[R <: ResourceOwner](id: String,
                                            clientId: String,
                                            resourceOwnerId: R#Id,
                                            scope: Option[String] = None,
                                            consumed: Boolean = false)
  extends Grant[R] {

  val grantType = RefreshToken.GrantType

}

object RefreshToken {

  val GrantType = "refresh_token"

}