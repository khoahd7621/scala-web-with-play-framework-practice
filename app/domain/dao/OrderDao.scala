package domain.dao

import com.google.inject.{ImplementedBy, Inject, Singleton}
import domain.models.Order
import domain.tables.OrderTable
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Future

@ImplementedBy(classOf[OrderDaoImpl])
trait OrderDao {

  /**
    * Finds an order by id.
    *
    * @param id The order's id to find.
    * @return The found order or None if no order for the given id could be found.
    */
  def find(id: Long): Future[Option[Order]]

  /**
    * List all orders.
    *
    * @return All existing orders.
    */
  def listAll(): Future[Iterable[Order]]

  /**
    * Saves an order.
    *
    * @param order The order to save.
    * @return The saved order.
    */
  def save(order: Order): Future[Order]

  /**
    * Updates an order.
    *
    * @param order The order to update.
    * @return The saved order.
    */
  def update(order: Order): Future[Order]

  /**
    * Deletes an order
    *
    * @param id The order's id to delete.
    * @return The deleted order's id.
    */
  def delete(id: Long): Future[Int]
}

@Singleton
class OrderDaoImpl @Inject()(daoRunner: DaoRunner)(
  implicit ec: DbExecutionContext
) extends OrderDao {

  private val orders = TableQuery[OrderTable]

  override def find(id: Long): Future[Option[Order]] = daoRunner.run {
    orders.filter(_.id === id).result.headOption
  }

  override def listAll(): Future[Iterable[Order]] = daoRunner.run {
    orders.result
  }

  override def save(order: Order): Future[Order] = daoRunner.run {
    orders returning orders += order
  }

  override def update(order: Order): Future[Order] = daoRunner.run {
    orders.filter(_.id === order.id).update(order).map(_ => order)
  }

  override def delete(id: Long): Future[Int] = daoRunner.run {
    orders.filter(_.id === id).delete
  }
}
