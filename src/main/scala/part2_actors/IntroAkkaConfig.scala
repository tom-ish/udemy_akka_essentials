package part2_actors

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

/**
  * Created by Tomohiro on 23 juillet 2019.
  */

object IntroAkkaConfig {

  class SimpleLoggingActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

  /**
    * 1 - Inline config
    */
  val configString =
    """
      | akka {
      |  loglevel = "DEBUG"
      | }
    """.stripMargin

  val config = ConfigFactory.parseString(configString)
  val system = ActorSystem("ConfigurationDemo", ConfigFactory.load(config))
  val actor = system.actorOf(Props[SimpleLoggingActor])

  actor ! "A message to remember"

  /**
    * 2 - Config file
    */

  val defaultConfigFileSystem = ActorSystem("DefaultConfigFileDemo")
  val defaultConfigActor = defaultConfigFileSystem.actorOf(Props[SimpleLoggingActor])
  defaultConfigActor! "Remember me"

  /**
    * 3 - Seperate config in the same file
    */
  val specialConfig = ConfigFactory.load().getConfig("mySpecialConfig")
  val specialConfigSystem = ActorSystem("SpecialConfigDemo", specialConfig)
  val specialConfigActor = specialConfigSystem.actorOf(Props[SimpleLoggingActor])
  specialConfigActor ! "Remember me, I am special"

  /**
    * 4 - Seperate config in another file
    */
  val seperateConfig = ConfigFactory.load("secretFolder/secretConfiguration.conf")
  println(s"Seperate config log leve: ${seperateConfig.getString("akka.loglevel")}")

  /**
    * 5 - different file formats
    *  JSON, properties
    */
  val jsonConfig = ConfigFactory.load("json/jsonConfig.json")
  println(s"json config: ${jsonConfig.getString("aJSONProperty")}")
  println(s"json config: ${jsonConfig.getString("akka.loglevel")}")

  val propsConfig = ConfigFactory.load("props/propsConfiguration.properties")
  println(s"properties config: ${propsConfig.getString("my.simpleProperty")}")
  println(s"properties config: ${propsConfig.getString("akka.loglevel")}")

}
