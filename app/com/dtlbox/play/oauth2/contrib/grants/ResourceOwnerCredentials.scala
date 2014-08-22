package com.dtlbox.play.oauth2.contrib.grants

import com.dtlbox.play.oauth2.core.{ResourceOwner, Grant}

case class ResourceOwnerCredentials[R <: ResourceOwner](id: String,
                                                             clientId: String,
                                                             resourceOwnerId: R#Id,
                                                             scope: Option[String])
  extends Grant[R] {

  val grantType = ResourceOwnerCredentials.GrantType

}


object ResourceOwnerCredentials {

  val GrantType = "password"

}
