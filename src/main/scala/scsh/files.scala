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
    * Split the name of the file in basename and extension.
    */
  def splitName(f: File): (String, String) =  f.name.lastIndexOf('.') match {
    case i if i > 0 => (f.name.substring(0, i), f.name.substring(i + 1))
    case _ => (f.name, "")
  }

  /**
    * Return the extension of the given file (without the dot).
    */
  def getExtension(p: File): Option[String] =
    Some(splitName(p)._2).filter(_.nonEmpty)

  /**
    * Return the name without extension.
    */
  def baseName(f: File): String = splitName(f)._1

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
    * Change extension of `p` to `ext` (given without dot!). If
    * `ext` is empty, the extension of `p` is removed.
    */
  def changeExtension(ext: String)(p: File): File =
    if (ext.isEmpty) p.path.resolveSibling(baseName(p))
    else p.path.resolveSibling(s"${baseName(p)}.$ext")

}
