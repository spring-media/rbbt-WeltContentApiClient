package de.welt.contentapi.pressed.client.services

import akka.actor.ActorSystem
import com.codahale.metrics.Timer.Context
import com.kenshoo.play.metrics.Metrics
import de.welt.contentapi.core.client.services.CapiExecutionContext
import de.welt.contentapi.core.models.{ApiContent, ApiSectionData}
import de.welt.contentapi.pressed.models.{ApiChannel, ApiPressedContent, ApiPressedContentResponse}
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.{verify, when}
import org.mockito.{Answers, Mockito}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.http.Status
import play.api.libs.json.Json
import play.api.libs.ws.ahc.AhcWSRequest
import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class DetailsThemeServiceTest extends PlaySpec with MockitoSugar {

  trait TestScope {

    private val actorSystem = ActorSystem("test")
    implicit val executionContext: CapiExecutionContext = CapiExecutionContext(actorSystem, "akka.actor.default-dispatcher")

    val mockWsClient: WSClient = mock[WSClient]
    val mockRequest: AhcWSRequest = mock[AhcWSRequest](Mockito.withSettings().defaultAnswer(Answers.RETURNS_SELF))
    val responseMock: WSResponse = mock[WSResponse]
    val metricsMock: Metrics = mock[Metrics]
    val mockTimerContext: Context = mock[Context]
    val mockPressedContent: PressedContentService = mock[PressedContentService]

    when(mockRequest.execute(anyString())).thenReturn(Future {
      responseMock
    })

    when(metricsMock.defaultRegistry).thenReturn(new com.codahale.metrics.MetricRegistry())
    when(mockWsClient.url(anyString)).thenReturn(mockRequest)
  }

  "DetailsThemeService" should {

    "provide content and related data by theme path" in new TestScope {
      val service = new DetailsThemeServiceIml(mockWsClient, metricsMock, mockPressedContent, executionContext)

      val relatedArticle: ApiContent = ApiContent(webUrl = "", `type` = "", sections = Some(ApiSectionData(home = Some("/test"))))
      val pressedRelatedArticle: ApiPressedContent = ApiPressedContent(
        content = relatedArticle,
        channel = Some(ApiChannel())
      )
      when(mockPressedContent.pressSingleApiContent(relatedArticle)).thenReturn(pressedRelatedArticle)

      when(responseMock.status).thenReturn(Status.OK)
      when(responseMock.json).thenReturn(Json.obj(
          "currentPage" -> 1,
          "pages" -> 1,
          "pageSize" -> 1,
          "total" -> 1,
          "content" -> Json.obj("webUrl" -> "", "type" -> ""),
          "related" -> Json.arr(Json.obj("webUrl" -> "", "type" -> "", "sections" -> Json.obj("home" -> "/test")))
      ))

      val res: ApiPressedContentResponse = Await.result(service.findDetailsByPath("ard", page = 1, pageSize = 1), Duration.Inf)
      assert(res == ApiPressedContentResponse(
        result = ApiPressedContent(
          content = ApiContent(webUrl = "", `type` = ""),
          related = Some(Seq(pressedRelatedArticle))
        ),
        source = "theme-service",
        total = Some(1),
        pages = Some(1),
        pageSize = Some(1),
        currentPage = Some(1)
      ))

      val queryParameters = Seq("path" -> "ard", "page" -> "1", "pageSize" -> "1", "teaserOnly" -> "true")
      verify(mockRequest).withQueryStringParameters(queryParameters: _*)
      verify(mockWsClient).url("localhost/themes")
    }
  }

  "provide content and related data by theme id" in new TestScope {
    val service = new DetailsThemeServiceIml(mockWsClient, metricsMock, mockPressedContent, executionContext)

    val relatedArticle: ApiContent = ApiContent(webUrl = "", `type` = "", sections = Some(ApiSectionData(home = Some("/test"))))
    val pressedRelatedArticle: ApiPressedContent = ApiPressedContent(
      content = relatedArticle,
      channel = Some(ApiChannel())
    )
    when(mockPressedContent.pressSingleApiContent(relatedArticle)).thenReturn(pressedRelatedArticle)

    when(responseMock.status).thenReturn(Status.OK)
    when(responseMock.json).thenReturn(Json.obj(
      "currentPage" -> 1,
      "pages" -> 1,
      "pageSize" -> 1,
      "total" -> 1,
      "content" -> Json.obj("webUrl" -> "", "type" -> ""),
      "related" -> Json.arr(Json.obj("webUrl" -> "", "type" -> "", "sections" -> Json.obj("home" -> "/test")))
    ))

    val res: ApiPressedContentResponse = Await.result(service.findDetailsById("1", page = 1, pageSize = 1), Duration.Inf)
    assert(res == ApiPressedContentResponse(
      result = ApiPressedContent(
        content = ApiContent(webUrl = "", `type` = ""),
        related = Some(Seq(pressedRelatedArticle))
      ),
      source = "theme-service",
      total = Some(1),
      pages = Some(1),
      pageSize = Some(1),
      currentPage = Some(1)
    ))

    val queryParameters = Seq("id" -> "1", "page" -> "1", "pageSize" -> "1", "teaserOnly" -> "true")
    verify(mockRequest).withQueryStringParameters(queryParameters: _*)
    verify(mockWsClient).url("localhost/themes")
  }
}
