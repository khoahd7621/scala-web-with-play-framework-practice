package domain.models

import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.api.{Identity, LoginInfo}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import com.mohiva.play.silhouette.password.BCryptSha256PasswordHasher
import domain.dtos.request.UserPostRequest
import play.api.libs.json.{Json, OFormat}

import java.time.LocalDateTime

case class User(id: Option[Long],
                email: String,
                firstName: String,
                lastName: String,
                role: String,
                address: String,
                phoneNumber: String,
                birthDate: LocalDateTime = LocalDateTime.now(),
                password: Option[String] = None)
    extends Identity {

  /**
    * Generates login info from email
    *
    * @return login info
    */
  def loginInfo: LoginInfo = LoginInfo(CredentialsProvider.ID, email)

  /**
    * Generates password info from password.
    *
    * @return password info
    */
  def passwordInfo: PasswordInfo =
    PasswordInfo(BCryptSha256PasswordHasher.ID, password.get)
}

object User {

  /**
    * Mapping to read/write a User Resource out as a JSON value.
    */
  implicit val format: OFormat[User] = Json.format[User]

  def fromUserPostRequest(userPostRequest: UserPostRequest): User =
    User(
      None,
      userPostRequest.email,
      userPostRequest.firstName,
      userPostRequest.lastName,
      userPostRequest.role,
      userPostRequest.address,
      userPostRequest.phoneNumber,
      userPostRequest.birthDate,
      Some(userPostRequest.password)
    )
}
