package part5_infrastructure

import akka.actor.{Actor, ActorLogging, ActorSystem, Cancellable, Props, Timers}

import scala.concurrent.duration._

/**
  * Created by Tomohiro on 24 juillet 2019.
  */

object TimersSchedulers extends App {

  class SimpleActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

  val system = ActorSystem("TimersSchedulersDemo")
  val simpleActor = system.actorOf(Props[SimpleActor])

  system.log.info("Scheduling reminder for simpleActor")

//  implicit val executionContext = system.dispatcher
  import system.dispatcher

  system.scheduler.scheduleOnce(1 second) {
    simpleActor ! "reminder"
  }

  val routine : Cancellable = system.scheduler.schedule(1 second, 2 seconds) {
    simpleActor ! "heartbeat"
  }

  system.scheduler.scheduleOnce(5 seconds) {
    routine.cancel()
  }

  /**
    * Exercise: implement a self-closing actor
    *
    *   - if the actor receives a message (anything), you have 1 second to send it another message
    *   - if the time window expires, the actor will stop itself
    *   - if you send another message, the time window is reset
    */


  class SelfClosingActor extends Actor with ActorLogging {
    val timeWindow = 1 second

    var schedule = createTimeoutWindow()

    def createTimeoutWindow() : Cancellable =
      context.system.scheduler.scheduleOnce(1 second) {
        self ! "timeout"
      }

    override def receive: Receive = {
      case "timeout" =>
        log.info("Stopping myself")
        context.stop(self)
      case message =>
        log.info(s"received $message, staying alive")
        schedule.cancel()
        schedule = createTimeoutWindow()
    }
  }

  val selfClosingActor = system.actorOf(Props[SelfClosingActor], "selfClosingActor")
  system.scheduler.scheduleOnce(250 millis) {
    selfClosingActor ! "ping"
  }

  system.scheduler.scheduleOnce(2 seconds) {
    system.log.info("sending pong to the selfClosingActor")
    selfClosingActor ! "pong"
  }


  /**
    * Timer
    */

  case object TimerKey
  case object Start
  case object Reminder
  case object Stop
  class TimerBasedHeartbeatActor extends Actor with ActorLogging with Timers {
    timers.startSingleTimer(TimerKey, Start, 500 millis)

    override def receive: Receive = {
      case Start =>
        log.info("Bootstrapping")
        timers.startPeriodicTimer(TimerKey, Reminder, 1 second)
      case Reminder =>
        log.info("I'm alive")
      case Stop =>
        log.warning("Stopping...")
        timers.cancel(TimerKey)
        context.stop(self)
    }
  }

  val timerHeartbeatActor = system.actorOf(Props[TimerBasedHeartbeatActor], "timerActor")
  system.scheduler.scheduleOnce(5 seconds) {
    timerHeartbeatActor ! Stop
  }
}
