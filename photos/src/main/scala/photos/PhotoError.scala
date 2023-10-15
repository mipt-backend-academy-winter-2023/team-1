package photos

import photos.repository.S3Error
import photos.utils.AuthError

sealed trait PhotoError

case class Auth(error: AuthError) extends PhotoError
case class S3(error: S3Error) extends PhotoError
