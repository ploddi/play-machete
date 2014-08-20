package com.dtlbox.play.oauth2.core

case class GrantInfo[R <: ResourceOwner](grantId: String,
                                         grantType: String,
                                         resourceOwnerId: R#Id,
                                         clientId: String)
