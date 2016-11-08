package services

import actors.SubscribableActor
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.stream.ActorMaterializer
import play.api.libs.json.{JsArray, JsValue, Json}
import protocols.{DestinationFeedAggregatorSubscribe, DestinationFeedAggregatorUnSubscribe}

import scala.concurrent.duration._

class DestinationFeedAggregator(geoCoordinatesKafkaConsumer: ActorRef) extends Actor with SubscribableActor[JsValue] {
  import formatters.JsonFormatters._

  private var destinationCounter = Map.empty[GeoCoordinate, Int]

  override def preStart() = {
    geoCoordinatesKafkaConsumer ! RegisterListener
  }

  override def postStop() = {
    geoCoordinatesKafkaConsumer ! UnRegisterListener
  }

  override def receive = {
    case geo: GeoCoordinate =>
      println(s"found $geo")
      notifySubscribers(updatedState(geo))
    case DestinationFeedAggregatorSubscribe =>
      println("A SUBSCRIPTION request")
      val subscriber = sender()
      subscribe(sender())
      subscriber ! getStateAsJson
    case DestinationFeedAggregatorUnSubscribe =>
      println("A UNSUBSCRIPTION request")
      unsubscribe(sender())
    case other =>
      println(s"Unhandled Message: $other")
  }

  def updatedState(geo: GeoCoordinate): JsValue = {
    updateCount(geo)
    getStateAsJson
  }

  private def updateCount(geo: GeoCoordinate): Unit = {
    val currentCount = destinationCounter.getOrElse(geo, 0)
    destinationCounter = destinationCounter.updated(geo, currentCount + 1)
  }

  private def getStateAsJson: JsValue = {
    val counts: List[JsValue] = destinationCounter.map { case (g, c) =>
      GeoCodeWithCount(g.country, g.city, g.latitude, g.longitude, c)
    }.map(Json.toJson(_)).toList
    JsArray(counts)
  }

}

object DestinationFeedAggregator extends App {
  implicit val system = ActorSystem("DestinationsFeedManager")
  implicit val materializer = ActorMaterializer()
  val geoCoordinatesKafkaConsumer = system.actorOf(
    Props(new GeoCoordinatesKafkaConsumer),
    name = "GeoCoordinatesKafkaConsumer"
  )

  val feedAggregator = system.actorOf(
    Props(new DestinationFeedAggregator(geoCoordinatesKafkaConsumer)),
    name = "DestinationFeedAggregatorActor"
  )

  geoCoordinatesKafkaConsumer ! StartConsuming
}

case class GeoCodeWithCount(country: String, city: String, latitude: String, longitude: String, count: Int)
