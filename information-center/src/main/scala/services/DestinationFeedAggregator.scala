package services

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import play.api.libs.json.{JsArray, JsValue, Json}

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DestinationFeedAggregator extends Actor {
  implicit val system = context.system
  implicit val materializer = ActorMaterializer()
  implicit val geoCoordinateFormat = Json.format[GeoCoordinate]
  implicit val geoCoordinateWithCountFormat = Json.format[GeoCodeWithCount]

  private val subscribers = new ArrayBuffer[ActorRef]()
  private var destinationCounter = Map.empty[GeoCoordinate, Int]
  private val consumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
    .withBootstrapServers("localhost:9092")
    .withGroupId("madebar")
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  override def preStart() = {
    Consumer.plainSource(consumerSettings, Subscriptions.topics("barmade"))
      .mapAsync(1) { r =>
        println(s"Read record ${r.value()}")
        val geoOpt: Option[GeoCoordinate] = GeoCoordinate.parseFromString(r.value())
        println(s"Geo: $geoOpt")
        updateCount(geoOpt)
        val obj: JsValue = getStateAsJson
        println(obj)
        //        val obj = r.value()
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

  private def updateCount(geoOpt: Option[GeoCoordinate]) = {
    geoOpt.foreach { geo =>
      val currentCount = destinationCounter.getOrElse(geo, 0)
      destinationCounter = destinationCounter.updated(geo, currentCount + 1)
    }
  }

  private def getStateAsJson: JsValue = {
    val counts: List[JsValue] = destinationCounter.map {case (g, c) =>
        GeoCodeWithCount(g.country, g.city, g.latitude, g.longitude, c)
    }.map(Json.toJson(_)).toList
    JsArray(counts)
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

  case class GeoCodeWithCount(country: String, city: String, latitude: String, longitude: String, count: Int)
}

object DestinationFeedAggregator extends App {
  val system = ActorSystem("HelloRemoteSystem")
  val remoteActor = system.actorOf(Props[DestinationFeedAggregator], name = "RemoteActor")
  remoteActor ! "The RemoteActor is alive"
}
