/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author cristopherson
 */
public class MoveValidator {

    public static int nextMove(int prevX, int prevY, int newX, int newY) {

        if (((prevX == newX) && (prevY == newY)) || ((newX < 1) || newX > 15) || ((newY < 1) || newY > 15)) {
            return -1;
        }

        if (prevX == newX) {
            if ((prevX % 2) == 1) {
                if (prevY > newY) {
                    return BomberPlayer.UP;
                } else {
                    return BomberPlayer.DOWN;
                }
            }
        } else if (prevY == newY) {
            if ((prevY % 2) == 1) {
                if (prevX > newX) {
                    return BomberPlayer.LEFT;
                } else {
                    return BomberPlayer.RIGHT;
                }
            }
        }
        return -1;
    }

    public static boolean hasElementAround(BomberMap map, int type, int x, int y) {
        if (x < 1 || x > 15 || y < 1 || y > 15) {
            return false;
        }
        if (map.grid[x + 1][y] == type) {
            return true;
        }
        if (map.grid[x - 1][y] == type) {
            return true;
        }
        if (map.grid[x][y + 1] == type) {
            return true;
        }
        if (map.grid[x][y - 1] == type) {
            return true;
        }
        return false;
    }

    public static int nextMove(BomberMap map, int type, int x, int y) {
        if (x < 1 || x > 15 || y < 1 || y > 15) {
            return -1;
        }
        if (map.grid[x + 1][y] == type) {
            return BomberPlayer.RIGHT;
        }
        if (map.grid[x - 1][y] == type) {
            return BomberPlayer.LEFT;
        }
        if (map.grid[x][y - 1] == type) {
            return BomberPlayer.UP;
        }
        if (map.grid[x][y + 1] == type) {
            return BomberPlayer.DOWN;
        }
        return BomberPlayer.BOMB;
    }
}
