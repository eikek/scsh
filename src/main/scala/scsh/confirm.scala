package scsh

object confirm {

  def yesOrNo(prompt: String): Boolean = {
    print(prompt + " (y/n)? ")
    val a = System.in.read()
    a == 'y' || a == 'Y'
  }

  def continue(): Boolean = yesOrNo("Continue")

}
