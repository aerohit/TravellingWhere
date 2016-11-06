package services

import akka.actor.{Actor, ActorSystem, Props}

object Local extends App {
  implicit val system = ActorSystem("LocalSystem")
  val localActor = system.actorOf(Props[LocalActor], name = "LocalActor")
  localActor ! "START"

}

class LocalActor extends Actor {
  val remote = context.actorSelection("akka.tcp://HelloRemoteSystem@127.0.0.1:5150/user/RemoteActor")

  override def preStart() = {
    remote ! "subscribe"
  }

  def receive = {
    case "START" =>
      remote ! "Hello from the LocalActor"
    case msg: String =>
      println(s"LocalActor received message: '$msg'")
  }
}
