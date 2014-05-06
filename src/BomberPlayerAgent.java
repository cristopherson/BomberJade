
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

    protected void setup() {
        // Printout a welcome message
        System.out.println("Hello World. I’m a bomber agent!");
        System.out.println("My local-name is " + getAID().getLocalName());
        /*        System.out.println("My GUID is " + getAID().getName());
         System.out.println("My addresses are:");*/

        addBehaviour(new TickerBehaviour(this, 500) {
            protected void onTick() {
                // perform operation Y
                ACLMessage msg = receive();
                if (msg != null) {
                    System.out.println(msg.getContent());
                    logged = true;
                } else if(!logged){
                    msg = new ACLMessage(ACLMessage.INFORM);
                    msg.addReceiver(new AID("Cris", AID.ISLOCALNAME));
                    msg.setLanguage("English");
                    msg.setOntology("Weather-forecast-ontology");
                    msg.setContent("Hi All\nI am " + getAID().getLocalName());
                    send(msg);
                } else {
                    msg = new ACLMessage(ACLMessage.INFORM);
                    msg.addReceiver(new AID("Cris", AID.ISLOCALNAME));
                    msg.setLanguage("English");
                    msg.setOntology("Weather-forecast-ontology");
                    int move = (int)(Math.random() * 5);
                    msg.setContent("Move:"+getAID().getLocalName()+":"+move);
                    send(msg);                    
                }
            }
        });

    }
}
