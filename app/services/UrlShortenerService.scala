package services

import java.util.UUID
import javax.inject._

import com.google.inject.ImplementedBy

import scala.collection.concurrent.TrieMap

@ImplementedBy(classOf[MemUrlShortenerService])
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
class MemUrlShortenerService extends UrlShortenerService {

  private val urlsById = TrieMap.empty[UUID, String]
  private val idsByUrl = TrieMap.empty[String, UUID]

  override def shortenUrl(sourceUrl: String): String = {
    val newId = UUID.randomUUID()
    idsByUrl.putIfAbsent(sourceUrl, newId).
      getOrElse {
        urlsById.update(newId, sourceUrl)
        newId
      }.
      toString
  }

  override def restoreUrl(urlId: String): String = {
    urlsById(UUID.fromString(urlId)) // TODO: no error handling
  }
}
