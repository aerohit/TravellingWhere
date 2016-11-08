package services

import java.util.Properties

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

trait LogQueue {
  def publish(topic: String, message: String)
}

class KafkaProducerService(props: Properties) extends LogQueue {
  private val producer = new KafkaProducer[String, String](props)

  override def publish(topic: String, message: String) = {
    val timeAndMessage = s"${System.currentTimeMillis()}|$message"
    val record = new ProducerRecord[String, String](topic, timeAndMessage)
    producer.send(record)
  }
}

object KafkaProducerService {
  // TODO: read the configuration for a config file
  def apply() = {
    val  props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

    new KafkaProducerService(props)
  }
}
