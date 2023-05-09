package domain.dao

import domain.models.User
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar

import java.time.LocalDateTime

class UserDaoSpec extends AbstractDaoTest with MockitoSugar with ScalaFutures {

  val userDao: UserDao = get[UserDao]

  var user1: User = _
  var user2: User = _
  var user3: User = _

  override protected def beforeAll(): Unit = {
    // Get data from DB
    user1 = userDao.find(1).futureValue.get // id = 1
    user2 = userDao.find(2).futureValue.get // id = 2
    user3 = userDao.find(3).futureValue.get // id = 3
  }

  "UserDao#find(id: Long)" should {

    "get a user successfully" in {
      val result = userDao.find(1L).futureValue
      result.isEmpty mustBe false
      val user = result.get
      verifyUser(user, user1)
    }

    "user not found" in {
      val result = userDao.find(4L).futureValue
      result.isEmpty mustBe true
    }
  }

  "UserDao#find(email: String)" should {

    "get a user successfully" in {
      val result = userDao.find("admin@nashtechglobal.com").futureValue
      result.isEmpty mustBe false
      val user = result.get
      verifyUser(user, user1)
    }

    "user not found" in {
      val result = userDao.find("sample@nashtechglobal.com").futureValue
      result.isEmpty mustBe true
    }
  }

  "UserDao#listAll" should {

    "get all users successfully" in {
      val result = userDao.listAll().futureValue
      result.size mustBe 3
      result.map(_.id.get) must contain allOf (1L, 2L, 3L)
    }
  }

  "ProductDao#save(user)" should {

    "save a user successfully" in {
      val user4 = User(
        Some(4L),
        "user4@nashtechglobal.com",
        "FirstName",
        "LastName",
        "User",
        "Address",
        "0123456789",
        LocalDateTime.now(),
        Some("Password")
      )
      userDao.save(user4).futureValue

      val result = userDao.find(4L).futureValue
      result.isEmpty mustBe false
      val user = result.get
      verifyUser(user, user4)
    }
  }

  "UserDao#update(user)" should {

    "update a user successfully" in {
      val userUpdate =
        User(
          Some(4L),
          "user41@nashtechglobal.com",
          "FirstName1",
          "LastName1",
          "User1",
          "Address1",
          "01234567891",
          LocalDateTime.now(),
          Some("Password1")
        )
      userDao.update(userUpdate).futureValue

      val result = userDao.find(4L).futureValue
      result.isEmpty mustBe false
      val user = result.get
      verifyUser(user, userUpdate)
    }
  }

  "UserDao#delete(id: Long)" should {

    "delete a user successfully" in {
      userDao.delete(4L).futureValue

      val result = userDao.find(4L).futureValue
      result.isEmpty mustBe true

      val resultAll = userDao.listAll().futureValue
      resultAll.size mustBe 3
      resultAll.map(_.id.get) must contain allOf (1L, 2L, 3L)
    }
  }

  private def verifyUser(actual: User, expected: User): Unit = {
    actual.id.get mustEqual expected.id.get
    actual.email mustEqual expected.email
    actual.firstName mustEqual expected.firstName
    actual.lastName mustEqual expected.lastName
    actual.role mustEqual expected.role
    actual.address mustEqual expected.address
  }
}
