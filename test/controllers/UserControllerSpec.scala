package controllers

import com.mohiva.play.silhouette.test._
import domain.dtos.response.UserResponse
import domain.models.User
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.Helpers._
import play.api.test._
import utils.auth.JWTEnvironment

import java.time.LocalDateTime
import scala.concurrent.Future

class UserControllerSpec extends ControllerFixture {
  val host = "localhost:9000"
  val baseUri = "/users"

  val id = 1L
  val user1: User =
    User(
      Some(id),
      "user1@nashtechglobal.com",
      "FirstName",
      "LastName 1",
      "User",
      "Address1",
      "01234567891",
      LocalDateTime.now(),
      Some("Password")
    )
  val user2: User =
    User(
      Some(2L),
      "user2@nashtechglobal.com",
      "FirstName 2",
      "LastName 2",
      "User",
      "Address2",
      "01234567892",
      LocalDateTime.now(),
      Some("Password")
    )

  "UserController#getUserById(id: Long)" should {

    "get a user successfully" in {
      // mock response data
      when(mockUserService.retrieve(identity.loginInfo))
        .thenReturn(Future.successful(Some(identity)))
      when(mockUserService.find(ArgumentMatchers.eq(id)))
        .thenReturn(Future.successful(Some(user1)))

      // prepare test request
      val request: FakeRequest[AnyContentAsEmpty.type] =
        FakeRequest(GET, s"$baseUri/$id")
          .withHeaders(HOST -> host)
          .withAuthenticator[JWTEnvironment](identity.loginInfo)

      // Execute test and then extract result
      val result: Future[Result] = route(app, request).get

      // verify result after test
      status(result) mustEqual OK
      val resUser: UserResponse =
        Json.fromJson[UserResponse](contentAsJson(result)).get
      verifyUser(resUser, user1)
    }

    "user not found" in {
      // mock response data
      when(mockUserService.retrieve(identity.loginInfo))
        .thenReturn(Future.successful(Some(identity)))
      when(mockUserService.find(ArgumentMatchers.eq(1L)))
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

  "UserController#getAllProducts" should {

    "get all users successfully" in {
      // mock response data
      when(mockUserService.retrieve(identity.loginInfo))
        .thenReturn(Future.successful(Some(identity)))
      when(mockUserService.listAll())
        .thenReturn(Future.successful(Seq(user1, user2)))

      // prepare test request
      val request: FakeRequest[AnyContentAsEmpty.type] =
        FakeRequest(GET, baseUri)
          .withHeaders(HOST -> host)
          .withAuthenticator[JWTEnvironment](identity.loginInfo)

      // Execute test and then extract result
      val result: Future[Result] = route(app, request).get

      // verify result after test
      status(result) mustEqual OK
      val resUsers: Seq[UserResponse] =
        Json.fromJson[Seq[UserResponse]](contentAsJson(result)).get
      resUsers.size mustEqual 2
    }
  }

  "UserController#delete(id: Long)" should {

    "delete a user successfully" in {
      // mock response data
      when(mockUserService.retrieve(identity.loginInfo))
        .thenReturn(Future.successful(Some(identity)))
      when(mockUserService.delete(ArgumentMatchers.eq(1L)))
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

  private def verifyUser(actual: UserResponse, expected: User): Unit = {
    actual.id.get mustEqual expected.id.get
    actual.email mustEqual expected.email
    actual.firstName mustEqual expected.firstName
    actual.lastName mustEqual expected.lastName
    actual.role mustEqual expected.role
    actual.address mustEqual expected.address
  }
}
