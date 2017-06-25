# Snakes And Ladders #

Note where ever a reference is made to `game state` this means json payload representation of the game..  
Eg

```json
{
   "_id":{"$oid":"594fe2ef331a00213f900ade"},
  "player":[
        {"identifier":"player1","dice":0,"tokenLocation":1,"roll":true},
        {"identifier":"player2","dice":0,"tokenLocation":1,"roll":false}
        ],
   "computer":10,
   "snakes":[
          {"start":12,"end":2}
          ],
   "ladders":[{
          "start":2,"end":12}
          ],
   "state":true
}

```
#### Starting game ####

You must create a post request to the following endpoint  
`POST localhost:9000/startGame/${start}`

| Response  | Meaning | Body|  
| ------------- | ------------- | ------------- |
| 201  | New game created  | game state |
| 202  | Game already exist  | game state |
| 400  | Param is not a number  | |

if **${start} > 0** This starts game for a user against a computer: user id = "player1"  
if **${start} < 1** This starts game for two users: "player1" and "player2"

#### Roll Dice ####

To roll dice you must make a post request to the following endpoint  

`POST localhost:9000/diceRoll/${gameId}/${playerId}`

| Response  | Meaning | Body|  
| ------------- | ------------- | ------------- |
| 200  | Dice rolled  | number |
| 403  | unable to roll dice |  |      
 


###### Parameters ######   
`${gameId}` The game id  
`${playerId} `The player id


#### Move Token ####
To move token make a post request to the following endpoint

`POST localhost:9000/moveToken/${gameId}/${playerId}`
   
   
| Response  | Meaning | Body|  
| ------------- | ------------- | ------------- |
| 200  | Player won  | player id |
| 202  | move made | game state|
| 304  | player was skipped |  |
| 400  | game does not exist |  |
| 401| player does not exist|  |
| 405  | player needs to roll dice | player id |



###### Parameters ######   
`${gameId}` The game id  
`${playerId}` The player id



## Running Tests ##

#### Unit Test ####
`test:test`

#### Integration Test ####
`it:test`

#### Gatling Test ####
This demonstrates how to play against the computer  
NOTE: These test expects the collection to be initially empty  
`gatling:test`

