package scsh

import scala.tools.nsc._

object boot {
  val defaultOptions = List("-nocompdaemon", "-usejavacp", "-deprecation", "-savecompiled")

  def main(args: Array[String]): Unit = {
    if (args.isEmpty) {
      // starts the repl
      MainGenericRunner.main(args)
    } else {
      val cmd = new GenericRunnerCommand(defaultOptions ++ args.toList)
      ScriptRunner.runScript(cmd.settings, cmd.thingToRun, cmd.arguments)
    }
  }
}
