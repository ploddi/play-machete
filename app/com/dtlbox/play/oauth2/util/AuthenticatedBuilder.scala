package com.dtlbox.play.oauth2.util

import play.api.mvc._
import play.api.mvc.Security.AuthenticatedRequest
import play.api.mvc.Results._
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

class AuthenticatedBuilder[U](userInfo: RequestHeader => Future[Option[U]], onUnauthorized: RequestHeader => Result)
  extends ActionBuilder[({ type R[A] = AuthenticatedRequest[A, U] })#R] {

  def invokeBlock[A](request: Request[A], block: (AuthenticatedRequest[A, U]) => Future[Result]) =
    authenticate(request, block)

  def authenticate[A](request: Request[A], block: (AuthenticatedRequest[A, U]) => Future[Result]) = {
    userInfo(request).flatMap {
      _.map { user =>
        block(new AuthenticatedRequest(user, request))
      } getOrElse {
        Future.successful(onUnauthorized(request))
      }
    }
  }
}
