package domain.dao

import domain.models.Product
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar

import java.time.LocalDateTime

class ProductDaoSpec
    extends AbstractDaoTest
    with MockitoSugar
    with ScalaFutures {

  val productDao: ProductDao = get[ProductDao]

  var product1: Product = _
  var product2: Product = _
  var product3: Product = _
  var product4: Product = _

  override protected def beforeAll(): Unit = {
    // Get data from DB
    product1 = productDao.find(1).futureValue.get // id = 1
    product2 = productDao.find(2).futureValue.get // id = 2
    product3 = productDao.find(3).futureValue.get // id = 3
    product4 = productDao.find(4).futureValue.get // id = 4
  }

  "ProductDao#find(id: Long)" should {

    "get a product successfully" in {
      val result = productDao.find(1L).futureValue
      result.isEmpty mustBe false
      val product = result.get
      product.id.get mustEqual 1L
      product.productName mustEqual product1.productName
      product.price mustEqual product1.price
    }

    "product not found" in {
      val result = productDao.find(6L).futureValue
      result.isEmpty mustBe true
    }
  }

  "ProductDao#listAll" should {

    "get all products successfully" in {
      val result = productDao.listAll().futureValue
      result.size mustBe 5
      result.map(_.id.get) must contain allOf (1L, 2L, 3L, 4L, 5L)
    }
  }

  "ProductDao#save(product)" should {

    "save a product successfully" in {
      val product6 = Product(Some(6), "Product 6", 1, LocalDateTime.now())
      productDao.save(product6).futureValue

      val result = productDao.find(6L).futureValue
      result.isEmpty mustBe false
      val product = result.get
      product.id.get mustEqual 6L
      product.productName mustEqual product6.productName
      product.price mustEqual product6.price
    }
  }

  "ProductDao#update(product)" should {

    "update a product successfully" in {
      val productUpdate =
        Product(Some(2L), "Product 2 Update", 2, LocalDateTime.now())
      productDao.update(productUpdate).futureValue

      val result = productDao.find(2L).futureValue
      result.isEmpty mustBe false
      val product = result.get
      product.id.get mustEqual productUpdate.id.get
      product.productName mustEqual productUpdate.productName
      product.price mustEqual productUpdate.price
    }
  }

  "ProductDao#delete(id: Long)" should {

    "delete a product successfully" in {
      productDao.delete(3L).futureValue

      val result = productDao.find(3L).futureValue
      result.isEmpty mustBe true // product is no longer exists.

      val resultAll = productDao.listAll().futureValue
      resultAll.size mustBe 5
      resultAll.map(_.id.get) must contain allOf (1L, 2L, 4L, 5L, 6L) // product 6 is inserted in the above test
    }
  }
}
