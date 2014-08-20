package com.dtlbox.play.oauth2.core

import play.api.mvc.{Request, WrappedRequest}

case class ClientCredentialsRequest[A](credentials: ClientCredentials, request: Request[A])
  extends WrappedRequest[A](request)
