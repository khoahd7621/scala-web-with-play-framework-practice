package fixtures

import com.mohiva.play.silhouette.password.BCryptSha256PasswordHasher
import domain.dao.{ProductDao, UserDao}
import domain.models.{Product, User}
import org.scalatestplus.play.PlaySpec

import java.time.LocalDateTime
import scala.concurrent.Await
import scala.concurrent.duration._

abstract class DataFixtures extends PlaySpec with AbstractPersistenceTests {

  val userDao: UserDao = get[UserDao]
  val productDao: ProductDao = get[ProductDao]

  def createUsers(users: Seq[User]): Unit = {
    Await.result(userDao.saveAll(users), 5.seconds)
  }

  def createProducts(products: Seq[Product]): Unit = {
    Await.result(productDao.saveAll(products), 5.seconds)
  }

  object Users {
    val plainPassword: String = "fakeP@ssw0rd";
    val password: String =
      new BCryptSha256PasswordHasher().hash(plainPassword).password

    val admin: User = User(
      Some(4L),
      "admin@test.com",
      "Admin",
      "Admin",
      "Admin",
      "Address",
      "0123456789",
      LocalDateTime.now(),
      Some(password)
    )
    val user: User = User(
      Some(5L),
      "user@test.com",
      "User",
      "User",
      "User",
      "Address",
      "0123456789",
      LocalDateTime.now(),
      Some(password)
    )
    val operator: User = User(
      Some(6L),
      "operator@test.com",
      "Operator",
      "Operator",
      "Operator",
      "Address",
      "0123456789",
      LocalDateTime.now(),
      Some(password)
    )

    val allUsers: Seq[User] = Seq(admin, user, operator)
  }

  object Products {
    val product6: Product =
      Product(Some(6L), "Product 6", 6, LocalDateTime.now())
    val product7: Product =
      Product(Some(7L), "Product 7", 7, LocalDateTime.now())
    val product8: Product =
      Product(Some(8L), "Product 8", 8, LocalDateTime.now())
    val product9: Product =
      Product(Some(9L), "Product 9", 9, LocalDateTime.now())

    val allProducts: Seq[Product] = Seq(product6, product7, product8, product9)
  }
}
