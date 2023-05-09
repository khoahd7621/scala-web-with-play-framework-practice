package controllers

import com.mohiva.play.silhouette.test._
import domain.dtos.response.ProductResponse
import domain.models.Product
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.Helpers._
import play.api.test._
import utils.auth.JWTEnvironment

import java.time.LocalDateTime
import scala.concurrent.Future

class ProductControllerSpec extends ControllerFixture {

  "ProductController#getProductById(id: Long)" should {

    "get a product successfully" in {

      // mock response data
      val id = 1L
      val product: Product =
        Product(Some(id), "Product Name", 1, LocalDateTime.now())
      when(mockUserService.retrieve(identity.loginInfo))
        .thenReturn(Future.successful(Some(identity)))
      when(mockProductService.find(ArgumentMatchers.eq(id)))
        .thenReturn(Future.successful(Some(product)))

      // prepare test request
      val request: FakeRequest[AnyContentAsEmpty.type] =
        FakeRequest(GET, s"/products/$id")
          .withHeaders(HOST -> "localhost:9000")
          .withAuthenticator[JWTEnvironment](identity.loginInfo)

      // Execute test and then extract result
      val result: Future[Result] = route(app, request).get

      // verify result after test
      status(result) mustEqual OK
      val resProduct: ProductResponse =
        Json.fromJson[ProductResponse](contentAsJson(result)).get
      verifyPost(resProduct, product)
    }
  }

  // Same for remaining methods

  private def verifyPost(actual: ProductResponse, expected: Product): Unit = {
    actual.id.get mustEqual expected.id.get
    actual.productName mustEqual expected.productName
    actual.price mustEqual expected.price
  }

}
