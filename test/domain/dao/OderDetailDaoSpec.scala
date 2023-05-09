package domain.dao

import domain.models.OrderDetail
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar

class OderDetailDaoSpec
    extends AbstractDaoTest
    with MockitoSugar
    with ScalaFutures {
  val orderDetailDao: OrderDetailDao = get[OrderDetailDao]

  var orderDetail1: OrderDetail = _
  var orderDetail2: OrderDetail = _

  override protected def beforeAll(): Unit = {
    orderDetail1 = orderDetailDao.find(1L).futureValue.get
    orderDetail2 = orderDetailDao.find(2L).futureValue.get
  }

  "OrderDetailDao#find(id: Long)" should {

    "get an order detail successfully" in {
      val result = orderDetailDao.find(1L).futureValue
      result.isEmpty mustBe false
      val orderDetail = result.get
      verifyOrderDetail(orderDetail, orderDetail1)
    }

    "order detail not found" in {
      val result = orderDetailDao.find(3L).futureValue
      result.isEmpty mustBe true
    }
  }

  "OrderDetailDao#listAll" should {

    "get all order details successfully" in {
      val result = orderDetailDao.listAll().futureValue
      result.size mustBe 2
      result.map(_.id.get) must contain allOf (1L, 2L)
    }
  }

  "OrderDetailDao#save(orderDetail)" should {

    "save an order detail successfully" in {
      val orderDetail3 = OrderDetail(Some(3L), 1, 2, 1, 2990000)
      orderDetailDao.save(orderDetail3).futureValue

      val result = orderDetailDao.find(3L).futureValue
      result.isEmpty mustBe false
      val orderDetail = result.get
      verifyOrderDetail(orderDetail, orderDetail3)
    }
  }

  "OrderDetailDao#update(orderDetail)" should {

    "update an order detail successfully" in {
      val orderDetailUpdate =
        OrderDetail(Some(3L), 1, 2, 2, 2990000)
      orderDetailDao.update(orderDetailUpdate).futureValue

      val result = orderDetailDao.find(3L).futureValue
      result.isEmpty mustBe false
      val orderDetail = result.get
      verifyOrderDetail(orderDetail, orderDetailUpdate)
    }
  }

  "OrderDetailDao#delete(id: Long)" should {

    "delete an order detail successfully" in {
      orderDetailDao.delete(3L).futureValue

      val result = orderDetailDao.find(3L).futureValue
      result.isEmpty mustBe true

      val resultAll = orderDetailDao.listAll().futureValue
      resultAll.size mustBe 2
      resultAll.map(_.id.get) must contain allOf (1L, 2L)
    }
  }

  private def verifyOrderDetail(actual: OrderDetail,
                                expected: OrderDetail): Unit = {
    actual.id.get mustEqual expected.id.get
    actual.orderId mustEqual expected.orderId
    actual.productId mustEqual expected.productId
    actual.quantity mustEqual expected.quantity
    actual.price mustEqual expected.price
  }
}
