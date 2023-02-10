package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.HashMap;
import java.util.Random;
import java.util.ArrayList;


public class Room {
    private static int WIDTH;
    private static int HEIGHT;

    private static long SEED;

    //create random number from seed
    private static Random RANDOM;

    private static HashMap<Integer, ArrayList<Position>> builtTiles;

    private static ArrayList<ArrayList<Integer>> roomList;

    private static ArrayList<Integer> roomDimensions;

    private static boolean isRoom(int roomNum) {
        ArrayList<Integer> dimensions = roomList.get(roomNum);
        return dimensions.get(2).equals(0);
    }

    private static boolean wasVertical() {
        int latestWidth = getX(roomNumber);
        int latestHeight = getY(roomNumber);
        return latestHeight >= latestWidth;
    }

    private static int getX(int roomNum) {
        ArrayList<Integer> dimensions = roomList.get(roomNum);
        return dimensions.get(0);
    }

    private static int getY(int roomNum) {
        ArrayList<Integer> dimensions = roomList.get(roomNum);
        return dimensions.get(1);
    }

    private static Position getP(int roomNum) {
        ArrayList<Integer> dimensions = roomList.get(roomNum);
        int xCoor = dimensions.get(3);
        int yCoor = dimensions.get(4);
        return new Position(xCoor, yCoor);
    }


    private static ArrayList<Position> wallTracker;

    private static ArrayList<Position> allWallTracker;

    private static ArrayList<Position> removedWalls;

    private static int roomHWCount;

    private static int roomNumber;

    private static Position currentP;

    private static boolean ableToMakeRoom;

    private static boolean fullRoom;


    private static int nearestRoom() {
        int nearestRoom = 0;
        for (int i = roomNumber; i >= 0; i--) {
            ArrayList<Integer> dimensions = roomList.get(i);
            if (dimensions.get(2) == 0) {
                nearestRoom = i;
            }
            if (nearestRoom != 0) {
                return nearestRoom;
            }
        }
        return nearestRoom;
    }



    public Room(long seed, int width, int height) {
        roomHWCount = 0;
        roomNumber = 0;
        SEED = seed;
        WIDTH = width;
        HEIGHT = height;
        builtTiles = new HashMap<>();
        roomList = new ArrayList<>();
        RANDOM = new Random(SEED);
        roomDimensions = new ArrayList<>(5);
        wallTracker = new ArrayList<Position>();
        allWallTracker = new ArrayList<Position>();
        removedWalls = new ArrayList<>();
        ableToMakeRoom = true;
        fullRoom = false;
    }

    private static class Position {
        int x;
        int y;

        Position(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public Position shift(int dx, int dy) {
            return new Position(this.x + dx, this.y + dy);
        }

        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }

            if (obj.getClass() != this.getClass()) {
                return false;
            }

            final Position other = (Position) obj;
            if (this.x != other.x) {
                return false;
            }

            if (this.y != other.y) {
                return false;
            }

            return true;
        }

        public int hashCode() {
            return (this.x * 100 + this.y);
        }
    }



    public static void fillBoardWithNothing(TETile[][] tiles) {
        int height = tiles[0].length;
        int width = tiles.length;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y += 1) {
                tiles[x][y] = Tileset.NOTHING;
            }
        }
    }

    //Puts down rows of specific tiles
    public static void drawRow(TETile[][] tiles, Position p, int length) {
        tiles[p.x][p.y] = Tileset.WALL;
        wallTracker.add(p);
        allWallTracker.add(p);
        for (int i = 1; i < length - 1; i++) {
            tiles[p.x + i][p.y] = Tileset.FLOOR;
        }
        Position temp = p.shift(length - 1, 0);
        wallTracker.add(temp);
        allWallTracker.add(temp);
        tiles[temp.x][temp.y] = Tileset.WALL;
    }

    public static void drawRowWall(TETile[][] tiles, Position p, int length) {
        Position temp = p;
        for (int i = 0; i < length; i++) {
            wallTracker.add(temp);
            allWallTracker.add(temp);
            tiles[p.x + i][p.y] = Tileset.WALL;
            temp = temp.shift(1, 0);
        }
    }

    private static boolean checkUp(Position p, int width, int height) {
        boolean boundary = !(p.x + width < WIDTH && p.y + height < HEIGHT);
        boolean overlap = false;
        if (allWallTracker == null) {
            return boundary;
        }
        for (int i = 1; i < width; i++) {
            for (int j = 1; j < height; j++) {
                Position curr = p.shift(i, j);
                if (allWallTracker.contains(curr)) {
                    overlap = true;
                    break;
                }
            }
            if (overlap) {
                break;
            }
        }
        return (boundary || overlap);
    }

    private static boolean checkDown(Position p, int width, int height) {
        ArrayList<Position> allWall = allWallTracker;
        boolean boundary = !(p.x + width < WIDTH && p.y - height > 0);
        boolean overlap = false;
        for (int i = 0; i < width; i++) {
            for (int j = 1; j < height; j++) {
                Position curr = p.shift(i, -j);
                if (allWallTracker.contains(curr)) {
                    overlap = true;
                    break;
                }
            }
            if (overlap) {
                break;
            }
        }
        return (boundary || overlap);
    }

    private static boolean checkLeft(Position p, int width, int height) {
        boolean boundary = !(p.x - width > 0 && p.y + height < HEIGHT);
        boolean overlap = false;
        for (int i = 1; i < width; i++) {
            for (int j = 0; j < height; j++) {
                Position curr = p.shift(-i, j);
                if (allWallTracker.contains(curr)) {
                    overlap = true;
                    break;
                }
            }
            if (overlap) {
                break;
            }
        }
        return (boundary || overlap);
    }

    private static void makeRoomHelper2(TETile[][] tiles, Position p, int width, int height) {
        drawRowWall(tiles, p, width);
        for (int i = 1; i < height - 1; i++) {
            Position temp = p.shift(0, i);
            drawRow(tiles, temp, width);
        }
        Position temp2 = p.shift(0, height - 1);
        drawRowWall(tiles, temp2, width);
    }
    private static void makeRoomHelper1(TETile[][] tiles, Position p,
                                        int width, int height, String direction) {
        ArrayList<Position> array = removedWalls;
        ArrayList<Position> prevTiles = builtTiles.get(roomNumber);
        if (prevTiles == null) {
            prevTiles = new ArrayList<Position>();

        }
        switch (direction) {
            case "up":
                for (int i = -3; i < width + 1; i++) {
                    Position temp = p.shift(i, 0);
                    if (prevTiles.contains(temp)) {
                        array.add(temp);
                    }
                }
                makeRoomHelper2(tiles, p, width, height);
                currentP = p;
                break;
            case "right":
                for (int i = -3; i < height + 1; i++) {
                    Position temp1 = p.shift(0, i);
                    if (prevTiles.contains(temp1)) {
                        array.add(temp1);
                    }
                }
                makeRoomHelper2(tiles, p, width, height);
                currentP = p;
                break;
            case "down":
                for (int i = -3; i < width + 1; i++) {
                    Position temp2 = p.shift(i, 0);
                    if (prevTiles.contains(temp2)) {
                        array.add(temp2);
                    }
                }
                Position now = p.shift(0, -height + 1);
                currentP = now;
                makeRoomHelper2(tiles, now, width, height);
                break;
            case "left":
                for (int i = -3; i < height + 1; i++) {
                    Position temp3 = p.shift(0, i);
                    if (prevTiles.contains(temp3)) {
                        array.add(temp3);
                    }
                }
                Position now1 = p.shift(-width + 1, 0);
                currentP = now1;
                makeRoomHelper2(tiles, now1, width, height);
                break;
            default:
                break;
        }
        removedWalls = array;
    }


    private static String findDirectionHorizontal(TETile[][] tiles, Position p) {
        if ((p.x + 1 >= WIDTH - 1)
                || (p.x - 1 <= 1)
                || (p.y + 1 >= HEIGHT - 1)
                || (p.y - 1 <= 1)) {
            return null;
        }
        String up = tiles[p.x][p.y + 1].description();
        String down = tiles[p.x][p.y - 1].description();
        String left = tiles[p.x - 1][p.y].description();
        String right = tiles[p.x + 1][p.y].description();
        String itself = tiles[p.x][p.y].description();
        if (tiles[p.x + 1][p.y].equals(Tileset.WALL)
                && (tiles[p.x][p.y - 1].equals(Tileset.FLOOR) || tiles[p.x][p.y - 1].equals(Tileset.WALL))) {
            return "up";
        }
        if (tiles[p.x + 1][p.y].equals(Tileset.WALL)
                && (tiles[p.x][p.y + 1].equals(Tileset.FLOOR))) {
            return "down";
        }
        if (tiles[p.x][p.y + 1].equals(Tileset.WALL)
                && (tiles[p.x + 1][p.y].equals(Tileset.FLOOR) || tiles[p.x + 1][p.y].equals(Tileset.WALL))) {
            return "left";
        }
        if (tiles[p.x][p.y + 1].equals(Tileset.WALL)
                && (tiles[p.x - 1][p.y].equals(Tileset.FLOOR) || tiles[p.x - 1][p.y].equals(Tileset.WALL))) {
            return "right";
        }
        return null;
    }

    private static String findDirectionVertical(TETile[][] tiles, Position p) {
        if ((p.x + 1 >= WIDTH - 1)
                || (p.x - 1 <= 1)
                || (p.y + 1 >= HEIGHT -1 )
                || (p.y - 1 <= 1)) {
            return null;
        }
        if (tiles[p.x + 1][p.y].equals(Tileset.WALL) &&
                (tiles[p.x][p.y - 1].equals(Tileset.FLOOR) || tiles[p.x][p.y - 1].equals(Tileset.WALL))) {
            return "up";
        }
        if (tiles[p.x + 1][p.y].equals(Tileset.WALL) &&
                (tiles[p.x][p.y + 1].equals(Tileset.FLOOR)|| tiles[p.x][p.y + 1].equals(Tileset.WALL))) {
            return "down";
        }
        if (tiles[p.x][p.y + 1].equals(Tileset.WALL) &&
                (tiles[p.x + 1][p.y].equals(Tileset.FLOOR))) {
            return "left";
        }
        if (tiles[p.x][p.y + 1].equals(Tileset.WALL) &&
                (tiles[p.x - 1][p.y].equals(Tileset.FLOOR) || tiles[p.x - 1][p.y].equals(Tileset.WALL))) {
            return "right";
        }
        return null;
    }


    private static void makeHelper (TETile[][] tiles, Position p, int width, int height, String direction) {
        boolean check = false;
        if (direction == null) {
            check = true;
        } else if (direction.equals("up") || direction.equals("right")) {
            check = checkUp(p, width, height);
        } else if (direction.equals("down")) {
            check  = checkDown(p, width, height);
        } else if (direction.equals("left")) {
            check = checkLeft(p, width, height);
        }

        if (!check) {
            makeRoomHelper1(tiles, p, width, height, direction);
            ableToMakeRoom = true;
        } else {
            ableToMakeRoom = false;
        }
    }

    public static void makeRoom(TETile[][] tiles, Position p) {
        String direction;
        if (wasVertical()) {
            direction = findDirectionVertical(tiles, p);
        } else {
            direction = findDirectionHorizontal(tiles, p);
        }
        int roomWidth = RANDOM.nextInt(5) + 4;
        int roomHeight = RANDOM.nextInt(5) + 4;
        makeHelper(tiles, p, roomWidth, roomHeight, direction);
        if (ableToMakeRoom) {
            roomNumber += 1;
            removeTiles();
            updateParameter(roomWidth, roomHeight, p, 0);
        } else {
            roomNumber = nearestRoom();
            Position temp = getP(roomNumber);
            Position curr = newPHallWayFromRoom(temp);
            makeHallway(tiles, curr);
        }
    }

    private static void makeFirstRoom(TETile[][] tiles) {
        Position p = new Position(RANDOM.nextInt(WIDTH / 2) + 2, RANDOM.nextInt(HEIGHT / 2) + 2);
        String direction = "up";
        int roomWidth = RANDOM.nextInt(5) + 5;
        int roomHeight = RANDOM.nextInt(5) + 5;
        makeHelper(tiles, p, roomWidth, roomHeight, direction);
        updateParameter(roomWidth, roomHeight, p, 0);
    }

    public static void makeHallway(TETile[][] tiles, Position p) {
        if (roomNumber == 0 && p == null) {
            fullRoom = true;
        } else {
            if (p == null) {
                unableToMakeRoom(tiles);
            } else {
                String direction;
                int width;
                int height;
                if (isRoom(roomNumber)) {
                    direction = findDirectionHorizontal(tiles, p);
                    if (direction == null || direction.equals("left") || direction.equals("right")) {
                        width = RANDOM.nextInt(5) + 4;
                        height = 3;
                    } else {
                        width = 3;
                        height = RANDOM.nextInt(5) + 4;
                    }
                } else {
                    if (wasVertical()) {
                        direction = findDirectionVertical(tiles, p);
                        width = RANDOM.nextInt(5) + 4;
                        height = 3;
                    } else {
                        direction = findDirectionHorizontal(tiles, p);
                        width = 3;
                        height = RANDOM.nextInt(5) + 4;
                    }
                }
                makeHelper(tiles, p, width, height, direction);
                if (ableToMakeRoom) {
                    roomNumber += 1;
                    removeTiles();
                    updateParameter(width, height, p, 1);
                } else {
                    unableToMakeRoom(tiles);
                }
            }
        }
    }

    private static void unableToMakeRoom(TETile[][] tiles) {
        roomNumber = nearestRoom();
        Position temp = getP(roomNumber);
        ArrayList<Position> availTiles = builtTiles.get(roomNumber);
        if (availTiles.size() != 0) {
            Position curr = newPHallWayFromRoom(temp);
            makeHallway(tiles, curr);
        } else {
            if (roomNumber == 0) {
                makeHallway(tiles, null);
                return;
            }
            roomNumber -= 1;
            unableToMakeRoom(tiles);
        }
    }

    private static void removeTiles() {
        ArrayList<Position> remove = removedWalls;
        ArrayList<Position> all = wallTracker;
        ArrayList<Position> prev = builtTiles.get(roomNumber - 1);
        for (Position p1: remove) {
            for (Position p2: all) {
                if ((p1.x == p2.x) && (p1.y == p2.y)) {
                    wallTracker.remove(p2);
                    break;
                }
            }
        }

        for (Position p1: remove) {
            for (Position p2: prev) {
                if ((p1.x == p2.x) && (p1.y == p2.y)) {
                    prev.remove(p2);
                    break;
                }
            }
        }
        builtTiles.put(roomNumber - 1, prev);
    }

    private static void updateParameter(int x, int y, Position p, int room) {
        roomDimensions.add(0, x);
        roomDimensions.add(1, y);
        roomDimensions.add(2, room);
        roomDimensions.add(3, currentP.x);
        roomDimensions.add(4, currentP.y);
        roomList.add(roomNumber, roomDimensions);
        roomHWCount += 1;
        builtTiles.put(roomNumber, wallTracker);
        showParameter();
        wallTracker = new ArrayList<Position>();
        roomDimensions = new ArrayList<>(5);
        removedWalls = new ArrayList<Position>();
    }

    private static void showParameter() {
        ArrayList<Integer> dimensions = roomDimensions;
        ArrayList<ArrayList<Integer>> rooms = roomList;
        int roomNum = roomNumber;
        int count = roomHWCount;
        HashMap<Integer, ArrayList<Position>> trackedTiles = builtTiles;
        ArrayList<Position> allTiles = allWallTracker;
        ArrayList<Position> thisRoomTiles = wallTracker;
        ArrayList<Position> removedTiles = removedWalls;
    }

    private static void removeNewTile(Position p) {
        ArrayList<Position> newTiles = builtTiles.get(roomNumber);
        newTiles.remove(p);
    }

    private static Position newPRoom(TETile[][] tiles, Position p) {
        int latestWidth = getX(roomNumber);
        int latestHeight = getY(roomNumber);
        Position returnP;
        if (isRoom(roomNumber - 1)) {
            if (latestWidth > latestHeight) {
                if (tiles[p.x - 1][p.y].equals(Tileset.NOTHING)) {
                    returnP = p;
                } else {
                    returnP = p.shift(latestWidth - 1, 0);
                }
            } else {
                if (tiles[p.x][p.y - 1].equals(Tileset.NOTHING)) {
                    returnP = p;
                } else {
                    returnP = p.shift(0, latestHeight - 1);
                }
            }
        } else {
            if (latestWidth > latestHeight) {
                returnP = p.shift(latestWidth - 1, 0);
            } else {
                returnP = p.shift(0, latestHeight - 1);
            }
        }
        return returnP;
    }

    private static Position newPHallWayFromRoom(Position p) {
        int latestWidth = getX(roomNumber);
        int latestHeight = getY(roomNumber);
        int xRange = p.x + latestWidth - 2;
        int yRange = p.y + latestHeight - 2;
        ArrayList<Position> temp = builtTiles.get(roomNumber);
        if (!(temp.size() == 0)) {
            int rand = RANDOM.nextInt(temp.size());
            Position curr = temp.get(rand);
            removeNewTile(curr);
            if ((curr.x >= xRange && (curr.y == p.y || curr.y == (p.y + latestHeight - 1)))
                    || (curr.y >= yRange && (curr.x == p.x || curr.x == (p.x + latestWidth - 1)))) {
                return newPHallWayFromRoom(p);
            } else {
                return curr;
            }
        } else {
            return null;
        }
    }

    private static Position newPHallwayFromHW(TETile[][] tiles, Position p) {
        int latestWidth = getX(roomNumber);
        int latestHeight = getY(roomNumber);
        Position returnP;
        if (isRoom(roomNumber - 1)) {
            if (latestWidth > latestHeight) {
                if (tiles[p.x - 1][p.y].equals(Tileset.NOTHING)) {
                    returnP = p;
                } else {
                    returnP = p.shift(latestWidth - 1, 0);
                }
            } else {
                if (tiles[p.x][p.y - 1].equals(Tileset.NOTHING)) {
                    returnP = p;
                } else {
                    returnP = p.shift(0, latestHeight - 1);
                }
            }
        } else {
            if (latestWidth > latestHeight) {
                returnP = p.shift(latestWidth - 1, 0);
            } else {
                returnP = p.shift(0, latestHeight - 1);
            }
        }
        return returnP;
    }

    private static void connectRooms(TETile[][] tiles) {
        int size = allWallTracker.size();
        for (Position p : allWallTracker) {
            if (tiles[p.x + 1][p.y].equals(Tileset.FLOOR) && tiles[p.x - 1][p.y].equals(Tileset.FLOOR)) {
                tiles[p.x][p.y] = Tileset.FLOOR;
            } else if (tiles[p.x][p.y + 1].equals(Tileset.FLOOR) && tiles[p.x][p.y - 1].equals(Tileset.FLOOR)) {
                tiles[p.x][p.y] = Tileset.FLOOR;
            } else if ((tiles[p.x + 1][p.y].equals(Tileset.FLOOR)
                    && tiles[p.x - 1][p.y].equals(Tileset.WALL)
                    && tiles[p.x - 2][p.y].equals(Tileset.FLOOR))) {
                tiles[p.x][p.y] = Tileset.FLOOR;
                tiles[p.x - 1][p.y] = Tileset.FLOOR;
            } else if (tiles[p.x][p.y + 1].equals(Tileset.FLOOR)
                    && tiles[p.x][p.y - 1].equals(Tileset.WALL)
                    && tiles[p.x][p.y - 2].equals(Tileset.FLOOR)) {
                tiles[p.x][p.y] = Tileset.FLOOR;
                tiles[p.x][p.y - 1] = Tileset.FLOOR;
            }
        }
    }

    public static void drawWorld(TETile[][] tiles, int count) {
        makeFirstRoom(tiles);
        while (roomHWCount < count) {
            if (!fullRoom) {
                boolean isRoom = isRoom(roomNumber);
                Position latestP = getP(roomNumber);
                if (isRoom) {
                    Position HWP = newPHallWayFromRoom(latestP);
                    makeHallway(tiles, HWP);
                } else {
                    int rand = RANDOM.nextInt(2);
                    switch (rand) {
                        case 0:
                            Position roomP = newPRoom(tiles, latestP);
                            makeRoom(tiles, roomP);
                            break;
                        case 1:
                            Position hallwayP = newPHallwayFromHW(tiles, latestP);
                            makeHallway(tiles, hallwayP);
                            break;
                    }
                }
            } else {
                roomHWCount = count + 1;
            }
        }
        connectRooms(tiles);
    }

    public static void main(String[] args) {
        TERenderer render = new TERenderer();
        render.initialize(50, 50);

        TETile[][] world = new TETile[50][50];
        Long seed = 234l;
        Room newRoom = new Room(seed,50, 50);
        newRoom.fillBoardWithNothing(world);
        newRoom.drawWorld(world, 200);
        System.out.println(currentP.x);
        System.out.println(currentP.x);

        render.renderFrame(world);
    }
}
