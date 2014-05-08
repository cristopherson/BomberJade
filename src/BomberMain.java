
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ReceiverBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JFrame;

/**
 * File: BomberMain.java Copyright: Copyright (c) 2001
 *
 * @author Sammy Leong
 * @version 1.0
 */
/**
 * This is the starting point of the game.
 */
public class BomberMain extends Agent {

    private JFrame mainFramework;
    /**
     * relative path for files
     */
    public static String RP = "./";
    /**
     * menu object
     */
    private BomberMenu menu = null;
    /**
     * game object
     */
    public BomberGame game = null;

    /**
     * sound effect player
     */
    public static BomberSndEffect sndEffectPlayer = null;
    /**
     * this is used to calculate the dimension of the game
     */
    public static final int shiftCount = 4;

    /**
     * List of agents subscribed to this agent's notifications
     */
    public static String[] subscribers = new String[4];

    /**
     * Index for subscribed agents
     */
    public static int index = 0;

    /**
     * this is the size of each square in the game
     */
    public static final int size = 1 << shiftCount;
    ReceiverBehaviour subscribeBehavior = new ReceiverBehaviour(this, 100, MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE));

    static {
        sndEffectPlayer = new BomberSndEffect();
    }

    /* flag to send positions only once */

    boolean positionsSent = false;
    /**
     * Constructs the main frame.
     */
    public BomberMain() {
        String localname = "Cris";
        AID id = new AID(localname, AID.ISLOCALNAME);

        addBehaviour(subscribeBehavior);
        addBehaviour(new CyclicBehaviour(this) {

            @Override
            public void action() {
                ACLMessage msg = null;

                if (subscribeBehavior.done()) {
                    try {
                        msg = subscribeBehavior.getMessage();
                        subscribeBehavior.reset();
                    } catch (ReceiverBehaviour.TimedOut ex) {
                    } catch (ReceiverBehaviour.NotYetReady ex) {
                    } finally {
                        if (index < subscribers.length) {
                            myAgent.addBehaviour(subscribeBehavior);
                        } else {
                            myAgent.removeBehaviour(this);
                        }
                    }
                }

                if (msg != null) {
                    String content = msg.getContent();
                    String agent = msg.getSender().getLocalName();

                    System.out.println(getAID().getLocalName() + " got subscription request");
                    System.out.println(agent + " says " + content);

                    if (index < subscribers.length) {
                        subscribers[index] = agent;
                        index++;
                    }
                    /* send response message */
                    msg = new ACLMessage(ACLMessage.CONFIRM);
                    /* content is already set to the name of the agent who sent the original */
                    msg.addReceiver(new AID(agent, AID.ISLOCALNAME));
                    msg.setLanguage("English");
                    /* The ontology determines the elements that agents can use within the content of the message.
                     * It defines a vocabulary and relationships between the elements in such a vocabulary. Said relationships
                     * can be structural or semantic
                     */
                    msg.setOntology("Weather-forecast-ontology");
                    msg.setContent("Hi " + agent + "\nI am " + getAID().getLocalName() + "\nWelcome to the game");
                    send(msg);

                }

            }

        });

        addBehaviour(new TickerBehaviour(this, 100) {
            public void onTick() {
                // perform operation Y

                /* If there's no message, players may need to get each other's positions,
                 * so send them */
                    for (int i = 0; i < 4; i++) {
                        BomberGame.players[i].sendPosition();
                    }

            }
        });

        mainFramework = new JFrame();
        /**
         * add window event handler
         */
        mainFramework.addWindowListener(new WindowAdapter() {
            /**
             * Handles window closing events.
             *
             * @param evt window event
             */
            @Override
            public void windowClosing(WindowEvent evt) {
                /**
                 * terminate the program
                 */
                System.exit(0);
            }
        });

        /**
         * set the window title
         */
        mainFramework.setTitle("Bomberman 1.0 by Sammy Leong");

        /**
         * set the window icon
         */
        try {
            mainFramework.setIconImage(Toolkit.getDefaultToolkit().getImage(
                    new File(RP + "Images/Bomberman.gif").getCanonicalPath()));
        } catch (IOException e) {
            new ErrorDialog(e);
        }

        /**
         * create and add the menu to the frame
         */
        mainFramework.getContentPane().add(menu = new BomberMenu(this));

        /**
         * set the window so that the user can't resize it
         */
        mainFramework.setResizable(false);
        /**
         * minimize the size of the window
         */
        mainFramework.pack();

        /**
         * get screen size
         */
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();

        int x = (d.width - mainFramework.getSize().width) / 2;
        int y = (d.height - mainFramework.getSize().height) / 2;

        /**
         * center the window on the screen
         */
        mainFramework.setLocation(x, y);
        /**
         * show the frame
         */
        mainFramework.show();
        /**
         * make this window the top level window
         */
        mainFramework.toFront();
        newGame(4);
    }

    /**
     * Creates a new game.
     *
     * @param players total number of players
     */
    public void newGame(int players) {
        JDialog dialog = new JDialog(getMainFramework(), "Loading Game...", false);
        dialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        dialog.setSize(new Dimension(200, 0));
        dialog.setResizable(false);
        int x = getMainFramework().getLocation().x + (getMainFramework().getSize().width - 200) / 2;
        int y = getMainFramework().getLocation().y + getMainFramework().getSize().height / 2;
        dialog.setLocation(x, y);
        /**
         * show the dialog
         */
        dialog.show();

        /**
         * remove existing panels in the content pane
         */
        getMainFramework().getContentPane().removeAll();
        getMainFramework().getLayeredPane().removeAll();
        /**
         * get rid of the menu
         */
        menu = null;
        /**
         * create the map
         */
        BomberMap map = new BomberMap(this);

        /**
         * create the game
         */
        game = new BomberGame(this, map, players);

        /**
         * get rid of loading dialog
         */
        dialog.dispose();
        /**
         * show the frame
         */
        getMainFramework().show();
        /**
         * if Java 2 available
         */
        if (Main.J2) {
            BomberBGM.unmute();
            /**
             * player music
             */
            BomberBGM.change("Battle");
        }
    }

    /**
     * Starting ponit of program.
     *
     * @param args arguments
     */
    public static void main(String[] args) {
        BomberMain bomberMain1 = new BomberMain();
    }

    /**
     * @return the mainFramework
     */
    public JFrame getMainFramework() {
        return mainFramework;
    }

    /**
     * @param mainFramework the mainFramework to set
     */
    public void setMainFramework(JFrame mainFramework) {
        this.mainFramework = mainFramework;
    }

    protected void setup() {
        // Printout a welcome message
        System.out.println("Game has started!");
        System.out.println("Host is " + getAID().getLocalName());
        System.out.println("Waiting for players...");
        game.createBomberPlayerAgents();
    }

    /**
     * Send a notification message to agents
     */
    public void sendMessage(String message) {
        System.out.println("Sending message" + message);
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setLanguage("English");
        msg.setOntology("Weather-forecast-ontology");
        msg.setContent(message);
        /* Iterate over the list of subscribed agents */
        for (int i = 0; i < BomberMain.index; i++) {
            System.out.println("KIDDO subscribers" + BomberMain.subscribers[i]);
            msg.addReceiver(new AID(BomberMain.subscribers[i], AID.ISLOCALNAME));
        }
        send(msg);
    }
}
