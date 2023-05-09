package domain.dao

import com.google.inject.{ImplementedBy, Inject, Singleton}
import domain.models.Product
import domain.tables.ProductTable
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Future

@ImplementedBy(classOf[ProductDaoImpl])
trait ProductDao {

  /**
    * Finds a product by id.
    *
    * @param id The product id to find.
    * @return The found product or None if no product for the given id could be found.
    */
  def find(id: Long): Future[Option[Product]]

  /**
    * List all products.
    *
    * @return All existing products.
    */
  def listAll(): Future[Iterable[Product]]

  /**
    * Saves a product.
    *
    * @param product The product to save.
    * @return The saved product.
    */
  def save(product: Product): Future[Product]

  /**
    * Saves all products.
    *
    * @param list The products to save.
    * @return The saved products.
    */
  def saveAll(list: Seq[Product]): Future[Seq[Product]]

  /**
    * Updates a product.
    *
    * @param product The product to update.
    * @return The saved product.
    */
  def update(product: Product): Future[Product]

  /**
    * Deletes a product
    *
    * @param id The product's id to delete.
    * @return The deleted product's id.
    */
  def delete(id: Long): Future[Int]
}

@Singleton
class ProductDaoImpl @Inject()(daoRunner: DaoRunner)(
  implicit ec: DbExecutionContext
) extends ProductDao {

  private val products = TableQuery[ProductTable]

  override def find(id: Long): Future[Option[Product]] = daoRunner.run {
    products.filter(_.id === id).result.headOption
  }

  override def listAll(): Future[Iterable[Product]] = daoRunner.run {
    products.result
  }

  override def save(product: Product): Future[Product] = daoRunner.run {
    products returning products += product
  }

  override def saveAll(list: Seq[Product]): Future[Seq[Product]] =
    daoRunner.run {
      (products ++= list).map(_ => list)
    }

  override def update(product: Product): Future[Product] = daoRunner.run {
    products.filter(_.id === product.id).update(product).map(_ => product)
  }

  override def delete(id: Long): Future[Int] = daoRunner.run {
    products.filter(_.id === id).delete
  }
}
