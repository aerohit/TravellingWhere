package services

import akka.actor.{Actor, ActorRef, ActorSelection, Props}
import play.api.Logger
import play.api.libs.json.JsValue

class DestinationsFeedWSConnection(destinationFeedManager: ActorSelection, out: ActorRef)
  extends Actor with DestinationFeedProtocolParser with FeedResponseGenerator {

  def receive = {
    case jsonRequest: JsValue =>
      parseAndHandle(jsonRequest)
    case SubscriptionFeed(json) =>
      out ! constructJsonResponse(LiveFeed(json))
    case other =>
      println("UNHANDLED MESSAGE")
      println(other)
  }

  override def postStop() {
    Logger.info("Client unsubscribing from stream")
    destinationFeedManager ! UnSubscribeToFeed
  }

  private def parseAndHandle(jsonRequest: JsValue) = {
    handleRequest(parseRequest(jsonRequest))
  }

  private def handleRequest(request: DestinationFeedProtocol) = request match {
    case DestinationFeedSubscriptionRequest =>
      out ! constructJsonResponse(Subscribing)
      destinationFeedManager ! SubscribeToFeed

    case DestinationFeedUnSubscriptionRequest =>
      destinationFeedManager ! UnSubscribeToFeed

    case DestinationFeedUnknownRequest =>
      out ! constructJsonResponse(UnknownRequest)
  }
}

object DestinationsFeedWSConnection {
  def props(destinationFeedManager: ActorSelection, out: ActorRef) =
    Props(new DestinationsFeedWSConnection(destinationFeedManager, out))
}
