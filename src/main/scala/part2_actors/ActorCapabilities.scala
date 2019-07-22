package part2_actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2_actors.ActorCapabilities.Person.LiveTheLife

import scala.collection.immutable.HashMap

/**
  * Created by Tomohiro on 22 juillet 2019.
  */

object ActorCapabilities extends App {

  class SimpleActor extends Actor {
    context.self
    override def receive: Receive = {
      case "Hi !" => sender() ! "Hello there !" // replying to a message
      case message : String => println(s"[$self] I have received $message")
      case number : Int => println(s"[Simple Actor] I have received a NUMBER : $number")
      case SpecialMessage(contents) => println(s"[SimpleActor] I have received something SPECIAL : $contents")
      case SendMessageToYourself(content) =>
        self ! content
      case SayHiTo(ref) => ref ! "Hi !" // <=> (ref ! "Hi !")(self) // alice is being passed as the sender
      case WirelessPhoneMessage(content, ref) => ref forward (content + "s") // I keep the original sender of the WPM
    }
  }

  val system = ActorSystem("actorCapabilitiesDemo")
  val simpleActor = system.actorOf(Props[SimpleActor], "simpleActor")

  simpleActor ! "hello actor"

  // 1 - messages can be of any type
  // a) messages must be IMMUTABLE
  // b) messages must be SERIALIZABLE
  // in practice use case classes and case objects

  simpleActor ! 42 // who is the sender

  case class SpecialMessage(contents: String)
  simpleActor ! SpecialMessage("some special contents")


  // 2 - actors have information about their context and about themselves
  // context.self === `this` in OOP

  case class SendMessageToYourself(contents: String)
  simpleActor ! SendMessageToYourself("I am an actor, and I am proud of it")

  // 3 - actors can REPLY to messages
  val alice = system.actorOf(Props[SimpleActor], "Alice")
  val bob = system.actorOf(Props[SimpleActor], "Bob")

  case class SayHiTo(ref: ActorRef)
  alice ! SayHiTo(bob)


  // 4 - Dead Letters
  alice ! "Hi !" // reply to "me"

  // 5 - Forwarding messages
  // D -> A -> B
  // forwarding = sending a message with the ORIGINAL sender

  case class WirelessPhoneMessage(contents: String, ref: ActorRef)
  alice ! WirelessPhoneMessage("Hi !", bob) // noSender


  /**
    * Exercises
    *
    * 1. a Counter actor
    *   - Increment
    *   - Decrement
    *   - Print
    *
    * 2. a Bank account as an actor
    *   receives :
    *   - Deposit an amount
    *   - Withdraw an amount
    *   - Statement
    *
    *   replies with :
    *   - Success / Failure
    *
    *   interact with other kind of actor
    */


  // 1. Counter actor

  // DOMAIN of the counter
  object CounterActor {
    case object Increment
    case object Decrement
    case object Print

  }
  class CounterActor extends Actor {
    import CounterActor._

    var counter = 0

    override def receive: Receive = {
      case Increment => counter += 1
      case Decrement => counter -= 1
      case Print => println(s"[Counter Actor] Counter is now : $counter")
    }
  }


  import CounterActor._
  val counterActor = system.actorOf(Props[CounterActor], "CounterActor")
   (1 to 5).foreach(_ => counterActor ! Increment)
  (1 to 10).foreach(_ => counterActor ! Decrement)
  counterActor ! Print



  // 2. Bank account
  object BankAccount {
    case class Deposit(amount: Int)
    case class Withdraw(amount: Int)
    case object Statement

    case class TransactionSuccess(message: String)
    case class TransactionFailure(message: String)
  }

  class BankAccount extends Actor {
    import BankAccount._

    var funds = 0
    override def receive: Receive = {
      case Deposit(a) =>
        if(a < 0)
          sender() ! TransactionFailure("Invalid amount for deposit")
        else {
          funds += a
          sender() ! TransactionSuccess(s"Deposit successfully an amount of $a")
        }
      case Withdraw(a) =>
        if(a < 0)
          sender() ! TransactionFailure("Invalid amount for withdraw")
        else if(a > funds)
          sender() ! TransactionFailure("insufficient funds")
        else {
          funds -= a
          sender() ! TransactionSuccess(s"Withdraw successfully an amount of $a")
        }
      case Statement =>
        sender() ! println(s"You have $funds")
    }
  }

  object Person {
    case class LiveTheLife(bankAccount: ActorRef)
  }

  class Person extends Actor {
    import Person._
    import BankAccount._

    override def receive: Receive = {
      case LiveTheLife(account: ActorRef) =>
        account ! Deposit(10000)
        account ! Withdraw(90000)
        account ! Withdraw(500)
        account ! Statement
      case message => println(message.toString)
    }
  }

  val account = system.actorOf(Props[BankAccount], "BankAccount")
  val person = system.actorOf(Props[LiveTheLife], "billionaire")

  person ! LiveTheLife(account)
}
