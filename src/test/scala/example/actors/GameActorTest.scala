package example.actors

import akka.actor.{ActorSystem, PoisonPill, Props}
import akka.testkit.{ImplicitSender, TestKit}
import example.model.{ClearDb, Game, GameAlreadyInProgressException, GameMeta, GameNotInProgress, GameScore, GetGameForOneTeam, Score}
import org.scalatest.{BeforeAndAfterEach, FunSuite, FunSuiteLike, Matchers, WordSpecLike}

import scala.util.{Failure, Success}

class GameActorTest extends TestKit(ActorSystem("TestSystem"))
  with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterEach {

  val actor = system.actorOf(Props[GameActor])

  override def beforeEach(): Unit = {
    actor ! ClearDb
  }

  " GameActor" should {
    "start a game when a match state is STARTED" in {
      val actor = system.actorOf(Props[GameActor])
      actor ! GameMeta(List("tm1", "tm2"),"loc1", "STARTED" )
      expectMsg(Success(()))
    }
    "END a game when the game is started" in {
      actor ! GameMeta(List("tm1", "tm2"),"loc1", "STARTED" )
      expectMsg(Success(()))
      actor ! GameMeta(List("tm1", "tm2"),"loc1", "ENDED" )
      expectMsg(Success(()))
    }

    "not END a game when the game is not started" in {
      actor ! GameMeta(List("tm3", "tm4"),"loc1", "ENDED" )
      expectMsg(Failure(GameNotInProgress))
    }

    "NOT start a game when a match is already started" in {
      actor ! GameMeta(List("tm1", "tm2"),"loc1", "STARTED" )
      expectMsg(Success(()))
      actor ! GameMeta(List("tm1", "tm2"),"loc1", "STARTED" )
      expectMsg(Failure(GameAlreadyInProgressException))
    }

    "should update the score, and should be able to retrive the same" in {
      actor ! GameMeta(List("tm1", "tm2"),"loc1", "STARTED" )
      expectMsg(Success(()))

      val score_1 = GameScore(
        teams = List("tm1", "tm2"),
        score = Score(batting = "tm1",runs = 1,overs = 1.1)
      )
      actor ! score_1
      expectMsg(())
      actor ! GetGameForOneTeam("tm1")
      expectMsg(Some(Game(GameMeta(List("tm1", "tm2"),"loc1", "STARTED" ), Some(score_1))))

      val score_2 = GameScore(
        teams = List("tm2", "tm1"),
        score = Score(batting = "tm2",runs = 2,overs = 1.2)
      )
      actor ! score_2
      expectMsg(())
      actor ! GetGameForOneTeam("tm2")
      expectMsg(Some(Game(GameMeta(List("tm1", "tm2"),"loc1", "STARTED" ), Some(score_2))))


    }

  }

}
