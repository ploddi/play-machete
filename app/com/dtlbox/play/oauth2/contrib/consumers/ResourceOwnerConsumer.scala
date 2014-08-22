package com.dtlbox.play.oauth2.contrib.consumers

import com.dtlbox.play.oauth2.contrib.services.ResourceOwnerService
import com.dtlbox.play.oauth2.core._
import com.dtlbox.play.oauth2.util.Params
import com.dtlbox.play.oauth2.contrib.grants.ResourceOwnerCredentials
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future

class ResourceOwnerConsumer[R <: ResourceOwner](resourceOwnerService: ResourceOwnerService[R])
  extends GrantConsumer[R, ResourceOwnerCredentials[R]]
  with Params {

  private def credentials(username: String,
                          password: String,
                          clientId: String,
                          scope: Option[String]): Future[Either[OAuth2Error, ResourceOwnerCredentials[R]]] = {

    resourceOwnerService
      .retrieveByUsername(username)
      .flatMap {
        case None => Future(Left(OAuth2Error.InvalidGrant(Some("ResourceOwner not found"))))
        case Some(owner) =>
          resourceOwnerService
            .matchPassword(owner, password)
            .map{ correctPassword =>
              if (correctPassword) {
                val grant = ResourceOwnerCredentials[R](
                  id = username,
                  clientId = clientId,
                  resourceOwnerId = owner.id,
                  scope = scope
                )

                Right(grant)
              } else {
                // masking incorrect password response with user not found
                Left(OAuth2Error.InvalidGrant(Some("ResourceOwner not found")))
              }
            }
      }
  }

  /**
   * Extracts grant from incoming request and validates it
   *
   * @param request play request
   * @return future grant or error
   */
  override def extract(request: ClientRefinerProfile[Client]#ClientRequest[_]) = {
    val params = allParams(request).mapValues(_.head)
    val scope = params.get(ResourceOwnerConsumer.Scope)

    (for {
      username <- params.get(ResourceOwnerConsumer.Username)
      password <- params.get(ResourceOwnerConsumer.Password)
    } yield (username, password)) match {
      case Some((username, password)) => credentials(username, password, request.client.id, scope)
      case None => Future(Left(OAuth2Error.InvalidGrant()))
    }

  }

  override def consume(grant: ResourceOwnerCredentials[R]): Unit = {}
  override def isConsumed(grant: ResourceOwnerCredentials[R]): Future[Boolean] = Future(false)
}

object ResourceOwnerConsumer {
  val Username = "username"
  val Password = "password"
  val Scope = "scope"

}