package services

import actors.SubscribableActor
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.stream.ActorMaterializer
import play.api.libs.json.{JsArray, JsValue, Json}
import protocols._

import scala.concurrent.duration._

class DestinationFeedAggregator(geoCoordinatesKafkaConsumer: ActorRef)
  extends Actor with SubscribableActor[DestinationFeedAggregatorUpdate[JsValue]] {
  import formatters.JsonFormatters._

  import context.dispatcher
  private val sesstionTimeSeconds = 15
  private val tick =
    context.system.scheduler.schedule(2 seconds, 5 seconds, self, DestinationFeedAggregatorNotifySubscribers)
  private var geoCoordinates = List.empty[GeoCoordinate]

  override def preStart() = {
    geoCoordinatesKafkaConsumer ! RegisterListener
  }

  override def postStop() = {
    geoCoordinatesKafkaConsumer ! UnRegisterListener
    tick.cancel()
  }

  override def receive = {
    case geo: GeoCoordinate =>
      println(s"found $geo")
      updateGeoCoordinates(geo)
    case DestinationFeedAggregatorSubscribe =>
      println("A SUBSCRIPTION request")
      val subscriber = sender()
      subscribe(sender())
      subscriber ! getStateAsJson
    case DestinationFeedAggregatorUnSubscribe =>
      println("A UNSUBSCRIPTION request")
      unsubscribe(sender())
    case DestinationFeedAggregatorNotifySubscribers =>
      purgeOldResults()
      notifySubscribers(DestinationFeedAggregatorUpdate(getStateAsJson))
    case other =>
      println(s"Unhandled Message: $other")
  }

  private def updateGeoCoordinates(geo: GeoCoordinate): Unit = {
    geoCoordinates = geo :: geoCoordinates
  }

  private def getStateAsJson: JsValue = {
    val counts = geoCoordinates
      .groupBy(_.city)
      .values
      .map((geos: List[GeoCoordinate]) => geos.head -> geos.length)
      .map { case (g, c) =>
        GeoCodeWithCount(g.country, g.city, g.latitude, g.longitude, c)
      }
      .map(Json.toJson(_))
      .toList
    JsArray(counts)
  }

  private def purgeOldResults() = {
    val currentTime = System.currentTimeMillis()
    geoCoordinates = geoCoordinates.filter(g => withinSessionTime(currentTime, g.requestTime))
  }

  private def withinSessionTime(currentTimeMills: Long, requestTimeMills: Long): Boolean = {
    (currentTimeMills - requestTimeMills) < sesstionTimeSeconds * 1000
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
