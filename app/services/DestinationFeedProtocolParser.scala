package services

import play.api.libs.json.JsValue

sealed trait DestinationFeedProtocol

case object DestinationFeedSubscriptionRequest extends DestinationFeedProtocol

case object DestinationFeedUnSubscriptionRequest extends DestinationFeedProtocol

case object DestinationFeedUnknownRequest extends DestinationFeedProtocol

trait DestinationFeedProtocolParser {
  def parseRequest(jsonRequest: JsValue): DestinationFeedProtocol = {
    val requestType = (jsonRequest \ "requestType").asOpt[String]
    requestType match {
      case Some("SUBSCRIBE") =>
        DestinationFeedSubscriptionRequest

      case Some("UNSUBSCRIBE") =>
        DestinationFeedUnSubscriptionRequest

      case _ =>
        DestinationFeedUnknownRequest
    }
  }
}
