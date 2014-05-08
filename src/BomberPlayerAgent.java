
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
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
    private BomberPlayer player;

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
        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                // perform operation Y
                GridCoordinates new_pos = new GridCoordinates();

                /* get the first message on my message queue.
                 */
                ACLMessage msg = receive();

                /* whenever I can destroy something, do so */
                if(MoveValidator.hasElementAround(player.map, BomberMap.BRICK, player.prev_pos.x, player.prev_pos.y)) {
                    moveRequest(BomberPlayer.BOMB);
                }


                /* message has been received */
                if (msg != null) {
                    //System.out.println(getAID().getLocalName() + " got message " + msg.getContent());
                    int performative = msg.getPerformative();
                    String content = msg.getContent();
                    //System.out.println("message is of performative " + performative);
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
                                    //System.out.println("I am at position (" + receivedX + "," + receivedY + ")");
                                    new_pos.x = receivedX;
                                    new_pos.y = receivedY;

                                    /* TODO: may need extra locks here to prevent placing bombs if moving away from one */
                                    if (new_pos.x == player.prev_pos.x && new_pos.y == player.prev_pos.y) {
                                        player.samePlace = true;
                                    } else {
                                        player.samePlace = false;
                                    }

                                    /* I'm attempting to go somewhere but I can't... there's a wall.
                                     * blow it up!
                                     */
                                    /*
                                    if (player.samePlace) {
                                        if(MoveValidator.hasElementAround(player.map, BomberMap.BRICK, new_pos.x, new_pos.y))
                                              moveRequest(BomberPlayer.BOMB);
                                    }
                                    */

                                    player.prev_pos.x = new_pos.x;
                                    player.prev_pos.y = new_pos.y;
                                /* Is an enemy position what I've received? */
                                } else if (receivedTeam != player.team) {
                                        //System.out.println(receivedPlayer + " player's enemy detected at position (" + receivedX + "," + receivedY + ")");
                                        Iterator<GridCoordinates> i = player.enemies.iterator();
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
                                                player.enemies.add(current);
                                        }
                                /* Is it an ally then? */
                                } else {
                                        /* TODO: Do I care? Maybe, if we want to work in teams... but later */
                                        //System.out.println("Player " + receivedPlayer + " is on " + player.playerNo + "'s team");
                                }

                            } else if (args[0].equals("Dead")) {
                                int receivedPlayer = Integer.parseInt(args[1]);
                                int receivedTeam = Integer.parseInt(args[2]);

                                /* only care about enemies for now */
                                if (receivedTeam != player.team) {
                                        System.out.println(receivedPlayer + "dead enemy " + receivedPlayer + " detected");
                                        Iterator<GridCoordinates> i = player.enemies.iterator();
                                        GridCoordinates current;
                                        boolean found = false;

                                        while (i.hasNext()) {
                                                current = i.next();
                                                if (current.id == receivedPlayer) {
                                                        player.enemies.remove(i);
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

                                player.bombs.add(current);

                                System.out.println(player.playerNo + " player's detected a bomb at position (" + receivedX + "," + receivedY + ")");

                            } else if (args[0].equals("Explosion")) {
                                int receivedX = Integer.parseInt(args[1]);
                                int receivedY = Integer.parseInt(args[2]);

                                System.out.println(player.playerNo + " player's detected an explosion at position (" + receivedX + "," + receivedY + ")");
                                Iterator<GridCoordinates> i = player.bombs.iterator();
                                GridCoordinates current;
                                boolean found = false;

                                while (i.hasNext()) {
                                        current = i.next();
                                        if (current.x == receivedX && current.y == receivedY) {
                                                player.bombs.remove(i);
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
                }

                //System.out.println("Player " + player.playerNo + " has " + player.bombs.size() + " bombs and "
                                        //+ player.enemies.size() + " enemies in sight");

                int move = -1;

                /* Look for bombs */
                if (player.bombs.size() > 0) {
                    /* player in same row as bomb? */
                    for (int i = 0; i < player.bombs.size(); i++) {
                        if (player.bombs.get(i).x == player.prev_pos.x) {
                            /* attempt move to another row */
                            do {
                                /* TODO: will need to protect myself from going back to danger */
                                move = MoveValidator.nextMove(player.prev_pos.x, player.prev_pos.y,
                                                            player.prev_pos.x +1, player.prev_pos.y);
                                if (move != -1) {
                                    moveRequest(move);
                                    return;
                                }

                                move = MoveValidator.nextMove(player.prev_pos.x, player.prev_pos.y,
                                                            player.prev_pos.x -1, player.prev_pos.y);
                                if (move != -1) {
                                    moveRequest(move);
                                    return;
                                }
                            } while (false);
                        }
                    }


                    /* TODO: I'm attempting to save time by sending consecutive requests...
                     * but the validation is wrong because I have not yet moved.
                     * Because of this, I am adding returns after row and column checks... but
                     * I believe that could be optimized.
                     */
                    /* player in same column as bomb? */
                    for (int i = 0; i < player.bombs.size(); i++) {
                        if (player.bombs.get(i).y == player.prev_pos.y) {
                            /* attempt move to another column */
                            do {
                                /* TODO: will need to protect myself from going back to danger */
                                move = MoveValidator.nextMove(player.prev_pos.x, player.prev_pos.y,
                                                            player.prev_pos.x, player.prev_pos.y+1);
                                if (move != -1) {
                                    moveRequest(move);
                                    return;
                                }

                                move = MoveValidator.nextMove(player.prev_pos.x, player.prev_pos.y,
                                                            player.prev_pos.x, player.prev_pos.y-1);
                                if (move != -1) {
                                    moveRequest(move);
                                    return;
                                }
                            } while (false);
                        }
                    }
                    //moveRequest(move);
                } else {
                    //System.out.println(player.playerNo + ": No bombs in range");
                }

                /* Look for enemies */
                if (player.enemies.size() > 0) {
                    /* look for closer enemy */
                    int closer_index = -1;
                    /* no player can be this far */
                    int smaller = 36;
                    int distance;

                    for (int i = 0; i < player.enemies.size(); i++) {
                        distance = Math.abs(player.prev_pos.x - player.enemies.get(i).x)
                                    + Math.abs(player.prev_pos.x - player.enemies.get(i).y);
                        if (distance < smaller) {
                            smaller = distance;
                            closer_index = i;
                        }
                    }

                    if (closer_index > 4 || closer_index < 0) {
                        /* impossible scenario!! should I make a random move? */
                        /* between 0 and 3, no bombs! */
                        Random rand = new Random();
                        //System.out.println(player.playerNo + ": Making a random move");
                        moveRequest(rand.nextInt(4));
                        return;
                    }

                    //System.out.println(player.playerNo + ": closest player is at position "
                    //                    + player.enemies.get(closer_index).x + ":" + player.enemies.get(closer_index).y);
                    /* attempt to get near, choice of x or y could be randomized,
                     * for now first through x then through y */
                    do {
                        int x_dir = (player.prev_pos.x - player.enemies.get(closer_index).x) < 0 ? 1 : -1;
                           move = MoveValidator.nextMove(player.prev_pos.x, player.prev_pos.y,
                                                            player.prev_pos.x + x_dir, player.prev_pos.y);
                        if (move != -1) {
                            moveRequest(move);
                            return;
                        }

                        int y_dir = (player.prev_pos.y - player.enemies.get(closer_index).y) < 0 ? 1 : -1;
                        move = MoveValidator.nextMove(player.prev_pos.x, player.prev_pos.y,
                                                        player.prev_pos.x, player.prev_pos.y + y_dir);
                        if (move != -1) {
                            moveRequest(move);
                            return;
                        }
                    } while (false);
                }
                /* nothing to act upon. Move randomly */
                Random rand = new Random();
                //System.out.println(player.playerNo + ": Making a random move");
                moveRequest(rand.nextInt(4));
                return;
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
        /* ensure only valid requests are sent */
        if (move >= BomberPlayer.UP && move <= BomberPlayer.BOMB) {
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.addReceiver(new AID("Cris", AID.ISLOCALNAME));
            msg.setLanguage("English");
            msg.setOntology("Weather-forecast-ontology");
            msg.setContent("Move:" + move);
            send(msg);
        }
    }
}
