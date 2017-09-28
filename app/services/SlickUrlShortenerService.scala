package services

import javax.inject.{Inject, Singleton}

import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api._

import scala.concurrent.{ExecutionContext, Future}

object SlickUrlShortenerService {

  class Urls(tag: Tag) extends Table[(String, String)](tag, "URLS") {

    def urlId = column[String]("url_id", O.PrimaryKey)

    def originalUrl = column[String]("original_url", O.Unique)

    override def * = (urlId, originalUrl)
  }

  val urls = TableQuery[Urls]
}

import services.SlickUrlShortenerService._

@Singleton
class SlickUrlShortenerService @Inject()(idGenerator: IdGenerator,
                                         override protected val dbConfigProvider: DatabaseConfigProvider)
                                        (implicit ex: ExecutionContext)
  extends UrlShortenerService
    with HasDatabaseConfigProvider[H2Profile] {

  private val dbInit = db.run(urls.schema.create)

  private def initialized[T](block: => Future[T]): Future[T] = dbInit.flatMap(_ => block)

  override def shortenUrl(sourceUrl: String): Future[String] = initialized {

    val newUrlId = idGenerator.generateId()
    db.
      run {
        {
          for (url <- urls if url.originalUrl === sourceUrl) yield url.urlId
        }.
          result.headOption.
          flatMap {
            case Some(oldUrlId) =>
              DBIO.successful(oldUrlId)
            case None =>
              (urls += (newUrlId, sourceUrl)).map(_ => newUrlId)
          }.
          transactionally
      }
  }

  override def restoreUrl(urlId: String): Future[String] = initialized {
    db.
      run {
        (for (url <- urls if url.urlId === urlId) yield url.originalUrl).result.map(_.headOption)
      }.
      map {
        case Some(foundUrl) => foundUrl
        case None => throw new NoSuchElementException(urlId)
      }
  }
}
