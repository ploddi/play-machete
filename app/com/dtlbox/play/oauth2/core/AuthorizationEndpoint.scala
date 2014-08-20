package com.dtlbox.play.oauth2.core

import com.dtlbox.play.oauth2.util.StatePreserver
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future
import scala.language.existentials

abstract class AuthorizationEndpoint[R <: ResourceOwner](grantIssuers: Map[String, GrantIssuer[R, _ <: Grant[R]]])
  extends Controller
  with GrantIssuingProfile[R]
  with ResourceOwnerProfile[R] {

  case class AuthorizationRequest[A](grantIssuer: GrantIssuer[R, _ <: Grant[R]], resourceOwner: R, request: Request[A])
    extends WrappedRequest[A](request)

  class AuthorizationRefiner(authorize: RequestHeader => Future[Option[R]], unAuthorized: RequestHeader => Future[Result])
    extends ActionRefiner[GrantIssuerRequest, AuthorizationRequest] {

    override protected def refine[A](request: GrantIssuerRequest[A]): Future[Either[Result, AuthorizationRequest[A]]] = {
      authorize(request).flatMap {
        case Some(ro) => Future(Right(AuthorizationRequest(request.grantIssuer, ro, request)))
        case None => unAuthorized(request).map(Left(_))
      }
    }
  }

  val issuerRefiner = new GrantIssuerRefiner(grantIssuers)
  val validationRefiner = new GrantValidationRefiner {}

  private val authorizationAction = Action andThen
    StatePreserver(Action) andThen
    issuerRefiner andThen
    validationRefiner

  def AuthorizeAction(authorize: RequestHeader => Future[Option[R]], unAuthorized: RequestHeader => Future[Result]) = {
    val authorizationRefiner = new AuthorizationRefiner(authorize, unAuthorized)
    authorizationAction andThen authorizationRefiner
  }
}
