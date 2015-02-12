package io.nao.softshake.api


/**
 * Created by nicolasjorand on 04/02/15.
 */
trait SoftShakeService {
  def start()

  def stop()

  def state()

  def count(to: Int)
}
