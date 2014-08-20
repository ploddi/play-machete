package com.dtlbox.play.oauth2.core

trait ResourceOwner {

  type Id

  def id: Id

}
