package auth.utils

import org.mindrot.jbcrypt.BCrypt

object JBCryptUtils {
  def encodePassword(password: String): String = {
    BCrypt.hashpw(password, BCrypt.gensalt(12))
  }
}
