package domain.dto.request

import play.api.libs.json.{Json, OFormat}

case class LoginPostRequest(email: String, password: String)

object LoginPostRequest {
  implicit val format: OFormat[LoginPostRequest] = Json.format[LoginPostRequest]
}
