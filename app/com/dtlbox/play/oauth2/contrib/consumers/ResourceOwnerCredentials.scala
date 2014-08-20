package com.dtlbox.play.oauth2.contrib.consumers

/*
object ResourceOwnerCredentials extends GrantExtractor with Params {
  private val Username = "username"
  private val Password = "password"

  def unapply(request: ClientRequest[_]): Option[ResourceOwnerCredentials] = {
    val params = allParams(request)

    for {
      username <- params.get(Username).flatMap(_.headOption)
      password <- params.get(Password).flatMap(_.headOption)
    } yield ResourceOwnerCredentialsGrant(request.client, username, password)
  }

}
*/