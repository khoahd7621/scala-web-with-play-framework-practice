package domain.tables

import domain.models.Order
import slick.jdbc.PostgresProfile.api._

import java.time.LocalDateTime

class OrderTable(tag: Tag) extends Table[Order](tag, Some("public"), "orders") {

  def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc, O.Unique)

  def userId = column[Long]("user_id")

  def orderDate = column[LocalDateTime]("order_date")

  def totalPrice = column[Double]("total_price")

  def * =
    (id, userId, orderDate, totalPrice) <> ((Order.apply _).tupled, Order.unapply)
}
