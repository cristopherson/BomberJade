
import java.util.Iterator;
import java.util.LinkedList;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author cristopherson
 */
public class BomberPlayerAgent extends Agent {

    private boolean logged = false;
    private boolean moving = false;
    private BomberPlayer player;
    private GridCoordinates new_pos, prev_pos;
    private LinkedList<GridCoordinates> bombs;
    private LinkedList<GridCoordinates> enemies;

    protected void setup() {
        Object[] args = this.getArguments();
        player = (BomberPlayer) args[0];

        String localName = getAID().getLocalName();
        //playerId = Integer.parseInt(localName.replaceAll("Bomber", ""));

        // Printout a welcome message
        System.out.println("Hello World. I’m a bomber agent!");
        System.out.println("My local-name is " + localName);
        System.out.println("I play for team " + player.team);

        /*        System.out.println("My GUID is " + getAID().getName());
         System.out.println("My addresses are:");*/

        /* this agent will execute this behaviour every 500 ms */
        addBehaviour(new TickerBehaviour(this, 500) {
            protected void onTick() {
                // perform operation Y
                new_pos = new GridCoordinates();
                prev_pos = new GridCoordinates();
                /* get the first message on my message queue.
                 */
                ACLMessage msg = receive();
                int x = player.x >> BomberMain.shiftCount;
                int y = player.y >> BomberMain.shiftCount;

                /* message has been received */
                if (msg != null) {
                    System.out.println(getAID().getLocalName() + " got message " + msg.getContent());
                    int performative = msg.getPerformative();
                    String content = msg.getContent();
                    System.out.println("message is of performative " + performative);
                    switch (performative) {
                        case ACLMessage.CONFIRM:
                            logged = true;
                            break;
                        case ACLMessage.INFORM:
                            String args[] = content.split(":");

                            if (args[0].equals("Player")) {
                                int receivedPlayer = Integer.parseInt(args[1]);
                                int receivedTeam = Integer.parseInt(args[2]);
                                int receivedX = Integer.parseInt(args[3]);
                                int receivedY = Integer.parseInt(args[4]);

                                /* Is mine the reported position? */
                                if (receivedPlayer == player.playerNo) {
                                    System.out.println("Player " + receivedPlayer + " is at position (" + receivedX + "," + receivedY + ")");
                                    new_pos.x = receivedX;
                                    new_pos.y = receivedY;

                                    /* I'm attempting to go somewhere but I can't... there's a wall.
                                     * blow it up!
                                     */
                                    if (new_pos.x == prev_pos.x && new_pos.y == prev_pos.y && moving) {
                                        moveRequest(4);
                                    } else {
                                        prev_pos.x = new_pos.x;
                                        prev_pos.y = new_pos.y;
                                    }
                                /* Is an enemy position what I've received? */
                                } else if (receivedTeam != player.team) {
                                        System.out.println(receivedPlayer + " player's enemy detected at position (" + receivedX + "," + receivedY + ")");
                                        Iterator<GridCoordinates> i = enemies.iterator();
                                        GridCoordinates current;
                                        boolean found = false;

                                        while (i.hasNext()) {
                                                current = i.next();
                                                if (current.id == receivedPlayer) {
                                                        current.x = receivedX;
                                                        current.y = receivedY;
                                                        found = true;
                                                        break;
                                                }
                                        }

                                        if (!found) {
                                                current = new GridCoordinates();
                                                current.id = receivedPlayer;
                                        current.x = receivedX;
                                        current.y = receivedY;
                                                enemies.add(current);
                                        }
                                /* Is it an ally then? */
                                } else {
                                        /* TODO: Do I care? Maybe, if we want to work in teams... but later */
                                        System.out.println("Player " + receivedPlayer + " is on " + player.playerNo + "'s team");
                                }

                            } else if (args[0].equals("Dead")) {
                                int receivedPlayer = Integer.parseInt(args[1]);
                                int receivedTeam = Integer.parseInt(args[2]);

                                /* only care about enemies for now */
                                if (receivedTeam != player.team) {
                                        System.out.println(receivedPlayer + "dead enemy " + receivedPlayer + " detected");
                                        Iterator<GridCoordinates> i = enemies.iterator();
                                        GridCoordinates current;
                                        boolean found = false;

                                        while (i.hasNext()) {
                                                current = i.next();
                                                if (current.id == receivedPlayer) {
                                                        enemies.remove(i);
                                                        found = true;
                                                        break;
                                                }
                                        }

                                        if (!found) {
                                                System.out.println("Dead enemy not in list!?!");
                                        }
                                /* Is it an ally then? */
                                }

                            } else if (args[0].equals("Bomb")) {
                                int receivedX = Integer.parseInt(args[1]);
                                int receivedY = Integer.parseInt(args[2]);

                                GridCoordinates current = new GridCoordinates();
                                current.x = receivedX;
                                current.y = receivedY;

                                bombs.add(current);

                                System.out.println(player.playerNo + " player's detected a bomb at position (" + receivedX + "," + receivedY + ")");

                            } else if (args[0].equals("Explosion")) {
                                int receivedX = Integer.parseInt(args[1]);
                                int receivedY = Integer.parseInt(args[2]);

                                System.out.println(player.playerNo + " player's detected an explosion at position (" + receivedX + "," + receivedY + ")");
                                Iterator<GridCoordinates> i = bombs.iterator();
                                GridCoordinates current;
                                boolean found = false;

                                while (i.hasNext()) {
                                        current = i.next();
                                        if (current.x == receivedX && current.y == receivedY) {
                                                bombs.remove(i);
                                                found = true;
                                                break;
                                        }
                                }

                                if (!found) {
                                        System.out.println("Exploded bomb not found!?!");
                                }
                            }
                            break;
                        default:
                            System.out.println("Unexpected type message " + performative + "" + content);
                    }
                }

                /* Send a message if I have not said my name */
                if (!logged) {
                    msg = new ACLMessage(ACLMessage.SUBSCRIBE);
                    msg.addReceiver(new AID("Cris", AID.ISLOCALNAME));
                    msg.setLanguage("English");
                    msg.setOntology("Weather-forecast-ontology");
                    msg.setContent("Hi All\nI am " + getAID().getLocalName());
                    send(msg);
                    /* no message received but I already said my name
                     * Request the scenario to move me.  
                     *  */
                } else {
                    /*
                    msg = new ACLMessage(ACLMessage.REQUEST);
                    msg.addReceiver(new AID("Cris", AID.ISLOCALNAME));
                    msg.setLanguage("English");
                    msg.setOntology("Weather-forecast-ontology");
                    int move = (int) (Math.random() * 5);
                    msg.setContent("Move:" + move);
                    send(msg);
                    */

                    if (new_pos != null) {
                        int move = -1;

                        do {
                            move = MoveValidator.nextMove(new_pos.x, new_pos.y, new_pos.x, new_pos.y + 1);
                            if (move != -1) {
                                break;
                            }
                            move = MoveValidator.nextMove(new_pos.x, new_pos.y, new_pos.x, new_pos.y - 1);
                            if (move != -1) {
                                break;
                            }
                            move = MoveValidator.nextMove(new_pos.x, new_pos.y, new_pos.x - 1, new_pos.y);
                            if (move != -1) {
                                break;
                            }
                            move = MoveValidator.nextMove(new_pos.x, new_pos.y, new_pos.x + 1, new_pos.y);
                            if (move == -1) {
                                System.out.println("Can not move at at all");
                            }
                        } while (false);

                        if (move != -1) {
                            msg.setContent("Move:" + move);
                            send(msg);
                        }
                    }
                }

            }
        }
        );

    }

    /**
     * Send move request to host agent.
     * Movements are interpreted as follows:
     * UP    = 0
     * DOWN  = 1
     * LEFT  = 2
     * RIGHT = 3
     * BOMB  = 4
     */
    private void moveRequest(int move) {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(new AID("Cris", AID.ISLOCALNAME));
        msg.setLanguage("English");
        msg.setOntology("Weather-forecast-ontology");
        msg.setContent("Move:" + move);
        send(msg);
        /* TODO: would I need to change this somewhere else? */
        moving = true;
    }
}
