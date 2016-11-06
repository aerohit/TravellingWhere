package controllers

import javax.inject._

import akka.actor.ActorSystem
import akka.stream.Materializer
import play.api.libs.json._
import play.api.libs.streams.ActorFlow
import play.api.mvc._
import services.LiveStatusWSConnection

@Singleton
class HomeController @Inject() (implicit system: ActorSystem, materializer: Materializer) extends Controller {
  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def liveState = WebSocket.accept[JsValue, JsValue] { request =>
    ActorFlow.actorRef(out => LiveStatusWSConnection.props(out))
  }
}
