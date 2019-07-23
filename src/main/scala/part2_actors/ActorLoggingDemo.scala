package part2_actors

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.event.Logging

/**
  * Created by Tomohiro on 23 juillet 2019.
  */

object ActorLoggingDemo extends App {

  // #1 - explicit Logging
  class SimpleActorWithExplicitLogger extends Actor {
    val logger = Logging(context.system, this)

    override def receive: Receive = {
      /*
        1 - DEBUG
        2 - INFO
        3 - WARNING
        4 - ERROR
       */
      case message => logger.info(message.toString) // LOG IT
    }
  }

  val system = ActorSystem("LoggingDemo")
  val actor = system.actorOf(Props[SimpleActorWithExplicitLogger])

  actor ! "Logging a simple message"


  // #2 - Actor Logging
  class ActorWithLogging extends Actor with ActorLogging {
    override def receive: Receive = {
      case (a, b) => log.info("Two things {} and {}", a, b) // Two things 2 and 3
      case message => log.info(message.toString)
    }
  }

  val simpleActor = system.actorOf(Props[ActorWithLogging])
  simpleActor ! "Logging a simple message by extending a trait "
}
