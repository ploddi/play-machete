package com.dtlbox.play.oauth2.contrib.consumers

import com.dtlbox.play.oauth2.contrib.grants.RefreshToken
import com.dtlbox.play.oauth2.core.services.{AccessTokenService, GrantService}
import com.dtlbox.play.oauth2.core._
import com.dtlbox.play.oauth2.util.{GrantValidation, Params}
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

class RefreshTokenConsumer[R <: ResourceOwner](grantService: GrantService[R, RefreshToken[R]],
                                               accessTokenService: AccessTokenService[R])

  extends GrantConsumer[R, RefreshToken[R]]
  with GrantValidation[R, RefreshToken[R]]
  with Params {

  private val RefreshToken = "refresh_token"

  private def revokeAccessToken(refreshToken: RefreshToken[R]) = {
    accessTokenService
      .retrieveByRefreshToken(refreshToken.id)
      .onSuccess {
        case Some(token) => accessTokenService.revoke(token)
      }
  }

  /**
   * Consumes refresh token, and revokes access token associated with it early.
   *
   * @param tokenId refresh token id
   */
  private def retrieve(tokenId: String) = {
    val foToken = grantService.retrieve(tokenId)

      foToken.onSuccess {
        case Some(token) => revokeAccessToken(token)
      }

      foToken.map(_.toRight(OAuth2Error.InvalidGrant(Some("refresh_token not found"))))
  }

  def extract(request: ClientRefinerProfile[Client]#ClientRequest[_]) = {
    val params = allParams(request)

    params.get(RefreshToken).flatMap(_.headOption) match {
      case Some(id) => retrieve(id)
      case None => Future(Left(OAuth2Error.InvalidGrant()))
    }
  }

  override def consume(grant: RefreshToken[R]): Unit = grantService.update(grant.copy(consumed = true))

  override def isConsumed(grant: RefreshToken[R]): Future[Boolean] = Future(grant.consumed)
}
