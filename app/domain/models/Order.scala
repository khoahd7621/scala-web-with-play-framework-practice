package domain.models

import play.api.libs.json.{Json, OFormat}

import java.time.LocalDateTime

case class Order(id: Option[Long],
                 userId: Long,
                 orderDate: LocalDateTime = LocalDateTime.now(),
                 totalPrice: Double)

object Order {

  /**
    * Mapping to read/write a Order Resource out as a JSON value.
    */
  implicit val format: OFormat[Order] = Json.format[Order]
}
