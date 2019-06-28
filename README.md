# scorer
Gully cricket scorer app


Design:
This is an akka actor based implementation for a gully cricket scorer game. The persistance is in-memory data structure and the game can be started /end by sending UDP messages as

To start a game:
```
{
	"teams":["team-bangalore", "team-delhi"],
	"location":"BBMP Cricket Grounds",
	"state": "started"
}
```
To End a game
```
{
	"teams":["team-bangalore", "team-delhi"],
	"location":"BBMP Cricket Grounds",
	"state": "ended"
}
```
To set a score:
```
{
    "teams": ["team-bangalore", "team-delhi"],

    "score": {

	    "batting": "team-bangalore",

	    "runs": 99,

	    "overs": 15,

	    "chasing": 0  

	    }
}
```
or
```
{
    "teams": ["team-bangalore", "team-delhi"],

    "score": {

	    "batting": "team-delhi",

	    "runs": 55,

	    "overs": 12,

	    "chasing": 99  

	    }
}
```

From console anytiem the score can be checked by inputting any team name. if the game is in-progress it will display;
```
Enter the name of one of the teams playing the match:
team-bangalore
======
Match between "team-bangalore" & "team-delhi" at "BBMP Cricket Grounds"
Game is not started yet !!!
======

Enter the name of one of the teams playing the match:
team-bbb
======
"No match with "team-bbb" is currently in progress
======

Enter the name of one of the teams playing the match:
team-delhi
======
Match between "team-bangalore" & "team-delhi" at "BBMP Cricket Grounds"
"team-bangalore" is batting first and has scored 98 runs in 15.0 overs , .
======

Enter the name of one of the teams playing the match:
team-bangalore
======
Match between "team-bangalore" & "team-delhi" at "BBMP Cricket Grounds"
"team-delhi" is batting second and has scored 55 runs in 12.0 overs , chasing 99 runs.
======
```

How to run the program:
1. clone the repo
2. go into the directory "scorer"
2. using sbt, the app can be run with command "sbt run" from inside the project folder
3. using UDP client , the game details/score can be set/updated by communicating at port number 55501 and 55502.

