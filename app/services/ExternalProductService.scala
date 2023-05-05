package services

import com.google.inject.{ImplementedBy, Inject, Singleton}
import domain.models.Product
import httpclient.ExternalProductClient
import play.api.Configuration
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[ExternalProductServiceImpl])
trait ExternalProductService {

  /**
    * List all external products.
    *
    * @return All external products.
    */
  def listAll(): Future[Iterable[Product]]

  /**
    * Saves an external product.
    *
    * @param product The external product to save.
    * @return The saved product.
    */
  def save(product: Product): Future[Product]

}

/**
  * Handles actions to External Products.
  *
  * @param client  The HTTP Client instance
  * @param ex      The execution context.
  */
@Singleton
class ExternalProductServiceImpl @Inject()(
  client: ExternalProductClient,
  config: Configuration
)(implicit ex: ExecutionContext)
    extends ExternalProductService {

  def getAllProducts: String =
    config.get[String]("external.products.getAllProducts")

  def createProduct: String =
    config.get[String]("external.products.createProduct")

  override def listAll(): Future[Iterable[Product]] =
    client.get[Seq[Product]](getAllProducts)

  override def save(Product: Product): Future[Product] =
    client.post[Product](createProduct, Some(Json.toJson(Product)))

}
