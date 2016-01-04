package scsh

import org.scalatest._
import bulkfile._
import better.files._
import files._

class bulkfiletest extends FlatSpec with Matchers {

  case class Config(
    params: Params = Params(),
    count: Int = 0
  ) extends BulkFileConfig

  val testFile = File("target/test").createIfNotExists()

  "BulkFileParser" should "be extensible" in {
    val parser = new BulkFileParser[Config]("prg") {
      head("test", "1.0")
      opt[Int]("count") optional() action {(x, c) => c.copy(count = x)}
      addBaseOptions()
      def update(cfg: Config, params: Params): Config =
        cfg.copy(params = params)
    }

    parser.parse(List("--count", "12", "target/test"), Config()) match {
      case Some(cfg) =>
        cfg.count should be (12)
        cfg.params.in should be (testFile)
      case _ =>
        sys.error("failed")
    }
  }

  "BulkFileParser" should "validate properly" in {
    val parser = new BulkFileParser[Config]("prg") {
      addBaseOptions()
      def update(cfg: Config, params: Params): Config =
        cfg.copy(params = params)
    }

    parser.parse(List("--regex", ".*.avi$"), Config()) should be (None)
    parser.parse(List("--regex", ".*.avi$", "target/test"), Config()) should be (None)
    parser.parse(List("--regex", "", "target"), Config()) should be (None)
    parser.parse(List("target"), Config()) should be (None)

    val Some(cfg) = parser.parse(List("--regex", ".*.avi$", "target"), Config())
    cfg should be (Config(Params(file"target", Some(".*.avi$")), 0))
  }

  "Cmd" should "have implicits in scope" in {
    val t1: Option[Int] = None
    val t2: Option[String] = None
    val t3: Option[Long] = Some(123L)
    val tr = new FileTransformProcess[Config] {
      val parser = new BulkFileParser[Config]("test") {
        addBaseOptions()
        def update(c: Config, p: Params) = c.copy(params = p)
      }
      def makeOutFile(in: File, cfg: Config) = in.mapExtension(_ => "txt")
      def makeCommand(in: File, out: File, cfg: Config): Cmd =
        Cmd("none") ~ "astring" ~ t1 ~ 31 ~ t2 ~ t3 ~ out
    }

    val cmd = tr.makeCommand(File("in"), File("out"), Config())
    cmd.args.reverse should be (List("astring", "31", "123", File("out").path.toString))
    cmd.value should be (Seq("none", "astring", "31", "123", File("out").path.toString))
  }
}
