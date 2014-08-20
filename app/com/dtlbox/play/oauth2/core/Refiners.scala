package com.dtlbox.play.oauth2.core

/**
 * All refiner profiles.
 *
 * {{{
 *   import refiners._
 * }}}
 *
 * @tparam C The client type.
 * @tparam R The resource owner type.
 */
trait Refiners[C <: Client, R <: ResourceOwner] {

  val refiners = new ClientRefinerProfile[C] with GrantConsumingProfile[C, R]

}
