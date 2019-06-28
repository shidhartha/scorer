package example.actors

import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets

import akka.actor.{Actor, ActorRef, Props}
import akka.io.{IO, Udp}
import example.model.{Game, GameMeta, GameScore, UdpMessage}

import scala.util.Try

class UdpServerActor(port: Int, gameController: ActorRef) extends Actor {
  import context.system
  IO(Udp) ! Udp.Bind(self, new InetSocketAddress("localhost", port))

  override def receive = {
    case Udp.Bound(local) =>
//      println(s"Udp server bind completed on port=$port... waiting for data...")
      context.become(readyForUdpMessage(sender()))
  }

  private def parseUdpMessage(dataRecieved: String):Try[UdpMessage] = {
//    println(s"UDP: Processing $dataRecieved")
    import example.model.JsonFormat._
    import spray.json._
    val gameMeta = Try(dataRecieved.parseJson.convertTo[GameMeta])
    if (gameMeta.isFailure) {
      val gameScore = Try(dataRecieved.parseJson.convertTo[GameScore])
//      println(s"GameScoreRecieved: $gameScore")
      gameScore
    } else {
      gameMeta
    }

  }

  def readyForUdpMessage(socket: ActorRef): Receive = {
    case Udp.Received(data, remote) =>{
      val dataRecieved = data.decodeString(StandardCharsets.UTF_8)

      val message = parseUdpMessage(dataRecieved)
      message.map { _ match {
        case gameMeta :GameMeta =>
//          println(s"GameMeta recieved: $gameMeta")
          gameController ! gameMeta
        case gameScore: GameScore =>
//          println(s"GameScore recieved: $gameScore")
          gameController ! gameScore
      }}

    }
    case Udp.Unbind  => socket ! Udp.Unbind
    case Udp.Unbound => context.stop(self)
  }
}

object UdpServerActor {
  def props(port: Int, gameController: ActorRef): Props = {
    Props(new UdpServerActor(port, gameController))
  }
}
