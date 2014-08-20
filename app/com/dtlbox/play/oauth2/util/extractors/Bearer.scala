package com.dtlbox.play.oauth2.util.extractors

import play.api.http.HeaderNames
import play.api.mvc.RequestHeader

/**
 * Extracts Bearer authorization from play request
 */
object Bearer {
  def unapply(request: RequestHeader) = {
    request
      .headers
      .get(HeaderNames.AUTHORIZATION)
      .map(_.split(" ").toList)
      .flatMap {
      case "Bearer" :: accessToken :: Nil => Some(accessToken)
      case _ => None
    }
  }
}
