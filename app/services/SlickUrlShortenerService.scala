package services

import javax.inject.{Inject, Singleton}

import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api._

import scala.concurrent.{ExecutionContext, Future}

object SlickUrlShortenerService {

  class Urls(tag: Tag) extends Table[(Long, String)](tag, "URLS") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def originalUrl = column[String]("original_url", O.Unique)

    override def * = (id, originalUrl)
  }

  val urls = TableQuery[Urls]
}

import services.SlickUrlShortenerService._

@Singleton
class SlickUrlShortenerService @Inject()(override protected val dbConfigProvider: DatabaseConfigProvider)
                                        (implicit ex: ExecutionContext)
  extends UrlShortenerService
    with HasDatabaseConfigProvider[H2Profile] {

  private val dbInit = db.run(urls.schema.create)

  private def initialized[T](block: => Future[T]): Future[T] = dbInit.flatMap(_ => block)

  override def shortenUrl(sourceUrl: String): Future[String] = initialized {

    db.
      run {
        {
          for (url <- urls if url.originalUrl === sourceUrl) yield url.id
        }.
          result.headOption.
          flatMap {
            case Some(existingUrlId) =>
              DBIO.successful(existingUrlId)
            case None =>
              urls.returning(urls.map(_.id)) += (0, sourceUrl)
          }.
          transactionally
      }.
      map(_.toString)
  }

  override def restoreUrl(urlId: String): Future[String] = initialized {
    db.
      run {
        (for (url <- urls if url.id === urlId.toLong) yield url.originalUrl).result.map(_.headOption)
      }.
      map {
        case Some(foundUrl) => foundUrl
        case None => throw new NoSuchElementException(urlId)
      }
  }
}
