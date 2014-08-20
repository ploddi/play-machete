package com.dtlbox.play.oauth2.core

import com.dtlbox.play.oauth2.util.RenderingHelpers
import play.api.mvc._
import play.api.http.{Status => Codes}
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future
import scala.language.existentials


trait GrantIssuingProfile[R <: ResourceOwner] {

  case class GrantIssuerRequest[A](grantIssuer: GrantIssuer[R, _ <: Grant[R]], request: Request[A])
    extends WrappedRequest[A](request)

  class GrantIssuerRefiner(issuers: Map[String, GrantIssuer[R, _ <: Grant[R]]])
    extends ActionRefiner[Request, GrantIssuerRequest]
    with RenderingHelpers {

    val ResponseType = "response_type"

    private def issuerForRequest(request: RequestHeader) = {
      request
        .queryString
        .get(ResponseType)
        .flatMap(_.headOption)
        .flatMap(issuers.get)
    }

    override def refine[A](request: Request[A]): Future[Either[Result, GrantIssuerRequest[A]]] = { implicit val r = request
      @inline def errResponseType = OAuth2Error.InvalidClient(Option(s"this $ResponseType is not supported."))
      Future {
        issuerForRequest(request)
          .map(i => Right(GrantIssuerRequest(i, request)))
          .getOrElse(Left(renderAsBody(Codes.BAD_REQUEST, errResponseType)))
      }
    }
  }

  trait GrantValidationRefiner
    extends ActionRefiner[GrantIssuerRequest, GrantIssuerRequest]
    with RenderingHelpers {

    private def buildRequest[A](request: GrantIssuerRequest[A], params: Map[String, Seq[String]]): GrantIssuerRequest[A] = {
      val req = Request(request.copy(queryString = params), request.body)
      GrantIssuerRequest(request.grantIssuer, req)
    }

    private def mapLeftRight[A](errorOrParams: Either[OAuth2Error, Map[String, Seq[String]]])
                               (implicit request: GrantIssuerRequest[A]): Either[Result, GrantIssuerRequest[A]] = {
      errorOrParams
        .left.map(renderAsBody(Codes.BAD_REQUEST, _))
        .right.map(buildRequest(request, _))
    }

    override def refine[A](request: GrantIssuerRequest[A]): Future[Either[Result, GrantIssuerRequest[A]]] = {
      implicit val r = request
      val issuer = request.grantIssuer

      issuer
        .validateRequest(request)
        .map(mapLeftRight[A])
    }
  }
}
