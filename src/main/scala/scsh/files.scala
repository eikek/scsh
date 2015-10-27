package scsh

import better.files._

object files {

  /**
    * Create a stream of all files and directories below the given
    * path.
    */
  def walkFiles(start: File): Stream[File] = {
    if (start.isDirectory) {
      val entries = start.list.toStream
      start #:: entries.flatMap(walkFiles)
    } else {
      Stream(start)
    }
  }

  /**
    * Return the extension of the given file (without the dot).
    */
  def getExtension(p: File): Option[String] =
    p.extension.map(_.substring(1))

  /**
    * Check whether the given path has an extension from the given
    * set. Convert the extension to lowercase before comparing.
    */
  def hasExtensions(exts: Set[String])(p: File): Boolean =
    getExtension(p).map(_.toLowerCase()) match {
      case Some(ext) => exts contains ext
      case _ => exts.isEmpty
    }


  /**
    * Change extension of `p` to `ext` (given without dot!)
    */
  def changeExtension(ext: String)(p: File): File =
    p.changeExtensionTo(s".$ext")
}
