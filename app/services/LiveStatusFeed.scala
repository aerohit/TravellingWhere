package services

import akka.actor.{ActorRef, _}
import play.api.Logger
import play.api.libs.json.Json

import scala.collection.mutable.ArrayBuffer

class LiveStatusFeed extends Actor {
  val remote = context.actorSelection("akka.tcp://HelloRemoteSystem@127.0.0.1:5150/user/RemoteActor")

  override def preStart() = {
    remote ! "subscribe"
  }

  def receive = {
    case msg: String =>
      println(s"LocalActor received message: '$msg'")
      LiveStatusFeed.notifySubscribers(msg)
  }
}

object LiveStatusFeed {
  private val subscribers = new ArrayBuffer[ActorRef]()
  // This should run in the same actor system as the HomeController
  implicit val system = ActorSystem("LiveStatusFeed")
  val localActor = system.actorOf(Props[LiveStatusFeed], name = "LiveStatusFeed")

  def notifySubscribers(message: String) =  {
    subscribers.foreach(s => s ! Json.obj("message" -> message))
  }

  def subscribe(out: ActorRef): Unit = {
    subscribers += out
  }

  def unsubscribe(subscriber: ActorRef): Unit = {
    val index = subscribers.indexWhere(_ == subscriber)
    if (index > 0) {
      subscribers.remove(index)
      Logger.info("Unsubscribed client from stream")
    }
  }
}
