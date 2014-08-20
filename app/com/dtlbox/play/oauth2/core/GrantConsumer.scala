package com.dtlbox.play.oauth2.core

import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future

/**
 * Extracts grant from incoming request, validates it, and then consumes it.
 *
 * @tparam R Resource Owner concrete type.
 * @tparam G Grant concrete type.
 */
trait GrantConsumer[R <: ResourceOwner, G <: Grant[R]] {

  /**
   * Extracts grant from incoming request and validates it
   *
   * @param request play request
   * @return future grant or error
   */
  def extract(request: ClientRefinerProfile[Client]#ClientRequest[_]): Future[Either[OAuth2Error, G]]

  /**
   * Consumes grant, making it invalid for future use
   *
   * @param grant grant to consuming
   */
  def consume(grant: G): Unit

  /**
   * Checks if grant already consumed
   *
   * @param grant grant for checking
   * @return future boolean
   */
  def isConsumed(grant: G): Future[Boolean]

  /**
   * Applies extractor and checks if it already consumed
   *
   * @param request play request
   * @return future grant or error
   */
  def apply(request: ClientRefinerProfile[Client]#ClientRequest[_]): Future[Either[OAuth2Error, Grant[R]]] = {

    @inline def err = OAuth2Error.InvalidGrant(Some("grant already consumed"))

    extract(request).flatMap {
      case l @ Left(_) => Future(l)
      case r @ Right(grant) => isConsumed(grant).map { b =>
        if (b) {
          Left(err)
        } else {
          consume(grant)
          r
        }
      }
    }
  }
}