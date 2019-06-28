package example.persistance

import example.model.{Game, GameAlreadyInProgressException, GameMeta, GameNotInProgress, GameScore, Score}

import scala.collection.mutable
import scala.util.{Failure, Success, Try}

trait Persistance{
  def startGame(gameMeta: GameMeta): Try[Unit]
  def endGame(gameMeta: GameMeta): Try[Unit]
  def getGameByKey(key: String):Option[Game]
  def getGameByTeamName(teamName: String): Option[Game]
  def updateScore(gScore:GameScore): Unit
  def clearDb: Unit
}

object GamePersistance extends Persistance {
  val teamNameSeperator = "@"
  private var gameDb = new mutable.HashMap[String, Game]()

  private def getKey(teams: List[String]): String = {
    teams.sorted.mkString(teamNameSeperator)
  }

  override def startGame(gameMeta: GameMeta): Try[Unit] = {
    if (gameMeta.teams.exists(gameExistsForTeamName(_)))
      Failure(GameAlreadyInProgressException)
    else {
      val game = gameDb.+=((getKey(gameMeta.teams), Game(gameMeta, None)))
//      println(s"StartGame:$gameDb")
      Success()
    }
  }

  override def endGame(gameMeta: GameMeta): Try[Unit] = {
    val key = getKey(gameMeta.teams)
    getGameByKey(key).map(_ => {
      gameDb.-=(key)
//      println(s"endGame:$gameDb")
      Success(())
    }).getOrElse(Failure(GameNotInProgress))
  }

  override def getGameByKey(key: String): Option[Game] = gameDb.get(key)


  override def updateScore(gScore: GameScore): Unit = {
    def isBattingTeamChanged(currentScore: GameScore, batting: String): Boolean = {
      !currentScore.score.batting.equalsIgnoreCase(batting)
    }

    def isLatestScore(currentScore: GameScore, latestScore: GameScore): Boolean = {
      currentScore.score.overs < latestScore.score.overs ||
        currentScore.score.runs < latestScore.score.runs
    }

    val key = getKey(gScore.teams)
    val game = getGameByKey(key).map { game =>

      val currentScore = game.score
      currentScore match {
        case None => gameDb.update(key, Game(game.meta, Some(gScore)))
        case Some(score) => if (isBattingTeamChanged(score, gScore.score.batting)) {
          // update the score
          val newGame = Game(game.meta, Some(gScore))
          gameDb.update(key, newGame)
        } else {
          // verify if the over is increasing or runs is increasing
          if (isLatestScore(score, gScore)) {
            val newGame = Game(game.meta, Some(gScore))
            gameDb.update(key, newGame)
          }
        }
      }
//      println(s"updateScore:$gameDb")
    }
  }

  def gameExistsForTeamName(name: String): Boolean = {
    gameDb.keySet.map(_.split(teamNameSeperator).exists(_.toUpperCase.equals(name.toUpperCase))).exists(bool => bool)
  }

  private def gameForTeamName(name: String): Option[Game] = {
    gameDb.keySet.filter(_.toUpperCase.contains(name.toUpperCase)).toList match {
      case Nil => None
      case key :: _ => getGameByKey(key)
    }
  }

  private[persistance] def resetData = gameDb = mutable.HashMap.empty

  override def getGameByTeamName(teamName: String): Option[Game] = gameForTeamName(teamName)

  override def clearDb: Unit = gameDb.clear
}