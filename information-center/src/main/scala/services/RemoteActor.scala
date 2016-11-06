package services


import akka.actor.{ActorRef, Actor, ActorSystem, Props}
import akka.kafka.{Subscriptions, ConsumerSettings}
import akka.kafka.scaladsl.Consumer
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object HelloRemote extends App  {
  val system = ActorSystem("HelloRemoteSystem")
  val remoteActor = system.actorOf(Props[RemoteActor], name = "RemoteActor")
  remoteActor ! "The RemoteActor is alive"
}

class RemoteActor extends Actor {
  private val subscribers = new ArrayBuffer[ActorRef]()
  implicit val system = context.system
  implicit val materializer = ActorMaterializer()

  override def preStart() = {

    val consumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
      .withBootstrapServers("localhost:9092")
      .withGroupId("foobar")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    Consumer.plainSource(consumerSettings, Subscriptions.topics("barfoo"))
      .mapAsync(1) { r =>
        println(s"Read record ${r.value()}")
        val obj = r.value()
        subscribers.foreach(s => s ! obj)
        // This I think is futile. Figure out a better way.
        Future(obj)
      }.runWith(Sink.ignore)
  }

  override def receive = {
    case "subscribe" =>
      subscribe(sender())
    case "unsubscribe" =>
      unsubscribe(sender())
    case msg: String =>
      println(s"RemoteActor received message '$msg'")
      sender ! "Hello from the RemoteActor"
  }

  private def subscribe(out: ActorRef): Unit = {
    subscribers += out
  }

  private def unsubscribe(subscriber: ActorRef): Unit = {
    val index = subscribers.indexWhere(_ == subscriber)
    if (index > 0) {
      subscribers.remove(index)
    }
  }
}
