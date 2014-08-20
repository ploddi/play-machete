package com.dtlbox.play.oauth2.contrib.consumers

import com.dtlbox.play.oauth2.core._
import com.dtlbox.play.oauth2.core.services.GrantService
import com.dtlbox.play.oauth2.util.{GrantValidation, Params}
import com.dtlbox.play.oauth2.contrib.grants.AuthorizationCodeGrant
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future

class AuthorizationCodeConsumer[R <: ResourceOwner](grantService: GrantService[R, AuthorizationCodeGrant[R]])
  extends GrantConsumer[R, AuthorizationCodeGrant[R]]
  with GrantValidation[R, AuthorizationCodeGrant[R]]
  with Params {

  private val Code = "code"
  private val RedirectUri = "redirect_uri"

  private def retrieveAndValidate(id: String, redirectUri: String) = {
    def validate(grantOpt: Option[AuthorizationCodeGrant[R]]) = {
      val validateRedirectUri = validateGrant("invalid redirect_uri") {
        _.redirectUri == redirectUri
      }

      val validateFound = grantOpt.toRight(OAuth2Error.InvalidClient(Some("grant not found")))

      for {
        g1 <- validateFound.right
        g2 <- validateRedirectUri(g1).right
      } yield g2
    }

    grantService
      .retrieve(id)
      .map(validate)
  }

  def extract(request: ClientRefinerProfile[Client]#ClientRequest[_]) = {
    val params = allParams(request)

    (for {
      id <- params.get(Code).flatMap(_.headOption)
      redirectUri <- params.get(RedirectUri).flatMap(_.headOption)
    } yield (id, redirectUri)) match {
      case Some(tuple) => (retrieveAndValidate _).tupled(tuple)
      case None => Future(Left(OAuth2Error.InvalidGrant()))
    }
  }

  override def consume(grant: AuthorizationCodeGrant[R]): Unit = grantService.update(grant.copy(consumed = true))

  override def isConsumed(grant: AuthorizationCodeGrant[R]): Future[Boolean] = Future(grant.consumed)
}
