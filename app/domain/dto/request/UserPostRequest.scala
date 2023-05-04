package domain.dto.request

import play.api.libs.json.{Json, OFormat}

import java.time.LocalDateTime

case class UserPostRequest(email: String,
                           firstName: String,
                           lastName: String,
                           role: String,
                           address: String,
                           phoneNumber: String,
                           birthDate: LocalDateTime,
                           password: String)

object UserPostRequest {
  implicit val format: OFormat[UserPostRequest] = Json.format[UserPostRequest]
}
