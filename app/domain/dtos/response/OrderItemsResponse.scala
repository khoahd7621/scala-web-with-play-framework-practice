package domain.dtos.response

import domain.models.OrderDetail
import play.api.libs.json.{Json, OFormat}

case class OrderItemsResponse(productId: Long, quantity: Int, price: Double)

object OrderItemsResponse {
  implicit val format: OFormat[OrderItemsResponse] =
    Json.format[OrderItemsResponse]

  def fromOrderDetail(orderDetail: OrderDetail): OrderItemsResponse =
    OrderItemsResponse(
      orderDetail.productId,
      orderDetail.quantity,
      orderDetail.price
    )
}
