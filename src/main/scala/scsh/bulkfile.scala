package scsh

import java.io.{File => JFile}
import java.nio.file.{Path => JPath}
import scala.util.matching.Regex
import scala.util.{Try, Success, Failure}
import better.files._
import files._
import FileCheck._

object bulkfile {

  case class Params(
    in: File = File("."),
    regex: Option[String] = None,
    glob: Option[String] = None,
    parallel: Boolean = false,
    debug: Boolean = false,
    dry: Boolean = false)

  trait BulkFileConfig {
    def params: Params
  }

  abstract class BulkFileParser[V <: BulkFileConfig](name: String)
      extends scopt.OptionParser[V](name) {

    def addBaseOptions(): Unit = {
      opt[String]("regex") optional() action { (x, c) =>
        update(c, c.params.copy(regex = Some(x)))
      } validate { x =>
        if (x.isEmpty) failure("Empty regex specified")
        else Try(new Regex(x)) match {
          case Failure(e) => failure(s"Invalid regex: ${e.getMessage}")
          case _ => success
        }
      } text ("""Use a regex to convert all matched files. The <input> argument must
        then be a directory and all files are scanned recursively. `regex' is
        matched against the complete path name of each file.""")

      opt[String]("glob") optional() action { (x, c) =>
        update(c, c.params.copy(glob = Some(x)))
      } text ("""Use a glob to convert all matched files. The <input> argument must
        then be a directory and all files are scanned recursively. `glob' is
        matched against the complete path name of each file.""")

      opt[Unit]('p', "parallel") optional() action { (_, c) =>
        update(c, c.params.copy(parallel = true))
      } text ("Execute the transformation on all files in parallel")

      opt[Unit]("dry") optional() action { (_, c) =>
        update(c, c.params.copy(dry = true))
      } text("Do not execute the transformation, but show what would happen.")

      opt[Unit]("debug") hidden() action { (_, c) =>
        update(c, c.params.copy(debug = true))
      }

      help("help") text("prints this usage text")
      version("version") text("prints version information and exits")

      arg[File]("input") required() valueName "<file or dir>" action { (x, c) =>
        update(c, c.params.copy(in = x))
      } text("""The input file to convert. It must be a directory if `--regex'
        is specified.""")

      checkConfig { cfg =>
        val p = cfg.params
        if (p.regex.isDefined && p.glob.isDefined)
          failure("Specify either a glob or a regex")
        else if ((p.regex.isDefined || p.glob.isDefined) && !p.in.isDirectory)
          failure(s"`${p.regex.orElse(p.glob).get}' given, so `${p.in.path}' must be a directory")
        else if (p.regex.isEmpty && p.glob.isEmpty && !p.in.isRegularFile)
          failure(s"${p.in.path} must be an existing file!")
        else success
      }
    }

    def update(cfg: V, params: Params): V
  }

  trait FileTransform[C <: BulkFileConfig] {

    def parser: scopt.OptionParser[C]

    def transform(cfg: C)(in: File): File

    def logLine(line: String): Unit =
      println(line)

    def findFiles(p: Params): Iterator[File] = {
      val pattern = p.glob.orElse(p.regex).get
      val syntax = p.regex.map(_ => "regex").getOrElse("glob")
      p.in.assert(directory).glob(pattern, syntax)
    }

    def transformAll(cfg: C): Unit = {
      val p = cfg.params
      logLine(s"Transform files in ${p.in.path} matching `${p.regex.orElse(p.glob).get}':")
      val preview = findFiles(p)
        .take(5)
        .map(_.path.toString)
      if (preview.isEmpty) {
        logLine("No file matched.")
      } else {
        preview.foreach(logLine)
        logLine("...")
        if (confirm.continue()) {
          val files = findFiles(p).toSeq
          if (p.dry || !p.parallel) files foreach transform(cfg)
          else files.par foreach transform(cfg)
        }
      }
    }

    def handleError(cfg: C, e: Exception): Unit = {
      if (cfg.params.debug) e.printStackTrace()
      else System.err.println(e.getMessage)
      System.exit(2)
    }

    def main(args: Array[String], init: C): Unit = {
      parser.parse(args, init) match {
        case Some(cfg) =>
          try {
            val p = cfg.params
            if (p.regex.orElse(p.glob).isEmpty) transform(cfg)(p.in)
            else transformAll(cfg)
          }  catch {
            case e: Exception =>
              handleError(cfg, e)
          }
        case _ =>
          System.exit(1)
      }
    }
  }

  trait FileTransformProcess[C <: BulkFileConfig] extends FileTransform[C] {
    import scala.language.implicitConversions

    def transform(cfg: C)(in: File): File = {
      val out = makeOutFile(in, cfg)
      logLine(s"""Transform ${if (cfg.params.dry) "(dry) " else ""}${in.path} => ${out.path}""")
      val cmd = makeCommand(
        in.assert(readableFile),
        out.assert(writeableFile),
        cfg)
      if (cfg.params.dry) logLine(cmd.asString)
      else sys.process.Process(cmd.value).!<
      out
    }

    def makeCommand(in: File, out: File, cfg: C): Cmd
    def makeOutFile(in: File, cfg: C): File

    case class Cmd(name: String, args: List[String] = Nil) {
      def ~[A](arg: A)(implicit f: A => String): Cmd =
        copy(args = f(arg) :: args)

      def ~[A](oarg: Option[A])(implicit f: A => String): Cmd = oarg match {
        case Some(x) => this.~(x)
        case _ => this
      }

      def ~[A](seq: Iterable[A])(implicit f: A => String): Cmd =
        seq.foldLeft(this){ (c, e) => c ~ e }

      lazy val value: Seq[String] = name :: args.reverse
      lazy val asString: String = value.mkString(" ")
    }

    implicit val forInt: Int => String = _.toString
    implicit val forLong: Long => String = _.toString
    implicit val forFile: File => String = _.path.toString
    implicit val forJavaFile: JFile => String = f => forFile(f.toScala)
    implicit val forJavaPath: JPath => String = f => forFile(f)

  }
}
