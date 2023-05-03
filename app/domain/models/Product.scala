package domain.models

import play.api.libs.json.{Json, OFormat}

import java.time.LocalDateTime

case class Product(id: Option[Long],
                   productName: String,
                   price: Double,
                   expDate: LocalDateTime = LocalDateTime.now())

object Product {

  /**
    * Mapping to read/write a Product Resource out as a JSON value.
    */
  implicit val format: OFormat[Product] = Json.format[Product]
}
