package services

import actors.SubscribableActor
import akka.actor.Actor
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case object StartConsuming

case object RegisterListener

case object UnRegisterListener

class GeoCoordinatesKafkaConsumer(implicit val materializer: ActorMaterializer)
  extends Actor with SubscribableActor[Option[GeoCoordinate]] {

  private val consumerSettings =
    ConsumerSettings(context.system, new StringDeserializer, new StringDeserializer)
      .withBootstrapServers("localhost:9092")
      .withGroupId("madebar")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  override def receive = {
    case StartConsuming =>
      Consumer.plainSource(consumerSettings, Subscriptions.topics("barmade"))
        .mapAsync(1) { r =>
          val geoOpt: Option[GeoCoordinate] = GeoCoordinate.parseFromString(r.value())
          // TODO: Create a custom sink which fires to the listener ActorRef
          notifySubscribers(geoOpt)
          // TODO: nothing shall be done of the following future
          println(geoOpt)
          Future(geoOpt)
        }.runWith(Sink.ignore)
    case RegisterListener =>
      subscribe(sender())
    case UnRegisterListener =>
      unsubscribe(sender())
  }
}
