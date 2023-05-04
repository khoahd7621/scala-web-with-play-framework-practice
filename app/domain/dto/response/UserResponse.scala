package domain.dto.response

import domain.models.User
import play.api.libs.json.{Json, OFormat}

import java.time.LocalDateTime

case class UserResponse(id: Option[Long],
                        email: String,
                        firstName: String,
                        lastName: String,
                        role: String,
                        address: String,
                        phoneNumber: String,
                        birthDate: LocalDateTime)

object UserResponse {

  /**
    * Mapping to read/write a PostResource out as a JSON value.
    */
  implicit val format: OFormat[UserResponse] = Json.format[UserResponse]

  def fromUser(user: User): UserResponse =
    UserResponse(
      user.id,
      user.email,
      user.firstName,
      user.lastName,
      user.role,
      user.address,
      user.phoneNumber,
      user.birthDate
    )
}
