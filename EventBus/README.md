## Requirements
The use case for this task is as bellow:

1. create 2 players

2. one of the players should send a message to second player (let's call this player "initiator")

3. when a player receives a message should send back a new message that contains the received message concatenated with 
the message counter that this player sent.

4. finalize the program (gracefully) after the initiator sent 10 messages and received back 10 messages (stop condition)

5. both players should run in the same java process (strong requirement)

# Solution Architecture
Design of architecture is EventBus.

An Eventbus is a mechanism that allows different components to communicate with each other without 
knowing about each other. A component can send an Event to the Eventbus without knowing who will pick it up
or how many others will pick it up. Components can also listen to Events on an Eventbus, without knowing
who sent the Events. That way, components can communicate without depending on each other. Also, it is very easy 
to substitute a component. As long as the new component understands the Events that are being sent and received, 
the other components will never know.

In our case, we create 2 Player and they are chatting each other.

![Alt text](EventBus.png?raw=true )

#Building and running application

```shell 
./run.sh
```