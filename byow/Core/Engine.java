package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.introcs.StdDraw;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static byow.Core.FileUtils.*;
import static java.lang.Character.*;

public class Engine {
    private static TERenderer ter = new TERenderer();
    private static TETile[][] world;
    /* Feel free to change the width and height. */
    public static final int WIDTH = 50;
    public static final int HEIGHT = 50;
    private static final File CWD = new File(System.getProperty("user.dir"));
    private static final File SAVED = join(CWD, "saved.txt");

    private static boolean menu;
    private static boolean pressedN;
    private static boolean pressedColon;
    private static boolean winGame;
    private static boolean inputString;
    private static  boolean theEnd;
    private static String seed;
    private static String input;
    private static ArrayList<Integer> player;
    private static ArrayList<Integer> door;

    public Engine() {
        menu = true;
        pressedN = false;
        pressedColon = false;
        winGame = false;
        inputString = false;
        theEnd = false;
        seed = "";
        input = "";
        world = new TETile[WIDTH][HEIGHT];
        player = new ArrayList<Integer>();
        door = new ArrayList<Integer>();
    }

    public static void setupPersistence() {
        if (!SAVED.exists()) {
            try {
                SAVED.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void processInput(String input) {
        if (theEnd) {
            return;
        }

        char current = input.charAt(0);
        current = toLowerCase(current);
        processChar(current);
        String restOfInput = input.substring(1);
        if (restOfInput.length() == 0) {
            theEnd = true;
        }
        processInput(restOfInput);
    }


    /** What do I do when the user enter different keys than the stated? Do I exit or do I just not do anything?
     * It does not do anything in the below implementation
     * Not sure if the :q quit is enough **/
    private static void processChar(char c) {
        if (menu) {
            switch(c) {
                case 'n':
                    pressedN = true;
                    input = input + c;
                    break;
                case 's':
                    createWorld();
                    input = input + c;
                    break;
                case 'l':
                    load();
                    break;
                case 'r':
                    replay();
                case 'q':
                    System.exit(0);
                    break;
                default:
                    if (Character.isDigit(c)) {
                        seed = seed + c;
                    }
                    input = input + c;
                    break;
            }
        } else {

            switch(c) {
                case 'w':
                case 's':
                case 'a':
                case 'd':
                    move(c);
                    input = input + c;
                    break;
                case ':':
                    pressedColon = true;
                    break;
                case 'q':
                    if (pressedColon) {
                        save();
                        if (!(inputString && !theEnd )) {
                            System.exit(0);
                        }
                    }
                    break;
            }
        }
    }


    private static void createWorld() {
        if (pressedN) {
            long seedL = Long.parseLong(seed);
            Room newRoom = new Room(seedL, WIDTH, HEIGHT);
            newRoom.fillBoardWithNothing(world);
            newRoom.drawWorld(world, 200);
            setPlayerAndGate();
            menu = false;
        } else {
            System.exit(0);
        }
    }

    private static ArrayList<ArrayList<Integer>> floorList() {
        ArrayList<ArrayList<Integer>> floorList = new ArrayList();
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j ++) {
                if (world[i][j].equals(Tileset.FLOOR)) {
                    ArrayList<Integer> position = new ArrayList<>();
                    position.add(i);
                    position.add(j);
                    floorList.add(position);
                }
            }
        }
        return floorList;
    }

    private static void setPlayerAndGate() {
        ArrayList<ArrayList<Integer>> floors = floorList();
        int widthP = 0;
        int heightP = 0;
        int widthG = 0;
        int heightG = 0;
        double maxDistance = 0;
        for (ArrayList<Integer> x : floors) {
            for (ArrayList<Integer> y : floors) {
                double currDistance = Math.sqrt(Math.pow(x.get(0) - y.get(0), 2) + Math.pow(x.get(1) - y.get(1), 2));
                if (currDistance > maxDistance) {
                    maxDistance = currDistance;
                    widthP = x.get(0);
                    heightP = x.get(1);
                    widthG = y.get(0);
                    heightG = y.get(1);
                }
            }
        }
        player.add(widthP);
        player.add(heightP);
        world[player.get(0)][player.get(1)] = Tileset.AVATAR;
        if (world[widthG - 1][heightG].equals(Tileset.FLOOR)) {
            widthG += 1;

        } else if (world[widthG + 1][heightG].equals(Tileset.FLOOR)) {
            widthG -= 1;
        } else if (world[widthG][heightG - 1].equals(Tileset.FLOOR)) {
            heightG += 1;
        } else if (world[widthG][heightG + 1].equals(Tileset.FLOOR)) {
            heightG -= 1;
        }
        world[widthG][heightG] = Tileset.LOCKED_DOOR;
        door.add(widthG);
        door.add(heightG);
    }

    private static void save() {
        SAVED.delete();
        try {
            SAVED.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        writeContents(SAVED, input);
    }

    private static void load() {
        String userInput = readContentsAsString(SAVED);
        interactWithInputString(userInput);
    }

    private static void move(char c) {
        int playerX = player.get(0);
        int playerY = player.get(1);
        world[playerX][playerY] = Tileset.FLOOR;
        switch (c) {
            case 'w':
                if (world[playerX][playerY + 1].equals(Tileset.FLOOR)) {
                    playerY += 1;
                }
                if (world[playerX][playerY + 1].equals(Tileset.LOCKED_DOOR)) {
                    winGame = true;
                }
                 break;
            case 's':
                if (world[playerX][playerY - 1].equals(Tileset.FLOOR)) {
                    playerY -= 1;
                }
                if (world[playerX][playerY - 1].equals(Tileset.LOCKED_DOOR)) {
                    winGame = true;
                }
                break;
            case 'd':
                if (world[playerX + 1][playerY].equals(Tileset.FLOOR)) {
                    playerX += 1;
                }
                if (world[playerX + 1][playerY].equals(Tileset.LOCKED_DOOR)) {
                    winGame = true;
                }
                break;
            case 'a':
                if (world[playerX - 1][playerY].equals(Tileset.FLOOR)) {
                    playerX -= 1;
                }
                if (world[playerX - 1][playerY].equals(Tileset.LOCKED_DOOR)) {
                    winGame = true;
                }
                break;
        }
        world[playerX][playerY] = Tileset.AVATAR;
        updatePlayer(playerX, playerY);
    }

    private static void replay() {
        String userInput = readContentsAsString(SAVED);
        String seedSt = "";
        for (int i = 0; i < userInput.length(); i++) {
            char c = userInput.charAt(0);
            seedSt += c;
            userInput = userInput.substring(1);
            if (c == 's') {
                break;
            }
        }
        input = seedSt;
        pressedN = true;
        seed = seedSt.substring(1, seedSt.length() - 1);
        createWorld();
        ter.renderFrame(world);
        while (userInput.length() != 0) {
            char c = userInput.charAt(0);
            int x = player.get(0);
            int y = player.get(1);
            processChar(c);
            ter.renderFrame(world);
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            userInput = userInput.substring(1);
        }
        interactWithKeyboard();
    }


    private static void updatePlayer(int x, int y) {
        player = new ArrayList<Integer>();
        player.add(x);
        player.add(y);
    }




    private static void generateMenu() {
        StdDraw.clear(StdDraw.BLACK);
        StdDraw.setPenColor(StdDraw.WHITE);

        StdDraw.setFont(new Font("Monaco", Font.BOLD, 40));
        StdDraw.text(WIDTH / 2, HEIGHT * 2 / 3, "THE WORLD");

        StdDraw.setFont(new Font("Monaco", Font.PLAIN, 20));
        StdDraw.text(WIDTH /2, HEIGHT / 2, "New Game (N)");
        StdDraw.text(WIDTH /2, HEIGHT / 2 - 2, "Load Game (L)");
        StdDraw.text(WIDTH /2, HEIGHT / 2 - 4, "Replay Game (R)");
        StdDraw.text(WIDTH /2, HEIGHT / 2 - 6, "Quit (Q)");

        if (pressedN) {
            StdDraw.clear(StdDraw.BLACK);
            StdDraw.setPenColor(StdDraw.WHITE);
            StdDraw.setFont(new Font("Arial", Font.PLAIN, 20));
            StdDraw.text(WIDTH / 2 , HEIGHT / 2, "Seed: " + seed);
            StdDraw.text(WIDTH / 2, HEIGHT / 2 + 2, "(Press a series of number and S to start the game)");
        }

        StdDraw.show();
    }

    private static void HUD() {
        int x = (int) StdDraw.mouseX();
        int y = (int) StdDraw.mouseY();
        String type = world[x][y].description();
        Font font = new Font("Monaco", Font.PLAIN, 10);
        StdDraw.setFont(font);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.textLeft(1, HEIGHT - 1, type);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss yyyy/MM/dd");
        LocalDateTime local = LocalDateTime.now();
        String time = dtf.format(local);
        StdDraw.textLeft(10, HEIGHT - 1, time);

        StdDraw.show();
        StdDraw.pause(50);
    }

    /** private static void displayTime() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss yyyy/MM/dd");
        LocalDateTime local = LocalDateTime.now();
        String time = dtf.format(local);

        Font font = new Font("Monaco", Font.PLAIN, 10);
        StdDraw.setFont(font);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.textLeft(10, HEIGHT - 1, time);
        StdDraw.show();
    } **/

    private static void displayWin() {
        StdDraw.clear(StdDraw.BLACK);
        StdDraw.setPenColor(StdDraw.WHITE);

        StdDraw.setFont(new Font("Arial", Font.BOLD, 40));
        StdDraw.text(WIDTH / 2, HEIGHT / 2, "YOU WIN!");
        StdDraw.show();
        StdDraw.pause(5000000);
    }









    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */

    public static void interactWithKeyboard() {
        ter.initialize(WIDTH, HEIGHT);
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char c = toLowerCase(StdDraw.nextKeyTyped());
                processChar(c);
            }
            generateMenu();
            if (!menu) {
                break;
            }
        }

        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char c = toLowerCase(StdDraw.nextKeyTyped());
                processChar(c);
            }
            ter.renderFrame(world);
            HUD();
            //displayTime();
            if (winGame) {
                displayWin();
            }
        }
    }

    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     *
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     *
     * In other words, both of these calls:
     *   - interactWithInputString("n123sss:q")
     *   - interactWithInputString("lww")
     *
     * should yield the exact same world state as:
     *   - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public static TETile[][] interactWithInputString(String input) {
        // TODO: Fill out this method so that it run the engine using the input
        // passed in as an argument, and return a 2D tile representation of the
        // world that would have been drawn if the same inputs had been given
        // to interactWithKeyboard().
        //
        // See proj3.byow.InputDemo for a demo of how you can make a nice clean interface
        // that works for many different input types.
        inputString = true;
        if (input.charAt(0) == 'l') {
            String saved = readContentsAsString(SAVED);
            String newInput = saved + input.substring(1);
            return interactWithInputString(newInput);
        }
        processInput(input);
        return world;
    }

    public static void main(String[] args) {
        Engine e = new Engine();
        interactWithKeyboard();


    }


}