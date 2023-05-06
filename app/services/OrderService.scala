package services

import com.google.inject.{ImplementedBy, Inject, Singleton}
import domain.dao.{OrderDao, OrderDetailDao}
import domain.dto.request.{OrderPostRequest, OrderPutRequest}
import domain.dto.response.{OrderItemsResponse, OrderResponse}
import domain.models.{Order, OrderDetail}

import java.time.LocalDateTime
import scala.concurrent.{Await, ExecutionContext, Future}

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
  def update(order: OrderPutRequest,
             userId: Option[Long]): Future[OrderResponse]

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
      case Some(order) => Some(getOrderItemsOfOrder(order))
      case None        => None
    }
  }

  override def listAll(): Future[Iterable[OrderResponse]] = {
    orderDao.listAll().map(_.map(order => getOrderItemsOfOrder(order)))
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

  override def update(order: OrderPutRequest,
                      userId: Option[Long]): Future[OrderResponse] = ???

  override def delete(id: Long): Future[Int] = ???

  private def getOrderItemsOfOrder(order: Order): OrderResponse = {
    val orderDetails = orderDetailDao.findByOrderId(order.id.get)
    val orderItemsResponse = List[OrderItemsResponse]()
    orderDetails.map(orderDetail => {
      orderItemsResponse.appended(
        OrderItemsResponse.fromOrderDetail(orderDetail.get)
      )
    })

    OrderResponse.fromOrder(order).copy(orderItems = orderItemsResponse)
  }
}
