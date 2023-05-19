package auth.utils

object PasswordEncoder {
  def encode(password: String): String = {
    password.hashCode.toString
  }
}
