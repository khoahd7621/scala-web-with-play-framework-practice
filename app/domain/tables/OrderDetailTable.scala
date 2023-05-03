package domain.tables

import domain.models.OrderDetail
import slick.jdbc.PostgresProfile.api._

class OrderDetailTable(tag: Tag)
    extends Table[OrderDetail](tag, Some("scala-demo"), "order_details") {

  def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc, O.Unique)

  def orderId = column[Long]("order_id")

  def productId = column[Long]("product_id")

  def quantity = column[Int]("quantity")

  def price = column[Double]("price")

  def * =
    (id, orderId, productId, quantity, price) <> ((OrderDetail.apply _).tupled, OrderDetail.unapply)
}
