package services

import akka.actor.{Actor, ActorSystem, Props}

object Local extends App {
  implicit val system = ActorSystem("LocalSystem")
  val localActor = system.actorOf(Props[LocalActor], name = "LocalActor")
  localActor ! "START"

}

class LocalActor extends Actor {
  val remote = context.actorSelection("akka.tcp://HelloRemoteSystem@127.0.0.1:5150/user/RemoteActor")

  def receive = {
    case "START" =>
      remote ! "Hello from the LocalActor"
      // TODO: could it be moved to preStart()?
      remote ! "subscribe"
    case msg: String =>
      println(s"LocalActor received message: '$msg'")
  }
}
