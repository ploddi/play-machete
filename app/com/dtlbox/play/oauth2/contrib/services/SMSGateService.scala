package com.dtlbox.play.oauth2.contrib.services

import scala.concurrent.Future


object SMSGateResponseStatuses {
  sealed trait Response
  case object Ok extends Response
  case class Error(code: Int = -1, description: Option[String]) extends Exception with Response
}

trait SMSGateService {

  def send(message: String, phoneNumber: String): Future[SMSGateResponseStatuses.Response]

}
