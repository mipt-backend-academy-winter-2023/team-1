package photos

import photos.api.ApiError
import photos.repository.PhotoRepoError
import photos.utils.AuthError
import zio.http.Response

sealed trait PhotoError

case class Auth(error: AuthError) extends PhotoError
case class Repo(error: PhotoRepoError) extends PhotoError
case class Api(error: ApiError) extends PhotoError
