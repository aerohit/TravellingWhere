package controllers

import javax.inject._

import akka.actor.ActorSystem
import akka.stream.Materializer
import play.api.libs.json._
import play.api.libs.streams.ActorFlow
import play.api.mvc._
import services.{DestinationsFeedWSConnection, KafkaProducerService}

@Singleton
class HomeController @Inject()(
  implicit system: ActorSystem,
  materializer: Materializer,
  environment: play.api.Environment,
  configuration: play.api.Configuration) extends Controller {
  // TODO: Have this be injected
  private val logQueue = KafkaProducerService()
  lazy val destinationFeedManager = system.actorSelection("akka://application/user/DestinationsFeedManager")

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def liveState = WebSocket.accept[JsValue, JsValue] { request =>
    ActorFlow.actorRef(out => DestinationsFeedWSConnection.props(destinationFeedManager, out))
  }

  def cityPage(country: String, city: String) = Action { request =>
    logQueue.publish("requestpathdata", request.path)
    Ok(s"You requested $city in $country")
  }
}
