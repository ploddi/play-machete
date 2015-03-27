package com.dtlbox.play.oauth2.contrib.services

import com.dtlbox.play.oauth2.contrib.grants.SMSCodeGrant
import com.dtlbox.play.oauth2.core.ResourceOwner
import com.dtlbox.play.oauth2.core.services.GrantService
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future

/**
 * Wrapper on top of [[GrantService]] that implements SMS message sending
 * via [[SMSGateService]] and incrementing tries on reads.
 *
 * @param smsGate [[SMSGateService]] for message sending
 */
abstract class SMSCodeGrantService[R <: ResourceOwner](smsGate: SMSGateService)
 extends GrantService[R, SMSCodeGrant[R]] {

  protected def messageForGrant(grant: SMSCodeGrant[R]): String = s"Код подверждения: ${grant.smsCode}"

  /**
   * Inner [[GrantService]]
   */
  protected val grantService: GrantService[R, SMSCodeGrant[R]]

  /**
   * Updates grants. Necessary for number of tries counting mechanism.
   *
   * @param grant grant to update
   * @return future updated grant
   */
  def update(grant: SMSCodeGrant[R]): Future[SMSCodeGrant[R]] = grantService.update(grant)

  /**
   * Finds grant by phone number
   *
   * @param phoneNumber phone number
   * @return future optional grant
   */
  def retrieveByPhone(phoneNumber: String): Future[Option[SMSCodeGrant[R]]]

  /**
   * Saves grant in storage and sends SMS message with code using [[SMSGateService]].
   *
   * @param grant grant to persist
   * @return future grant
   */
  def create(grant: SMSCodeGrant[R]) = {
    val msg = messageForGrant(grant)

    smsGate
      .send(msg, grant.phoneNumber)
      .flatMap {
        case SMSGateResponseStatuses.Ok => grantService.create(grant)
        case e: SMSGateResponseStatuses.Error => Future.failed(e)
      }
  }

  def retrieve(id: String) = grantService.retrieve(id)

  /**
   * Tries to retrieve grant and on success increment grant's numOfTries counter.
   *
   * @param id grant request_token
   * @return optional future grant
   */
  def retrieveTrying(id: String) = {
    this
      .retrieve(id)
      .flatMap {
        case Some(grant) =>
          this
            .update(grant.copy(numOfTries = grant.numOfTries + 1))
            .map(Option(_))
        case None =>
          Future(Option.empty[SMSCodeGrant[R]])
      }
  }
}
