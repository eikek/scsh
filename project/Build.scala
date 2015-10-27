import sbt._
import Keys._
import sbtassembly.AssemblyPlugin.autoImport._
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermission._
import java.util.EnumSet

object Build extends sbt.Build {
  val scsh = config("scsh") describedAs ("Config for scsh script generation.")

  val shebang = settingKey[String]("The full path to the shell used as shebang for the script")
  val javaBin = settingKey[String]("The full path to java executable.")
  val `gen-scsh` = taskKey[File]("Create the assembly and shell script")

  lazy val root = Project(id = "scsh", base = file(".")).settings(
    `gen-scsh` := genScshImpl.value
  )

  lazy val genScshImpl = Def.task {
    val template = sourceDirectory.value / "main" / "shell" / "scsh"
    val out = target.value / "bin" / "scsh"
    val body = IO.read(template)
      .replace("$assembly-jar$", assembly.value.toString)
      .replace("$shebang$", (shebang in scsh).value)
      .replace("$java-bin$", (javaBin in scsh).value)
      .replace("$options$", (javaOptions in scsh).value.mkString(" "))
    IO.write(out, body)
    Files.setPosixFilePermissions(out.toPath, EnumSet.of(
      OWNER_READ, OWNER_WRITE, OWNER_EXECUTE,
      GROUP_READ, GROUP_EXECUTE,
      OTHERS_READ, OTHERS_EXECUTE
    ))
    out
  }
}
