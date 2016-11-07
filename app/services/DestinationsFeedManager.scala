package services

import akka.actor._
import play.api.Logger
import play.api.libs.json.JsValue

import scala.collection.mutable.ArrayBuffer

sealed trait DestinationsFeedProtocol
case object SubscribeToFeed extends DestinationsFeedProtocol
case object UnSubscribeToFeed extends DestinationsFeedProtocol
case class SubscriptionFeed(json: JsValue) extends DestinationsFeedProtocol

class DestinationsFeedManager extends Actor {
  private val remote = context.actorSelection("akka.tcp://DestinationsFeedManager@127.0.0.1:5150/user/DestinationFeedAggregatorActor")
  private val subscribers = new ArrayBuffer[ActorRef]()

  override def preStart() = {
    remote ! "subscribe"
  }

  def receive = {
    case SubscribeToFeed =>
      val subscriber = sender()
      subscribe(subscriber)

    case UnSubscribeToFeed =>
      val subscriber = sender()
      unsubscribe(subscriber)

    case jsonResponse: JsValue =>
      notifySubscribers(jsonResponse)

    case other =>
      // TODO: Log it in a warningful way
      println(s"DestinationsFeedManager received unknown: '$other'")
  }

  private def subscribe(out: ActorRef): Unit = {
    Logger.info("Subscribing to feed.")
    subscribers += out
  }

  private def unsubscribe(subscriber: ActorRef): Unit = {
    val index = subscribers.indexWhere(_ == subscriber)
    if (index > 0) {
      subscribers.remove(index)
      Logger.info("Unsubscribed from feed")
    }
  }

  private def notifySubscribers(json: JsValue) =  {
    subscribers.foreach(s => s ! SubscriptionFeed(json))
  }
}

object DestinationsFeedManager {
  def props = Props[DestinationsFeedManager]
}
