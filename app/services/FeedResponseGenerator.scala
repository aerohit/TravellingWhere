package services

import play.api.libs.json.{JsValue, JsObject, Json}

sealed trait FeedResponse

case object Subscribing extends FeedResponse

case object UnknownRequest extends FeedResponse

case class LiveFeed(responseData: JsValue) extends FeedResponse

trait FeedResponseGenerator {
  def constructJsonResponse(feedResponse: FeedResponse): JsObject = feedResponse match {
    case Subscribing =>
      Json.obj("responseType" -> "SUBSCRIBING")
    case UnknownRequest =>
      Json.obj("responseType" -> "UNKNOWN_REQUEST")
    case LiveFeed(json) =>
      Json.obj("responseType" -> "LIVE_FEED", "responseData" -> json)
  }
}
