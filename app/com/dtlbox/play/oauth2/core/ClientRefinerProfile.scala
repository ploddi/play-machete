package com.dtlbox.play.oauth2.core

import com.dtlbox.play.oauth2.core.services.ClientService
import com.dtlbox.play.oauth2.util.RenderingHelpers
import play.api.http.{Status => StatusCodes}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc._


/**
 * Helper trait to parametrize ClientRequest by generic Client.
 *
 * Type lambdas does not help here. Trust me.
 */
trait ClientRefinerProfile[+C <: Client] {

  case class ClientRequest[A](client: C, request: Request[A]) extends WrappedRequest[A](request)

  /**
   * [[ActionRefiner]] that retrieves client from [[ClientService]] with [[ClientCredentials]] extracted early.
   */
  class ClientRefiner(clientService: ClientService[C])
    extends ActionRefiner[ClientCredentialsRequest, ClientRequest]
    with RenderingHelpers
    with Results {

    private def error(implicit request: RequestHeader) = {
      renderAsBody(StatusCodes.UNAUTHORIZED, OAuth2Error.InvalidClient())
    }

    override protected def refine[A](request: ClientCredentialsRequest[A]) = {
      val credentials = request.credentials

      clientService
        .retrieve(credentials.id)
        .map(_.filter(_.credentials == credentials))
        .map(_.map(ClientRequest(_, request)))
        .map(_.toRight(error(request)))
    }

  }
}
