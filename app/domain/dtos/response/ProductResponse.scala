package domain.dtos.response

import domain.models.Product
import play.api.libs.json.{Json, OFormat}

import java.time.LocalDateTime

case class ProductResponse(id: Option[Long],
                           productName: String,
                           price: Double,
                           expDate: LocalDateTime)

object ProductResponse {

  /**
    * Mapping to read/write a PostResource out as a JSON value.
    */
  implicit val format: OFormat[ProductResponse] = Json.format[ProductResponse]

  def fromProduct(product: Product): ProductResponse =
    ProductResponse(
      product.id,
      product.productName,
      product.price,
      product.expDate
    )
}
