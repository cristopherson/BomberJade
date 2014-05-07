
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
    private int playerId;
    private int team;
    private int x;
    private int y;

    protected void setup() {
        Object[] args = this.getArguments();
        String localName = getAID().getLocalName();

        team = (Integer) args[0];
        playerId = Integer.parseInt(localName.replaceAll("Bomber", ""));
        // Printout a welcome message
        System.out.println("Hello World. I’m a bomber agent!");
        System.out.println("My local-name is " + localName);
        System.out.println("I play for team " + team);
        /*        System.out.println("My GUID is " + getAID().getName());
         System.out.println("My addresses are:");*/

        /* this agent will execute this behaviour every 500 ms */
        addBehaviour(new TickerBehaviour(this, 500) {
            protected void onTick() {
                // perform operation Y
            	/* get the first message on the message queue.
                 * is it possible to get a message not meant for me?
                 */
                ACLMessage msg = receive();
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

                                if (receivedPlayer == playerId) {
                                    System.out.println("Player " + receivedPlayer + " is at position (" + receivedX + "," + receivedY + ")");
                                    x = receivedX;
                                } else if (receivedTeam == team) {
                                    System.out.println("Player " + receivedPlayer + " is on " + playerId + "'s team");
                                    y = receivedY;
                                } else {
                                    System.out.println(receivedPlayer + " player's enemy detected at position (" + receivedX + "," + receivedY + ")");
                                }

                            } else if (args[0].equals("Bomb")) {
                                int receivedX = Integer.parseInt(args[1]);
                                int receivedY = Integer.parseInt(args[2]);

                                System.out.println(playerId + " player's detected a bomb at position (" + receivedX + "," + receivedY + ")");
                            } else if (args[0].equals("Explosion")) {
                                int receivedX = Integer.parseInt(args[1]);
                                int receivedY = Integer.parseInt(args[2]);

                                System.out.println(playerId + " player's detected an explosion at position (" + receivedX + "," + receivedY + ")");
                            }
                            break;
                        default:
                            System.out.println("Unexpected type message " + performative + "" + content);
                    }

                    /* no message received. Send a message if I have not said my name */
                }

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
                    msg = new ACLMessage(ACLMessage.REQUEST);
                    msg.addReceiver(new AID("Cris", AID.ISLOCALNAME));
                    msg.setLanguage("English");
                    msg.setOntology("Weather-forecast-ontology");
                    int move = (int) (Math.random() * 5);
                    msg.setContent("Move:" + move);
                    send(msg);
                }
            }
        });

    }
}
