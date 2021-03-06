package com.dtlbox.play.oauth2.contrib.services

import com.dtlbox.play.oauth2.core.ResourceOwner
import scala.concurrent.Future

trait ResourceOwnerService[R <: ResourceOwner] {

  def retrieve(ownerId: R#Id): Future[Option[R]]

  def retrieveByUsername(username: String): Future[Option[R]]

  def matchPassword(resourceOwner: R, suppliedPassword: String): Future[Boolean]

}
