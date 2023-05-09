package services

import domain.dao.ProductDao
import domain.models.Product
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class ProductServiceSpec extends PlaySpec with MockitoSugar with ScalaFutures {

  val mockProductDao: ProductDao = mock[ProductDao]

  val productService: ProductService =
    new ProductServiceImpl(mockProductDao)(global)

  private val product1 = Product(Some(1L), "Product 1", 1, LocalDateTime.now())
  private val product2 = Product(Some(2L), "Product 2", 2, LocalDateTime.now())



  "ProductService#find(id: Long)" should {

    "get a product successfully" in {
      when(mockProductDao.find(anyLong()))
        .thenReturn(Future.successful(Some(product1)))

      val result = productService.find(1L).futureValue
      result.isEmpty mustBe false
      val actual = result.get
      verifyProduct(actual, product1)
    }

    "product not found" in {
      when(mockProductDao.find(anyLong())).thenReturn(Future.successful(None))

      val result = productService.find(1L).futureValue
      result.isEmpty mustBe true
    }
  }

  "ProductService#listAll" should {

    "get all products successfully" in {
      val products = Seq(product1, product2)
      when(mockProductDao.listAll()).thenReturn(Future.successful(products))

      val result = productService.listAll().futureValue
      result.isEmpty mustBe false
      result.size mustBe 2
      result.map(_.id.get) must contain allOf (1L, 2L)
    }
  }

  "ProductService#save(product)" should {

    "save a product successfully" in {
      when(mockProductDao.save(product1))
        .thenReturn(Future.successful(product1))

      val result = productService.save(product1).futureValue
      verifyProduct(result, product1)
    }
  }

  "ProductService#update(product)" should {

    "update a product successfully" in {
      when(mockProductDao.update(product1))
        .thenReturn(Future.successful(product1))

      val result = productService.update(product1).futureValue
      verifyProduct(result, product1)
    }
  }

  "ProductService#delete(id: Long)" should {

    "delete a product successfully" in {
      when(mockProductDao.delete(1L)).thenReturn(Future.successful(1))

      val result = productService.delete(1L).futureValue
      result mustEqual 1
    }
  }

  private def verifyProduct(product: Product, expected: Product): Unit = {
    product.id.get mustEqual expected.id.get
    product.productName mustEqual expected.productName
    product.price mustEqual expected.price
  }
}
