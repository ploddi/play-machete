package com.dtlbox.play.oauth2.core.services

import com.dtlbox.play.oauth2.core.{ResourceOwner, Grant}

import scala.concurrent.Future

trait GrantService[R <: ResourceOwner, G <: Grant[R]] {

  def create(grant: G): Future[G]

  def retrieve(id: String): Future[Option[G]]

  def update(grant: G): Future[G]

}
