package de.welt.contentapi.pressed.client.services

import com.google.inject.{ImplementedBy, Inject, Singleton}
import com.kenshoo.play.metrics.Metrics
import de.welt.contentapi.core.client.services.CapiExecutionContext
import de.welt.contentapi.core.client.services.configuration.ServiceConfiguration
import de.welt.contentapi.core.client.services.contentapi.AbstractService
import de.welt.contentapi.core.models.ApiContent
import de.welt.contentapi.pressed.models.{ApiPressedContent, ApiPressedContentResponse, PressedReads}
import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.Future
import scala.util.Try

@ImplementedBy(classOf[DetailsThemeServiceIml])
trait DetailsThemeService {

  /**
   * Search for theme page article and related by path.
   *
   * @param path theme path e.g. /themen/ard
   * @param page page number
   * @param pageSize number of items per page
   * @return theme page article and related articles
   */
  def findDetails(path: String, page: Int, pageSize: Int): Future[ApiPressedContentResponse]
}

@Singleton
class DetailsThemeServiceIml @Inject()(ws: WSClient,
                                       metrics: Metrics,
                                       pressedContentService: PressedContentService,
                                       capi: CapiExecutionContext)
  extends AbstractService[ThemeDetails](ws, metrics, ServiceConfiguration("theme_details_service"), capi)
    with DetailsThemeService {

  import AbstractService.implicitConversions._

  override def validate: WSResponse => Try[ThemeDetails] = response =>
    response.json.result.validate[ThemeDetails](PressedReads.detailsResponseItemReads)


  override def findDetails(path: String, page: Int, pageSize: Int): Future[ApiPressedContentResponse] = {
    val detailsFuture: Future[ThemeDetails] = execute(
      urlArguments = Seq(path),
      parameters = Seq("page" -> page.toString, "pageSize" -> pageSize.toString)
    )

    detailsFuture.map(details => ApiPressedContentResponse(
      source = "theme-service",
      total = Some(details.total),
      pages = Some(details.pages),
      pageSize = Some(details.pageSize),
      currentPage = Some(details.currentPage),
      result = ApiPressedContent(
        content = details.content,
        related = Some(details.related.map(pressedContentService.pressSingleApiContent))
      )
    ))(capi)
  }
}

case class ThemeDetails(currentPage: Int,
                        pageSize: Int,
                        pages: Int,
                        total: Int,
                        content: ApiContent,
                        related: Seq[ApiContent])
