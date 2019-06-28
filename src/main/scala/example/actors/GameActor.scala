package example.actors

import akka.actor.Actor
import example.model._
import example.persistance.GamePersistance

class GameActor extends Actor {

  override def receive: Receive = {
    case msg: GameMeta if msg.state.equalsIgnoreCase("STARTED")=> {
      val mySender = sender()
      val res = GamePersistance.startGame(msg)
      mySender ! res  // for UT only
    }
    case msg: GameMeta if msg.state.equalsIgnoreCase("ENDED")=> {
      val mySender = sender()
      val res = GamePersistance.endGame(msg)
      mySender ! res  // for UT only
    }
    case msg: GameScore => {
      val mySender = sender()
      val res = GamePersistance.updateScore(msg)
      mySender ! res  // for UT only
    }
    case msg: GetGameForOneTeam => {
      val mySender = sender()
      val gameOp =  GamePersistance.getGameByTeamName(msg.temaName)
      mySender ! gameOp
    }
    case ClearDb => GamePersistance.clearDb // for UT only
    case _ => ???

  }
}



