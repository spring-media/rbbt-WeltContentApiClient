package de.welt.contentapi.pressed.client.services

import com.google.inject.{ImplementedBy, Inject, Singleton}
import com.kenshoo.play.metrics.Metrics
import de.welt.contentapi.core.client.services.CapiExecutionContext
import de.welt.contentapi.core.client.services.configuration.ServiceConfiguration
import de.welt.contentapi.core.client.services.contentapi.AbstractService
import de.welt.contentapi.core.models.ApiContent
import de.welt.contentapi.pressed.models.{ApiPressedContent, ApiPressedContentResponse, PressedReads, ThemeDetails}
import play.api.libs.json.{Json, Reads}
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
  def find(path: String, page: Int, pageSize: Int): Future[ApiPressedContentResponse]
}

@Singleton
class DetailsThemeServiceIml @Inject()(ws: WSClient,
                                    metrics: Metrics,
                                    capi: CapiExecutionContext)
  extends AbstractService[ThemeDetails](ws, metrics, ServiceConfiguration("theme_service"), capi)
    with DetailsThemeService {

  import AbstractService.implicitConversions._

//  implicit lazy val detailsResponseItem: Reads[ThemeDetails] = Json.reads[ThemeDetails]

  override def validate: WSResponse => Try[ThemeDetails] = response =>
    response.json.result.validate[ThemeDetails](PressedReads.detailsResponseItemReads)


  override def find(path: String, page: Int, pageSize: Int): Future[ApiPressedContentResponse] = {
    val detailsFuture: Future[ThemeDetails] = executeWithEndpoint(
      endpoint = "/themes/%s",
      urlArguments = Seq(path),
      parameters = Seq("page" -> page.toString, "pageSize" -> pageSize.toString)
    )

    detailsFuture.map(details => ApiPressedContentResponse(
      source = "nevermind",
      total = Some(details.total),
      pages = Some(details.pages),
      pageSize = Some(details.pageSize),
      currentPage = Some(details.currentPage),
      result = ApiPressedContent(
        content = details.content,
        related = Some(details.related.map(relatedArticle => ApiPressedContent(content = relatedArticle.copy(roles = Some(List("more-from-theme-page"))))))
      )
    ))(capi)
  }
}
