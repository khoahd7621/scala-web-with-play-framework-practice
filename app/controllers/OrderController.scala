package controllers

import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.actions.SecuredActionBuilder
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import domain.dtos.request.{
  OrderItemsRequest,
  OrderPostRequest,
  OrderPutRequest
}
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats.doubleFormat
import play.api.libs.json.{JsString, Json}
import play.api.mvc._
import services.OrderService
import utils.auth.{JWTEnvironment, WithRole}
import utils.logging.RequestMarkerContext

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class OrderController @Inject()(
  cc: ControllerComponents,
  orderService: OrderService,
  silhouette: Silhouette[JWTEnvironment]
)(implicit ec: ExecutionContext)
    extends AbstractController(cc)
    with RequestMarkerContext {

  def SecuredAction: SecuredActionBuilder[JWTEnvironment, AnyContent] =
    silhouette.SecuredAction

  private val logger = Logger(getClass)

  def getAllOrders: Action[AnyContent] =
    SecuredAction(WithRole[JWTAuthenticator]("Admin"))
      .async { implicit request =>
        logger.trace("get all orders")
        orderService.listAll().map { orders =>
          Ok(Json.toJson(orders))
        }
      }

  def getOrderById(id: Long): Action[AnyContent] =
    SecuredAction(WithRole[JWTAuthenticator]("Admin", "User"))
      .async { implicit request =>
        logger.trace(s"get an order by id: $id")
        orderService.find(id).map {
          case Some(order) => {
            request.identity.role match {
              case "Admin" => Ok(Json.toJson(order))
              case "User" => {
                if (order.userId == request.identity.id.get) {
                  Ok(Json.toJson(order))
                } else {
                  Forbidden
                }
              }
              case _ => Forbidden
            }
          }
          case None => NotFound
        }
      }

  def createOrder: Action[AnyContent] =
    SecuredAction(WithRole[JWTAuthenticator]("Admin", "User")).async {
      implicit request =>
        {
          logger.trace("create new order")

          request.identity.role match {
            case "Admin" => processJsonPost(None)
            case "User"  => processJsonPost(request.identity.id)
            case _       => Future.successful(Forbidden)
          }
        }
    }

  def updateOrder(id: Long): Action[AnyContent] =
    SecuredAction(WithRole[JWTAuthenticator]("Admin", "User")).async {
      implicit request =>
        {
          logger.trace("update an order")

          request.identity.role match {
            case "Admin" => processJsonPut()
            case "User" =>
              orderService.find(id).flatMap {
                case Some(order) => {
                  if (order.userId != request.identity.id.get) {
                    Future.successful(Forbidden)
                  } else {
                    processJsonPut()
                  }
                }
                case None => Future.successful(NotFound)
              }
            case _ => Future.successful(Forbidden)
          }
        }
    }

  def deleteOrder(id: Long): Action[AnyContent] =
    SecuredAction(WithRole[JWTAuthenticator]("Admin", "User")).async {
      implicit request =>
        logger.trace(s"delete order with: id = $id")

        request.identity.role match {
          case "Admin" =>
            orderService.delete(id).map { deletedCnt =>
              if (deletedCnt == 1)
                Ok(JsString(s"delete order with id: $id successfully"))
              else BadRequest(JsString(s"unable to delete order with $id"))
            }
          case "User" =>
            orderService.find(id).map {
              case Some(order) =>
                if (order.userId == request.identity.id.get) {
                  orderService.delete(id)
                  Ok(JsString(s"delete order with id: $id successfully"))
                } else {
                  Forbidden
                }
              case None => NotFound
            }
        }
    }

  private val orderItemsMapping = mapping(
    "productId" -> longNumber,
    "quantity" -> number,
    "price" -> of(doubleFormat)
  )(OrderItemsRequest.apply)(OrderItemsRequest.unapply)

  private val formPost: Form[OrderPostRequest] = Form(
    mapping(
      "userId" -> longNumber,
      "orderItemsRequest" -> seq(orderItemsMapping)
    )(OrderPostRequest.apply)(OrderPostRequest.unapply)
  )

  private def processJsonPost[A](
    userId: Option[Long]
  )(implicit request: Request[A]): Future[Result] = {

    def failure(badForm: Form[OrderPostRequest]) = {
      Future.successful(BadRequest(JsString("invalid input")))
    }

    def success(orderPostRequest: OrderPostRequest) = userId match {
      case None =>
        orderService.save(orderPostRequest).map { order =>
          Created(Json.toJson(order))
        }
      case Some(userId) =>
        orderService.save(orderPostRequest.copy(userId = userId)).map { order =>
          Created(Json.toJson(order))
        }
    }

    formPost.bindFromRequest().fold(failure, success)
  }

  private val formPut: Form[OrderPutRequest] = Form(
    mapping(
      "id" -> longNumber,
      "userId" -> longNumber,
      "orderItemsRequest" -> seq(orderItemsMapping)
    )(OrderPutRequest.apply)(OrderPutRequest.unapply)
  )

  private def processJsonPut[A]()(
    implicit request: Request[A]
  ): Future[Result] = {

    def failure(badForm: Form[OrderPutRequest]) = {
      Future.successful(BadRequest(JsString("invalid input")))
    }

    def success(orderPutRequest: OrderPutRequest) =
      orderService.update(orderPutRequest).map { order =>
        Created(Json.toJson(order))
      }

    formPut.bindFromRequest().fold(failure, success)
  }
}
