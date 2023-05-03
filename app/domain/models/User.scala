package domain.models

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

object User {

  /**
    * Mapping to read/write a User Resource out as a JSON value.
    */
  implicit val format: OFormat[User] = Json.format[User]
}
