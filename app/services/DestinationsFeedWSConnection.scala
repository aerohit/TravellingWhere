package services

import akka.actor.{Props, Actor, ActorRef}
import play.api.Logger
import play.api.libs.json.{JsValue, Json}

case class StatusFeedUpdate(msg: String)

class DestinationsFeedWSConnection(out: ActorRef) extends Actor {
  def receive = {
    case x: JsValue =>
      // for now, just assuming that any request from the client is for subscription only
      Logger.info(s"Received an untyped message $x")
      out ! Json.obj("text" -> "Making a subscription")
      DestinationsFeedManager.subscribe(out)
    case unhandled =>
      println("UNHANDLED MESSAGE")
      println(unhandled)
  }

  override def postStop() {
    Logger.info("Client unsubscribing from stream")
    DestinationsFeedManager.unsubscribe(out)
  }

}

object DestinationsFeedWSConnection {
  def props(out: ActorRef) = Props(new DestinationsFeedWSConnection(out))
}
