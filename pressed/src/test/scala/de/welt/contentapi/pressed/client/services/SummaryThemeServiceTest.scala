package de.welt.contentapi.pressed.client.services

import akka.actor.ActorSystem
import com.codahale.metrics.Timer.Context
import com.kenshoo.play.metrics.Metrics
import de.welt.contentapi.core.client.services.CapiExecutionContext
import de.welt.contentapi.pressed.models.ThemeSummary
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.http.Status
import play.api.libs.json.{JsArray, JsObject, JsString}
import play.api.libs.ws.ahc.AhcWSRequest
import play.api.libs.ws.{WSAuthScheme, WSClient, WSResponse}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class SummaryThemeServiceTest extends PlaySpec with MockitoSugar {

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


  "ThemeService" should {

    "provide list of summaries by letter" in new TestScope {
      val service = new SummaryThemeServiceImpl(mockWsClient, metricsMock, executionContext)

      when(responseMock.status).thenReturn(Status.OK)
      when(responseMock.json).thenReturn(JsArray(Seq(
        JsObject(Seq("path" -> JsString("p1"), "id" -> JsString("id1"), "title" -> JsString("title1")))
      )))

      val res: Seq[ThemeSummary] = Await.result(service.find(letter = 'a'), Duration.Inf)
      assert(res == Seq(ThemeSummary(path = "p1", id = "id1", title = "title1")))

      verify(mockRequest).withQueryStringParameters(Seq("letter" -> "a"): _*)
    }
  }
}
