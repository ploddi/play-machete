package com.dtlbox.play.oauth2.core

import com.dtlbox.play.oauth2.util.extractors._
import com.dtlbox.play.oauth2.util.RenderingHelpers
import play.api.http.{HeaderNames, Status => StatusCodes}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc._

import scala.concurrent.Future

/**
 * [[ActionRefiner]] that extracts client authorization from request
 *
 * @param extractor Partial function for client credentials extraction. Convenient extractor provided by default.
 */
class ClientCredentialRefiner(extractor:  PartialFunction[Request[_], ClientCredentials] = ClientCredentialRefiner.defaultExtractor)
  extends ActionRefiner[Request, ClientCredentialsRequest]
  with RenderingHelpers {

  private def error(implicit request: Request[_]) = {
    val challenge = "Basic realm=\"Client Credentials Realm\""
    val error = OAuth2Error.InvalidClient()

    renderAsBody(StatusCodes.UNAUTHORIZED, error).withHeaders(HeaderNames.WWW_AUTHENTICATE -> challenge)
  }

  override def refine[A](request: Request[A]) = {
    implicit val r = request
    Future {
      extractor
        .lift(request)
        .map(ClientCredentialsRequest(_, request))
        .toRight(error)
    }
  }

}

object ClientCredentialRefiner {

  /**
   * Default client credentials extractor.
   *
   * Tries to extract Credentials from Basic Authentication header and both from query and url-encoded parameters.
   */
  val defaultExtractor: PartialFunction[Request[_], ClientCredentials] = {
    case BasicAuth(u, p) => ClientCredentials(u, p)
    case ClientParamAuth(u, p) => ClientCredentials(u, p)
  }
}