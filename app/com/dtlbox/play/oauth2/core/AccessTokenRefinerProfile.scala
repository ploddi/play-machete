package com.dtlbox.play.oauth2.core

import com.dtlbox.play.oauth2.core.services.AccessTokenService
import com.dtlbox.play.oauth2.util.extractors.Bearer
import com.dtlbox.play.oauth2.util.{AccessTokenValidation, RenderingHelpers}
import play.api.http.{HeaderNames, Status => Codes}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc._

import scala.concurrent.Future

trait AccessTokenRefinerProfile[R <: ResourceOwner]
  extends AccessTokenValidation[R] {

  case class AccessTokenRequest[A](accessToken: AccessToken[R], request: Request[A]) extends WrappedRequest[A](request)

  class AccessTokenRefiner(accessTokenService: AccessTokenService[R],
                           scope: Option[Set[String]] = None,
                           realm: String = "Protected Resource")

    extends ActionRefiner[Request, AccessTokenRequest]
    with RenderingHelpers {

    private implicit class StringQuoting(s: String) {
      def unary_~ = "\"" + s + "\""
    }

    private val realmParam = s"${AccessTokenRefinerProfile.Realm}=${~realm}"

    private def error(e: OAuth2Error)(implicit req: RequestHeader) = {
      val challenge = {
        val errorCode = s"${OAuth2Error.Code}=${~e.code}"
        val params = Seq(realmParam, errorCode) ++
          e.description.map(d => s"${OAuth2Error.Description}=${~d}")

        s"${AccessTokenRefinerProfile.Bearer} ${params.mkString(",")}"
      }

      (e.code match {
        case OAuth2ErrorCodes.InvalidToken => renderAsBody(Codes.UNAUTHORIZED, e)
        case OAuth2ErrorCodes.InsufficientScope => renderAsBody(Codes.FORBIDDEN, e)
        case _ => renderAsBody(Codes.BAD_REQUEST, e)
      }).withHeaders(HeaderNames.WWW_AUTHENTICATE -> challenge)
    }

    private def liftRequest[A](request: Request[A])(tokenOrError: ValidationResult) = {
      tokenOrError
        .right.map(AccessTokenRequest(_, request))
        .left.map(error(_)(request))
    }

    override protected def refine[A](request: Request[A]): Future[Either[Result, AccessTokenRequest[A]]] = {
      def tokenIsMalformed = OAuth2Error.InvalidToken(Some("access token is malformed"))
      val bearerAndRealm = s"${AccessTokenRefinerProfile.Bearer} $realmParam"
      def authNotFound = Unauthorized.withHeaders(HeaderNames.WWW_AUTHENTICATE -> bearerAndRealm)

      def validateNotExpired = validate("access token is expired")(_.expires > System.currentTimeMillis() / 1000)
      def validateNotRevoked = validate("access token is revoked")(!_.isRevoked)

      def scopeError(unsatisfiedScopes: Set[String]) = {
        val message = s"request requires higher privileges (${unsatisfiedScopes.mkString(",")}) than provided by the access token"
        OAuth2Error.InsufficientScope(Some(message))
      }

      def validateScope: Validation = {
        case t if t.scope.isEmpty && scope.isDefined => Left(scopeError(scope.get))
        case t if t.scope.isDefined && scope.isDefined =>
          val tokenScopes = t.scope.get.split(",").toSet
          val unsatisfiedScopes = scope.get &~ tokenScopes
          if (unsatisfiedScopes.isEmpty) Right(t) else Left(scopeError(unsatisfiedScopes))
        case t => Right(t)
      }

      Bearer
        .unapply(request)
        .map { token =>

          accessTokenService
            .retrieveById(token)
            .map { tokenOpt =>
              for {
                t1 <- tokenOpt.toRight(tokenIsMalformed).right
                t2 <- validateNotExpired(t1).right
                t3 <- validateNotRevoked(t2).right
                t4 <- validateScope(t3).right
              } yield t4
            }
            .map(liftRequest(request))
        }
        .getOrElse(Future(Left(authNotFound)))
    }
  }
}

object AccessTokenRefinerProfile {
  val Realm = "realm"
  val Bearer = "Bearer"
}