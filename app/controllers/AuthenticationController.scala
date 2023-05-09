package controllers

import com.google.inject.Inject
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.util.Credentials
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import controllers.abstraction.{
  SilhouetteController,
  SilhouetteControllerComponents
}
import domain.dtos.request.{LoginPostRequest, UserPostRequest}
import domain.dtos.response.UserResponse
import domain.models.User
import play.api.libs.json.{JsString, Json}
import play.api.mvc.Results.{BadRequest, Conflict, Ok}
import play.api.mvc.{Action, AnyContent, Request}

import scala.concurrent.{ExecutionContext, Future}

class AuthenticationController @Inject()(
  components: SilhouetteControllerComponents
)(implicit ex: ExecutionContext)
    extends SilhouetteController(components) {

  def register: Action[AnyContent] = UnsecuredAction.async {
    implicit request: Request[AnyContent] =>
      request.body.asJson.flatMap(_.asOpt[UserPostRequest]) match {
        case Some(newUser) =>
          userService
            .retrieve(LoginInfo(CredentialsProvider.ID, newUser.email))
            .flatMap {
              case Some(_) =>
                Future.successful(Conflict(JsString(("User already exist"))))
              case None =>
                val authInfo =
                  passwordHasherRegistry.current.hash(newUser.password)
                val userFormInput = newUser copy (password = authInfo.password)
                userService
                  .save(User fromUserPostRequest userFormInput)
                  .map(u => Ok(Json toJson (UserResponse fromUser u)))
            }
        case _ =>
          Future.successful(BadRequest(JsString(("Invalid body"))))
      }
  }

  def login: Action[AnyContent] = UnsecuredAction.async {
    implicit request: Request[AnyContent] =>
      request.body.asJson.flatMap(_.asOpt[LoginPostRequest]) match {
        case Some(loginPostRequest: LoginPostRequest) =>
          val credentials =
            Credentials(loginPostRequest.email, loginPostRequest.password)
          credentialsProvider
            .authenticate(credentials)
            .flatMap { loginInfo =>
              userService.retrieve(loginInfo).flatMap {
                case Some(_) =>
                  for {
                    authenticator <- authenticatorService.create(loginInfo)
                    token <- authenticatorService.init(authenticator)
                    result <- authenticatorService.embed(token, Ok)
                  } yield {
                    logger
                      .debug(s"User ${loginInfo} signed success")
                    result
                  }
                case None =>
                  Future
                    .successful(BadRequest(JsString(("could.not.find.user"))))
              }
            }
            .recover {
              case _: ProviderException =>
                BadRequest(JsString(("invalid.credentials")))
            }
        case None =>
          Future.successful(BadRequest(JsString(("could.not.find.user"))))
      }
  }

}
