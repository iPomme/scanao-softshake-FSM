/*
 * -----------------------------------------------------------------------------
 *  - ScaNao is an open-source enabling Nao's control from Scala code.            -
 *  - At the low level jNaoqi is used to bridge the C++ code with the JVM.        -
 *  -                                                                             -
 *  -  CreatedBy: Nicolas Jorand                                                  -
 *  -       Date: 11 Feb 2015                                                      -
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

import akka.actor.{Actor, ActorLogging, ActorRef, FSM}
import akka.util.Timeout
import io.nao.scanao.msg.{memory, txt}
import io.nao.softshake.FSMSoftshakeMediator._
import akka.pattern.ask


import scala.collection.immutable.HashMap
import scala.concurrent.Await
import scala.concurrent.duration._

sealed trait State

object Day extends State

object Night extends State

object CheckLight extends State


case class LightSwitchedOff(level: Int, naoActors: HashMap[String, Option[ActorRef]])

case class LightEvt(level: Int)

class FSMDayNight(val naoRefs: HashMap[String, Option[ActorRef]]) extends Actor with FSM[State, Unit] with ActorLogging {

  implicit val timeout = Timeout(5 seconds)


  // Set the initial state not initialized with the current light value
  startWith(Day, Unit)

  when(Day) {
    case Event(l: LightSwitchedOff, _) =>
      log.info(s"When Day received the event: $l")
      goto(Night)
    case Event(e, d) =>
      log.debug(s"--Day----------> $e , $d")
      stay()
  }

  when(Night, stateTimeout = 1 seconds) {
    case Event(StateTimeout, _) =>
      goto(CheckLight)
    case Event(l: LightEvt, _) if l.level < 60 =>
      log.info(s"When Night received the event $l")
      goto(Day)
    case Event(e, d) =>
      log.debug(s"--Night--------> $e , $d")
      stay()
  }

  when(CheckLight, stateTimeout = 100 milliseconds) {
    case Event(StateTimeout, _) =>
      goto(Night)
    case Event(l: LightEvt, _) if l.level < 60 =>
      log.info(s"When CheckLight received the event $l")
      goto(Day)
    case Event(e, d) =>
      log.debug(s"--CheckLight--------> $e , $d")
      stay()
  }

  onTransition {
    case Day -> Night =>
      naoRefs(naoText).map(_ ! txt.Say("Hee, il fait nuit !"))
    case Night -> Day | CheckLight -> Day =>
      naoRefs(naoText).map(_ ! txt.Say("aaaa! encore un gag a Nicolas!"))
    case Night -> CheckLight | CheckLight -> Night =>
      val future = ask(naoRefs(naoMemory).get, memory.DataInMemoryAsString("DarknessDetection/DarknessValue")).mapTo[Int]
      val newLight = Await.result(future, 10 seconds)
      log.info(s"Got a new Light value of $newLight")
      self ! LightEvt(newLight)

  }

  initialize()
}
