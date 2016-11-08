package services

import actors.SubscribableActor
import akka.actor._
import play.api.libs.json.JsValue
import protocols.{DestinationFeedAggregatorUnSubscribe, DestinationFeedAggregatorSubscribe}

sealed trait DestinationsFeedProtocol

case object SubscribeToFeed extends DestinationsFeedProtocol

case object UnSubscribeToFeed extends DestinationsFeedProtocol

case class SubscriptionFeed(json: JsValue) extends DestinationsFeedProtocol

class DestinationsFeedManager extends Actor with SubscribableActor[SubscriptionFeed] {
  private val remote =
    context.actorSelection("akka.tcp://DestinationsFeedManager@127.0.0.1:5150/user/DestinationFeedAggregatorActor")

  override def preStart() = {
    remote ! DestinationFeedAggregatorSubscribe
  }

  override def postStop() = {
    remote ! DestinationFeedAggregatorUnSubscribe
  }

  def receive = {
    case SubscribeToFeed =>
      val subscriber = sender()
      subscribe(subscriber)

    case UnSubscribeToFeed =>
      val subscriber = sender()
      unsubscribe(subscriber)

    case jsonResponse: JsValue =>
      notifySubscribers(SubscriptionFeed(jsonResponse))

    case other =>
      // TODO: Log it in a warningful way
      println(s"DestinationsFeedManager received unknown: '$other'")
  }
}

object DestinationsFeedManager {
  def props = Props[DestinationsFeedManager]
}
