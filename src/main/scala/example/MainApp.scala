package example

import akka.actor.{ActorSystem, Props}
import akka.pattern._
import akka.util.Timeout
import example.actors.{GameActor, UdpServerActor}
import example.model.{Game, GetGameForOneTeam}

import scala.concurrent.Await
import scala.concurrent.duration._

object MainApp extends App {

  val actorSystem = ActorSystem("Gully-Cricket")

  val gameActor = actorSystem.actorOf(Props[GameActor])
  val udpServer1 = actorSystem.actorOf(UdpServerActor.props(55501,gameActor ))
  val udpServer2 = actorSystem.actorOf(UdpServerActor.props(55502,gameActor ))

  implicit val timeout = Timeout(5.second)

  while (true) {
    println(s"Enter the name of one of the teams playing the match:")
    val teamName = scala.io.StdIn.readLine()
    val gameInfoFut = gameActor ? GetGameForOneTeam(teamName)
    val gameInfo = Await.result(gameInfoFut, 5.second)
    println("======")
    gameInfo match {
      case Some(info)=>{
        val game: Game = info.asInstanceOf[Game]
        println(s"""Match between \"${game.meta.teams.head}\" & "${game.meta.teams.last}" at "${game.meta.location}"""")

        game.score match {
          case Some(gameScore) =>{
            val score = gameScore.score
            val batSeq = if (score.chasing == 0) "first" else "second"
            val chasingStr = if (score.chasing !=0) s"chasing ${score.chasing} runs" else ""
            println(s""""${score.batting}" is batting $batSeq and has scored ${score.runs} runs in ${score.overs} overs , $chasingStr.""")

          }
          case None =>  println("Game is not started yet !!!")
        }
      }
      case None => println(s""""No match with "$teamName\" is currently in progress""")
    }
    println("======\n")
  }


}
