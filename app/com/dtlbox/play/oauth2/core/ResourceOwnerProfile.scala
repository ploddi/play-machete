package com.dtlbox.play.oauth2.core

import play.api.mvc.{WrappedRequest, Request}

trait ResourceOwnerProfile[R <: ResourceOwner] {

  case class ResourceOwnerRequest[A](resourceOwner: R, request: Request[A]) extends WrappedRequest[A](request)

}
