package domain.models

import play.api.libs.json.{Json, OFormat}

case class OrderDetail(id: Option[Long],
                       orderId: Long,
                       productId: Long,
                       quantity: Int,
                       price: Double)

object OrderDetail {

  /**
    * Mapping to read/write a OrderDetail Resource out as a JSON value.
    */
  implicit val format: OFormat[OrderDetail] = Json.format[OrderDetail]
}
