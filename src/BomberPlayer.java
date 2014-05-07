
import jade.core.AID;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.lang.Integer;
import java.util.LinkedList;

/**
 * File: BomberPlayer.java Copyright: Copyright (c) 2001
 *
 * @author Sammy Leong
 * @version 1.0
 */
/**
 * This class creates player objects.
 */
public class BomberPlayer extends Thread {

    /**
     * game object handle
     */
    public BomberGame game = null;
    /**
     * map object handle
     */
    private BomberMap map = null;
    /**
     * player's own bomb grid (must have for synchronization)
     */
    public boolean[][] bombGrid = null;
    /**
     * input key queue
     */
    private BomberKeyQueue keyQueue = null;
    /**
     * bomb key is down or not
     */
    private boolean bombKeyDown = false;
    /**
     * direction keys down
     */
    private byte dirKeysDown = 0x00;
    /**
     * current direction key down
     */
    private byte currentDirKeyDown = 0x00;
    /**
     * sprite width
     */
    private final int width = BomberMain.size;
    /**
     * sprite height
     */
    private final int height = 44 / (32 / BomberMain.size);
    /**
     * is exploding flag
     */
    private boolean isExploding = false;
    /**
     * is dead flag
     */
    private boolean isDead = false;
    /**
     * whether a key is pressed or not
     */
    private boolean keyPressed = false;
    /**
     * the player's input keys
     */
    private int[] keys = null;
    /**
     * total bombs the player has
     */
    public int totalBombs = 1;
    /**
     * total bombs the player used
     */
    public int usedBombs = 0;
    /**
     * the player's fire strength
     */
    public int fireLength = 2;
    /**
     * if player is alive
     */
    public boolean isActive = true;
    /**
     * player position
     */
    public int x = 0;
    public int y = 0;

    /**
     * Previous player position
     */
    public int prev_x = -1;
    public int prev_y = -1;
    /**
     * player's number
     */
    public int playerNo = 0;
    /**
     * user's state : default to face down
     */
    private int state = DOWN;
    /**
     * flag : whether the player is moving or not
     */
    private boolean moving = false;
    /**
     * sprite frame number
     */
    private int frame = 0;
    /**
     * clear mode flag
     */
    private boolean clear = false;

    /**
     * variable to determine player team
     */
    private static int teamAssigner = 0;

    /**
     * Actual team for this player
     */
    public int team;

    /**
     * Controller for this player's agent
     */
    AgentController ac = null;

    /**
     * Reference to the main agent
     */
    BomberMain mainAgent = null;

    /**
     * Coordinates for my previous position
     */
    GridCoordinates prev_pos = new GridCoordinates();

    /**
     * Flag to determine whether I am attempting to move
     */
    public boolean attemptingToMove = false;

    /**
     * Databases for enemies and bombs...
     */
    /* TODO: these may work if static... */
    public LinkedList<GridCoordinates> bombs = new LinkedList<GridCoordinates>();
    public LinkedList<GridCoordinates> enemies = new LinkedList<GridCoordinates>();
    /**
     * byte enumerations
     */
    private static final byte BUP = 0x01;
    private static final byte BDOWN = 0x02;
    private static final byte BLEFT = 0x04;
    private static final byte BRIGHT = 0x08;
    private static final byte BBOMB = 0x10;
    /**
     * number enumerations
     */
    public static final int UP = 0;
    public static final int DOWN = 1;
    public static final int LEFT = 2;
    public static final int RIGHT = 3;
    public static final int BOMB = 4;
    public static final int EXPLODING = 4;

    /**
     * Number of desired teams
     */
    private static final int NUMBER_OF_TEAMS = 4;
    /**
     * all player sprite images
     */
    private static Image[][][] sprites = null;
    /**
     * rendering hints
     */
    private static Object hints = null;

    private boolean notifyStaticPosition = false;

    static {
        /**
         * if java runtime is Java 2
         */
        if (Main.J2) {
            /**
             * create the rendering hints for better graphics output
             */
            RenderingHints h = null;
            h = new RenderingHints(null);
            h.put(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            h.put(RenderingHints.KEY_FRACTIONALMETRICS,
                    RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            h.put(RenderingHints.KEY_ALPHA_INTERPOLATION,
                    RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            h.put(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            h.put(RenderingHints.KEY_COLOR_RENDERING,
                    RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            hints = (RenderingHints) h;
        }

        /**
         * create the images
         */
        sprites = new Image[4][5][5];
        int[] states = {UP, DOWN, LEFT, RIGHT, EXPLODING};
        Toolkit tk = Toolkit.getDefaultToolkit();
        String path = new String();
        /**
         * open the files
         */
        try {
            for (int p = 0; p < 4; p++) {
                for (int d = 0; d < 5; d++) {
                    for (int f = 0; f < 5; f++) {
                        /**
                         * generate file name
                         */
                        path = BomberMain.RP + "Images/";
                        path += "Bombermans/Player " + (p + 1) + "/";
                        path += states[d] + "" + (f + 1) + ".gif";
                        /**
                         * open the file
                         */
                        sprites[p][d][f] = tk.getImage(
                                new File(path).getCanonicalPath());
                    }
                }
            }
        } catch (Exception e) {
            new ErrorDialog(e);
        }
    }

    /**
     * Constructs a player.
     *
     * @param game game object
     * @param map map object
     * @param playerNo player's number
     */
    public BomberPlayer(BomberGame game, BomberMap map, int playerNo, BomberMain hostAgent) {
        this.game = game;
        this.map = map;
        this.playerNo = playerNo;
        this.mainAgent = hostAgent;

        /**
         * create the bomb grid
         */
        bombGrid = new boolean[17][17];
        for (int i = 0; i < 17; i++) {
            for (int j = 0; j < 17; j++) {
                bombGrid[i][j] = false;
            }
        }

        int r = 0, c = 0;
        /**
         * find player's starting position
         */
        switch (this.playerNo) {
            case 1:
                r = c = 1;
                break;
            case 2:
                r = c = 15;
                break;
            case 3:
                r = 15;
                c = 1;
                break;
            case 4:
                r = 1;
                c = 15;
        }
        /**
         * calculate position
         */
        x = r << BomberMain.shiftCount;
        y = c << BomberMain.shiftCount;

        MediaTracker tracker = new MediaTracker(game);
        try {
            int counter = 0;
            /**
             * load the images
             */
            for (int p = 0; p < 4; p++) {
                for (int d = 0; d < 5; d++) {
                    for (int f = 0; f < 5; f++) {
                        tracker.addImage(sprites[p][d][f], counter++);
                    }
                }
            }
            /**
             * wait for images to finish loading
             */
            tracker.waitForAll();
        } catch (Exception e) {
            new ErrorDialog(e);
        }

        /**
         * create the key queue
         */
        keyQueue = new BomberKeyQueue();
        /**
         * create the key configurations array
         */
        keys = new int[5];
        /**
         * load the configurations
         */
        for (int k = BomberKeyConfig.UP; k <= BomberKeyConfig.BOMB; k++) {
            keys[k] = BomberKeyConfig.keys[playerNo - 1][k];
        }
        /**
         * HOG THE CPU!!!
         */
        setPriority(Thread.MAX_PRIORITY);
        /**
         * start looping
         */
        start();
    }

    /**
     * Key pressed event handler.
     *
     * @param evt key event
     */
    public void keyPressed(KeyEvent evt) {
        /**
         * assume no new key is pressed
         */
        byte newKey = 0x00;
        /**
         * if player isn't exploding or dead and key pressed is in player's
         */
        /**
         * key list
         */
        if (!isExploding && !isDead
                && evt.getKeyCode() == keys[UP]
                || evt.getKeyCode() == keys[DOWN]
                || evt.getKeyCode() == keys[LEFT]
                || evt.getKeyCode() == keys[RIGHT]) {
            /**
             * if down key pressed
             */
            if (evt.getKeyCode() == keys[DOWN]) {
                newKey = BDOWN;
                /**
                 * if only the up key is pressed
                 */
                if ((currentDirKeyDown & BUP) > 0
                        || ((currentDirKeyDown & BLEFT) == 0
                        && (currentDirKeyDown & BRIGHT) == 0)) {
                    currentDirKeyDown = BDOWN;
                }
            } /**
             * if up key is pressed
             */
            else if (evt.getKeyCode() == keys[UP]) {
                newKey = BUP;
                /**
                 * if only the down key is pressed
                 */
                if ((currentDirKeyDown & BDOWN) > 0
                        || ((currentDirKeyDown & BLEFT) == 0
                        && (currentDirKeyDown & BRIGHT) == 0)) {
                    currentDirKeyDown = BUP;
                }
            } /**
             * if left key is pressed
             */
            else if (evt.getKeyCode() == keys[LEFT]) {
                newKey = BLEFT;
                /**
                 * if only the right key is pressed
                 */
                if ((currentDirKeyDown & BRIGHT) > 0
                        || ((currentDirKeyDown & BUP) == 0
                        && (currentDirKeyDown & BDOWN) == 0)) {
                    currentDirKeyDown = BLEFT;
                }
            } /**
             * if right key is pressed
             */
            else if (evt.getKeyCode() == keys[RIGHT]) {
                newKey = BRIGHT;
                /**
                 * if only the left is pressed
                 */
                if ((currentDirKeyDown & BLEFT) > 0
                        || ((currentDirKeyDown & BUP) == 0
                        && (currentDirKeyDown & BDOWN) == 0)) {
                    currentDirKeyDown = BRIGHT;
                }
            }
            /**
             * if new key isn't in the key queue
             */
            if (!keyQueue.contains(newKey)) {
                /**
                 * then push it on top
                 */
                keyQueue.push(newKey);
                /**
                 * reset keys pressed buffer
                 */
                dirKeysDown |= newKey;
                keyPressed = true;
                /**
                 * if thread is sleeping, then wake it up
                 */
                interrupt();
            }
        }
        /**
         * if no direction key is pressed
         */
        /**
         * and bomb key is pressed
         */
        if (!isExploding && !isDead
                && evt.getKeyCode() == keys[BOMB] && !bombKeyDown && isActive) {
            bombKeyDown = true;
            interrupt();
        }
    }

    /**
     * Key released handler.
     *
     * @param evt key event
     */
    public void keyReleased(KeyEvent evt) {
        /**
         * if a direction key is released
         */
        if (!isExploding && !isDead && (evt.getKeyCode() == keys[UP]
                || evt.getKeyCode() == keys[DOWN]
                || evt.getKeyCode() == keys[LEFT]
                || evt.getKeyCode() == keys[RIGHT])) {
            /**
             * if down key is released
             */
            if (evt.getKeyCode() == keys[DOWN]) {
                /**
                 * remove key from the all keys down buffer
                 */
                dirKeysDown ^= BDOWN;
                /**
                 * reset current key down
                 */
                currentDirKeyDown ^= BDOWN;
                /**
                 * remove it from the key queue
                 */
                keyQueue.removeItems(BDOWN);
            } /**
             * if up key is released
             */
            else if (evt.getKeyCode() == keys[UP]) {
                /**
                 * remove key from the all keys down buffer
                 */
                dirKeysDown ^= BUP;
                /**
                 * reset current key down
                 */
                currentDirKeyDown ^= BUP;
                /**
                 * remove it from the key queue
                 */
                keyQueue.removeItems(BUP);
            } /**
             * if left key is released
             */
            else if (evt.getKeyCode() == keys[LEFT]) {
                /**
                 * remove key from the all keys down buffer
                 */
                dirKeysDown ^= BLEFT;
                /**
                 * reset current key down
                 */
                currentDirKeyDown ^= BLEFT;
                /**
                 * remove it from the key queue
                 */
                keyQueue.removeItems(BLEFT);
            } /**
             * if right key is released
             */
            else if (evt.getKeyCode() == keys[RIGHT]) {
                /**
                 * remove key from the all keys down buffer
                 */
                dirKeysDown ^= BRIGHT;
                /**
                 * reset current key down
                 */
                currentDirKeyDown ^= BRIGHT;
                /**
                 * remove it from the key queue
                 */
                keyQueue.removeItems(BRIGHT);
            }
            /**
             * if no key is currently down
             */
            if (currentDirKeyDown == 0) {
                /**
                 * see if last key pressed is still pressed or not
                 */
                boolean keyFound = false;
                /**
                 * search for last key pressed
                 */
                while (!keyFound && keyQueue.size() > 0) {
                    /**
                     * if key is found then exit the loop
                     */
                    if ((keyQueue.getLastItem() & dirKeysDown) > 0) {
                        currentDirKeyDown = keyQueue.getLastItem();
                        keyFound = true;
                    } /**
                     * if key is not found then pop the current key
                     */
                    /**
                     * and on to the next one
                     */
                    else {
                        keyQueue.pop();
                    }
                }
                /**
                 * if no key found
                 */
                if (!keyFound) {
                    /**
                     * remove all keys from queue if not already removed
                     */
                    keyQueue.removeAll();
                    /**
                     * reset key buffers
                     */
                    currentDirKeyDown = 0x00;
                    dirKeysDown = 0x00;
                    keyPressed = false;
                    interrupt();
                }
            }
        }
        /**
         * if the bomb key is released
         */
        if (!isExploding && !isDead && evt.getKeyCode() == keys[BOMB]) {
            bombKeyDown = false;
            interrupt();
        }
    }

    /**
     * Deactivates the player so it can't be controlled.
     */
    public void deactivate() {
        isActive = false;
    }

    /**
     * Kills the player
     */
    public void kill() {
        /**
         * is player isn't dead or isn't dieing already
         */
        if (!isDead && !isExploding) {
            /**
             * lower players left
             */
            BomberGame.playersLeft -= 1;
            /**
             * reset frame counter
             */
            frame = 0;
            /**
             * set exploding mode
             */
            state = EXPLODING;
            /**
             * make it animate
             */
            moving = true;
            /**
             * prepare to explode!
             */
            isExploding = true;
            /**
             * release keys
             */
            keyPressed = false;
            BomberMain.sndEffectPlayer.playSound("Die");
            /**
             * wake up and die
             */
            interrupt();
        }
    }

    /**
     * @return x co-ordinate
     */
    public int getX() {
        return x;
    }

    /**
     * @return y co-ordinate
     */
    public int getY() {
        return y;
    }

    /**
     * @return whether player is (dead or dieing) or not
     */
    public boolean isDead() {
        return (isDead | isExploding);
    }

    /**
     * Main loop
     */
    public void run() {
        /**
         * can move flat
         */
        boolean canMove;
        /**
         * keeps track of last key state
         */
        boolean lastState = false;
        /**
         * shift count
         */
        int shiftCount = BomberMain.shiftCount;
        /**
         * offset size
         */
        int offset = 1 << (BomberMain.shiftCount / 2);
        /**
         * block size
         */
        int size = BomberMain.size;
        /**
         * half the block size
         */
        int halfSize = BomberMain.size / 2;
        /**
         * temporary variables
         */
        int bx = 0, by = 0;
        /**
         * unconditional loop
         */
        while (true) {
            /**
             * if bomb key is down
             */
            if (!isExploding && !isDead && bombKeyDown && isActive) {
                /**
                 * if bombs are available
                 */
                if ((totalBombs - usedBombs) > 0
                        && /**
                         * and a bomb isn't placed there already
                         */
                        map.grid[x >> shiftCount][y >> shiftCount]
                        != BomberMap.BOMB && !bombGrid[(x + halfSize)
                        >> BomberMain.shiftCount][(y + halfSize)
                        >> BomberMain.shiftCount]) {
                    usedBombs += 1;
                    bombGrid[(x + halfSize) >> BomberMain.shiftCount][(y + halfSize) >> BomberMain.shiftCount] = true;
                    /**
                     * create bomb
                     */
                    map.createBomb(x + halfSize, y + halfSize, playerNo);
                }
            }
            /**
             * if other keys are down
             */
            if (!isExploding && !isDead && keyPressed) {
                /**
                 * store last state
                 */
                lastState = keyPressed;
                /**
                 * increase frame
                 */
                frame = (frame + 1) % 5;
                /**
                 * set moving to true
                 */
                moving = true;
                /**
                 * assume can't move
                 */
                canMove = false;
                /**
                 * make sure a key is down
                 */
                if (dirKeysDown > 0) {
                    /**
                     * if left key is down
                     */
                    if ((currentDirKeyDown & BLEFT) > 0) {
                        state = LEFT;
                        /**
                         * if west slot is empty then it can move
                         */
                        canMove = (x % size != 0 || (y % size == 0
                                && (map.grid[(x >> shiftCount) - 1][y >> shiftCount]
                                <= BomberMap.NOTHING)));

                        /**
                         * if it can't move
                         */
                        if (!canMove) {
                            int oy = 0;
                            /**
                             * if it's a little bit north
                             */
                            for (oy = -offset; oy < 0; oy += (size / 4)) {
                                /**
                                 * and west slot is empty
                                 */
                                if ((y + oy) % size == 0
                                        && map.grid[(x >> shiftCount) - 1][(y + oy) >> shiftCount] <= BomberMap.NOTHING) {
                                    /**
                                     * then move anyway
                                     */
                                    canMove = true;
                                    break;
                                }
                            }
                            /**
                             * if it still can't move
                             */
                            if (!canMove) {
                                /**
                                 * if it's a little bit south
                                 */
                                for (oy = (size / 4); oy <= offset;
                                        oy += (size / 4)) {
                                    /**
                                     * and west slot is empty
                                     */
                                    if ((y + oy) % size == 0
                                            && map.grid[(x >> shiftCount) - 1][(y + oy) >> shiftCount]
                                            <= BomberMap.NOTHING) {
                                        /**
                                         * move anyway
                                         */
                                        canMove = true;
                                        break;
                                    }
                                }
                            }
                            /**
                             * if it can move now
                             */
                            if (canMove) {
                                /**
                                 * clear original spot
                                 */
                                clear = true;
                                game.paintImmediately(x,
                                        y - halfSize, width, height);
                                /**
                                 * move up or down
                                 */
                                y += oy;
                                /**
                                 * redraw the sprite
                                 */
                                clear = false;
                                game.paintImmediately(x,
                                        y - halfSize, width, height);
                            }
                        }
                        /**
                         * if it can move
                         */
                        if (canMove) {
                            /**
                             * clear original spot
                             */
                            clear = true;
                            game.paintImmediately(x,
                                    y - halfSize, width, height);
                            /**
                             * move left
                             */
                            x -= (size / 4);
                            /**
                             * redraw the sprite
                             */
                            clear = false;
                            game.paintImmediately(x,
                                    y - halfSize, width, height);
                        } /**
                         * if it can't move
                         */
                        else {
                            /**
                             * refresh the sprite
                             */
                            moving = false;
                            game.paintImmediately(x,
                                    y - halfSize, width, height);
                        }
                    } /**
                     * if the right key is down
                     */
                    else if ((currentDirKeyDown & BRIGHT) > 0) {
                        state = RIGHT;
                        canMove = false;
                        /**
                         * if east slot is empty
                         */
                        canMove = (x % size != 0 || (y % size == 0
                                && (map.grid[(x >> shiftCount) + 1][y >> shiftCount]
                                <= BomberMap.NOTHING)));

                        /**
                         * if it can't move
                         */
                        if (!canMove) {
                            int oy = 0;
                            /**
                             * see if it's a bit south
                             */
                            for (oy = -offset; oy < 0; oy += (size / 4)) {
                                /**
                                 * and the east slot is empty
                                 */
                                if ((y + oy) % size == 0
                                        && map.grid[(x >> shiftCount) + 1][(y + oy) >> shiftCount] <= BomberMap.NOTHING) {
                                    /**
                                     * move it
                                     */
                                    canMove = true;
                                    break;
                                }
                            }
                            /**
                             * if it still can't move
                             */
                            if (!canMove) {
                                /**
                                 * see if it's a bit north
                                 */
                                for (oy = (size / 4); oy <= offset;
                                        oy += (size / 4)) {
                                    /**
                                     * and the east slot if empty
                                     */
                                    if ((y + oy) % size == 0
                                            && map.grid[(x >> shiftCount) + 1][(y + oy) >> shiftCount]
                                            <= BomberMap.NOTHING) {
                                        /**
                                         * move it
                                         */
                                        canMove = true;
                                        break;
                                    }
                                }
                            }
                            /**
                             * if it can move now
                             */
                            if (canMove) {
                                /**
                                 * clear original spot
                                 */
                                clear = true;
                                game.paintImmediately(x,
                                        y - halfSize, width, height);
                                /**
                                 * move up or down
                                 */
                                y += oy;
                                /**
                                 * refresh the sprite
                                 */
                                clear = false;
                                game.paintImmediately(x,
                                        y - halfSize, width, height);
                            }
                        }
                        /**
                         * if it can move
                         */
                        if (canMove) {
                            /**
                             * clear original spot
                             */
                            clear = true;
                            game.paintImmediately(x,
                                    y - halfSize, width, height);
                            /**
                             * move right
                             */
                            x += (size / 4);
                            /**
                             * refresh the sprite
                             */
                            clear = false;
                            game.paintImmediately(x,
                                    y - halfSize, width, height);
                        } /**
                         * if it can't move
                         */
                        else {
                            moving = false;
                            /**
                             * refresh the sprite
                             */
                            game.paintImmediately(x,
                                    y - halfSize, width, height);
                        }
                    } /**
                     * if up key is down
                     */
                    else if ((currentDirKeyDown & BUP) > 0) {
                        state = UP;
                        canMove = false;
                        /**
                         * if north slot is empty
                         */
                        canMove = (y % size != 0 || (x % size == 0
                                && (map.grid[x >> shiftCount][(y >> shiftCount) - 1]
                                <= BomberMap.NOTHING)));

                        /**
                         * if it can't move
                         */
                        if (!canMove) {
                            int ox = 0;
                            /**
                             * see if it's a bit to the left
                             */
                            for (ox = -offset; ox < 0; ox += (size / 4)) {
                                /**
                                 * and the north slot is empty
                                 */
                                if ((x + ox) % size == 0
                                        && map.grid[(x + ox) >> shiftCount][(y >> shiftCount) - 1] <= BomberMap.NOTHING) {
                                    canMove = true;
                                    break;
                                }
                            }
                            /**
                             * if it still can't move
                             */
                            if (!canMove) {
                                /**
                                 * see if it's a bit to the right
                                 */
                                for (ox = (size / 4); ox <= offset; ox += (size / 4)) {
                                    /**
                                     * and the north block is empty
                                     */
                                    if ((x + ox) % size == 0
                                            && map.grid[(x + ox) >> shiftCount][(y >> shiftCount) - 1]
                                            <= BomberMap.NOTHING) {
                                        canMove = true;
                                        break;
                                    }
                                }
                            }
                            /**
                             * if it can move
                             */
                            if (canMove) {
                                /**
                                 * clear original block
                                 */
                                clear = true;
                                game.paintImmediately(x,
                                        y - halfSize, width, height);
                                /**
                                 * move right
                                 */
                                x += ox;
                                /**
                                 * refresh the sprite
                                 */
                                clear = false;
                                game.paintImmediately(x,
                                        y - halfSize, width, height);
                            }
                        }
                        /**
                         * if it can move
                         */
                        if (canMove) {
                            /**
                             * clear original block
                             */
                            clear = true;
                            game.paintImmediately(x,
                                    y - halfSize, width, height);
                            /**
                             * move up
                             */
                            y -= (size / 4);
                            /**
                             * refresh the sprite
                             */
                            clear = false;
                            game.paintImmediately(x,
                                    y - halfSize, width, height);
                        } /**
                         * if it can't move
                         */
                        else {
                            /**
                             * refresh the block
                             */
                            moving = false;
                            game.paintImmediately(x,
                                    y - halfSize, width, height);
                        }
                    } /**
                     * if the down is is down
                     */
                    else if ((currentDirKeyDown & BDOWN) > 0) {
                        state = DOWN;
                        canMove = false;
                        /**
                         * if the south block is empty
                         */
                        canMove = (y % size != 0 || (x % size == 0
                                && (map.grid[x >> shiftCount][(y >> shiftCount) + 1]
                                <= BomberMap.NOTHING)));

                        /**
                         * if it can't move
                         */
                        if (!canMove) {
                            int ox = 0;
                            /**
                             * see if it's a bit to the west
                             */
                            for (ox = -offset; ox < 0; ox += (size / 4)) {
                                /**
                                 * and the south block is empty
                                 */
                                if ((x + ox) % size == 0
                                        && map.grid[(x + ox) >> shiftCount][(y >> shiftCount) + 1] <= BomberMap.NOTHING) {
                                    canMove = true;
                                    break;
                                }
                            }
                            /**
                             * if it still can't move
                             */
                            if (!canMove) {
                                /**
                                 * see if it's a bit to the east
                                 */
                                for (ox = (size / 4); ox <= offset;
                                        ox += (size / 4)) {
                                    /**
                                     * and the south block is empty
                                     */
                                    if ((x + ox) % size == 0
                                            && map.grid[(x + ox) >> shiftCount][(y >> shiftCount) + 1]
                                            <= BomberMap.NOTHING) {
                                        canMove = true;
                                        break;
                                    }
                                }
                            }
                            /**
                             * if it can move now
                             */
                            if (canMove) {
                                /**
                                 * clear orignal block
                                 */
                                clear = true;
                                game.paintImmediately(x,
                                        y - halfSize, width, height);
                                /**
                                 * move left or right
                                 */
                                x += ox;
                                /**
                                 * refresh the sprite
                                 */
                                clear = false;
                                game.paintImmediately(x,
                                        y - halfSize, width, height);
                            }
                        }
                        /**
                         * if it can move now
                         */
                        if (canMove) {
                            /**
                             * clear original spot
                             */
                            clear = true;
                            game.paintImmediately(x,
                                    y - halfSize, width, height);
                            /**
                             * move down
                             */
                            y += (size / 4);
                            /**
                             * refresh the sprite
                             */
                            clear = false;
                            game.paintImmediately(x,
                                    y - halfSize, width, height);
                        } /**
                         * if it can't move
                         */
                        else {
                            /**
                             * refresh the sprite
                             */
                            moving = false;
                            game.paintImmediately(x,
                                    y - halfSize, width, height);
                        }
                    }
                }
            } /**
             * if all keys are up
             */
            else if (!isExploding && !isDead && lastState != keyPressed) {
                /**
                 * reset frame to 0
                 */
                frame = 0;
                moving = false;
                /**
                 * refresh sprite
                 */
                game.paintImmediately(x, y - halfSize, width, height);
                lastState = keyPressed;
            } /**
             * if it's exploding
             */
            else if (!isDead && isExploding) {
                /**
                 * if frame reached 4 then it's dead
                 */
                if (frame >= 4) {
                    isDead = true;
                }
                /**
                 * refresh sprite
                 */
                game.paintImmediately(x, y - halfSize, width, height);
                /**
                 * rotate frame count
                 */
                frame = (frame + 1) % 5;
            } /**
             * if it's dead
             */
            else if (isDead) {
                /**
                 * clear the block
                 */
                clear = true;
                game.paintImmediately(x, y - halfSize, width, height);
                /**
                 * exit the loop
                 */
                break;
            }
            /**
             * see if the player stepped on any bonuses
             */
            /**
             * try normal position
             */
            if (map.bonusGrid[x >> shiftCount][y >> shiftCount] != null) {
                bx = x;
                by = y;
            } /**
             * try a bit to the north
             */
            else if (map.bonusGrid[x >> shiftCount][(y + halfSize)
                    >> shiftCount] != null) {
                bx = x;
                by = y + halfSize;
            } /**
             * try a bit to the left
             */
            else if (map.bonusGrid[(x + halfSize) >> shiftCount][y
                    >> shiftCount] != null) {
                bx = x + halfSize;
                by = y;
            }
            /**
             * if the player did step on a bonus
             */
            if (bx != 0 && by != 0) {
                map.bonusGrid[bx >> shiftCount][by
                        >> shiftCount].giveToPlayer(playerNo);
                bx = by = 0;
            }
            /**
             * if it's dead, then exit the loop
             */
            if (isDead) {
                try {
                    System.out.println("Killing agent controller" + ac.getName());
                    ac.kill();
                } catch (StaleProxyException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                break;
            }
            /**
             * delay 65 milliseconds
             */
            try {
                sleep(65);
            } catch (Exception e) {
            }
        }
        interrupt();
    }

    /**
     * Drawing method.
     */
    public void paint(Graphics graphics) {
        Graphics g = graphics;
        /**
         * if java runtime is Java 2
         */
        if (Main.J2) {
            paint2D(graphics);
        } /**
         * if java runtime isn't Java 2
         */
        else {
            /**
             * if player isn't dead and clear mode isn't on
             */
            if (!isDead && !clear) {
                /**
                 * if moving
                 */
                if (moving) /**
                 * draw the animating image
                 */
                {
                    g.drawImage(sprites[playerNo - 1][state][frame],
                            x, y - (BomberMain.size / 2), width, height, null);
                } /**
                 * if not moving
                 */
                else /**
                 * draw the still image
                 */
                {
                    g.drawImage(sprites[playerNo - 1][state][0],
                            x, y - (BomberMain.size / 2), width, height, null);
                }
            }
        }
    }

    /**
     * Drawing method for Java 2's Graphics2D
     *
     * @param graphics graphics handle
     */
    public void paint2D(Graphics graphics) {
        Graphics2D g2 = (Graphics2D) graphics;        
        int new_x;
        int new_y;
        /**
         * set the rendering hints
         */
        g2.setRenderingHints((RenderingHints) hints);
        /**
         * if player isn't dead and clear mode isn't on
         */
        if (!isDead && !clear) {
            /**
             * if moving
             */
            if (moving) /**
             * draw the animating image
             */
            {
                g2.drawImage(sprites[playerNo - 1][state][frame],
                        x, y - (BomberMain.size / 2), width, height, null);
            } /**
             * if not moving
             */
            else /**
             * draw the still image
             */
            {
                g2.drawImage(sprites[playerNo - 1][state][0],
                        x, y - (BomberMain.size / 2), width, height, null);
            }
            /* TODO: send player position notifications here */
            /* this is to convert x y to grid positions used for bombs */
            new_x = (x / 15);
            new_y = (y / 15);

            if (new_x != prev_x || new_y != prev_y || notifyStaticPosition) {
                try {
                    if (ac != null) {
                        String id = ac.getName();
                        String[] names = id.split("@");
                        int playerId = Integer.parseInt(names[0].replaceAll("Bomber", ""));

                        String message = "player:" + playerId + ":" + this.team + ":" + new_x + ":" + new_y;
                        sendMessage(message);
                    }
                } catch (StaleProxyException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                prev_x = new_x;
                prev_y = new_y;
                notifyStaticPosition = false;
            } else {
                notifyStaticPosition = true;
            }

        }
    }

    /**
     * Send a notification message to agents
     */
    public void sendMessage(String message) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setLanguage("English");
        msg.setOntology("Weather-forecast-ontology");
        msg.setContent(message);
        /* Iterate over the list of subscribed agents */
        for (int i = 0; i < BomberMain.index; i++) {
            msg.addReceiver(new AID(BomberMain.subscribers[i], AID.ISLOCALNAME));
        }
        mainAgent.send(msg);

    }

    public AgentController createBomberAgent(
            String host, // JADE Book Trading environment Main Container
            String port, // JADE Book Trading environment Main Container port
            String name // Book Buyer agent name
    ) {
        // Retrieve the singleton instance of the JADE Runtime
        jade.core.Runtime runtime = jade.core.Runtime.instance();
        // Create a container to host the Book Buyer agent
        Profile p = new ProfileImpl();
        p.setParameter(Profile.MAIN_HOST, host);
        p.setParameter(Profile.MAIN_PORT, port);
        ContainerController cc = runtime.createAgentContainer(p);
        // arguments for the agent constructor
        Object[] args = new Object[5];
        args[0] = this;

        if (cc != null) {
            // Create the Book Buyer agent and start it
            try {
                ac = cc.createNewAgent(name,
                        "BomberPlayerAgent",
                        args);
                team = teamAssigner;
                ac.start();
                teamAssigner++;
                /* right value is the number of desired different teams */
                teamAssigner %= NUMBER_OF_TEAMS;
                return ac;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void createBomberPlayerAgent() {
        createBomberAgent("192.168.0.13", "1099", "Bomber" + playerNo);
    }

    public void sendPosition() {
        String message = "Player:" + playerNo + ":" + teamAssigner + ":" + (x >> BomberMain.shiftCount) + ":" + (y >> BomberMain.shiftCount);
        sendMessage(message);
    }
}
