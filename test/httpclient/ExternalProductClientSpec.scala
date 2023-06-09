package httpclient

import domain.models.Product
import org.mockito.{ArgumentMatchers, Mockito}
import org.mockito.Mockito.{never, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.Configuration
import play.api.http.HttpVerbs
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{
  BodyWritable,
  WSAuthScheme,
  WSClient,
  WSRequest,
  WSRequestFilter,
  WSResponse
}

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class ExternalProductClientSpec
    extends PlaySpec
    with MockitoSugar
    with ScalaFutures
    with BeforeAndAfterEach {

  val mockWSClient: WSClient = mock[WSClient]
  val wsRequest: WSRequest = mock[WSRequest]
  val wsResponse: WSResponse = mock[WSResponse]
  val mockConfig: Configuration = mock[Configuration]

  val httpClient: ExternalProductClient =
    new ExternalProductClient(mockWSClient, mockConfig)(global)

  val baseUrl = "https://test.com"
  val username = "username"
  val password = "password"
  val apiPath = "/external/products"
  val product: Product = Product(Some(2L), "Product 2", 1, LocalDateTime.now())

  val postJsValue: JsValue = Json.toJson(product)

  override def beforeEach(): Unit = {
    super.beforeEach()
    when(mockConfig.get[String]("external.products.auth.basic.url"))
      .thenReturn(baseUrl)
    when(mockConfig.get[String]("external.products.auth.basic.username"))
      .thenReturn(username)
    when(mockConfig.get[String]("external.products.auth.basic.password"))
      .thenReturn(password)

    // Mocking WSClient behaviour
    when(mockWSClient.url(ArgumentMatchers.any[String])).thenReturn(wsRequest)
    when(wsRequest.withMethod(ArgumentMatchers.any[String]))
      .thenReturn(wsRequest)
    when(wsRequest.withQueryStringParameters(ArgumentMatchers.any()))
      .thenReturn(wsRequest)
    when(
      wsRequest.withBody(ArgumentMatchers.any[JsValue])(
        ArgumentMatchers.any[BodyWritable[JsValue]]
      )
    ).thenReturn(wsRequest)
    when(wsRequest.addHttpHeaders(ArgumentMatchers.any())).thenReturn(wsRequest)
    when(
      wsRequest.withAuth(
        ArgumentMatchers.any[String],
        ArgumentMatchers.any[String],
        ArgumentMatchers.any[WSAuthScheme]
      )
    ).thenReturn(wsRequest)
    when(wsRequest.withRequestFilter(ArgumentMatchers.any[WSRequestFilter]))
      .thenReturn(wsRequest)
  }

  "ExternalProductClient#get" should {

    "get an external product successfully" in {

      // Mocking data
      when(wsRequest.execute())
        .thenReturn(Future.successful(wsResponse)) // when request is executed, it should return a response
      when(wsResponse.status).thenReturn(200) // this is OK
      when(wsResponse.body[JsValue]).thenReturn(postJsValue)

      // Execute test
      val result = httpClient.get[Product](s"$apiPath/2").futureValue

      // Verify data after tests
      verifyProduct(result, product)
      verify(mockConfig, times(3))
        .get[String](ArgumentMatchers.any[String])(ArgumentMatchers.any())
      verify(mockWSClient, Mockito.only())
        .url(ArgumentMatchers.eq(s"${baseUrl}${apiPath}/2"))

      verify(wsRequest, times(1)).withMethod(ArgumentMatchers.eq(HttpVerbs.GET))
      verify(wsRequest, never())
        .withQueryStringParameters(ArgumentMatchers.any())
      verify(wsRequest, never()).withBody(ArgumentMatchers.any[JsValue])(
        ArgumentMatchers.any[BodyWritable[JsValue]]
      )
      verify(wsRequest, never()).addHttpHeaders(ArgumentMatchers.any())
      verify(wsRequest, times(1))
        .withAuth(
          ArgumentMatchers.eq(username),
          ArgumentMatchers.eq(password),
          ArgumentMatchers.eq(WSAuthScheme.BASIC)
        )
      verify(wsRequest, times(1))
        .withRequestFilter(ArgumentMatchers.any[WSRequestFilter])
    }
  }

  // Same for remaining methods

  private def verifyProduct(actual: Product, expected: Product): Unit = {
    actual.id.get mustEqual expected.id.get
    actual.productName mustEqual expected.productName
    actual.price mustEqual expected.price
    actual.expDate mustEqual expected.expDate
  }
}
