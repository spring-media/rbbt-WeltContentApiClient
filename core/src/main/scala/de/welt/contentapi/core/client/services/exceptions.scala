package de.welt.contentapi.core.client.services

import play.api.PlayException
import play.api.http.Status

object exceptions extends Status {

  abstract class HttpStatusCodeException(statusCode: Int, statusPhrase: String, uri: String)
    extends PlayException(s"HttpStatusCodeException[$statusCode]", s"$statusPhrase while requesting '$uri'") {

    def getStatusCode: Int = statusCode

    def getStatusPhrase: String = statusPhrase

    def getUrl: String = uri

    override def toString: String = super.toString
  }

  case class HttpRedirectException(uri: String, statusPhrase: String)
    extends HttpStatusCodeException(MOVED_PERMANENTLY, statusPhrase, uri)

  case class HttpClientErrorException(statusCode: Int, statusPhrase: String, uri: String, cacheHeader: Option[String] = None)
    extends HttpStatusCodeException(statusCode, statusPhrase, uri)

  case class HttpServerErrorException(statusCode: Int, statusPhrase: String, uri: String)
    extends HttpStatusCodeException(statusCode, statusPhrase, uri)

}
