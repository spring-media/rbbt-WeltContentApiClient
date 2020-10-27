package de.welt.contentapi.pressed.client.services

import akka.actor.ActorSystem
import com.codahale.metrics.Timer.Context
import com.kenshoo.play.metrics.Metrics
import de.welt.contentapi.core.client.services.CapiExecutionContext
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

class SummaryThemeServiceTest extends PlaySpec with MockitoSugar {

  trait TestScope {

    private val actorSystem = ActorSystem("test")
    implicit val executionContext: CapiExecutionContext = CapiExecutionContext(actorSystem, "akka.actor.default-dispatcher")

    val mockWsClient: WSClient = mock[WSClient]
    val mockRequest: AhcWSRequest = mock[AhcWSRequest](Mockito.withSettings().defaultAnswer(Answers.RETURNS_SELF))
    val responseMock: WSResponse = mock[WSResponse]
    val metricsMock: Metrics = mock[Metrics]
    val mockTimerContext: Context = mock[Context]

    when(mockRequest.execute(anyString())).thenReturn(Future {
      responseMock
    })

    when(metricsMock.defaultRegistry).thenReturn(new com.codahale.metrics.MetricRegistry())
    when(mockWsClient.url(anyString)).thenReturn(mockRequest)
  }


  "SummaryThemeService" should {

    "provide list of summaries by letter" in new TestScope {
      val service = new SummaryThemeServiceImpl(mockWsClient, metricsMock, executionContext)

      when(responseMock.status).thenReturn(Status.OK)
      when(responseMock.json).thenReturn(
        Json.arr(Json.obj("path" -> "p1", "id" -> "id1", "title" -> "title1"))
      )

      val res: Seq[ThemeSummary] = Await.result(service.findSummaries(letter = 'a'), Duration.Inf)
      assert(res == Seq(ThemeSummary(path = "p1", id = "id1", title = "title1")))

      verify(mockRequest).withQueryStringParameters(Seq("letter" -> "a"): _*)
    }
  }
}
