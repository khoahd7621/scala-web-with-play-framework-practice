package controllers

import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.actions.{
  SecuredActionBuilder,
  UnsecuredActionBuilder
}
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import domain.dto.request.ProductPostRequest
import domain.dto.response.ProductResponse
import domain.models.Product
import httpclient.ExternalServiceException
import play.api.Logger
import play.api.data.Form
import play.api.data.format.Formats.doubleFormat
import play.api.libs.json.{JsString, Json}
import play.api.mvc._
import services.{ExternalProductService, ProductService}
import utils.auth.{JWTEnvironment, WithRole}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class ProductController @Inject()(
  cc: ControllerComponents,
  productService: ProductService,
  externalProductService: ExternalProductService,
  silhouette: Silhouette[JWTEnvironment]
)(implicit ec: ExecutionContext)
    extends AbstractController(cc) {

  def SecuredAction: SecuredActionBuilder[JWTEnvironment, AnyContent] =
    silhouette.SecuredAction

  def UnsecuredAction: UnsecuredActionBuilder[JWTEnvironment, AnyContent] =
    silhouette.UnsecuredAction

  private val logger = Logger(getClass)

  def getAllProducts: Action[AnyContent] =
    SecuredAction(WithRole[JWTAuthenticator]("User", "Admin", "Operator"))
      .async { implicit request =>
        logger.trace("get all products")
        productService.listAll().map { products =>
          Ok(Json.toJson(products))
        }
      }

  def createProduct: Action[AnyContent] =
    SecuredAction(WithRole[JWTAuthenticator]("Admin", "Operator")).async {
      implicit request =>
        {
          logger.trace("create new product")
          processJsonPost(None)
        }
    }

  def updateProduct(id: Long): Action[AnyContent] =
    SecuredAction(WithRole[JWTAuthenticator]("Admin", "Operator")).async {
      implicit request =>
        {
          logger.trace(s"update product with id: $id")
          processJsonPost(Some(id))
        }
    }

  def deleteProduct(id: Long): Action[AnyContent] =
    SecuredAction(WithRole[JWTAuthenticator]("Admin", "Operator")).async {
      implicit request =>
        logger.trace(s"delete product with: id = $id")
        productService.delete(id).map { deletedCnt =>
          if (deletedCnt == 1)
            Ok(JsString(s"delete product with id: $id successfully"))
          else BadRequest(JsString(s"unable to delete product with $id"))
        }
    }

  def getProductById(id: Long): Action[AnyContent] =
    SecuredAction(WithRole[JWTAuthenticator]("Admin", "Operator"))
      .async { implicit request =>
        logger.trace(s"get a product by id: $id")
        productService.find(id).map {
          case Some(product) => Ok(Json.toJson(product))
          case None          => NotFound
        }
      }

  def getAllExternalProducts: Action[AnyContent] =
    SecuredAction(WithRole[JWTAuthenticator]("Admin", "Operator")).async {
      implicit request =>
        logger.trace("get all external products")

        // try/catch Future exception with transform
        externalProductService.listAll().transform {
          case Failure(exception) => handleExternalError(exception)
          case Success(products) =>
            Try(
              Ok(
                Json.toJson(
                  products.map(product => ProductResponse.fromProduct(product))
                )
              )
            )
        }
    }

  private def handleExternalError(throwable: Throwable): Try[Result] = {
    throwable match {
      case ese: ExternalServiceException =>
        logger.trace(s"An ExternalServiceException occurred: ${ese.getMessage}")
        if (ese.error.isEmpty)
          Try(
            BadRequest(
              JsString(
                s"An ExternalServiceException occurred. statusCode: ${ese.statusCode}"
              )
            )
          )
        else Try(BadRequest(Json.toJson(ese.error.get)))
      case _ =>
        logger.trace(
          s"An other exception occurred on getAllExternal: ${throwable.getMessage}"
        )
        Try(BadRequest(JsString("Unable to create an external product")))
    }
  }

  private val form: Form[ProductPostRequest] = {
    import play.api.data.Forms._
    Form(
      mapping(
        "productName" -> nonEmptyText(maxLength = 100),
        "price" -> of(doubleFormat),
        "expDate" -> localDateTime("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
      )(ProductPostRequest.apply)(ProductPostRequest.unapply)
    )
  }

  private def processJsonPost[A](
    id: Option[Long]
  )(implicit request: Request[A]): Future[Result] = {

    def failure(badForm: Form[ProductPostRequest]) = {
      Future.successful(BadRequest(JsString("invalid input")))
    }

    def success(productPostRequest: ProductPostRequest) = {
      id match {
        case None =>
          productService
            .save(Product.fromUserPostRequest(productPostRequest))
            .map(p => Ok(Json.toJson(p)))
        case Some(_) => {
          productService.find(id.get).flatMap {
            case Some(product) =>
              logger.trace(s"update product with id: $id")
              val updatedProduct = product.copy(
                productName = productPostRequest.productName,
                price = productPostRequest.price,
                expDate = productPostRequest.expDate
              )
              productService.update(updatedProduct).map { p =>
                Ok(Json.toJson(ProductResponse.fromProduct(p)))
              }
            case _ =>
              logger.trace(s"product with id: $id not found")
              Future.successful(NotFound)
          }
        }
      }
    }

    form.bindFromRequest().fold(failure, success)
  }
}
