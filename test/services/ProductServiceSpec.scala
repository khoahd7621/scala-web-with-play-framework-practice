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

  "ProductService#find(id: Long)" should {
    "get a product successfully" in {
      val product = Product(Some(2L), "Product 2", 2, LocalDateTime.now())
      when(mockProductDao.find(anyLong()))
        .thenReturn(Future.successful(Some(product)))

      val result = productService.find(2L).futureValue
      result.isEmpty mustBe false
      val actual = result.get
      actual.id.get mustEqual product.id.get
      actual.productName mustEqual product.productName
      actual.price mustEqual product.price
    }

    "product not found" in {
      when(mockProductDao.find(anyLong())).thenReturn(Future.successful(None))

      val result = productService.find(1L).futureValue
      result.isEmpty mustBe true
    }
  }

  // Same for remaining methods
}
