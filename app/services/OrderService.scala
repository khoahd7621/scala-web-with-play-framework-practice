package services

import com.google.inject.{ImplementedBy, Inject, Singleton}
import domain.dao.{OrderDao, OrderDetailDao}
import domain.dtos.request.{OrderPostRequest, OrderPutRequest}
import domain.dtos.response.{OrderItemsResponse, OrderResponse}
import domain.models.{Order, OrderDetail}

import java.time.LocalDateTime
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
  * OrderService is a type of an IdentityService that can be used to authenticate Orders.
  */
@ImplementedBy(classOf[OrderServiceImpl])
trait OrderService {

  /**
    * Finds an order by id.
    *
    * @param id order's id.
    * @return The found order or None if no order for the given id could be found.
    */
  def find(id: Long): Future[Option[OrderResponse]]

  /**
    * List all orders.
    *
    * @return All existing orders.
    */
  def listAll(): Future[Iterable[OrderResponse]]

  /**
    * Saves an order.
    *
    * @param order The order to save.
    * @return The saved order.
    */
  def save(order: OrderPostRequest): Future[OrderResponse]

  /**
    * Updates an order.
    *
    * @param order The order to update.
    * @return The updated Order.
    */
  def update(order: OrderPutRequest): Future[OrderResponse]

  /**
    * Deletes an order.
    *
    * @param id The Order's id to delete.
    * @return The deleted Order's id.
    */
  def delete(id: Long): Future[Int]
}

/**
  * Handles actions to Orders.
  *
  * @param orderDao The Order DAO implementation.
  * @param orderDetailDao The OrderDetail DAO implementation.
  * @param ex      The execution context.
  */
@Singleton
class OrderServiceImpl @Inject()(
  orderDao: OrderDao,
  orderDetailDao: OrderDetailDao
)(implicit ex: ExecutionContext)
    extends OrderService {

  override def find(id: Long): Future[Option[OrderResponse]] = {
    orderDao.find(id).map {
      case Some(order) => {
        val orderResponse = getOrderResponseAndItsItems(order);
        Some(orderResponse)
      }
      case None => None
    }
  }

  override def listAll(): Future[Iterable[OrderResponse]] = {
    orderDao
      .listAll()
      .map(orders => orders.map(order => getOrderResponseAndItsItems(order)))
  }

  override def save(
    orderPostRequest: OrderPostRequest
  ): Future[OrderResponse] = {
    var totalPrice: Double = 0;
    var orderDetails = orderPostRequest.orderItemsRequest.map(orderItem => {
      totalPrice = totalPrice + orderItem.price * orderItem.quantity
      OrderDetail(
        None,
        0,
        orderItem.productId,
        orderItem.quantity,
        orderItem.price
      )
    })
    val order =
      Order(None, orderPostRequest.userId, LocalDateTime.now(), totalPrice)
    val savedOrder = orderDao.save(order)
    savedOrder.map(order => {
      var orderItemsResponse = List[OrderItemsResponse]()
      orderDetails = orderDetails.map(orderDetail => {
        orderItemsResponse = orderItemsResponse.appended(
          OrderItemsResponse.fromOrderDetail(orderDetail)
        )
        orderDetail.copy(orderId = order.id.get)
      })
      orderDetailDao.saveAll(orderDetails)

      OrderResponse.fromOrder(order).copy(orderItems = orderItemsResponse)
    })
  }

  override def update(
    orderPutRequest: OrderPutRequest
  ): Future[OrderResponse] = {
    val order = Await.result(
      orderDao.find(orderPutRequest.orderId),
      scala.concurrent.duration.Duration.Inf
    )
    order match {
      case Some(order) =>
        val oldOrderDetails = Await.result(
          orderDetailDao.findAllByOrderId(order.id.get),
          scala.concurrent.duration.Duration.Inf
        )
        oldOrderDetails.foreach(
          orderDetail => orderDetailDao.delete(orderDetail.id.get)
        )
        var newTotalPrice: Double = 0;
        val newOrderDetails =
          orderPutRequest.orderItemsRequest.map(orderItem => {
            newTotalPrice = newTotalPrice + orderItem.price * orderItem.quantity
            OrderDetail(
              None,
              order.id.get,
              orderItem.productId,
              orderItem.quantity,
              orderItem.price
            )
          })
        val savedOrderDetails = Await.result(
          orderDetailDao.saveAll(newOrderDetails),
          scala.concurrent.duration.Duration.Inf
        )
        var orderItemsResponse = List[OrderItemsResponse]()
        savedOrderDetails.foreach { orderDetail =>
          orderItemsResponse = orderItemsResponse
            .appended(OrderItemsResponse.fromOrderDetail(orderDetail))
        }
        val orderNew = order.copy(totalPrice = newTotalPrice)
        orderDao.update(orderNew)
        Future.successful(
          OrderResponse
            .fromOrder(orderNew)
            .copy(orderItems = orderItemsResponse)
        )
      case None => Future.failed(new Exception("Order not found"))
    }
  }

  override def delete(id: Long): Future[Int] = {
    orderDetailDao
      .findAllByOrderId(id)
      .onComplete({
        case Success(listOrderDetails) =>
          listOrderDetails.map(orderDetail => {
            orderDetailDao.delete(orderDetail.id.get)
          })
        case Failure(exception) => throw exception
      })
    orderDao.delete(id)
  }

  private def getOrderResponseAndItsItems(order: Order): OrderResponse = {
    val listOrderDetail = Await.result(
      orderDetailDao.findAllByOrderId(order.id.get),
      scala.concurrent.duration.Duration.Inf
    )
    var orderItemsResponse = Seq[OrderItemsResponse]()
    listOrderDetail.foreach(
      orderDetail =>
        orderItemsResponse = orderItemsResponse :+ OrderItemsResponse
          .fromOrderDetail(orderDetail)
    )
    OrderResponse.fromOrder(order).copy(orderItems = orderItemsResponse)
  }
}
