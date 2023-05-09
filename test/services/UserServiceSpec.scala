package services

import domain.dao.UserDao
import domain.models.User
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class UserServiceSpec extends PlaySpec with MockitoSugar with ScalaFutures {
  val mockUserDao: UserDao = mock[UserDao]

  val userService: UserService =
    new UserServiceImpl(mockUserDao)(global)

  private val user1 = User(
    Some(1L),
    "user1@sample.com",
    "User1",
    "User1",
    "User",
    "Address",
    "0123456789",
    LocalDateTime.now(),
    Some("Password")
  )
  private val user2 = User(
    Some(2L),
    "user2@sample.com",
    "User2",
    "User2",
    "User",
    "Address",
    "0123456789",
    LocalDateTime.now(),
    Some("Password")
  )

  "UserService#find(id: Long)" should {

    "get a user successfully" in {
      when(mockUserDao.find(anyLong()))
        .thenReturn(Future.successful(Some(user1)))

      val result = userService.find(1L).futureValue
      result.isEmpty mustBe false
      val actual = result.get
      verifyUser(actual, user1)
    }

    "user not found" in {
      when(mockUserDao.find(anyLong())).thenReturn(Future.successful(None))

      val result = userService.find(1L).futureValue
      result.isEmpty mustBe true
    }
  }

  "UserService#listAll" should {

    "get all users successfully" in {
      val users = Seq(user1, user2)
      when(mockUserDao.listAll()).thenReturn(Future.successful(users))

      val result = userService.listAll().futureValue
      result.isEmpty mustBe false
      result.size mustBe 2
      result.map(_.id.get) must contain allOf (1L, 2L)
    }
  }

  "UserService#save(user)" should {

    "save a user successfully" in {
      when(mockUserDao.save(user1))
        .thenReturn(Future.successful(user1))

      val result = userService.save(user1).futureValue
      verifyUser(result, user1)
    }
  }

  "UserService#update(user)" should {

    "update a user successfully" in {
      when(mockUserDao.update(user1))
        .thenReturn(Future.successful(user1))

      val result = userService.update(user1).futureValue
      verifyUser(result, user1)
    }
  }

  "UserService#delete(id: Long)" should {

    "delete a user successfully" in {
      when(mockUserDao.delete(1L)).thenReturn(Future.successful(1))

      val result = userService.delete(1L).futureValue
      result mustEqual 1
    }
  }

  private def verifyUser(user: User, expected: User): Unit = {
    user.id.get mustEqual expected.id.get
    user.email mustEqual expected.email
    user.firstName mustEqual expected.firstName
    user.lastName mustEqual expected.lastName
    user.role mustEqual expected.role
    user.address mustEqual expected.address
    user.phoneNumber mustEqual expected.phoneNumber
    user.password.get mustEqual expected.password.get
  }
}
