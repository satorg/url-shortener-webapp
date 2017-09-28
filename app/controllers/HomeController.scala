package controllers

import javax.inject._

import play.api.data.Forms._
import play.api.data._
import play.api.mvc._
import services.UrlShortenerService

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HomeController @Inject()(components: MessagesControllerComponents,
                               urlShortenerService: UrlShortenerService)
                              (implicit ex: ExecutionContext)
  extends MessagesAbstractController(components) {

  private lazy val urlForm = Form(single("url" -> nonEmptyText))

  /** Returns the home page with the URL input field.
    */
  def index(urlIdOpt: Option[String]): Action[AnyContent] =
    Action { implicit request =>
      val shortUrlOpt = urlIdOpt.map(routes.HomeController.gotoUrl(_).absoluteURL())
      Ok(views.html.index(urlForm, shortUrlOpt))
    }

  /** Receives URL from the input field, shorten it and shows the result URL.
    */
  def submitUrl(): Action[AnyContent] =
    Action.async { implicit request =>
      urlForm.bindFromRequest().fold(
        formWithErrors => {
          Future.successful(BadRequest(views.html.index(formWithErrors)))
        },
        url => {
          urlShortenerService.shortenUrl(url).map { shortUrlId =>
            Redirect(routes.HomeController.index(Some(shortUrlId)))
          }
        }
      )
    }

  /** Restores an original URL from the shortened part and redirects to this URL.
    */
  def gotoUrl(urlId: String): Action[AnyContent] =
    Action.async { implicit request =>
      urlShortenerService.restoreUrl(urlId).map { originalUrl =>
        Redirect(originalUrl)
      }
    }
}
