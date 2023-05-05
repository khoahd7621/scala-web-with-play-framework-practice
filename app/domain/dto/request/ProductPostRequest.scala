package domain.dto.request

import play.api.libs.json.{Json, OFormat}

import java.time.LocalDateTime

case class ProductPostRequest(productName: String,
                              price: Double,
                              expDate: LocalDateTime)

object ProductPostRequest {
  implicit val format: OFormat[ProductPostRequest] =
    Json.format[ProductPostRequest]
}
