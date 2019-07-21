package part2_actors

import akka.actor.Actor

/**
  * Created by Tomohiro on 22 juillet 2019.
  */

object ActorCapabilities extends App {

  class SimpleActor extends Actor {
    override def receive: Receive = {
      case message : String => println(s"[Simple Actor] I have receive $message")
    } 
  }
}
