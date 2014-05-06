
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
    private int team;

    protected void setup() {
    	Object[] args = this.getArguments();
    	team = (Integer) args[0];
        // Printout a welcome message
        System.out.println("Hello World. I’m a bomber agent!");
        System.out.println("My local-name is " + getAID().getLocalName());
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
                    System.out.println("message is of performative " + performative);
                    logged = true;
                    
                /* no message received. Send a message if I have not said my name */
                } else if(!logged){
                    msg = new ACLMessage(ACLMessage.INFORM);
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
                    int move = (int)(Math.random() * 5);
                    msg.setContent("Move:"+move);
                    send(msg);                    
                }
            }
        });

    }
}
