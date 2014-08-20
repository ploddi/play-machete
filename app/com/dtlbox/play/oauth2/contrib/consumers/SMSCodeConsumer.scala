package com.dtlbox.play.oauth2.contrib.consumers

import com.dtlbox.play.oauth2.contrib.grants.SMSCodeGrant
import com.dtlbox.play.oauth2.contrib.services.SMSCodeGrantService
import com.dtlbox.play.oauth2.core._
import com.dtlbox.play.oauth2.util.{GrantValidation, Params}
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future

class SMSCodeConsumer[R <: ResourceOwner](grantService: SMSCodeGrantService[R],
                                          smsCodeMaxTries: Int = 3)

  extends GrantConsumer[R, SMSCodeGrant[R]]
  with GrantValidation[R, SMSCodeGrant[R]]
  with Params {

  private val SMSCode = "sms_code"
  private val RequestToken = "request_token"

  private def retrieveAndValidate(requestToken: String, smsCode: String) = {
    def validate(grantOpt: Option[SMSCodeGrant[R]]) = {
      val validateSMSCode = validateGrant("invalid sms_code") {
        _.smsCode == smsCode
      }

      def validateNotExpires = validateGrant("request_token is expired") {
        _.expiresAt > System.currentTimeMillis / 1000
      }

      def validateMaxTries(maxTries: Int) = validateGrant("maximum number of tries exceeded") {
        _.numOfTries < maxTries + 1
      }

      val validateFound = grantOpt.toRight(OAuth2Error.InvalidClient(Some("grant not found")))

      for {
        g1 <- validateFound.right
        g2 <- validateSMSCode(g1).right
        g3 <- validateNotExpires(g2).right
        g4 <- validateMaxTries(smsCodeMaxTries)(g3).right
      } yield g4
    }

    grantService
      .retrieveTrying(requestToken)
      .map(validate)
  }

  def extract(request: ClientRefinerProfile[Client]#ClientRequest[_]) = {
    val params = allParams(request)

    (for {
      requestToken <- params.get(RequestToken).flatMap(_.headOption)
      smsCode <- params.get(SMSCode).flatMap(_.headOption)
    } yield (requestToken, smsCode)) match {
      case Some(tuple) => (retrieveAndValidate _).tupled(tuple)
      case None => Future(Left(OAuth2Error.InvalidGrant()))
    }
  }

  override def consume(grant: SMSCodeGrant[R]): Unit = grantService.update(grant.copy(consumed = true))

  override def isConsumed(grant: SMSCodeGrant[R]): Future[Boolean] = Future(grant.consumed)
}
