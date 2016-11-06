package services

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DestinationFeedAggregator extends Actor {
  implicit val system = context.system
  implicit val materializer = ActorMaterializer()
  private val subscribers = new ArrayBuffer[ActorRef]()
  private val consumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
    .withBootstrapServers("localhost:9092")
    .withGroupId("madebar")
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  override def preStart() = {
    Consumer.plainSource(consumerSettings, Subscriptions.topics("barmade"))
      .mapAsync(1) { r =>
        println(s"Read record ${r.value()}")
        val obj = r.value()
        subscribers.foreach(s => s ! obj)
        // TODO: This I think is futile. Figure out a better way.
        Future(obj)
      }.runWith(Sink.ignore)
  }

  override def receive = {
    case "subscribe" =>
      println("A SUBSCRIPTION request")
      subscribe(sender())
    case "unsubscribe" =>
      println("A UNSUBSCRIPTION request")
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

object DestinationFeedAggregator extends App {
  val system = ActorSystem("HelloRemoteSystem")
  val remoteActor = system.actorOf(Props[DestinationFeedAggregator], name = "RemoteActor")
  remoteActor ! "The RemoteActor is alive"
}
