package byow.lab12;
import org.junit.Test;
import static org.junit.Assert.*;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.Random;

/**
 * Draws a world consisting of hexagonal regions.
 */
public class HexWorld {
    private static final int WIDTH = 50;
    private static final int HEIGHT = 50;

    private static final long SEED = 2873123;
    private static final Random RANDOM = new Random(SEED);

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
    }

    public static void drawRow(TETile[][] tiles, Position p, TETile tile, int length) {
        for (int i = 0; i < length; i++) {
            tiles[p.x + i][p.y] = tile;
        }
    }

    private static void addHexagonHelper(TETile[][] tiles, Position p, TETile tile, int blank, int t) {
        Position startOfRow = p.shift(blank, 0);
        drawRow(tiles, startOfRow, tile, t);
        if (blank > 0) {
            Position nextP = p.shift(0, -1);
            addHexagonHelper(tiles, nextP, tile, blank - 1, t + 2);
        }
        Position startOfReflectedRow = startOfRow.shift(0, -(2 * blank + 1));
        drawRow(tiles, startOfReflectedRow, tile, t);

    }

    public static void addHexagon(TETile[][] tiles, Position p, TETile tile, int size) {
        if (size < 2) {
            return;
        }
        addHexagonHelper(tiles, p, tile, size - 1, size);
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



    public static void drawWorld(TETile[][] tiles, Position p, int hexSize, int tessSize) {
        addHexColumn(tiles, p, hexSize, tessSize);

        for (int i = 1; i < tessSize; i++) {
            p = getTopRightNeighbor(p, hexSize);
            addHexColumn(tiles, p, hexSize, tessSize + i);
        }

        for (int i = tessSize - 2; i >= 0; i--) {
            p = getBottomRightNeighbor(p, hexSize);
            addHexColumn(tiles, p, hexSize, tessSize + i);
        }
    }

    private static Position getTopRightNeighbor(Position p, int n) {
        return p.shift(2 * n - 1, n);
    }

    private static Position getBottomRightNeighbor(Position p, int n) {
        return p.shift(2 * n - 1, -n);
    }

    private static TETile randomTile() {
        int tileNum = RANDOM.nextInt(5);
        switch (tileNum) {
            case 0: return Tileset.SAND;
            case 1: return Tileset.FLOWER;
            case 2: return Tileset.GRASS;
            case 3: return Tileset.MOUNTAIN;
            case 4: return Tileset.TREE;
            default: return Tileset.NOTHING;
        }
    }

    public static void addHexColumn(TETile[][] tiles, Position p, int size, int num) {
        if (num < 1) {
            return;
        }
        addHexagon(tiles, p, randomTile(), size);

        if (num > 1) {
            Position bottomNeighbor = getBottomNeighborHelper(p, size);
            addHexColumn(tiles, bottomNeighbor, size, num - 1);
        }
    }

    private static Position getBottomNeighborHelper(Position p, int n) {
        return p.shift(0, -2 * n);
    }

    public static void main(String[] args) {
        TERenderer render = new TERenderer();
        render.initialize(WIDTH, HEIGHT);

        TETile[][] world = new TETile[WIDTH][HEIGHT];
        fillBoardWithNothing(world);
        Position pos = new Position(12, 34);
        drawWorld(world, pos, 3, 4);

        render.renderFrame(world);
    }
}