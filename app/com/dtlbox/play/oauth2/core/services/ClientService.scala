package com.dtlbox.play.oauth2.core.services

import com.dtlbox.play.oauth2.core.Client

import scala.concurrent.Future

trait ClientService[C <: Client] {

  def retrieve(id: String): Future[Option[C]]

}
