package de.welt.contentapi.pressed.client.services

import com.google.inject.{ImplementedBy, Inject, Singleton}
import com.kenshoo.play.metrics.Metrics
import de.welt.contentapi.core.client.services.CapiExecutionContext
import de.welt.contentapi.core.client.services.configuration.ServiceConfiguration
import de.welt.contentapi.core.client.services.contentapi.AbstractService
import play.api.libs.json.{Json, Reads}
import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.Future
import scala.util.Try

@ImplementedBy(classOf[SummaryThemeServiceImpl])
trait SummaryThemeService {

  /**
   * Search for all summaries with theme's sorting title starting with a specified letter.
   *
   * @param letter any char in range 'a' to 'z' or '#'
   * @return sequence of summaries
   */
  def findSummaries(letter: Char): Future[Seq[ThemeSummary]]
}

@Singleton
class SummaryThemeServiceImpl @Inject()(ws: WSClient,
                                        metrics: Metrics,
                                        capi: CapiExecutionContext)
  extends AbstractService[Seq[ThemeSummary]](ws, metrics, ServiceConfiguration("theme_service"), capi)
    with SummaryThemeService {

  import AbstractService.implicitConversions._

  implicit lazy val summaryResponseItem: Reads[ThemeSummary] = Json.reads[ThemeSummary]

  override def validate: WSResponse => Try[Seq[ThemeSummary]] = response =>
    response.json.result.validate[Seq[ThemeSummary]]

  override def findSummaries(letter: Char): Future[Seq[ThemeSummary]] =
    execute(parameters = Seq("letter" -> letter.toString))
}

case class ThemeSummary(path: String, id: String, title: String)
