import javax.inject.Inject

import akka.actor.ActorSystem
import com.google.inject.AbstractModule
import services.DestinationsFeedManager

trait ApplicationActors

class Actors @Inject()(system: ActorSystem) extends ApplicationActors {
  system.actorOf(
    props = DestinationsFeedManager.props,
    name = DestinationsFeedManager.toString
  )
}

class Module extends AbstractModule {

  override def configure() = {
    // Use the system clock as the default implementation of Clock
    // bind(classOf[Clock]).toInstance(Clock.systemDefaultZone)
    bind(classOf[ApplicationActors]).to(classOf[Actors]).asEagerSingleton()
  }

}
