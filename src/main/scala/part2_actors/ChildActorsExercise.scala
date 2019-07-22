package part2_actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

/**
  * Created by Tomohiro on 22 juillet 2019.
  */

object
ChildActorsExercise extends App {

  // Distributed Word counting

  object WordCounterMaster {
    case class Initialize(nChildren : Int)
    case class WordCountTask(id: Int, text: String)
    case class WordCountReply(id: Int, count: Int)
  }
  class WordCounterMaster extends Actor {
    import WordCounterMaster._

    var workers = List[ActorRef]()

    override def receive: Receive = {
      case Initialize(nChildren) =>
        println("[ Master ] Initializing...")
        val childrenRefs = for (i <- 1 to nChildren)
            yield context.actorOf(Props[WordCounterWorker], s"worker_$i")
        context.become(withChildren(childrenRefs,0, 0, Map()))
    }

    def withChildren(childrenRefs: Seq[ActorRef], currentChildIndex: Int, currentTaskId: Int, requestMap: Map[Int, ActorRef]): Receive = {
      case content : String => {
        println(s"[ Master ] I have received < ${content} > I will send it to child $currentChildIndex")
        val originalSender = sender()
        val task = WordCountTask(currentTaskId, content)
        val childRef = childrenRefs(currentChildIndex)
        childRef ! task
        val nextChildIndex = (currentChildIndex + 1) % childrenRefs.length
        val newTaskId = currentTaskId + 1
        val newRequestMap = requestMap + (currentTaskId -> originalSender)
        context.become(withChildren(childrenRefs, nextChildIndex, newTaskId, newRequestMap))
      }
      case WordCountReply(id, count) =>
        val originalSender = requestMap(id)
        originalSender ! count
        context.become(withChildren(childrenRefs, currentChildIndex, currentTaskId, requestMap - id))
    }
  }

  class WordCounterWorker extends Actor {
    import WordCounterMaster._

    override def receive: Receive = {
      case WordCountTask(id, content) =>
        println(s"[ ${self.path} ] I have rceived task $id with $content")
        sender() ! WordCountReply(id, content.split(" ").length)
    }
  }

  /*
    create WordCounterMaster
    send Initalize(10) to wordCounterMaster
    send "Akka is awesome" to wcm
      wcm will send a WordCountTask("...") to one of its children
        child replies with a WordCountReply(3) to the master
      master replies with 3 to the sender

    requester -> wcm -> wcw
            r <- wcm <-
   */
  // round robin logic
  // 1, 2, 3, 4, 5 actors and 7 tasks
  // 1, 2, 3, 4, 5, 1, 2


  class TestActor extends Actor {
    import WordCounterMaster._

    override def receive: Receive = {
      case "go" =>
        val master = context.actorOf(Props[WordCounterMaster], "master")
        master ! Initialize(3)

        val texts = List("Akka is awesome", "Scala is super dope", "yes", "me too")

        texts.foreach(master ! _)
      case count: Int => println(s"[ TestActor ] I have received a result : $count")
    }
  }

  val system = ActorSystem("RoundRobinWordCountExercise")
  val testActor = system.actorOf(Props[TestActor], "testActor")

  testActor ! "go"


}
