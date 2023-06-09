package controllers

import com.mohiva.play.silhouette.test._
import domain.dtos.response.ProductResponse
import domain.models.Product
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.Helpers._
import play.api.test._
import utils.auth.JWTEnvironment

import java.time.LocalDateTime
import scala.concurrent.Future

class ProductControllerSpec extends ControllerFixture {

  val host = "localhost:9000"
  val baseUri = "/products"

  val id = 1L
  val product1: Product =
    Product(Some(id), "Product Name", 1, LocalDateTime.now())
  val product2: Product =
    Product(Some(2L), "Product Name 1", 2, LocalDateTime.now())

  "ProductController#getProductById(id: Long)" should {

    "get a product successfully" in {
      // mock response data
      when(mockUserService.retrieve(identity.loginInfo))
        .thenReturn(Future.successful(Some(identity)))
      when(mockProductService.find(ArgumentMatchers.eq(id)))
        .thenReturn(Future.successful(Some(product1)))

      // prepare test request
      val request: FakeRequest[AnyContentAsEmpty.type] =
        FakeRequest(GET, s"$baseUri/$id")
          .withHeaders(HOST -> host)
          .withAuthenticator[JWTEnvironment](identity.loginInfo)

      // Execute test and then extract result
      val result: Future[Result] = route(app, request).get

      // verify result after test
      status(result) mustEqual OK
      val resProduct: ProductResponse =
        Json.fromJson[ProductResponse](contentAsJson(result)).get
      verifyProduct(resProduct, product1)
    }

    "product not found" in {
      // mock response data
      when(mockUserService.retrieve(identity.loginInfo))
        .thenReturn(Future.successful(Some(identity)))
      when(mockProductService.find(ArgumentMatchers.eq(1L)))
        .thenReturn(Future.successful(None))

      // prepare test request
      val request: FakeRequest[AnyContentAsEmpty.type] =
        FakeRequest(GET, s"$baseUri/$id")
          .withHeaders(HOST -> host)
          .withAuthenticator[JWTEnvironment](identity.loginInfo)

      // Execute test and then extract result
      val result: Future[Result] = route(app, request).get

      // verify result after test
      status(result) mustEqual NOT_FOUND
    }
  }

  "ProductController#getAllProducts" should {

    "get all products successfully" in {
      // mock response data
      when(mockUserService.retrieve(identity.loginInfo))
        .thenReturn(Future.successful(Some(identity)))
      when(mockProductService.listAll())
        .thenReturn(Future.successful(Seq(product1, product2)))

      // prepare test request
      val request: FakeRequest[AnyContentAsEmpty.type] =
        FakeRequest(GET, baseUri)
          .withHeaders(HOST -> host)
          .withAuthenticator[JWTEnvironment](identity.loginInfo)

      // Execute test and then extract result
      val result: Future[Result] = route(app, request).get

      // verify result after test
      status(result) mustEqual OK
      val resProducts: Seq[ProductResponse] =
        Json.fromJson[Seq[ProductResponse]](contentAsJson(result)).get
      resProducts.size mustEqual 2
    }
  }

  "ProductController#createProduct" should {

    "create a product successfully" in {
      // mock response data
      when(mockUserService.retrieve(identity.loginInfo))
        .thenReturn(Future.successful(Some(identity)))
      when(mockProductService.save(any(classOf[Product])))
        .thenReturn(Future.successful(product1))

      // prepare test request
      val request: FakeRequest[JsValue] =
        FakeRequest(POST, baseUri)
          .withHeaders(HOST -> host, CONTENT_TYPE -> JSON)
          .withAuthenticator[JWTEnvironment](identity.loginInfo)
          .withBody(
            Json.obj(
              "productName" -> "123",
              "price" -> "123",
              "expDate" -> "2024-12-31T00:00:00.000Z"
            )
          )

      // Execute test and then extract result
      val result: Future[Result] = route(app, request).get

      // verify result after test
      status(result) mustEqual OK
      val resProduct: ProductResponse =
        Json.fromJson[ProductResponse](contentAsJson(result)).get
      verifyProduct(resProduct, product1)
    }
  }

  "ProductController#updateProduct" should {

    "update a product successfully" in {
      // mock response data
      when(mockUserService.retrieve(identity.loginInfo))
        .thenReturn(Future.successful(Some(identity)))
      when(mockProductService.find(ArgumentMatchers.eq(1L)))
        .thenReturn(Future.successful(Some(product1)))
      when(mockProductService.update(any(classOf[Product])))
        .thenReturn(Future.successful(product1))

      // prepare test request
      val request: FakeRequest[JsValue] =
        FakeRequest(PUT, s"$baseUri/$id")
          .withHeaders(HOST -> host, CONTENT_TYPE -> JSON)
          .withAuthenticator[JWTEnvironment](identity.loginInfo)
          .withBody(
            Json.obj(
              "productName" -> "123",
              "price" -> "123",
              "expDate" -> "2024-12-31T00:00:00.000Z"
            )
          )

      // Execute test and then extract result
      val result: Future[Result] = route(app, request).get

      // verify result after test
      status(result) mustEqual OK
      val resProduct: ProductResponse =
        Json.fromJson[ProductResponse](contentAsJson(result)).get
      verifyProduct(resProduct, product1)
    }

    "product not found" in {
      // mock response data
      when(mockUserService.retrieve(identity.loginInfo))
        .thenReturn(Future.successful(Some(identity)))
      when(mockProductService.find(ArgumentMatchers.eq(1L)))
        .thenReturn(Future.successful(None))

      // prepare test request
      val request: FakeRequest[JsValue] =
        FakeRequest(PUT, s"$baseUri/$id")
          .withHeaders(HOST -> host, CONTENT_TYPE -> JSON)
          .withAuthenticator[JWTEnvironment](identity.loginInfo)
          .withBody(
            Json.obj(
              "productName" -> "123",
              "price" -> "123",
              "expDate" -> "2024-12-31T00:00:00.000Z"
            )
          )

      // Execute test and then extract result
      val result: Future[Result] = route(app, request).get

      // verify result after test
      status(result) mustEqual NOT_FOUND
    }
  }

  "ProductController#delete(id: Long)" should {

    "delete a product successfully" in {
      // mock response data
      when(mockUserService.retrieve(identity.loginInfo))
        .thenReturn(Future.successful(Some(identity)))
      when(mockProductService.delete(ArgumentMatchers.eq(1L)))
        .thenReturn(Future.successful(1))

      // prepare test request
      val request: FakeRequest[AnyContentAsEmpty.type] =
        FakeRequest(DELETE, s"$baseUri/$id")
          .withHeaders(HOST -> host)
          .withAuthenticator[JWTEnvironment](identity.loginInfo)

      // Execute test and then extract result
      val result: Future[Result] = route(app, request).get

      // verify result after test
      status(result) mustEqual OK
    }
  }

  private def verifyProduct(actual: ProductResponse,
                            expected: Product): Unit = {
    actual.id.get mustEqual expected.id.get
    actual.productName mustEqual expected.productName
    actual.price mustEqual expected.price
  }

}
