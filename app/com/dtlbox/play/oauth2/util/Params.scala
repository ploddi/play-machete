package com.dtlbox.play.oauth2.util

import play.api.mvc.Request

trait Params {

  final def allParams(implicit request: Request[_]): Map[String, Seq[String]] = {
    val form = request.body match {
      case body: play.api.mvc.AnyContent if body.asFormUrlEncoded.isDefined => body.asFormUrlEncoded.get
      case body: play.api.mvc.AnyContent if body.asMultipartFormData.isDefined => body.asMultipartFormData.get.asFormUrlEncoded
      case body: Map[_, _] => body.asInstanceOf[Map[String, Seq[String]]]
      case body: play.api.mvc.MultipartFormData[_] => body.asFormUrlEncoded
      case _ => Map.empty[String, Seq[String]]
    }

    form ++ request.queryString.map {
      case (k, v) => k -> (v ++ form.getOrElse(k, Nil))
    }
  }

}
