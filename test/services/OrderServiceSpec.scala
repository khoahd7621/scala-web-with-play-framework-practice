package services

import domain.dao.{OrderDao, OrderDetailDao}
import domain.dtos.request.{
  OrderItemsRequest,
  OrderPostRequest,
  OrderPutRequest
}
import domain.dtos.response.{OrderItemsResponse, OrderResponse}
import domain.models.{Order, OrderDetail}
import fixtures.AbstractPersistenceTests
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class OrderServiceSpec
    extends PlaySpec
    with MockitoSugar
    with AbstractPersistenceTests
    with ScalaFutures {
  val mockOrderDao: OrderDao = mock[OrderDao]
  val mockOrderDetailDao: OrderDetailDao = mock[OrderDetailDao]

  val orderService: OrderService =
    new OrderServiceImpl(mockOrderDao, mockOrderDetailDao)(global)

  val order: Order = Order(Some(1L), 1L, LocalDateTime.now(), 5980000)
  val orderDetail: OrderDetail = OrderDetail(Some(1L), 1L, 1L, 2, 2990000)
  val orderResponse: OrderResponse = OrderResponse(
    Some(1L),
    1L,
    LocalDateTime.now(),
    5980000,
    Seq(OrderItemsResponse(1L, 2, 2990000))
  )
  val orderPostRequest: OrderPostRequest =
    OrderPostRequest(1L, Seq(OrderItemsRequest(1L, 2, 2990000)))
  val orderPutRequest: OrderPutRequest =
    OrderPutRequest(1L, 1L, Seq(OrderItemsRequest(1L, 2, 2990000)))

  override protected def beforeAll(): Unit = {
    when(mockOrderDetailDao.findAllByOrderId(anyLong()))
      .thenReturn(Future.successful(Seq(orderDetail)))
  }

  "OrderService#find(id: Long)" should {

    "get an order successfully" in {
      when(mockOrderDao.find(anyLong()))
        .thenReturn(Future.successful(Some(order)))

      val result = orderService.find(1L).futureValue
      result.isEmpty mustBe false
      val actual = result.get
      verifyOrderResponse(actual, orderResponse)
    }

    "order not found" in {
      when(mockOrderDao.find(anyLong())).thenReturn(Future.successful(None))

      val result = orderService.find(1L).futureValue
      result.isEmpty mustBe true
    }
  }

  "OrderService#listAll" should {

    "get all orders successfully" in {
      when(mockOrderDao.listAll()).thenReturn(Future.successful(Seq(order)))

      val result = orderService.listAll().futureValue
      result.isEmpty mustBe false
      result.size mustBe 1
      result.map(
        orderResponseActual =>
          verifyOrderResponse(orderResponseActual, orderResponse)
      )
    }
  }

  "OrderService#save(orderPostRequest)" should {

    "save an order successfully" in {
      val orderCapture = ArgumentCaptor.forClass(classOf[Order])
      val seqOrderDetailsCapture =
        ArgumentCaptor.forClass(classOf[Seq[OrderDetail]])

      when(mockOrderDao.save(orderCapture.capture()))
        .thenReturn(Future.successful(order))
      when(mockOrderDetailDao.saveAll(seqOrderDetailsCapture.capture()))
        .thenReturn(Future.successful(Seq(orderDetail)))

      val result = orderService.save(orderPostRequest).futureValue

      verify(mockOrderDao).save(orderCapture.capture())
      verify(mockOrderDetailDao).saveAll(seqOrderDetailsCapture.capture())
      verifyOrderResponse(result, orderResponse)
    }
  }

  "OrderService#update(orderPutRequest)" should {

    "update an order successfully" in {
      val orderCapture = ArgumentCaptor.forClass(classOf[Order])
      val seqOrderDetailsCapture =
        ArgumentCaptor.forClass(classOf[Seq[OrderDetail]])

      when(mockOrderDao.find(anyLong()))
        .thenReturn(Future.successful(Some(order)))
      when(mockOrderDetailDao.findAllByOrderId(anyLong()))
        .thenReturn(Future.successful(Seq(orderDetail)))
      when(mockOrderDetailDao.delete(anyLong()))
        .thenReturn(Future.successful(1))
      when(mockOrderDetailDao.saveAll(seqOrderDetailsCapture.capture()))
        .thenReturn(Future.successful(Seq(orderDetail)))
      when(mockOrderDao.save(orderCapture.capture()))
        .thenReturn(Future.successful(order))

      val result = orderService.update(orderPutRequest).futureValue

      verify(mockOrderDetailDao, times(2))
        .saveAll(seqOrderDetailsCapture.capture())
      verify(mockOrderDao).save(orderCapture.capture())
      verifyOrderResponse(result, orderResponse)
    }
  }

  "OrderService#delete(id: Long)" should {

    "delete an order successfully" in {
      when(
        mockOrderDetailDao
          .findAllByOrderId(anyLong())
      ).thenReturn(Future.successful(Seq(orderDetail)))
      when(mockOrderDetailDao.delete(anyLong()))
        .thenReturn(Future.successful(1))
      when(mockOrderDao.delete(anyLong())).thenReturn(Future.successful(1))

      val result = orderService.delete(1L).futureValue
      result mustEqual 1
    }
  }

  private def verifyOrderResponse(actual: OrderResponse,
                                  expected: OrderResponse): Unit = {
    actual.id mustBe expected.id
    actual.userId mustBe expected.userId
    actual.totalPrice mustBe expected.totalPrice
    actual.orderItems.size mustBe expected.orderItems.size
    actual.orderItems.map(_.productId) must contain allElementsOf expected.orderItems
      .map(_.productId)
    actual.orderItems.map(_.quantity) must contain allElementsOf expected.orderItems
      .map(_.quantity)
    actual.orderItems.map(_.price) must contain allElementsOf expected.orderItems
      .map(_.price)
  }
}
