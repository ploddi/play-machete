package com.dtlbox.play.oauth2.core.services

import com.dtlbox.play.oauth2.contrib.grants.RefreshToken
import com.dtlbox.play.oauth2.core.{ResourceOwner, AccessToken, GrantInfo}
import play.api.libs.Crypto
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future

/**
 * Manages access token creation and storage
 *
 * @param refreshTokenService [[GrantService]] for refresh tokens creation
 */
abstract class AccessTokenService[R <: ResourceOwner](refreshTokenService: GrantService[R, RefreshToken[R]]) {

  /** A period during which token is valid */
  val tokenLifeTimeSeconds: Long

  /**
   * Inserts pregenerated token to storage.
   *
   * @param accessToken token to insertion
   * @return future accessToken
   */
  def insert(accessToken: AccessToken[R]): Future[AccessToken[R]]

  /**
   * Finds token by provided grant info
   *
   * @param grantInfo Grant info
   * @return future optional access token
   */
  def retrieveByGrantInfo(grantInfo: GrantInfo[R]): Future[Option[AccessToken[R]]]

  /**
   * Finds access token by string identifier
   *
   * @param accessTokenId identifier
   * @return future optional access token
   */
  def retrieveById(accessTokenId: String): Future[Option[AccessToken[R]]]

  /**
   * Finds token by provided refresh token.
   *
   * @param refreshTokenId refresh token
   * @return future optional access token
   */
  def retrieveByRefreshToken(refreshTokenId: String): Future[Option[AccessToken[R]]]

  /**
   * Invalidates token, making it impossible for future authentication.
   *
   * @param token token to revoke
   */
  def revoke(token: AccessToken[R])


  /**
   * Creates access and refresh tokens for provided grant info and scope.
   *
   * @param grantInfo grant info
   * @param scope required scope
   * @return future access token
   */
  def create(grantInfo: GrantInfo[R], scope: Option[String] = None): Future[AccessToken[R]] = {

    val refreshToken = RefreshToken(Crypto.generateToken, grantInfo.clientId, grantInfo.resourceOwnerId, scope)

    val accessToken = AccessToken(
      id = Crypto.generateToken,
      grantInfo = grantInfo,
      expires = System.currentTimeMillis() / 1000 + tokenLifeTimeSeconds,
      expiresIn = tokenLifeTimeSeconds,
      refreshToken = Some(refreshToken.id),
      scope = scope
    )

    for {
      _ <- refreshTokenService.create(refreshToken)
      token <- insert(accessToken)
    } yield token
  }

}
