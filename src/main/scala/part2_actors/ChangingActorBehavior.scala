package part2_actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2_actors.ActorCapabilities.{CounterActor, system}
import part2_actors.ChangingActorBehavior.Mom.MomStart

/**
  * Created by Tomohiro on 22 juillet 2019.
  */

object ChangingActorBehavior extends App {

  object FussyKid {
    case object KidAccept
    case object KidReject

    val HAPPY = "happy"
    val SAD = "sad"
  }
  class FussyKid extends Actor {
    import FussyKid._
    import Mom._

    // internal state of the kid
    var state = HAPPY
    override def receive: Receive = {
      case Food(VEGETABLES) => state = SAD
      case Food(CHOCOLATE) => state = HAPPY
      case Ask(_) =>
        if(state == HAPPY) sender() ! KidAccept
        else sender() ! KidReject
    }
  }

  class StatelessFussyKid extends Actor {
    import FussyKid._
    import Mom._

    override def receive: Receive = happyReceive

    def happyReceive : Receive = {
      case Food(VEGETABLES) => context.become(sadReceive, false) // change my receive handler to sadReceive
      case Food(CHOCOLATE) =>
      case Ask(_) => sender() ! KidAccept
    }
    def sadReceive : Receive = {
      case Food(VEGETABLES) => context.become(sadReceive, false)
      case Food(CHOCOLATE) => context.unbecome() // change my receive handler to happyReceive
      case Ask(_) => sender() ! KidReject
    }
  }

  object Mom {
    case class MomStart(kidRef: ActorRef)
    case class Food(food: String)
    case class Ask(message: String)

    val VEGETABLES = "veggies"
    val CHOCOLATE = "chocolate"
  }
  class Mom extends Actor {
    import Mom._
    import FussyKid._

    override def receive: Receive = {
      case MomStart(kidRef) =>
        // test our interaction
        kidRef ! Food(VEGETABLES)
        kidRef ! Food(VEGETABLES)
        kidRef ! Food(CHOCOLATE)
        kidRef ! Food(CHOCOLATE)
        kidRef ! Ask("Do you want to play")
      case KidAccept => println("Yay, my kid is happy")
      case KidReject => println("My kid is sad...")
    }
  }

  val system = ActorSystem("changingActorBehavior")
  val fussyKid = system.actorOf(Props[FussyKid])
  val statelessFussyKid = system.actorOf(Props[StatelessFussyKid])
  val mom = system.actorOf(Props[Mom])

  mom ! MomStart(statelessFussyKid)

  /*
    mom receives MomStart
      kid receives Food(VEG) -> kid will change the handler to sadReceive
      kid receives Ask(play?) -> kid replies with the sadReceive handler =>
    mom receives KidReject
   */

  /*

  context.become()

    Food(VEG) -> stack.push(sadReceive)
    Food(CHOCOLATE) -> stack.push(happyReceive)

    Stack :
    1 - happyReceive
    2 - sadReceive
    3 - happyReceive
   */

  /*

  context.unbecome()

    new behavior
    Food(VEG)
    Food(VEG)
    Food(CHOCOLATE)
    Food(CHOCOLATE)

    Stack :

    1 - happyReceive
   */


  /**
    * Exercise 1 - Recreate the Counter Actor with context.become and NO MUTABLE STATE
    */
  object CounterActor {
    case object Increment
    case object Decrement
    case object Print

  }
  class CounterActor extends Actor {
    import CounterActor._

    override def receive(): Receive = countReceive(0)

    def countReceive(currentCount: Int) : Receive = {
      case Increment => {
        println(s"[ $currentCount ] incrementing")
        context.become(countReceive(currentCount + 1))
      }
      case Decrement => {
        println(s"[ $currentCount ] decrementing")
        context.become(countReceive(currentCount - 1))
      }
      case Print => println(currentCount)
    }
  }


  import CounterActor._
  val counterActor = system.actorOf(Props[CounterActor], "CounterActor")
//  (1 to 5).foreach(_ => counterActor ! Increment)
//  (1 to 10).foreach(_ => counterActor ! Decrement)
//  counterActor ! Print


  /**
    * Exercise 2 - Simplified voting system
    */
  case class Vote(candidate: String)
  case object VoteStatusRequest
  case class VoteStatusReply(candidate: Option[String])

  class Citizen extends Actor {
    override def receive: Receive = {
      case Vote(candidate) =>
        context.become(voted(candidate))
      case VoteStatusRequest => sender() ! VoteStatusReply(None)
    }

    def voted(candidate: String) : Receive = {
      case VoteStatusRequest => sender() ! VoteStatusReply(Some(candidate))
    }
  }

  case class AggregateVotes(citizens: Set[ActorRef])

  class VoteAggregator extends Actor {

    override def receive: Receive = awaitingCommand

    def awaitingCommand : Receive = {
      case AggregateVotes(citizens) => {
        citizens.foreach(_ ! VoteStatusRequest)
        context.become(awaitingStatuses(citizens, Map()))
      }
    }

    def awaitingStatuses(stillWaiting : Set[ActorRef], currentStats : Map[String, Int]) : Receive = {
      case VoteStatusReply(None) =>
        // a citizen has not voted yet
        sender() ! VoteStatusRequest
      case VoteStatusReply(Some(candidate)) => {
        println(candidate)
        val newStillWaiting = stillWaiting - sender()
        val currentVotesOfCandidates = currentStats.getOrElse(candidate, 0)
        val newStats = currentStats + (candidate -> (currentVotesOfCandidates + 1))
        if (newStillWaiting.isEmpty)
          println(s"[ VoteAggregator ] poll stats: $newStats")
        else
          // still need to process some statuses from som other citizen
          context.become(awaitingStatuses(newStillWaiting, newStats))
      }

    }
  }

  val alice = system.actorOf(Props[Citizen])
  val bob = system.actorOf(Props[Citizen])
  val charlie = system.actorOf(Props[Citizen])
  val daniel = system.actorOf(Props[Citizen])

  alice ! Vote("Martin")
  bob ! Vote("Jonas")
  charlie ! Vote("Roland")
  daniel ! Vote("Roland")

  val voteAggregator = system.actorOf(Props[VoteAggregator])
  voteAggregator ! AggregateVotes(Set(alice, bob, charlie, daniel))

  /*
    Print the status of the votes
      Martin -> 1
      Jonas -> 1
      Roland -> 2
   */
}
