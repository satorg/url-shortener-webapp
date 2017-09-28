package services

import javax.inject.{Inject, Singleton}

import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api._

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

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

  // TODO: make it fully async
  Await.result(db.run(urls.schema.create), 5.seconds)

  override def shortenUrl(sourceUrl: String): Future[String] = {

    val newUrlId = idGenerator.generateId()
    db.run {
      val action = {
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

      action
    }
  }

  override def restoreUrl(urlId: String): Future[String] = db.run {
    (for (url <- urls if url.urlId === urlId) yield url.originalUrl).result.map(_.head)
  }
}
