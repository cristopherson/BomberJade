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

    public static boolean hasElement(BomberMap map, int type, int x, int y) {
        if (x < 1 || x > 15 || y < 1 || y > 15) {
            return false;
        }

        if (type == BomberMap.WARNING) {
            return map.warningGrid[x][y];
        }

        if (map.grid[x][y] == type) {
            return true;
        }
        return false;
    }

    public static boolean hasSafeMove(BomberMap map, int x, int y) {
        
        if (x < 1 || x > 15 || y < 1 || y > 15) {
            return false;
        }        

        if (canSafeMove(map, x + 1, y)) {
            return true;
        }

        if (canSafeMove(map, x - 1, y)) {
            return true;
        }
        if (canSafeMove(map, x, y - 1)) {
            return true;
        }
        if (canSafeMove(map, x, y + 1)) {
            return true;
        }
        return false;
    }

    public static int nextSafeMove(BomberMap map, int x, int y) {
        if (x < 1 || x > 15 || y < 1 || y > 15) {
            return -1;
        }

        if (map.grid[x + 1][y] <= BomberMap.NOTHING) {
            return BomberPlayer.RIGHT;
        }
        if (map.grid[x - 1][y] <= BomberMap.NOTHING) {
            return BomberPlayer.LEFT;
        }
        if (map.grid[x][y - 1] <= BomberMap.NOTHING) {
            return BomberPlayer.UP;
        }
        if (map.grid[x][y + 1] <= BomberMap.NOTHING) {
            return BomberPlayer.DOWN;
        }
        return -1;
    }

    public static boolean canSafeMoveCheckWarnings(BomberMap map, int x, int y) {
        if (x < 1 || x > 15 || y < 1 || y > 15) {
            return false;
        }

        return (map.grid[x][y] <= BomberMap.NOTHING && !MoveValidator.isWarning(map, x, y));
    }
    
    public static boolean canSafeMove(BomberMap map, int x, int y) {
        if (x < 1 || x > 15 || y < 1 || y > 15) {
            return false;
        }

        return (map.grid[x][y] <= BomberMap.NOTHING);
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
        return -1;
    }

    public static boolean isWarning(BomberMap map, int x, int y) {
        return map.warningGrid[x][y];
    }

    public static int getContent(BomberMap map, int move, int x, int y) {
        int new_x = x;
        int new_y = y;

        switch (move) {
            case BomberPlayer.UP:
                new_y -= 1;
                break;
            case BomberPlayer.DOWN:
                new_y += 1;
                break;
            case BomberPlayer.LEFT:
                new_x -= 1;
                break;
            case BomberPlayer.RIGHT:
                new_x += 1;
                break;
        }

        if (new_x < 1 || new_x > 15 || new_y < 1 || new_y > 15) {
            return BomberMap.WALL;
        }

        if (map.grid[new_x][new_y] > BomberMap.NOTHING || !map.warningGrid[new_x][new_y]) {
            return map.grid[new_x][new_y];
        }

        return BomberMap.WARNING;
    }
    
    public static boolean hasSafeMoveInFuture(BomberMap map, int move, int x, int y) {
        int new_x = x;
        int new_y = y;

        switch (move) {
            case BomberPlayer.UP:
                new_y -= 1;
                break;
            case BomberPlayer.DOWN:
                new_y += 1;
                break;
            case BomberPlayer.LEFT:
                new_x -= 1;
                break;
            case BomberPlayer.RIGHT:
                new_x += 1;
                break;
        }
        
        return hasSafeMove(map, new_x, new_y);
    }
}
