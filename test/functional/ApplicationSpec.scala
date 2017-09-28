package functional

import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.CSRFTokenHelper._
import play.api.test.Helpers._
import play.api.test._

class ApplicationSpec extends PlaySpec with GuiceOneAppPerSuite {
  "GET /" should {
    "not include short URL if no parameters are supplied" in {
      val request = FakeRequest(GET, "/")
      val result = route(app, request).get

      status(result) mustBe OK
      contentType(result) mustBe Some("text/html")
      contentAsString(result) mustNot include("Your short URL:")
      contentAsString(result) must include("Enter source URL here:")
    }
    "include short URL if the 'id' parameter is supplied" in {
      val request = FakeRequest(GET, "/?id=abcdef12345")
      val result = route(app, request).get

      status(result) mustBe OK
      contentType(result) mustBe Some("text/html")
      contentAsString(result) must include("Your short URL:")
      contentAsString(result) must include("abcdef12345")
      contentAsString(result) must include("Enter source URL here:")
    }
  }
  "POST /url" should {
    "create a new short URL if the source URL is passed for the first time or return existing URL otherwise" in {
      // Ask to shorten an URL
      val result1 = {
        val request1 =
          FakeRequest(POST, "/url").
            withCSRFToken.
            withBody(AnyContentAsFormUrlEncoded(Map("url" -> Seq("http://griddynamics.com"))))

        route(app, request1).get
      }
      status(result1) mustBe SEE_OTHER
      val redirect1 = header(LOCATION, result1).value

      // Ask to shorten another URL
      val result2 = {
        val request2 =
          FakeRequest(POST, "/url").
            withCSRFToken.
            withBody(AnyContentAsFormUrlEncoded(Map("url" -> Seq("http://tonomi.com"))))

        route(app, request2).get
      }
      status(result2) mustBe SEE_OTHER
      val redirect2 = header(LOCATION, result2).value

      redirect1 must not be redirect2

      // Ask to shorten the first URL again
      val result3 = {
        val request3 =
          FakeRequest(POST, "/url").
            withCSRFToken.
            withBody(AnyContentAsFormUrlEncoded(Map("url" -> Seq("http://griddynamics.com"))))

        route(app, request3).get
      }
      status(result3) mustBe SEE_OTHER
      val redirect3 = header(LOCATION, result3).value

      redirect1 mustBe redirect3
    }
  }
}
