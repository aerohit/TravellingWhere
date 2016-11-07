package services

import akka.actor._
import play.api.libs.json.JsValue

class DestinationsFeedManager extends Actor {
  val remote = context.actorSelection("akka.tcp://HelloRemoteSystem@127.0.0.1:5150/user/RemoteActor")

  override def preStart() = {
    remote ! "subscribe"
  }

  def receive = {
    case jsonResponse: JsValue =>
      DestinationsFeedSubscriptionPool.notifySubscribers(jsonResponse)
    case other =>
      // TODO: Log it in a warningful way
      println(s"DestinationsFeedManager received unknown: '$other'")
  }
}

object DestinationsFeedManager {
  def props = Props[DestinationsFeedManager]
}
