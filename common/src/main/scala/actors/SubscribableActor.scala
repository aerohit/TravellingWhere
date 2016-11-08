package actors

import akka.actor.ActorRef

import scala.collection.mutable.ArrayBuffer

trait SubscribableActor[T] {
  private val subscribers = new ArrayBuffer[ActorRef]()

  final def notifySubscribers(obj: T): Unit = {
    subscribers.foreach(s => s ! obj)
  }

  final def subscribe(out: ActorRef): Unit = {
    subscribers += out
  }

  final def unsubscribe(subscriber: ActorRef): Unit = {
    val index = subscribers.indexWhere(_ == subscriber)
    if (index > 0) {
      subscribers.remove(index)
    }
  }
}
