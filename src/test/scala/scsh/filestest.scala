package scsh

import org.scalatest._
import files._
import better.files._

class filestest extends FlatSpec with Matchers {

  def randomString = java.util.UUID.randomUUID().toString

  val target = File("target")

  "mapFileName" should "not move an existing file" in {
    val f = (target / "bla").createIfNotExists()
    val other = f.mapFileName(_ => "blup")
    other.exists should be (false)
    other.name should be ("blup")
  }

  "mapFileName" should "rename non-existing files" in {
    val f = target / randomString
    val other = f.mapFileName(_ => "hello")
    other.exists should be (false)
    other.name should be ("hello")
  }

  "mapBaseName" should "not rename extensions" in {
    val f = target / s"${randomString}.txt"
    val other = f.mapBaseName(n => "readme")
    other.exists should be (false)
    other.name should be ("readme.txt")
  }

  "mapExtension" should "not rename basename" in {
    val name = randomString
    val f = target / s"$name.txt"
    val other = f.mapExtension(e => "rst")
    other.exists should be (false)
    other.name should be (s"$name.rst")
  }

  "mapExtension" should "remove extension on empty result" in {
    val name = randomString
    val f = target / s"$name.txt"
    val other = f.mapExtension(e => "")
    other.exists should be (false)
    other.name should be (name)
  }

  "splitName" should "split on last extension" in {
    splitName("a.b.c") should be (("a.b", "c"))
  }

  "splitName" should "return empty string for no extension" in {
    splitName("abc") should be (("abc", ""))
  }

  "checks" should "compose" in {
    import FileCheck._
    (notExists & parentWriteable).pass(target / randomString)
    (directory & FileCheck.readable & FileCheck.writeable).pass(target)
  }
}
