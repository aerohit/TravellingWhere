package services

import akka.actor.ActorRef
import play.api.Logger
import play.api.libs.json.{JsObject, JsValue, Json}

import scala.collection.mutable.ArrayBuffer

object DestinationsFeedSubscriptionPool {
  private val subscribers = new ArrayBuffer[ActorRef]()

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

  def notifySubscribers(json: JsValue) =  {
    subscribers.foreach(s => s ! constructResponse(json))
  }

  private def constructResponse(json: JsValue): JsObject = {
    Json.obj("responseType" -> "LIVE_FEED", "responseData" -> json)
  }
}
