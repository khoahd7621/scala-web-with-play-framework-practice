package domain.dao

import domain.models.Order
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar

import java.time.LocalDateTime

class OrderDaoSpec extends AbstractDaoTest with MockitoSugar with ScalaFutures {
  val orderDao: OrderDao = get[OrderDao]

  var order1: Order = _

  override protected def beforeAll(): Unit = {
    order1 = orderDao.find(1).futureValue.get
  }

  "OrderDao#find(id: Long)" should {

    "get an order successfully" in {
      val result = orderDao.find(1L).futureValue
      result.isEmpty mustBe false
      val order = result.get
      verifyOrder(order, order1)
    }

    "order not found" in {
      val result = orderDao.find(2L).futureValue
      result.isEmpty mustBe true
    }
  }

  "OrderDao#listAll" should {

    "get all orders successfully" in {
      val result = orderDao.listAll().futureValue
      result.size mustBe 1
      result.map(_.id.get) must contain(1L)
    }
  }

  "OrderDao#save(order)" should {

    "save an order successfully" in {
      val order2 = Order(Some(2L), 1L, LocalDateTime.now(), 1)
      orderDao.save(order2).futureValue

      val result = orderDao.find(2L).futureValue
      result.isEmpty mustBe false
      val order = result.get
      verifyOrder(order, order2)
    }
  }

  "OrderDao#update(order)" should {

    "update an order successfully" in {
      val orderUpdate =
        Order(Some(1L), 1L, LocalDateTime.now(), 1)
      orderDao.update(orderUpdate).futureValue

      val result = orderDao.find(1L).futureValue
      result.isEmpty mustBe false
      val order = result.get
      verifyOrder(order, orderUpdate)
    }
  }

  "OrderDao#delete(id: Long)" should {

    "delete an order successfully" in {
      orderDao.delete(2L).futureValue

      val result = orderDao.find(2L).futureValue
      result.isEmpty mustBe true

      val resultAll = orderDao.listAll().futureValue
      resultAll.size mustBe 1
      resultAll.map(_.id.get) must contain(1L)
    }
  }

  private def verifyOrder(actual: Order, expected: Order): Unit = {
    actual.id.get mustEqual expected.id.get
    actual.userId mustEqual expected.userId
    actual.totalPrice mustEqual expected.totalPrice
  }
}
