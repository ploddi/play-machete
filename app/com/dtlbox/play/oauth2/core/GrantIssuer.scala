package com.dtlbox.play.oauth2.core

import com.dtlbox.play.oauth2.util.RenderingHelpers
import play.api.mvc.{RequestHeader, Request, Result}
import play.api.http.{Status => StatusCodes}
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future

trait GrantIssuer[R <: ResourceOwner, G <: Grant[R]]
  extends RenderingHelpers {

  def validateRequest(request: Request[_]): Future[Either[OAuth2Error, Map[String, Seq[String]]]]

  def issueGrant(resourceOwner: R, params: Map[String, Seq[String]])(implicit request: RequestHeader): Future[Result]

  def apply(request: ResourceOwnerProfile[R]#ResourceOwnerRequest[_]): Future[Result] = {
    implicit val req = request

    validateRequest(request)
      .flatMap {
        case Left(e) => Future(renderAsBody(StatusCodes.BAD_REQUEST, e))
        case Right(params) => issueGrant(request.resourceOwner, params)
      }
  }
}

