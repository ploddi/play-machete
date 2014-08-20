package com.dtlbox.play.oauth2.util

object UrlStringHelpers {

  implicit class UrlStringOps(url: String) {

    def appendParams(params: (String, String)*) = {
      url + {
        if (url.contains("?")) "&" else "?"
      } + params.map(t => t._1 + "=" + t._2).mkString("&")
    }
  }

}
