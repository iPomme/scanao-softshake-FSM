package io.nao.softshake.cmd


import io.nao.softshake.api.SoftShakeService
import org.apache.felix.gogo.commands.{Argument, Command}
import org.apache.karaf.shell.console.OsgiCommandSupport

/**
 * Created by nicolasjorand on 04/02/15.
 */
@Command(scope = "nao", name = "softShake", description = "Execute the demonstration of the Softshake demo")
class SoftShakeCommands extends OsgiCommandSupport {

  @Argument(index = 0, name = "action", description = "The action to perform on the service. Could be start | count", required = true, multiValued = false)
  var key: String = null

  protected def doExecute: String = {
    val srvName = classOf[SoftShakeService].getName()
    val ref = Option(getBundleContext().getServiceReference(srvName))

    val demo = ref match {
      case None =>
        println(s"Cannot get the reference to the service '$srvName'")
        None
      case Some(srvRef) =>
        Option(getService(classOf[SoftShakeService], srvRef))
    }
    (demo, key) match {
      case (Some(s), "start") => s.start()
      case (Some(s), "count") => s.count(10)
      case (Some(s), "state") => s.state()
      case (Some(s), "stop") => s.stop()
      case (None, _) => println(s"Command '$key' not executed !")
      case (_, _) => println(s"'$key' is an unknown command")
    }
    null
  }

}
