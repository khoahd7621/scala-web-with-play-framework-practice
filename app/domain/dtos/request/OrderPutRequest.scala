package domain.dtos.request

import play.api.libs.json.{Json, OFormat}

case class OrderPutRequest(orderId: Long,
                           userId: Long,
                           orderItemsRequest: Seq[OrderItemsRequest])

object OrderPutRequest {
  implicit val format: OFormat[OrderPutRequest] =
    Json.format[OrderPutRequest]
}
