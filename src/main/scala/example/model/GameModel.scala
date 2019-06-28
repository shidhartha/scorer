package example.model

sealed trait UdpMessage

case class GameMeta(teams: List[String], location: String, state: String) extends UdpMessage {
  require(teams.length ==2, "There should be 2 teams for a match")
  require(teams.forall(name => name.trim.length > 0 && name.matches("^[a-zA-Z0-9-]*$")), "team name should be not empty and alpha numeric")
  require(state.toUpperCase().equals("STARTED") || state.toUpperCase().equals("ENDED"), "State should be either STARTED or ENDED")
}

case class GameScore(teams: List[String], score: Score) extends UdpMessage {
  require(teams.length ==2, "There should be 2 teams for a match")
  require(teams.forall(name => name.trim.length > 0 && name.matches("^[a-zA-Z0-9-]*$")), "team name should be not empty and alpha numeric")

}

case class Score(batting:String, runs: Int, overs: Double, chasing: Int = 0)

case class Game(meta: GameMeta, score: Option[GameScore])
case object ClearDb
case class GetGameForOneTeam(temaName: String)

case object GameAlreadyInProgressException extends Exception
case object GameNotInProgress extends Exception

object JsonFormat {
  import spray.json._
  import DefaultJsonProtocol._

  implicit val scoreFormat = jsonFormat4(Score)
  implicit val gameMetaFormat = jsonFormat3(GameMeta)
  implicit val gameScoreFormat = jsonFormat2(GameScore)
  implicit val gameFormat = jsonFormat2(Game)
}



