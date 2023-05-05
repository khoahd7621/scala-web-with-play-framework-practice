package controllers

import com.mohiva.play.silhouette.api.actions.SecuredActionBuilder
import com.mohiva.play.silhouette.api.util.PasswordHasherRegistry
import com.mohiva.play.silhouette.api.{LoginInfo, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import domain.dto.request.UserPostRequest
import domain.dto.response.UserResponse
import domain.models.User
import play.api.Logger
import play.api.data.Form
import play.api.libs.json.{JsString, Json}
import play.api.mvc._
import services.UserService
import utils.auth.{JWTEnvironment, WithRole}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UserController @Inject()(
  cc: ControllerComponents,
  userService: UserService,
  passwordHasherRegistry: PasswordHasherRegistry,
  silhouette: Silhouette[JWTEnvironment]
)(implicit ec: ExecutionContext)
    extends AbstractController(cc) {

  def SecuredAction: SecuredActionBuilder[JWTEnvironment, AnyContent] =
    silhouette.SecuredAction

  private val logger = Logger(getClass)

  def getAllUsers: Action[AnyContent] =
    SecuredAction(WithRole[JWTAuthenticator]("Admin")).async {
      implicit request =>
        logger.trace("get all users")
        userService.listAll().map { users =>
          Ok(Json.toJson(users))
        }
    }

  def getUserById(id: Long): Action[AnyContent] =
    SecuredAction(WithRole[JWTAuthenticator]("Admin"))
      .async { implicit request =>
        logger.trace(s"get an user by id: $id")
        userService.find(id).map {
          case Some(user) => Ok(Json.toJson(UserResponse.fromUser(user)))
          case None       => NotFound
        }
      }

  def createUser: Action[AnyContent] =
    SecuredAction(WithRole[JWTAuthenticator]("Admin")).async {
      implicit request =>
        {
          logger.trace("create new User")
          processJsonPost(None)
        }
    }

  def updateUser(id: Long): Action[AnyContent] =
    SecuredAction(WithRole[JWTAuthenticator]("Admin")).async {
      implicit request =>
        {
          logger.trace(s"update User with id: $id")
          processJsonPost(Some(id))
        }
    }

  def deleteUser(id: Long): Action[AnyContent] =
    SecuredAction(WithRole[JWTAuthenticator]("Admin")).async {
      implicit request =>
        logger.trace(s"delete user with: id = $id")
        userService.delete(id).map { deletedCnt =>
          if (deletedCnt == 1)
            Ok(JsString(s"delete user with id: $id successfully"))
          else BadRequest(JsString(s"unable to delete user with $id"))
        }
    }

  private val form: Form[UserPostRequest] = {
    import play.api.data.Forms._
    Form(
      mapping(
        "email" -> nonEmptyText(maxLength = 100),
        "firstName" -> nonEmptyText(maxLength = 64),
        "lastName" -> nonEmptyText(maxLength = 64),
        "role" -> nonEmptyText(maxLength = 16),
        "address" -> nonEmptyText(maxLength = 255),
        "phoneNumber" -> nonEmptyText(maxLength = 12),
        "birthDate" -> localDateTime("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"),
        "password" -> nonEmptyText(maxLength = 128)
      )(UserPostRequest.apply)(UserPostRequest.unapply)
    )
  }

  private def processJsonPost[A](
    id: Option[Long]
  )(implicit request: Request[A]): Future[Result] = {

    def failure(badForm: Form[UserPostRequest]) = {
      Future.successful(BadRequest(JsString("invalid input")))
    }

    def success(userPostRequest: UserPostRequest) = {
      id match {
        case None =>
          userService
            .retrieve(LoginInfo(CredentialsProvider.ID, userPostRequest.email))
            .flatMap {
              case Some(_) =>
                Future.successful(Conflict(JsString(("email already exist"))))
              case None =>
                val authInfo =
                  passwordHasherRegistry.current.hash(userPostRequest.password)
                val newUserFormInput =
                  userPostRequest.copy(password = authInfo.password)
                userService
                  .save(User.fromUserPostRequest(newUserFormInput))
                  .map(u => Ok(Json.toJson(UserResponse fromUser u)))
            }
        case Some(_) => {
          userService.find(id.get).flatMap {
            case Some(user) =>
              if (!user.email.equals(userPostRequest.email)) {
                userService
                  .retrieve(
                    LoginInfo(CredentialsProvider.ID, userPostRequest.email)
                  )
                  .flatMap {
                    case Some(_) =>
                      Future
                        .successful(Conflict(JsString(("email already exist"))))
                    case None => {
                      val updatedUser = user.copy(
                        email = userPostRequest.email,
                        firstName = userPostRequest.firstName,
                        lastName = userPostRequest.lastName,
                        role = userPostRequest.role,
                        address = userPostRequest.address,
                        phoneNumber = userPostRequest.phoneNumber,
                        birthDate = userPostRequest.birthDate,
                        password = Some(
                          passwordHasherRegistry.current
                            .hash(userPostRequest.password)
                            .password
                        )
                      )
                      userService.update(updatedUser).map { u =>
                        Ok(Json.toJson(UserResponse.fromUser(u)))
                      }
                    }
                  }
              } else {
                logger.trace(s"update User with id: $id")
                val updatedUser = user.copy(
                  firstName = userPostRequest.firstName,
                  lastName = userPostRequest.lastName,
                  role = userPostRequest.role,
                  address = userPostRequest.address,
                  phoneNumber = userPostRequest.phoneNumber,
                  birthDate = userPostRequest.birthDate,
                  password = Some(
                    passwordHasherRegistry.current
                      .hash(userPostRequest.password)
                      .password
                  )
                )
                userService.update(updatedUser).map { u =>
                  Ok(Json.toJson(UserResponse.fromUser(u)))
                }
              }
            case _ =>
              logger.trace(s"user with id: $id not found")
              Future.successful(NotFound)
          }
        }
      }
    }

    form.bindFromRequest().fold(failure, success)
  }
}
