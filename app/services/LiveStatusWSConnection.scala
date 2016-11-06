package services

import akka.actor.{Props, Actor, ActorRef}
import play.api.Logger
import play.api.libs.json.Json

class LiveStatusWSConnection(out: ActorRef) extends Actor {
  def receive = {
    case x =>
      // for now, just assuming that any request from the client is for subscription only
      Logger.info(s"Received an untyped message $x")
      out ! Json.obj("text" -> "Hello, World!")
  }
}

object LiveStatusWSConnection {
  def props(out: ActorRef) = Props(new LiveStatusWSConnection(out))
}
