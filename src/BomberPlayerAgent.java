
import java.awt.event.KeyEvent;
import java.util.Iterator;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.ReceiverBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

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
    private ReceiverBehaviour confirmBehavior = new ReceiverBehaviour(this, 400, MessageTemplate.MatchPerformative(ACLMessage.CONFIRM));
    private ReceiverBehaviour informBehavior = new ReceiverBehaviour(this, 400, MessageTemplate.MatchPerformative(ACLMessage.INFORM));
    private ReceiverBehaviour cancelBehavior = new ReceiverBehaviour(this, 400, MessageTemplate.MatchPerformative(ACLMessage.CANCEL));
    private ReceiverBehaviour proposeBehaviour = new ReceiverBehaviour(this, 400, MessageTemplate.MatchPerformative(ACLMessage.PROPOSE));

    public GridCoordinates cur_pos = new GridCoordinates();

    protected void setup() {
        Object[] args = this.getArguments();
        player = (BomberPlayer) args[0];

        String localName = getAID().getLocalName();
        //playerId = Integer.parseInt(localName.replaceAll("Bomber", ""));

        // Printout a welcome message
        System.out.println("My local-name is " + localName);
        System.out.println("I play for team " + player.team);

        /* this agent will execute this behaviour every 500 ms */
        addBehaviour(new OneShotBehaviour(this) {

            public void action() {
                ACLMessage msg = new ACLMessage(ACLMessage.SUBSCRIBE);
                msg.addReceiver(new AID("Cris", AID.ISLOCALNAME));
                msg.setLanguage("English");
                msg.setOntology("Weather-forecast-ontology");
                msg.setContent("Hi All\nI am " + getAID().getLocalName());
                send(msg);
            }
        });

        this.addBehaviour(proposeBehaviour);
        this.addBehaviour(new TickerBehaviour(this, 100) {
            protected void onTick() {
                ACLMessage msg = null;

                if (proposeBehaviour.done()) {
                    try {
                        msg = proposeBehaviour.getMessage();

                        String content = msg.getContent();
                        String args[] = content.split(":");
                        proposeBehaviour.reset();

                        int receivedPlayer = Integer.parseInt(args[1]);
                        int receivedTeam = Integer.parseInt(args[2]);

                        if (args[0].equals("Player")) {

                            int receivedX = Integer.parseInt(args[3]);
                            int receivedY = Integer.parseInt(args[4]);
                            GridCoordinates new_pos = new GridCoordinates();

                            new_pos.x = receivedX;
                            new_pos.y = receivedY;

                            // Is mine the reported position?
                            /*
                            if (receivedPlayer == player.playerNo) {
                                System.out.println("I am at position (" + receivedX + "," + receivedY + ")");

                                // TODO: may need extra locks here to prevent placing bombs if moving away from one
                                if (new_pos.x == player.prev_pos.x && new_pos.y == player.prev_pos.y) {
                                    player.samePlace = true;
                                } else {
                                    player.samePlace = false;
                                }

                                // I'm attempting to go somewhere but I can't... there's a wall.
                                // blow it up!
                                //
                                if (player.samePlace) {
                                    // Don't sit here... move
                                    Random rand = new Random();
                                    System.out.println(player.playerNo + ": Making a random move");
                                    moveRequest(rand.nextInt(4));
                                }

                                player.prev_pos.x = new_pos.x;
                                player.prev_pos.y = new_pos.y;

                                // Is an enemy position what I've received?
                            } else  */
                            if(receivedTeam != player.team) {
                                if (BomberGame.players[receivedPlayer - 1].isDead()) {
                                    System.out.println("Ignoring report for Dead: player " + receivedPlayer);
                                    return;
                                }

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
                            }
                        } else if (args[0].equals("Dead")) {

                            /* only care about enemies for now */
                            if (receivedTeam != player.team) {
                                GridCoordinates current;
                                boolean found = false;

                                for (int i = 0; i < player.enemies.size(); i++) {
                                    current = player.enemies.get(i);
                                    if (current.id == receivedPlayer) {
                                        player.enemies.remove(i);
                                        found = true;
                                        break;
                                    }
                                }

                                if (!found) {
                                    System.out.println("Dead enemy not in list!?!");
                                }
                            }
                        }
                    } catch (ReceiverBehaviour.TimedOut ex) {

                    } catch (ReceiverBehaviour.NotYetReady ex) {

                    } finally {
                        myAgent.addBehaviour(proposeBehaviour);
                    }
                }

            }

        });

        this.addBehaviour(confirmBehavior);
        this.addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                ACLMessage msg = null;

                if (confirmBehavior.done()) {
                    try {
                        msg = confirmBehavior.getMessage();
                        confirmBehavior.reset();
                        myAgent.removeBehaviour(this);
                        System.out.println("Confirmation received");
                        logged = true;
                    } catch (ReceiverBehaviour.TimedOut ex) {

                    } catch (ReceiverBehaviour.NotYetReady ex) {

                    } finally {
                        if (!logged) {
                            myAgent.addBehaviour(confirmBehavior);
                        }
                    }
                }

            }

        });

        this.addBehaviour(cancelBehavior);
        this.addBehaviour(new TickerBehaviour(this, 100) {
            protected void onTick() {
                ACLMessage msg = null;

                if (cancelBehavior.done()) {
                    try {
                        msg = cancelBehavior.getMessage();

                        String content = msg.getContent();
                        String args[] = content.split(":");
                        cancelBehavior.reset();

                        int receivedX = Integer.parseInt(args[1]);
                        int receivedY = Integer.parseInt(args[2]);

                        GridCoordinates current = new GridCoordinates();
                        current.x = receivedX;
                        current.y = receivedY;

                        if (args[0].equals("Bomb")) {
                            player.bombs.add(current);
                        } else if (args[0].equals("Explosion")) {
                            for (int i = 0; i < player.bombs.size(); i++) {
                                if (player.bombs.get(i).x == current.x &&
                                        player.bombs.get(i).y == current.y)
                                    player.bombs.remove(i);
                            }
                        }

                    } catch (ReceiverBehaviour.TimedOut ex) {

                    } catch (ReceiverBehaviour.NotYetReady ex) {

                    } finally {
                        myAgent.addBehaviour(cancelBehavior);
                    }
                }

            }

        });

        this.addBehaviour(informBehavior);
        addBehaviour(new TickerBehaviour(this, 500) {
            public void onTick() {
                // perform operation Y

                System.out.println(player.playerNo + ": currently at " + cur_pos.x + ":" + cur_pos.y);

                /* get the first message on my message queue.
                 */
                ACLMessage msg = null;

                if (informBehavior.done()) {
                    try {
                        msg = informBehavior.getMessage();
                        informBehavior.reset();
                    } catch (ReceiverBehaviour.TimedOut ex) {
                    } catch (ReceiverBehaviour.NotYetReady ex) {
                    } finally {
                        myAgent.addBehaviour(informBehavior);
                    }
                }

                /* message has been received */
                if (msg != null) {
                    String content = msg.getContent();
                    String args[] = content.split(":");
                }

                System.out.println("Player " + player.playerNo + " has " + player.bombs.size() + " bombs and "
                        + player.enemies.size() + " enemies in sight");


                /* I'm just going to leave this here */
                if(MoveValidator.hasElementAround(player.map, BomberMap.BRICK, cur_pos.x, cur_pos.y)) {
                    moveRequest(BomberPlayer.BOMB);
                }

                /* Look for bombs */
                if (player.bombs.size() > 0) {
                    System.out.println(player.playerNo + ": Bombs present");
                    /* player in same column as bomb? */
                    for (int i = 0; i < player.bombs.size(); i++) {
                        if (player.bombs.get(i).x == cur_pos.x) {
                            System.out.println(player.playerNo + ": in same column as bomb at " + player.bombs.get(i).x + ":" + player.bombs.get(i).y);

/*
                            int nextMove = -1;

                            nextMove = MoveValidator.nextMove(player.map, BomberMap.NOTHING, cur_pos.x, cur_pos.y);
                            System.out.println(player.playerNo + ": must move away from (" + cur_pos.x + "," + cur_pos.y +") = " + nextMove);
                            if (nextMove != -1) {
                                moveRequest(nextMove);
                                break;
                            }
*/
                            /* attempt move to another column */
                            if (move(cur_pos.x + 1, cur_pos.y)) {
                                System.out.println(player.playerNo + ": moved right");
                                break;
                            }

                            if (move(cur_pos.x - 1, cur_pos.y)) {
                                System.out.println(player.playerNo + ": moved left");
                                break;
                            }

                            /* move inside danger column, hoping to find an exit */
                            if (move(cur_pos.x, cur_pos.y + 1)) {
                                System.out.println(player.playerNo + ": moved down");
                                break;
                            }

                            if (move(cur_pos.x, cur_pos.y - 1)) {
                                System.out.println(player.playerNo + ": moved up");
                                break;
                            }

                            /* TODO: will need to protect myself from going back to danger */
                            return;
                        }
                    }


                    /* TODO: I'm attempting to save time by sending consecutive requests...
                     * but the validation is wrong because I have not yet moved.
                     * Because of this, I am adding returns after row and column checks... but
                     * I believe that could be optimized.
                     */
                    /* player in same row as bomb? */
                    for (int i = 0; i < player.bombs.size(); i++) {
                        if (player.bombs.get(i).y == cur_pos.y) {
                            System.out.println(player.playerNo + ": in same row as bomb at " + player.bombs.get(i).x + ":" + player.bombs.get(i).y);
/*
                            nextMove = MoveValidator.nextMove(player.map, BomberMap.NOTHING, cur_pos.x, cur_pos.y);
                            System.out.println(player.playerNo + ": must move away from (" + cur_pos.x + "," + cur_pos.y +") = " + nextMove);
                            if (nextMove != -1) {
                                moveRequest(nextMove);
                                break;
                            }
*/
                            /* attempt move to another column */
                            if (move(cur_pos.x, cur_pos.y + 1)) {
                                System.out.println(player.playerNo + ": moved down");
                                break;
                            }

                            if (move(cur_pos.x, cur_pos.y - 1)) {
                                System.out.println(player.playerNo + ": moved up");
                                break;
                            }
                            /* TODO: will need to protect myself from going back to danger */
                            /* move into danger column, hoping to find an exit */
                            if (move(cur_pos.x + 1, cur_pos.y)) {
                                System.out.println(player.playerNo + ": moved right");
                                break;
                            }

                            if (move(cur_pos.x - 1, cur_pos.y)) {
                                System.out.println(player.playerNo + ": moved left");
                                break;
                            }
                        }
                    }
                    return;
                    //moveRequest(move);
                } else {
                    System.out.println(player.playerNo + ": No bombs in range");
                }

                /* Look for enemies, but only if there are no bombs to run from */
                if (player.enemies.size() > 0 && player.bombs.size() == 0) {
                    /* look for closer enemy */
                    int closer_index = -1;
                    /* no player can be this far */
                    int smaller = 36;
                    int distance;

                    for (int i = 0; i < player.enemies.size(); i++) {
                        distance = Math.abs(cur_pos.x - player.enemies.get(i).x)
                                + Math.abs(cur_pos.y - player.enemies.get(i).y);
                        if (distance < smaller) {
                            smaller = distance;
                            closer_index = i;
                        }
                    }

                    if (closer_index > 4 || closer_index < 0) {
                        /* impossible scenario!! should I make a random move? */
                        /* between 0 and 3, no bombs! */
                        Random rand = new Random();
                        System.out.println(player.playerNo + ": Making an impossible move");
                        moveRequest(rand.nextInt(4));
                        return;
                    }

                    System.out.println(player.playerNo + ": closest player is at position "
                            + player.enemies.get(closer_index).x + ":" + player.enemies.get(closer_index).y);
                    /* attempt to get near, choice of x or y could be randomized,
                     * for now first through x then through y */
                   // do {
                    int x_dir = (cur_pos.x - player.enemies.get(closer_index).x) < 0 ? 1 : -1;
                    if (move(cur_pos.x + x_dir, cur_pos.y)) {
                        return;
                    }

                    int y_dir = (cur_pos.y - player.enemies.get(closer_index).y) < 0 ? 1 : -1;
                    if (move(cur_pos.x, cur_pos.y + y_dir)) {
                            return;
                    }
                }
                /* nothing to act upon. Move randomly, but only if there are no bombs in the way */
                if (player.bombs.size() == 0) {
        /*
                    int x, y, x_move, y_move;
                    do {
                        x = cur_pos.x;
                        y = cur_pos.y;
                        x_move = 0;
                        y_move = 0;
                        Random rand = new Random();
                        switch (rand.nextInt(4)) {
                        case BomberPlayer.RIGHT:
                            x_move++;
                        case BomberPlayer.LEFT:
                            x_move--;
                        case BomberPlayer.UP:
                            y_move++;
                        case BomberPlayer.DOWN:
                            y_move--;
                        }
                    } while (!move(x+x_move, y+y_move));
        */
                    System.out.println(player.playerNo + ": Making a random move");
                    Random rand = new Random();
                    moveRequest(rand.nextInt(4));
                        return;

                }
            }
        }
        );
    }

    /* is it possible or safe to move to a particular position? */
    private boolean canMove(int x, int y) {

        System.out.println(player.playerNo + ": Evaluate moving to " + x + ":" + y);
        if (MoveValidator.hasElement(player.map, BomberMap.BRICK, x, y)) {
            System.out.println(player.playerNo + ": Rejected because BRICK");
            return false;
        } else if (MoveValidator.hasElement(player.map, BomberMap.WALL, x, y)) {
            System.out.println(player.playerNo + ": Rejected because WALL");
            return false;
        } else if (MoveValidator.hasElement(player.map, BomberMap.FIRE_BRICK, x, y)) {
            System.out.println(player.playerNo + ": Rejected because FIRE_BRICK");
            return false;
            /*
        } else if (MoveValidator.hasElement(player.map, BomberMap.FIRE_CENTER, x, y)) {
            System.out.println(player.playerNo + ": Rejected because FIRE_CENTER");
            return false;
        } else if (MoveValidator.hasElement(player.map, BomberMap.FIRE_EAST, x, y)) {
            System.out.println(player.playerNo + ": Rejected because FIRE_EAST");
            return false;
        } else if (MoveValidator.hasElement(player.map, BomberMap.FIRE_WEST, x, y)) {
            System.out.println(player.playerNo + ": Rejected because FIRE_WEST");
            return false;
        } else if (MoveValidator.hasElement(player.map, BomberMap.FIRE_NORTH, x, y)) {
            System.out.println(player.playerNo + ": Rejected because FIRE_NORTH");
            return false;
        } else if (MoveValidator.hasElement(player.map, BomberMap.FIRE_SOUTH, x, y)) {
            System.out.println(player.playerNo + ": Rejected because FIRE_SOUTH");
            return false;
            */
        } else if (MoveValidator.hasElement(player.map, BomberMap.FIRE_HORIZONTAL, x, y)) {
            System.out.println(player.playerNo + ": Rejected because FIRE_HORIZONTAL");
            return false;
        } else if (MoveValidator.hasElement(player.map, BomberMap.FIRE_VERTICAL, x, y)) {
            System.out.println(player.playerNo + ": Rejected because FIRE_VERTICAL");
            return false;
        }
        System.out.println(player.playerNo + ": Safe to move to " + x + ":" + y);
        return true;
    }

    /* Attempt to move to specified position.
     * Return true if move was allowed
     */
    private boolean move(int x, int y) {

        int move = -1;

        move = MoveValidator.nextMove(cur_pos.x, cur_pos.y, x, y);
        if (move != -1 && canMove(x, y)) {
            moveRequest(move);
            return true;
        }
        return false;
    }
    /**
     * Send move request to host agent. Movements are interpreted as follows:
     * UP = 0, DOWN = 1, LEFT = 2, RIGHT = 3, BOMB = 4
     */
    private void moveRequest(int move) {
        /* ensure only valid requests are sent */
        if (move >= BomberPlayer.UP && move <= BomberPlayer.BOMB) {
            KeyEvent event
                    = new KeyEvent(player.game, BomberKeyConfig.keys[player.playerNo - 1][move],
                            System.currentTimeMillis(), 0, BomberKeyConfig.keys[player.playerNo - 1][move],
                            KeyEvent.CHAR_UNDEFINED);

            //System.out.println("Moving " + agent + " to " + move);
            if (player.game != null) {
                player.game.keyPressed(event);
                try {
                    if (move == BomberPlayer.BOMB) {
                        Thread.sleep(50);
                    } else {
                        /* 300 seems a pretty good approximation
                         * to moving exactly one square.
                         */
                        Thread.sleep(300);
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(BomberMain.class.getName()).log(Level.SEVERE, null, ex);
                }
                player.game.keyReleased(event);
            }
            cur_pos.x = (player.x / 15);
            cur_pos.y = (player.y / 15);
            System.out.println(player.playerNo + ": now at " + cur_pos.x + ":" + cur_pos.y);
        }
    }
}
