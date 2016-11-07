package services

import akka.actor.{Actor, ActorRef, Props}
import play.api.Logger
import play.api.libs.json.{JsValue, Json}

class DestinationsFeedWSConnection(out: ActorRef) extends Actor {

  def receive = {
    case jsonRequest: JsValue =>
      parseAndHandle(jsonRequest)
    case other =>
      println("UNHANDLED MESSAGE")
      println(other)
  }

  override def postStop() {
    Logger.info("Client unsubscribing from stream")
    DestinationsFeedSubscriptionPool.unsubscribe(out)
  }

  private def parseAndHandle(jsonRequest: JsValue) = {
    handleRequest(parseRequest(jsonRequest))
  }

  private def parseRequest(jsonRequest: JsValue): DestinationFeedProtocol = {
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

  private def handleRequest(request: DestinationFeedProtocol) = request match {
    case DestinationFeedSubscriptionRequest =>
      out ! Json.obj("responseType" -> "SUBSCRIBING")
      DestinationsFeedSubscriptionPool.subscribe(out)

    case DestinationFeedUnSubscriptionRequest =>
      DestinationsFeedSubscriptionPool.unsubscribe(out)

    case DestinationFeedUnknownRequest =>
      out ! Json.obj("responseType" -> "UNKNOWN_REQUEST")
  }

}

object DestinationsFeedWSConnection {
  def props(out: ActorRef) = Props(new DestinationsFeedWSConnection(out))
}

sealed trait DestinationFeedProtocol

case object DestinationFeedSubscriptionRequest extends DestinationFeedProtocol

case object DestinationFeedUnSubscriptionRequest extends DestinationFeedProtocol

case object DestinationFeedUnknownRequest extends DestinationFeedProtocol
