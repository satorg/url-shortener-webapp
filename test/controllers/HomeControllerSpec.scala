package controllers

import org.mockito.Mockito._
import org.scalatest.OptionValues
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play._
import play.api.http.{DefaultFileMimeTypes, FileMimeTypes, FileMimeTypesConfiguration}
import play.api.i18n.{Langs, MessagesApi}
import play.api.mvc._
import play.api.test.CSRFTokenHelper._
import play.api.test.Helpers._
import play.api.test._
import services.UrlShortenerService

import scala.concurrent.{ExecutionContext, Future}

/**
  * HomeController unit tests
  */
//noinspection TypeAnnotation
class HomeControllerSpec extends PlaySpec with MockitoSugar with OptionValues {

  import HomeControllerSpec._

  trait Fixture {
    implicit def executionContext = ExecutionContext.global

    val urlShortenerService = mock[UrlShortenerService]
    val controller = new HomeController(stubMessagesControllerComponents(), urlShortenerService)

    def fakeRequest = FakeRequest().withCSRFToken
  }

  "index" should {
    "render the index page without the shortened URL if not URL ID is passed" in new Fixture {
      val result = controller.index(None).apply(fakeRequest)

      status(result) mustBe OK
      contentType(result) mustBe Some("text/html")
      contentAsString(result) mustNot include("Your URL:")
    }
    "render the index page with the shortened URL if an URL ID is passed" in new Fixture {
      val testUrlId = "abcdefg12345"
      val result = controller.index(Some(testUrlId)).apply(fakeRequest)

      status(result) mustBe OK
      contentType(result) mustBe Some("text/html")
      contentAsString(result) must include("Your URL:")
      contentAsString(result) must include(testUrlId)
    }
  }
  "submitUrl" should {
    "shorten URL and redirect to the index page with its ID" in new Fixture {
      val testUrl = "http://fake.griddynamics.com"
      val testId = "fake-id"

      when(urlShortenerService.shortenUrl(testUrl)) thenReturn Future.successful(testId)

      val request = fakeRequest.withBody(AnyContentAsFormUrlEncoded(Map("url" -> Seq(testUrl))))
      val result = controller.submitUrl().apply(request)

      status(result) mustBe SEE_OTHER
      header(LOCATION, result).value mustBe s"/?id=$testId"
    }
  }
  "gotoUrl" should {
    "restore the shortened URL and redirect to it" in new Fixture {
      val testId = "fake-id"
      val testUrl = "http://fake.griddynamics.com"

      when(urlShortenerService.restoreUrl(testId)) thenReturn Future.successful(testUrl)

      val result = controller.gotoUrl(testId).apply(fakeRequest)
      status(result) mustBe SEE_OTHER
      header(LOCATION, result).value mustBe testUrl
    }
  }
}

object HomeControllerSpec {

  class DefaultMessagesActionBuilder(override val parser: BodyParser[AnyContent],
                                     messagesApi: MessagesApi)
                                    (implicit override val executionContext: ExecutionContext)
    extends MessagesActionBuilder {

    override def invokeBlock[A](request: Request[A],
                                block: (MessagesRequest[A]) => Future[Result]): Future[Result] =
      block(new MessagesRequest[A](request, messagesApi))
  }

  def stubMessagesControllerComponents(bodyParser: BodyParser[AnyContent] = stubBodyParser(AnyContentAsEmpty),
                                       playBodyParsers: PlayBodyParsers = stubPlayBodyParsers(NoMaterializer),
                                       messagesApi: MessagesApi = stubMessagesApi(),
                                       langs: Langs = stubLangs(),
                                       fileMimeTypes: FileMimeTypes = new DefaultFileMimeTypes(FileMimeTypesConfiguration()))
                                      (implicit executionContext: ExecutionContext):
  DefaultMessagesControllerComponents = {

    DefaultMessagesControllerComponents(
      new DefaultMessagesActionBuilder(bodyParser, messagesApi),
      DefaultActionBuilder(bodyParser),
      playBodyParsers,
      messagesApi,
      langs,
      fileMimeTypes,
      executionContext)
  }
}
