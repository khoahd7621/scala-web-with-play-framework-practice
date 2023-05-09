import domain.dtos.response.ProductResponse
import domain.models.{Product, User}
import fixtures.DataFixtures
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{JsValue, Json, OFormat}
import play.api.libs.ws._
import play.api.test.Helpers._
import play.api.test._

class ProductIntegrationSpec
    extends DataFixtures
    with MockitoSugar
    with ScalaFutures {

  case class LoginBody(email: String, password: String)
  implicit val format: OFormat[LoginBody] = Json.format[LoginBody]
  val authHeaderKey: String = "X-Auth"

  override protected def beforeAll(): Unit = {
    // insert data before tests
    createUsers(Users.allUsers)
    createProducts(Products.allProducts)
  }

  "GET /products/:id" should {

    "get a product successfully" in new WithServer(app) {

      val product: Product = Products.product6
      val admin: User = Users.admin
      val loginBody: LoginBody = LoginBody(admin.email, Users.plainPassword)

      // login to get access token
      val loginRes: WSResponse =
        await(WsTestClient.wsUrl("/login").post(Json.toJson(loginBody)))
      val accessToken: Option[String] = loginRes.header(authHeaderKey)
      accessToken.isEmpty mustBe false

      // Execute test and then extract result
      val getPostRes: WSResponse = await(
        WsTestClient
          .wsUrl("/products/6")
          .addHttpHeaders(authHeaderKey -> accessToken.get)
          .get()
      )

      // verify result after test
      getPostRes.status mustEqual 200
      val actualProduct: ProductResponse =
        getPostRes.body[JsValue].as[ProductResponse]
      verifyProduct(actualProduct, product)
    }
  }

  private def verifyProduct(actual: ProductResponse,
                            expected: Product): Unit = {
    actual.id.get mustEqual expected.id.get
    actual.productName mustEqual expected.productName
    actual.price mustEqual expected.price
  }

}
