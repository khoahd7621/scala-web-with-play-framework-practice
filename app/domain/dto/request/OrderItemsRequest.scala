package domain.dto.request

import play.api.libs.json.{Json, OFormat}

case class OrderItemsRequest(productId: Long, quantity: Int, price: Double)

object OrderItemsRequest {
  implicit val format: OFormat[OrderItemsRequest] =
    Json.format[OrderItemsRequest]
}
