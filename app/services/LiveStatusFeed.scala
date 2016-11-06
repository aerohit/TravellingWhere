package services

import akka.actor.{ActorRef, _}
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import play.api.Logger
import play.api.libs.json.Json
import scala.concurrent.ExecutionContext.Implicits.global

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future

object LiveStatusFeed {
  private val subscribers = new ArrayBuffer[ActorRef]()
  // This should run in the same actor system as the HomeController
  implicit val system = ActorSystem("LiveStatusFeed")
  implicit val materializer = ActorMaterializer()

  val consumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
    .withBootstrapServers("localhost:9092")
    .withGroupId("foobar")
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  Consumer.plainSource(consumerSettings, Subscriptions.topics("barfoo"))
    .mapAsync(1) { r =>
      println(s"Read record ${r.value()}")
      val obj = Json.obj("message" -> r.value())
      subscribers.foreach(s => s ! obj)
      // This I think is futile. Figure out a better way.
      Future(obj)
    }.runWith(Sink.ignore)

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
