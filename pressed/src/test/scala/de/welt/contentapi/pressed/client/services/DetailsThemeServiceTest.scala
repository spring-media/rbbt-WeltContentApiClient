package de.welt.contentapi.pressed.client.services

import akka.actor.ActorSystem
import com.codahale.metrics.Timer.Context
import com.kenshoo.play.metrics.Metrics
import de.welt.contentapi.core.client.services.CapiExecutionContext
import de.welt.contentapi.core.models.ApiContent
import de.welt.contentapi.pressed.models.{ApiPressedContent, ApiPressedContentResponse}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.http.Status
import play.api.libs.json.{JsArray, JsNumber, JsObject, JsString}
import play.api.libs.ws.ahc.AhcWSRequest
import play.api.libs.ws.{WSAuthScheme, WSClient, WSResponse}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class DetailsThemeServiceTest extends PlaySpec with MockitoSugar {

  trait TestScope {

    private val actorSystem = ActorSystem("test")
    implicit val executionContext: CapiExecutionContext = CapiExecutionContext(actorSystem, "akka.actor.default-dispatcher")

    val mockWsClient: WSClient = mock[WSClient]
    val mockRequest: AhcWSRequest = mock[AhcWSRequest]
    val responseMock: WSResponse = mock[WSResponse]
    val metricsMock: Metrics = mock[Metrics]
    val mockTimerContext: Context = mock[Context]

    when(mockRequest.withHttpHeaders(ArgumentMatchers.any())).thenReturn(mockRequest)
    when(mockRequest.addHttpHeaders(ArgumentMatchers.any())).thenReturn(mockRequest)
    when(mockRequest.withQueryStringParameters(ArgumentMatchers.any())).thenReturn(mockRequest)
    when(mockRequest.withAuth(anyString, anyString, ArgumentMatchers.eq(WSAuthScheme.BASIC))).thenReturn(mockRequest)
    when(mockRequest.withBody(anyString)(ArgumentMatchers.any())).thenReturn(mockRequest)
    when(mockRequest.withRequestTimeout(ArgumentMatchers.any())).thenReturn(mockRequest)

    when(mockRequest.execute(anyString())).thenReturn(Future {
      responseMock
    })

    when(metricsMock.defaultRegistry).thenReturn(new com.codahale.metrics.MetricRegistry())
    when(mockWsClient.url(anyString)).thenReturn(mockRequest)
  }

  "ThemeDetailsService" should {

    "provide content and related data by theme path" in new TestScope {
      val service = new DetailsThemeServiceIml(mockWsClient, metricsMock, executionContext)

      when(responseMock.status).thenReturn(Status.OK)
      when(responseMock.json).thenReturn(JsObject(
        Seq(
          "currentPage" -> JsNumber(1),
          "pages" -> JsNumber(1),
          "pageSize" -> JsNumber(1),
          "total" -> JsNumber(1),
          "content" -> JsObject(Seq("webUrl" -> JsString(""), "type" -> JsString(""))),
          "related" -> JsArray()
        )
      ))

      val res: ApiPressedContentResponse = Await.result(service.find("ard", page = 1, pageSize = 1), Duration.Inf)
      assert(res == ApiPressedContentResponse(
        result = ApiPressedContent(
          content = ApiContent(webUrl = "", `type` = ""),
          related = Some(Seq())
        ),
        source = "theme-service",
        total = Some(1),
        pages = Some(1),
        pageSize = Some(1),
        currentPage = Some(1)
      ))

      verify(mockRequest).withQueryStringParameters(Seq("page" -> "1", "pageSize" -> "1"): _*)
      verify(mockWsClient).url("localhost/themes/ard")
    }
  }
}
