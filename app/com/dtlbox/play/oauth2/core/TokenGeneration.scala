package com.dtlbox.play.oauth2.core

import java.security.SecureRandom

import org.apache.commons.codec.binary.Hex

trait TokenGeneration {

  protected def random: SecureRandom

  def generateToken(length: Int = 64) = {
    val bytes = new Array[Byte](length)
    random.nextBytes(bytes)
    new String(Hex.encodeHex(bytes))
  }

}
