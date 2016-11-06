package services

import akka.actor._
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.kafka.{ConsumerSettings, ProducerSettings, Subscriptions}
import akka.stream.ActorMaterializer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object UriRequestToRichObjectTransformer extends App {
  implicit val system = ActorSystem("Request-Processor")
  implicit val materializer = ActorMaterializer()

  val consumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
    .withBootstrapServers("localhost:9092")
    .withGroupId("madebar")
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  val producerSettings = ProducerSettings(system, new StringSerializer, new StringSerializer)
    .withBootstrapServers("localhost:9092")

  Consumer.plainSource(consumerSettings, Subscriptions.topics("madebar"))
    .mapAsync(1) { r =>
      println(s"Read record ${r.value()}")
      // TODO: there shouldn't be a need to call get here, check if filter is possible
      val geo = GeoCoordinatesService.enrich(r.value()).get
      Future(new ProducerRecord[String, String]("barmade", geo.serializeToString()))
    }.runWith(Producer.plainSink(producerSettings))
}
