package com.dtlbox.play.oauth2.util

import play.api.http.MimeTypes
import play.api.libs.json._
import play.api.mvc._
import play.api.http.{Status => StatusCode}

/**
 * Helper for response rendering.
 */
trait RenderingHelpers
  extends Rendering
  with AcceptExtractors
  with Params
  with Results {

  private val AcceptsUrlFormEncoded = Accepting(MimeTypes.FORM)

  /**
   * Renders content according to Accept request header.
   *
   * @param status response status.
   * @param a content to render.
   * @param request play request.
   * @param p conversion to {{{Map[String, Seq[String]}}}.
   * @param w Json [[Writes]].
   * @tparam A content type.
   * @return play result.
   */
  def renderAsBody[A](status: Int, a: A)
                     (implicit request: RequestHeader, p: A => Map[String, Seq[String]], w: Writes[A]): Result = render {
    case Accepts.Json() => Status(status)(Json.toJson(a))
    case AcceptsUrlFormEncoded() => Status(status)(p(a))
  }

  /**
   * Render content as redirect with query parameters
   *
   * @param a content.
   * @param request play request.
   * @param p conversion to {{{Map[String, Seq[String]}}}.
   * @param w Json [[Writes]].
   * @tparam A content type.
   * @return play result.
   */
  def renderAsRedirect[A](uri: Option[String], a: A)
                         (implicit request: RequestHeader, p: A => Map[String, Seq[String]], w: Writes[A]) = {
    uri
      .map(Redirect(_, p(a)))
      .getOrElse(renderAsBody(StatusCode.BAD_REQUEST, a))
  }
}
