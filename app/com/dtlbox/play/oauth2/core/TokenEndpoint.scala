package com.dtlbox.play.oauth2.core

import com.dtlbox.play.oauth2.core.services.{AccessTokenService, ClientService}
import com.dtlbox.play.oauth2.util.{StatePreserver, RenderingHelpers}
import play.api.http.{Status => StatusCodes}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc._
import play.api.libs.Crypto
import scala.concurrent.Future

/**
 * Play [[play.api.mvc.Controller]] that Represents OAuth 2.0 Token Endpoint.
 *
 * Exchanges grants for access tokens.
 *
 * @param clientService client service, for client authorization
 * @param tokenService access tokens service, for token creation.
 * @param consumers grant consumers.
 * @tparam C The client type.
 * @tparam R The resource owner type.
 */
class TokenEndpoint[C <: Client, R <: ResourceOwner](clientService: ClientService[C],
                                                     tokenService: AccessTokenService[R],
                                                     consumers: Map[String, GrantConsumer[R, _ <: Grant[R]]])
  extends Controller
  with Refiners[C, R]
  with RenderingHelpers {

  import refiners._

  protected val credentialRefiner: ClientCredentialRefiner = new ClientCredentialRefiner()
  protected val clientRefiner: ClientRefiner = new ClientRefiner(clientService)
  protected val grantRefiner: GrantRefiner = new GrantRefiner(consumers.toSeq:_*)

  /** Play [[ActionFunction]] composition power! */
  protected val ClientAction = Action andThen
    credentialRefiner andThen
    clientRefiner

  protected val GrantAction = ClientAction andThen grantRefiner

  /**
   * Exchanges incoming grant for access token if possible.
   *
   * @param grant grant, extracted from incoming request by [[GrantConsumer]]
   * @return future token or error.
   */
  private def issueAccessToken(grant: Grant[R]): Future[Either[OAuth2Error, AccessToken[R]]] = {
    val grantInfo = GrantInfo[R](grant.id, grant.grantType, grant.resourceOwnerId, grant.clientId)

    tokenService
      .create(grantInfo, grant.scope)
      .map(Right(_))
  }

  /**
   * [[TokenEndpoint]] entry point action.
   *
   * Clients requests URI routed to this action to exchange grants for access tokens.
   *
   * @return play action.
   */
  def obtainAccessToken = GrantAction.async { implicit request =>

    issueAccessToken(request.grant)
      .map {
        case Left(error) => renderAsBody(StatusCodes.BAD_REQUEST, error)
        case Right(token) => renderAsBody(StatusCodes.OK, token)
      }
  }
}




