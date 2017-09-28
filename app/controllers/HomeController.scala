package controllers

import java.net.URL
import javax.inject._

import play.api.data.Forms._
import play.api.data._
import play.api.mvc._
import services.UrlShortenerService

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

object HomeController {
  /** Validates the passed URL string.
    */
  def isUrlValid(urlStr: String): Boolean = {
    Try {
      new URL(urlStr).toURI
    }.
      fold(_ => false, _ => true)
  }
}

import controllers.HomeController._

@Singleton
class HomeController @Inject()(components: MessagesControllerComponents,
                               urlShortenerService: UrlShortenerService)
                              (implicit ex: ExecutionContext)
  extends MessagesAbstractController(components) {

  private lazy val urlForm = Form(single("url" -> nonEmptyText.verifying("error.url", isUrlValid _)))

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
      urlShortenerService.restoreUrl(urlId).
        map { originalUrl =>
          // The original URL is retrieved successfully, redirect to it.
          Redirect(originalUrl)
        }.
        recover {
          // Failed to retrieve the original URL, show the error page.
          case _: NoSuchElementException => BadRequest(views.html.error())
        }
    }
}
