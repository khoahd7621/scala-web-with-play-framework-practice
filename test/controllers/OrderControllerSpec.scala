package controllers

import com.mohiva.play.silhouette.test._
import domain.dtos.request.{OrderPostRequest, OrderPutRequest}
import domain.dtos.response.{OrderItemsResponse, OrderResponse, ProductResponse}
import domain.models.Order
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

class OrderControllerSpec extends ControllerFixture {
  val host = "localhost:9000"
  val baseUri = "/orders"

  val id = 1L
  val order: Order = Order(Some(id), 1, LocalDateTime.now(), 100)
  val orderResponse: OrderResponse =
    OrderResponse(
      Some(id),
      1,
      LocalDateTime.now(),
      100,
      Seq(OrderItemsResponse(1, 2, 50))
    )

  "OrderController#getOrderById(id: Long)" should {

    "get an order successfully" in {
      // mock response data
      when(mockUserService.retrieve(identity.loginInfo))
        .thenReturn(Future.successful(Some(identity)))
      when(mockOrderService.find(ArgumentMatchers.eq(id)))
        .thenReturn(Future.successful(Some(orderResponse)))

      // prepare test request
      val request: FakeRequest[AnyContentAsEmpty.type] =
        FakeRequest(GET, s"$baseUri/$id")
          .withHeaders(HOST -> host)
          .withAuthenticator[JWTEnvironment](identity.loginInfo)

      // Execute test and then extract result
      val result: Future[Result] = route(app, request).get

      // verify result after test
      status(result) mustEqual OK
      val resProduct: OrderResponse =
        Json.fromJson[OrderResponse](contentAsJson(result)).get
      verifyOrderResponse(resProduct, orderResponse)
    }

    "order not found" in {
      // mock response data
      when(mockUserService.retrieve(identity.loginInfo))
        .thenReturn(Future.successful(Some(identity)))
      when(mockOrderService.find(ArgumentMatchers.eq(1L)))
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

  "OrderController#getAllOrders" should {

    "get all orders successfully" in {
      // mock response data
      when(mockUserService.retrieve(identity.loginInfo))
        .thenReturn(Future.successful(Some(identity)))
      when(mockOrderService.listAll())
        .thenReturn(Future.successful(Seq(orderResponse)))

      // prepare test request
      val request: FakeRequest[AnyContentAsEmpty.type] =
        FakeRequest(GET, baseUri)
          .withHeaders(HOST -> host)
          .withAuthenticator[JWTEnvironment](identity.loginInfo)

      // Execute test and then extract result
      val result: Future[Result] = route(app, request).get

      // verify result after test
      status(result) mustEqual OK
      val resOrders: Seq[OrderResponse] =
        Json.fromJson[Seq[OrderResponse]](contentAsJson(result)).get
      resOrders.size mustEqual 1
    }
  }

  "OrderController#createOrder" should {

    "create an order successfully" in {
      // mock response data
      when(mockUserService.retrieve(identity.loginInfo))
        .thenReturn(Future.successful(Some(identity)))
      when(mockOrderService.save(any(classOf[OrderPostRequest])))
        .thenReturn(Future.successful(orderResponse))

      // prepare test request
      val request: FakeRequest[JsValue] =
        FakeRequest(POST, baseUri)
          .withHeaders(HOST -> host, CONTENT_TYPE -> JSON)
          .withAuthenticator[JWTEnvironment](identity.loginInfo)
          .withBody(
            Json.obj(
              "userId" -> "1",
              "orderItemsRequest" -> Json.arr(
                Json.obj("productId" -> "1", "quantity" -> "2", "price" -> 100)
              )
            )
          )

      // Execute test and then extract result
      val result: Future[Result] = route(app, request).get

      // verify result after test
      status(result) mustEqual CREATED
      val resOrder: OrderResponse =
        Json.fromJson[OrderResponse](contentAsJson(result)).get
      verifyOrderResponse(resOrder, orderResponse)
    }
  }

  "OrderController#updateOrder" should {

    "update an order successfully" in {
      // mock response data
      when(mockUserService.retrieve(identity.loginInfo))
        .thenReturn(Future.successful(Some(identity)))
      when(mockOrderService.update(any(classOf[OrderPutRequest])))
        .thenReturn(Future.successful(orderResponse))

      // prepare test request
      val request: FakeRequest[JsValue] =
        FakeRequest(PUT, s"$baseUri/$id")
          .withHeaders(HOST -> host, CONTENT_TYPE -> JSON)
          .withAuthenticator[JWTEnvironment](identity.loginInfo)
          .withBody(
            Json.obj(
              "id" -> "1",
              "userId" -> "1",
              "orderItemsRequest" -> Json.arr(
                Json.obj("productId" -> "1", "quantity" -> "2", "price" -> 100)
              )
            )
          )

      // Execute test and then extract result
      val result: Future[Result] = route(app, request).get

      // verify result after test
      status(result) mustEqual CREATED
      val resOrder: OrderResponse =
        Json.fromJson[OrderResponse](contentAsJson(result)).get
      verifyOrderResponse(resOrder, orderResponse)
    }
  }

  "OrderController#delete(id: Long)" should {

    "delete an order successfully" in {
      // mock response data
      when(mockUserService.retrieve(identity.loginInfo))
        .thenReturn(Future.successful(Some(identity)))
      when(mockOrderService.delete(ArgumentMatchers.eq(1L)))
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
