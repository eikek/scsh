package scsh

import better.files._

object files {

  implicit val readBetterFile: scopt.Read[File] =
    scopt.Read.reads(File(_))

  /** Create a stream of all files and directories below the given
    * path.
    */
  def walkFiles(start: File, recursive: Boolean = true): Stream[File] = {
    if (start.isRegularFile) Stream(start)
    else {
      if (recursive) start.glob("**")(File.PathMatcherSyntax.glob).toStream
      else start.list.toStream
    }
  }


  /** Split the name of the file in basename and extension. The extension
    * is the part of the name following the last `.' character.
    */
  def splitName(name: String): (String, String) = name.lastIndexOf('.') match {
    case i if i > 0 => (name.substring(0, i), name.substring(i + 1))
    case _ => (name, "")
  }

  trait FileCheck {
    self =>

    /** Check some properties of a file. */
    def apply(f: File): Boolean

    /** Create error message if the property does not hold for the file. */
    def message(f: File): String

    /**
      * Tests the given file and throws an exception if it doesn't
      * pass. Otherwise the file is returned.
      */
    def pass(f: File): File =
      if (apply(f)) f
      else sys.error(message(f))

    def & (next: FileCheck): FileCheck = new FileCheck {
      def apply(f: File) = self(f) && next(f)
      def message(f: File) =
        if (self(f)) next.message(f)
        else if (next(f)) self.message(f)
        else s"${self.message(f)} and ${next.message(f)}"
    }

    def | (next: FileCheck): FileCheck = new FileCheck {
      def apply(f: File) = self(f) || next(f)
      def message(f: File) =
        s"${self.message(f)} and ${next.message(f)}"
    }
  }

  object FileCheck {
    val writeable: FileCheck = new FileCheck {
      def apply(f: File) = f.isWriteable
      def message(f: File) = s"${f.path} is not writeable"
    }

    val parentWriteable: FileCheck = new FileCheck {
      def apply(f: File) = f.parent.isWriteable
      def message(f: File) = s"${f.parent.path} is not writeable"
    }

    val readable: FileCheck = new FileCheck {
      def apply(f: File) = f.isReadable
      def message(f: File) = s"${f.path} is not readable"
    }

    val exists: FileCheck =  new FileCheck {
      def apply(f: File) = f.exists
      def message(f: File) = s"${f.path} does not exist"
    }

    val existing = exists

    val notExists: FileCheck =  new FileCheck {
      def apply(f: File) = !f.exists
      def message(f: File) = s"${f.path} exists"
    }

    val missing = notExists

    val directory: FileCheck =  new FileCheck {
      def apply(f: File) = f.isDirectory
      def message(f: File) = s"${f.path} is not a directory"
    }

    val regularFile: FileCheck = new FileCheck {
      def apply(f: File) = f.isRegularFile
      def message(f: File) = s"${f.path} is not a regular file"
    }

    val readableFile: FileCheck = readable & regularFile

    val writeableFile: FileCheck = parentWriteable & notExists
  }

  implicit class FileExt(f: File) {

    /**
      * Throw an exception if this file does not pass the given check,
      * otherwise return this.
      */
    def assert(check: FileCheck): File = check.pass(f)

    /**
      * Split the name of the file in basename and extension.
      */
    def splitFileName: (String, String) = files.splitName(f.name)

    /**
      * Return the extension of the given file (without the dot). This
      * method works on names, i.e. it doesn't care whether this names
      * a directory, symlink or file.
      */
    def getExtension: Option[String] =
      Some(f.splitFileName._2).filter(_.nonEmpty)

    /**
      * Return the name without extension. This method works on names,
      * i.e. it doesn't care whether this names a directory, symlink
      * or file.
      */
    def getBaseName: String = splitFileName._1

    /**
      * Check whether the given path has an extension from the given
      * set. Compare case-insensitiv.
      */
    def hasExtensions(exts: Set[String]): Boolean =
      f.getExtension.map(_.toLowerCase()) match {
        case Some(ext) => exts contains ext
        case _ => exts.isEmpty
      }

    /**
      * Same as `this / fn`.
      */
    def mapPath(fn: File => File): File = fn(f)

    // better.files has this from 2.13+
    def sibling(name: String): File =
      f.path resolveSibling name

    /**
      * Resolve to a sibling by mapping the file name.
      */
    def mapFileName(fn: String => String): File =
      mapPath(p => p.sibling(fn(f.name)))

    /**
      * Resolve to a sibling by mapping the base name.
      */
    def mapBaseName(fn: String => String): File = {
      def rename(ff: File): File = {
        val (bname, ext) = ff.splitFileName
        ff.sibling(s"${fn(bname)}.$ext")
      }
      mapPath(rename)
    }

    /**
      * Resolve to a sibling by mapping the extension of. If the
      * resulting extension is empty, the extension is removed from
      * `file`.
      */
    def mapExtension(fn: String => String): File = {
      def rename(ff: File): File = {
        val (bname, ext) = ff.splitFileName
        val newExt = fn(ext)
        if (newExt.isEmpty) ff.sibling(bname)
        else ff.sibling(s"$bname.${newExt}")
      }
      mapPath(rename)
    }
  }
}
