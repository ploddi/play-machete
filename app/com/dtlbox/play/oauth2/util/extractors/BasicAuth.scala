package com.dtlbox.play.oauth2.util.extractors

import org.apache.commons.codec.binary.Base64
import play.api.http.HeaderNames
import play.api.mvc.RequestHeader

/**
 * Tries to extract HTTP Basic Authorization from play request.
 *
 * Can be used in matching.
 */
object BasicAuth {

  def unapply(request: RequestHeader) = {
    request
      .headers
      .get(HeaderNames.AUTHORIZATION)
      .map(_.split(" ").toList)
      .flatMap {
      case "Basic" :: base64 :: Nil => Some(new String(Base64.decodeBase64(base64), "UTF-8"))
      case _ => None
    }
      .map(_.split(":").toList)
      .flatMap {
      case username :: password :: Nil => Some(username, password)
      case _ => None
    }
  }
}