package playground

import akka.actor.ActorSystem

/**
  * Created by Tomohiro on 21 juillet 2019.
  */

object Playground extends App {

  val actorSystem = ActorSystem("HelloA kka")
  println(actorSystem.name)

}
