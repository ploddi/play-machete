package com.dtlbox.play.oauth2.contrib.issuers

import com.dtlbox.play.oauth2.contrib.grants.AuthorizationCodeGrant
import com.dtlbox.play.oauth2.core.services.{GrantService, ClientService}
import com.dtlbox.play.oauth2.core._
import com.dtlbox.play.oauth2.util.RenderingHelpers
import play.api.data._
import play.api.data.Forms._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.Crypto
import play.api.mvc.{RequestHeader, Request}
import play.api.http.{Status => StatusCodes}

import scala.concurrent.Future

/**
 * Issues authorization code grant.
 * [[AuthorizationCodeGrant]] is rendered by redirecting to provided redirect_uri.
 *
 * @tparam R resource owner type
 */
class AuthorizationCodeIssuer[C <: Client, R <: ResourceOwner](clientService: ClientService[C],
                                                               authCodeService: GrantService[R, AuthorizationCodeGrant[R]])
  extends GrantIssuer[R, AuthorizationCodeGrant[R]]
  with RenderingHelpers {

  case class AuthorizationCodeData(clientId: String,
                                   redirectUri: String,
                                   scope: Option[String],
                                   state: Option[String])

  val authorizationCodeForm = Form(
    mapping(
      AuthorizationCodeIssuer.ClientId -> nonEmptyText,
      AuthorizationCodeIssuer.RedirectUri -> nonEmptyText,
      AuthorizationCodeIssuer.Scope -> optional(text),
      AuthorizationCodeIssuer.State -> optional(text)
    )(AuthorizationCodeData.apply)(AuthorizationCodeData.unapply)
  )

  /**
   * Validates form content.
   * Assumes form does not has errors.
   *
   * @param form form to validate
   * @return future [[OAuth2Error]] or query params map
   */
  private def validateForm(form: Form[AuthorizationCodeData]) = {
    val codeDate = form.get

    def validateClient(description: String)(predicate: Client => Boolean): Client => Either[OAuth2Error, Client] = {
      case cl if predicate(cl) => Right(cl)
      case _ => Left(OAuth2Error.InvalidRequest(Some(description)))
    }

    val validateAllowedGrants = validateClient("this response_type is not allowed for this client") {
      _.allowedGrants
       .filter(_.contains(AuthorizationCodeIssuer.GrantType))
       .isDefined
    }

    val validateRedirectUri = validateClient("invalid redirect_uri")(_.redirectUri == codeDate.redirectUri)

    clientService
      .retrieve(codeDate.clientId)
      .map { clientOpt =>

      val validateFound = clientOpt.toRight(OAuth2Error.InvalidRequest(Some("invalid client_id")))

      for {
        c <- validateFound.right
        _ <- validateAllowedGrants(c).right
        _ <- validateRedirectUri(c).right
      } yield form.data.mapValues(Seq(_))
    }
  }

  override def validateRequest(request: Request[_]) = {
    def error(errors: Seq[FormError]) = {
      val desc = errors.map(e => e.key + " " + e.message).mkString(",")
      OAuth2Error.InvalidRequest(Some(desc))
    }

    authorizationCodeForm.bindFromRequest()(request) match {
      case hasErrors if hasErrors.hasErrors => Future(Left(error(hasErrors.errors)))
      case form => validateForm(form)
    }
  }

  override def issueGrant(resourceOwner: R, params: Map[String, Seq[String]])
                         (implicit request: RequestHeader) = {

    val codeData = authorizationCodeForm.bindFromRequest(params).get

    val code = AuthorizationCodeGrant[R](
      id = Crypto.generateToken,
      redirectUri = codeData.redirectUri,
      clientId = codeData.clientId,
      resourceOwnerId = resourceOwner.id,
      scope = codeData.scope
    )

    authCodeService
      .create(code)
      .map(c => renderAsRedirect(Some(c.redirectUri), c))
  }
}

object AuthorizationCodeIssuer {
  val ClientId = "client_id"
  val RedirectUri = "redirect_uri"
  val Scope = "scope"
  val State = "state"
  val GrantType = "authorization_code"
}