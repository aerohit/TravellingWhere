package services

import akka.actor._
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.kafka.{ConsumerSettings, ProducerSettings, Subscriptions}
import akka.stream.ActorMaterializer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}

object RequestUriToGeoCoordinateTransformer extends App {
  implicit val system = ActorSystem("Request-Processor")
  implicit val materializer = ActorMaterializer()

  val consumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
    .withBootstrapServers("localhost:9092")
    .withGroupId("RequestPathConsumer")
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  val producerSettings = ProducerSettings(system, new StringSerializer, new StringSerializer)
    .withBootstrapServers("localhost:9092")

  Consumer.plainSource(consumerSettings, Subscriptions.topics("requestpathdata"))
    .map(r => GeoCoordinatesService.enrich(r.value()))
    .collect { case Some(geo) => geo }
    .map(convertToRecord)
    .runWith(Producer.plainSink(producerSettings))

  def convertToRecord(geo: GeoCoordinate): ProducerRecord[String, String] = {
    new ProducerRecord[String, String]("geocoordinatedata", geo.serializeToString())
  }
}
