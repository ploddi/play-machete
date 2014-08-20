package com.dtlbox.play.oauth2.core

trait Logger {
  /**
   * A named logger instance.
   */
  val logger = play.api.Logger(this.getClass)
}
