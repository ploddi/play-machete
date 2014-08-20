package com.dtlbox.play.oauth2.core

import com.dtlbox.play.oauth2.util.{GrantValidation, RenderingHelpers, Params}
import play.api.http.{Status => StatusCodes}
import play.api.mvc._
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

import scala.util.Right

trait GrantConsumingProfile[C <: Client, R <: ResourceOwner] {

  /**
   * Play [[WrappedRequest]] containing a grant and client
   *
   * @param grant The oauth2 grant.
   * @param client The authorized client
   * @param request The request.
   * @tparam A The request content type.
   */
  case class GrantRequest[A](grant: Grant[R], client: C, request: Request[A])
    extends WrappedRequest[A](request)

  /**
   * [[ActionRefiner]] that extracts oauth2 grant from incoming request.
   *
   * @param consumers [[GrantConsumer]] for specified grant types.
   */
  class GrantRefiner(consumers: (String, GrantConsumer[R, _ <: Grant[R]])*)
    extends ActionRefiner[ClientRefinerProfile[C]#ClientRequest, GrantRequest]
    with Results
    with RenderingHelpers
    with Params
    with GrantValidation[R, Grant[R]] {

    private val GrantType = "grant_type"

    private val consumersMap = consumers.toMap

    private def renderError(err: OAuth2Error)(implicit request: RequestHeader) = {
      renderAsBody(StatusCodes.BAD_REQUEST, err)
    }

    /**
     * Checks is client allowed to process this grant type
     * then tries to find grant consumer for request.
     *
     * @param request Incoming client request
     * @return either grant consumer or error
     */
    private def consumerForRequest(request: ClientRefinerProfile[C]#ClientRequest[_]) = {
      @inline def errParamReq     = OAuth2Error.InvalidRequest(Some("grant_type is required"))
      @inline def errDisallowed   = OAuth2Error.InvalidRequest(Some("this grant_type is not allowed for this client"))
      @inline def errNotSupported = OAuth2Error.UnsupportedGrantType(Some("this grant_type is not supported"))

      @inline def validate(gt: String): Either[OAuth2Error, GrantConsumer[R, _ <: Grant[R]]] = {
        val isAllowed = request.client.allowedGrants.map(_.contains(gt)).getOrElse(true)

        consumersMap
          .get(gt)
          .toRight(errNotSupported)
          .right.flatMap {
            case c if isAllowed => Right(c)
            case _ => Left(errDisallowed)
          }
      }

      allParams(request)
        .get(GrantType)
        .flatMap(_.headOption)
        .toRight(errParamReq)
        .right
        .flatMap(validate)
    }

    /** Simply maps {{{Either[OAuth2Error, Grant[R]]}}} to {{{Either[Result, GrantRequest[A]]}}}  */
    private def liftRequest[A](request: ClientRefinerProfile[C]#ClientRequest[A])
                               (grantOrError: Either[OAuth2Error, Grant[R]]): Either[Result, GrantRequest[A]] = {
      grantOrError
        .right.map(GrantRequest(_, request.client, request))
        .left.map(renderError(_)(request))
    }

    override def refine[A](request: ClientRefinerProfile[C]#ClientRequest[A]) = {

      consumerForRequest(request)
        .right.map(_.apply(request))
        .fold(e => Future(Left(e)), identity)
        .map(_.right.flatMap(validateGrant("grant was issued to another client")(_.clientId == request.client.id)))
        .map(liftRequest(request))
    }
  }
}
