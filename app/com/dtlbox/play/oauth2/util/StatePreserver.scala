package com.dtlbox.play.oauth2.util

import play.api.http.{HeaderNames, Status => StatusCodes}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc._

import scala.language.higherKinds
import scala.concurrent.Future

trait StatePreserver[R[_] <: Request[_]]
  extends ActionFunction[R, R]
  with Results
  with Params {

  import com.dtlbox.play.oauth2.util.UrlStringHelpers._

  val State = "state"

  def invokeBlock[A](request: R[A], block: R[A] => Future[Result]) = { implicit val r = request
    val stateOpt = allParams.get(State).flatMap(_.headOption)
    block(request).map { result =>
      if (result.header.status == StatusCodes.SEE_OTHER && stateOpt.isDefined) {
        val headers = result
          .header.headers
          .get(HeaderNames.LOCATION)
          .map(HeaderNames.LOCATION -> _.appendParams(State -> stateOpt.get))
          .toSeq

        result.withHeaders(headers:_*)
      } else {
        result
      }
    }
  }
}

object StatePreserver {
  def apply[P[_], R[_] <: Request[_]](func: ActionFunction[P, R]) = new StatePreserver[R] {}
}
