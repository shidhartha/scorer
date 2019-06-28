package example.persistance

import example.model.{Game, GameMeta, GameScore, GameAlreadyInProgressException, Score}
import org.scalatest.{BeforeAndAfterEach, FunSuite}

import scala.util.Failure

class GamePersistanceTest extends FunSuite with BeforeAndAfterEach{

  override def beforeEach(): Unit = {
    GamePersistance.resetData
  }

  test("Adding new game, without score"){
    GamePersistance.startGame(GameMeta(List("tm1", "tm2"),"loc1", "STARTED" ))
    assertResult(
      Some(Game(GameMeta(List("tm1", "tm2"),"loc1", "STARTED" ), None)))(GamePersistance.getGameByKey("tm1"+GamePersistance.teamNameSeperator+"tm2"))
  }

  test("Adding duplicate game") {
    GamePersistance.startGame(GameMeta(List("tm1", "tm2"), "loc1", "STARTED"))

    assertResult(Failure(GameAlreadyInProgressException))(GamePersistance.startGame(GameMeta(List("tm1", "tm3"), "loc2", "STARTED")))
  }

  test("Adding duplicate game in reverse order") {
    GamePersistance.startGame(GameMeta(List("tm1", "tm2"), "loc1", "STARTED"))

    assertResult(Failure(GameAlreadyInProgressException))(GamePersistance.startGame(GameMeta(List("tm2", "tm1"), "loc2", "STARTED")))
  }

  test("Verify if a game exist for a team"){
    GamePersistance.startGame(GameMeta(List("tm1", "tm2"),"loc1", "STARTED" ))
    assert(GamePersistance.gameExistsForTeamName("tm1"))
    assert(GamePersistance.gameExistsForTeamName("tm2"))
    assert(!GamePersistance.gameExistsForTeamName("tm3"))
  }

  test("Game ended..., data cleared"){
    GamePersistance.startGame(GameMeta(List("tm1", "tm2"),"loc1", "STARTED" ))
    GamePersistance.endGame(GameMeta(List("tm1", "tm2"),"loc1", "ENDED" ))
    assert(!GamePersistance.gameExistsForTeamName("tm1"))
    assert(!GamePersistance.gameExistsForTeamName("tm2"))
  }

  test("Score update: valid match, 1st score"){
    GamePersistance.startGame(GameMeta(List("tm1", "tm2"),"loc1", "STARTED" ))
    val gameKey = "tm1"+GamePersistance.teamNameSeperator+"tm2"
    val score_1 = GameScore(
      teams = List("tm1", "tm2"),
      score = Score(batting = "tm1",runs = 1,overs = 1.1)
    )
    GamePersistance.updateScore(score_1)
    assertResult(Game(GameMeta(List("tm1", "tm2"),"loc1", "STARTED" ), Some(score_1)))(
      GamePersistance.getGameByKey(gameKey).get)

  }

  test("Score update: valid match, 2nd score"){
    GamePersistance.startGame(GameMeta(List("tm1", "tm2"),"loc1", "STARTED" ))
    val score_1 = GameScore(
      teams = List("tm1", "tm2"),
      score = Score(batting = "tm1",runs = 1,overs = 1.1)
    )
    val score_2 = GameScore(
      teams = List("tm1", "tm2"),
      score = Score(batting = "tm1",runs = 2,overs = 2.2)
    )
    GamePersistance.updateScore(score_1)
    GamePersistance.updateScore(score_2)
    val gameKey = "tm1"+GamePersistance.teamNameSeperator+"tm2"
    assertResult(Game(GameMeta(List("tm1", "tm2"),"loc1", "STARTED" ), Some(score_2)))(
      GamePersistance.getGameByKey(gameKey).get)
  }

  test("Score update: valid match, outdated score, overs"){
    GamePersistance.startGame(GameMeta(List("tm1", "tm2"),"loc1", "STARTED" ))
    val score_1 = GameScore(
      teams = List("tm1", "tm2"),
      score = Score(batting = "tm1",runs = 1,overs = 1.1)
    )
    val outdated_score = GameScore(
      teams = List("tm1", "tm2"),
      score = Score(batting = "tm1",runs = 1,overs = 1)
    )
    GamePersistance.updateScore(score_1)
    GamePersistance.updateScore(outdated_score)
    val gameKey = "tm1"+GamePersistance.teamNameSeperator+"tm2"
    assertResult(Game(GameMeta(List("tm1", "tm2"),"loc1", "STARTED" ), Some(score_1)))(
      GamePersistance.getGameByKey(gameKey).get)
  }

  test("Score update: valid match, outdated score, runs"){
    GamePersistance.startGame(GameMeta(List("tm1", "tm2"),"loc1", "STARTED" ))
    val score_1 = GameScore(
      teams = List("tm1", "tm2"),
      score = Score(batting = "tm1",runs = 1,overs = 1.1)
    )
    val outdated_score = GameScore(
      teams = List("tm1", "tm2"),
      score = Score(batting = "tm1",runs = 0,overs = 1.1)
    )
    GamePersistance.updateScore(score_1)
    GamePersistance.updateScore(outdated_score)
    val gameKey = "tm1"+GamePersistance.teamNameSeperator+"tm2"
    assertResult(Game(GameMeta(List("tm1", "tm2"),"loc1", "STARTED" ), Some(score_1)))(
      GamePersistance.getGameByKey(gameKey).get)
  }

  test("Score update: valid match, batting changed"){
    GamePersistance.startGame(GameMeta(List("tm1", "tm2"),"loc1", "STARTED" ))
    val score_1 = GameScore(
      teams = List("tm1", "tm2"),
      score = Score(batting = "tm1",runs = 1,overs = 1.1)
    )
    val batting_changed_score = GameScore(
      teams = List("tm1", "tm2"),
      score = Score(batting = "tm2",runs = 1,overs = 10)
    )
    GamePersistance.updateScore(score_1)
    GamePersistance.updateScore(batting_changed_score)
    val gameKey = "tm1"+GamePersistance.teamNameSeperator+"tm2"
    assertResult(Game(GameMeta(List("tm1", "tm2"),"loc1", "STARTED" ), Some(batting_changed_score)))(
      GamePersistance.getGameByKey(gameKey).get)
  }

  test ("get Game details by one of the team name"){
    GamePersistance.startGame(GameMeta(List("tm1", "tm2"),"loc1", "STARTED" ))
    val score_1 = GameScore(
      teams = List("tm1", "tm2"),
      score = Score(batting = "tm1",runs = 1,overs = 1.1)
    )
    GamePersistance.updateScore(score_1)
    assertResult(Some(Game(GameMeta(List("tm1", "tm2"),"loc1", "STARTED" ),Some(score_1) )))(GamePersistance.getGameByTeamName("tm1"))

    val score_2 = GameScore(
      teams = List("tm1", "tm2"),
      score = Score(batting = "tm1",runs = 100,overs = 10.5)
    )
    GamePersistance.updateScore(score_2)
    assertResult(Some(Game(GameMeta(List("tm1", "tm2"),"loc1", "STARTED" ),Some(score_2) )))(GamePersistance.getGameByTeamName("tm2"))

  }

}
