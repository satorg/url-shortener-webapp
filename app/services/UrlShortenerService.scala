package services

import com.google.inject.ImplementedBy

import scala.concurrent.Future

@ImplementedBy(classOf[SlickUrlShortenerService])
trait UrlShortenerService {

  /** Shortens the passed URL.
    *
    * @return the shortened URL's ID.
    */
  def shortenUrl(sourceUrl: String): Future[String]

  /** Restores an original URL from the passed short URL's ID
    *
    * @return the original long URL or NoSuchElementException if the passed urlId doesn't exist
    */
  def restoreUrl(urlId: String): Future[String]
}
