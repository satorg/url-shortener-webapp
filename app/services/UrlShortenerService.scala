package services

import java.util.UUID
import javax.inject._

import com.google.inject.ImplementedBy

import scala.collection.concurrent.TrieMap
import scala.concurrent.{ExecutionContext, Future}

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

@Singleton
class MemUrlShortenerService @Inject()(implicit ex: ExecutionContext)
  extends UrlShortenerService {

  private val urlsById = TrieMap.empty[UUID, String]
  private val idsByUrl = TrieMap.empty[String, UUID]

  override def shortenUrl(sourceUrl: String): Future[String] = Future {
    val newId = UUID.randomUUID()
    idsByUrl.putIfAbsent(sourceUrl, newId).
      getOrElse {
        urlsById.update(newId, sourceUrl)
        newId
      }.
      toString
  }

  override def restoreUrl(urlId: String): Future[String] = Future {
    urlsById(UUID.fromString(urlId)) // TODO: no error handling
  }
}
