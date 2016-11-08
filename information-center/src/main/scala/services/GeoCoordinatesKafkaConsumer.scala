package services

import actors.SubscribableActor
import akka.Done
import akka.actor.Actor
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.ActorMaterializer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer

import scala.concurrent.Future

case object StartConsuming

case object RegisterListener

case object UnRegisterListener

class GeoCoordinatesKafkaConsumer(implicit val materializer: ActorMaterializer)
  extends Actor with SubscribableActor[GeoCoordinate] {
  var hasStartedConsuming = false

  override def receive = {
    case StartConsuming if !hasStartedConsuming =>
      hasStartedConsuming = true
      startConsumptionTask()
    case RegisterListener =>
      subscribe(sender())
    case UnRegisterListener =>
      unsubscribe(sender())
  }

  private def startConsumptionTask(): Future[Done] = {
    Consumer.plainSource(consumerSettings, Subscriptions.topics("geocoordinatedata"))
      .map(r => GeoCoordinate.parseFromString(r.value()))
      .collect { case Some(geo) => geo }
      .runForeach(notifySubscribers)
  }

  private val consumerSettings =
    ConsumerSettings(context.system, new StringDeserializer, new StringDeserializer)
      .withBootstrapServers("localhost:9092")
      .withGroupId("GeocoordinateConsumer")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
}
