/*
 * -----------------------------------------------------------------------------
 *  - ScaNao is an open-source enabling Nao's control from Scala code.            -
 *  - At the low level jNaoqi is used to bridge the C++ code with the JVM.        -
 *  -                                                                             -
 *  -  CreatedBy: Nicolas Jorand                                                  -
 *  -       Date: 21 Oct 2013                                                      -
 *  -                                                                            	-
 *  -       _______.  ______      ___      .__   __.      ___       ______       	-
 *  -      /       | /      |    /   \     |  \ |  |     /   \     /  __  \      	-
 *  -     |   (----`|  ,----'   /  ^  \    |   \|  |    /  ^  \   |  |  |  |     	-
 *  -      \   \    |  |       /  /_\  \   |  . `  |   /  /_\  \  |  |  |  |     	-
 *  -  .----)   |   |  `----. /  _____  \  |  |\   |  /  _____  \ |  `--'  |     	-
 *  -  |_______/     \______|/__/     \__\ |__| \__| /__/     \__\ \______/      	-
 *  -----------------------------------------------------------------------------
 */


package io.nao.softshake

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import io.nao.softshake.api.SoftShakeService

//import com.github.levkhomich.akka.tracing.ActorTracing

import io.nao.scanao.msg._

import scala.concurrent.Await
import scala.concurrent.duration._


class SoftShakeServiceImpl(system: ActorSystem) extends SoftShakeService {

  // create the actors for the demo
  val mediator = system.actorOf(Props[FSMSoftshakeMediator], "mediator")


  implicit val timeout = Timeout(5 seconds)

  /**
   *
   * SoftShakeService implementation
   *
   */

  override def start(): Unit = {

    /**
     * Thanks to the initialization, all the messages would be stached if the robot is not ready
     */

    /**
     * Inform that the demo is ready to start
     */
    mediator ! txt.Say(s"C'est partit pour la démo, pourvu que ca marche ! Je te dis des que je suis pret ...")

    /**
     * Subscribe to event and use a state Machine to handle it
     */
    mediator ! tech.SubscribeEvent("DarknessDetection/DarknessDetected", "SNEvents", "event", mediator) // Subscribe to an event

    println("Starting")
  }

  override def count(to: Int) {
    (1 to to).foreach(x => {
      println(x)
      mediator ! txt.Say(x.toString)
    })
  }

  override def stop() {
    system.shutdown()
    println("stopped")
  }

  override def state() {
    val state = Await.result(mediator.ask(InfoState), timeout.duration).asInstanceOf[String]
    println(state)
  }
}