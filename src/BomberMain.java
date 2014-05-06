import jade.core.AID;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import jade.core.Agent;
import java.util.Iterator;

/**
 * File:         BomberMain.java
 * Copyright:    Copyright (c) 2001
 * @author Sammy Leong
 * @version 1.0
 */

/**
 * This is the starting point of the game.
 */
public class BomberMain extends Agent {
    private JFrame mainFramework;
    /** relative path for files */
    public static String RP = "./";
    /** menu object */
    private BomberMenu menu = null;
    /** game object */
    private BomberGame game = null;

    /** sound effect player */
    public static BomberSndEffect sndEffectPlayer = null;
    /** this is used to calculate the dimension of the game */
    public static final int shiftCount = 4;
    /** this is the size of each square in the game */
    public static final int size = 1 << shiftCount;

    static {
        sndEffectPlayer = new BomberSndEffect();
    }

    /**
     * Constructs the main frame.
     */
    public BomberMain() {
        String localname = "Cris";
        AID id = new AID(localname, AID.ISLOCALNAME);
        
        mainFramework= new JFrame();
        /** add window event handler */
        mainFramework.addWindowListener(new WindowAdapter() {
            /**
             * Handles window closing events.
             * @param evt window event
             */
            @Override
            public void windowClosing(WindowEvent evt) {
                /** terminate the program */
                System.exit(0);
            }
        });

        
        /** set the window title */
        mainFramework.setTitle("Bomberman 1.0 by Sammy Leong");

        /** set the window icon */
        try {
            mainFramework.setIconImage(Toolkit.getDefaultToolkit().getImage(
                new File(RP + "Images/Bomberman.gif").getCanonicalPath()));
        }
        catch (IOException e) { new ErrorDialog(e); }
        
        /** create and add the menu to the frame */
        mainFramework.getContentPane().add(menu = new BomberMenu(this));

        /** set the window so that the user can't resize it */
        mainFramework.setResizable(false);
        /** minimize the size of the window */
        mainFramework.pack();

        /** get screen size */
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();

        int x = (d.width - mainFramework.getSize().width) / 2;
        int y = (d.height - mainFramework.getSize().height) / 2;

        /** center the window on the screen */
        mainFramework.setLocation(x, y);
        /** show the frame */
        mainFramework.show();
        /** make this window the top level window */
        mainFramework.toFront();
        newGame(4);
    }

    /**
     * Creates a new game.
     * @param players total number of players
     */
    public void newGame(int players)
    {
        JDialog dialog = new JDialog(getMainFramework(), "Loading Game...", false);
        dialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        dialog.setSize(new Dimension(200, 0));
        dialog.setResizable(false);
        int x = getMainFramework().getLocation().x + (getMainFramework().getSize().width - 200) / 2;
        int y = getMainFramework().getLocation().y + getMainFramework().getSize().height / 2;
        dialog.setLocation(x, y);
        /** show the dialog */
        dialog.show();

        /** remove existing panels in the content pane */
        getMainFramework().getContentPane().removeAll();
        getMainFramework().getLayeredPane().removeAll();
        /** get rid of the menu */
        menu = null;
        /** create the map */
        BomberMap map = new BomberMap(this);

        /** create the game */
        game = new BomberGame(this, map, players);

        /** get rid of loading dialog */
        dialog.dispose();
        /** show the frame */
        getMainFramework().show();
        /** if Java 2 available */
        if (Main.J2) {
           BomberBGM.unmute();
           /** player music */
           BomberBGM.change("Battle");
        }
    }

    /**
     * Starting ponit of program.
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
        System.out.println("Hello World. I’m an agent!");
        System.out.println("My local-name is "+getAID().getLocalName());
        System.out.println("My GUID is "+getAID().getName());
        System.out.println("My addresses are:");
        Iterator it = getAID().getAllAddresses();
        while (it.hasNext()) {
         System.out.println("- "+it.next());
        }
    }
}
