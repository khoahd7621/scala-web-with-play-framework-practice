package domain.tables

import domain.models.Product
import slick.jdbc.PostgresProfile.api._

import java.time.LocalDateTime

class ProductTable(tag: Tag)
    extends Table[Product](tag, Some("public"), "products") {

  def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc, O.Unique)

  def productName = column[String]("product_name")

  def price = column[Double]("price")

  def expDate = column[LocalDateTime]("exp_date")

  def * =
    (id, productName, price, expDate) <> ((Product.apply _).tupled, Product.unapply)
}
