package com.dtlbox.play.oauth2.util.extractors

import com.dtlbox.play.oauth2.util.Params
import play.api.mvc.Request

/**
 * Tries to extract OAuth 2.0 client authorization from request.
 *
 * Extracts from both query params and request body.
 *
 * Can be used in matching.
 */
object ClientParamAuth extends Params {
  private val ClientId = "client_id"
  private val ClientSecret = "client_secret"

  def unapply(request: Request[_]) = {
    val params = allParams(request)
    for {
      username <- params.get(ClientId).flatMap(_.headOption)
      password <- params.get(ClientSecret).flatMap(_.headOption)
    } yield (username, password)
  }
}
