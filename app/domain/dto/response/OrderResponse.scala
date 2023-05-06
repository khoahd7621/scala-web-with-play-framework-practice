package domain.dto.response

import domain.models.Order
import play.api.libs.json.{Json, OFormat}

import java.time.LocalDateTime

case class OrderResponse(id: Option[Long],
                         userId: Long,
                         orderDate: LocalDateTime,
                         totalPrice: Double,
                         orderItems: Seq[OrderItemsResponse])

object OrderResponse {
  implicit val format: OFormat[OrderResponse] =
    Json.format[OrderResponse]

  def fromOrder(order: Order): OrderResponse =
    OrderResponse(
      order.id,
      order.userId,
      order.orderDate,
      order.totalPrice,
      List[OrderItemsResponse]()
    )
}
