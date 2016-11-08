package services

import actors.SubscribableActor
import akka.actor._
import play.api.libs.json.JsValue
import protocols.{DestinationFeedAggregatorSubscribe, DestinationFeedAggregatorUnSubscribe}

sealed trait DestinationsFeedProtocol

case object SubscribeToFeed extends DestinationsFeedProtocol

case object UnSubscribeToFeed extends DestinationsFeedProtocol

case class SubscriptionFeed(json: JsValue) extends DestinationsFeedProtocol

class DestinationsFeedManager extends Actor with SubscribableActor[SubscriptionFeed] {
  private val remote =
    context.actorSelection("akka.tcp://DestinationsFeedManager@127.0.0.1:5150/user/DestinationFeedAggregatorActor")
  private var latestFeed: Option[SubscriptionFeed] = None

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
      latestFeed.foreach(feed => subscriber ! feed)

    case UnSubscribeToFeed =>
      val subscriber = sender()
      unsubscribe(subscriber)

    case jsonResponse: JsValue =>
      latestFeed = Option(SubscriptionFeed(jsonResponse))
      latestFeed.foreach(feed => notifySubscribers(feed))

    case other =>
      // TODO: Log it in a warningful way
      println(s"DestinationsFeedManager received unknown: '$other'")
  }
}

object DestinationsFeedManager {
  def props = Props[DestinationsFeedManager]
}
