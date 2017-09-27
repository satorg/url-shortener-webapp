package services

import javax.inject._

import com.google.inject.ImplementedBy

@ImplementedBy(classOf[UrlShortenerServiceImpl])
trait UrlShortenerService {

  /** Shortens the passed URL.
    *
    * @return the shortened URL's ID.
    */
  def shortenUrl(sourceUrl: String): String

  /** Restores an original URL from the passed short URL's ID
    *
    * @return the original long URL
    */
  def restoreUrl(urlId: String): String
}

@Singleton
class UrlShortenerServiceImpl extends UrlShortenerService {

  override def shortenUrl(sourceUrl: String): String = sourceUrl

  override def restoreUrl(urlId: String): String = urlId
}
