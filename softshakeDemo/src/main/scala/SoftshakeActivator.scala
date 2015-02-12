
package io.nao.softshake.osgi

import java.io.File

import akka.actor.ActorSystem
import akka.osgi.ActorSystemActivator
import com.typesafe.config.{Config, ConfigFactory}
import io.nao.softshake.SoftShakeServiceImpl
import io.nao.softshake.api.SoftShakeService
import org.osgi.framework.BundleContext

import scala.collection.mutable
import scala.collection.JavaConverters._


/**
 * This class is the activator of the demo.
 * It create the actor system, create an instance of the softshake demo with the system acotr as parameter.
 * Created by nicolasjorand on 10/02/15.
 */
class SoftshakeActivator extends ActorSystemActivator {
  override def configure(context: BundleContext, system: ActorSystem): Unit = {
    // Register the system Actor as a service
    registerService(context, system)
    val props: mutable.Map[String, AnyRef] = scala.collection.mutable.Map[String, AnyRef]("name" -> "softshake")
    context.registerService(classOf[SoftShakeService], new SoftShakeServiceImpl(system), props.asJavaDictionary)
  }

  override def getActorSystemName(bundle: BundleContext) = "softshake"

  override def getActorSystemConfiguration(context: BundleContext) = ConfigFactory.parseFile(new File("etc/softshake.conf"))
}
