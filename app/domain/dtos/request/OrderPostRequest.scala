package domain.dtos.request

import play.api.libs.json.{Json, OFormat}

case class OrderPostRequest(userId: Long,
                            orderItemsRequest: Seq[OrderItemsRequest])

object OrderPostRequest {
  implicit val format: OFormat[OrderPostRequest] =
    Json.format[OrderPostRequest]
}
