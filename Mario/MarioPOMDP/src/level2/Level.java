package level2;

import java.io.*;

import dk.itu.mario.MarioInterface.LevelInterface;
import dk.itu.mario.engine.sprites.SpriteTemplate;

public class Level implements LevelInterface {

    protected static final byte BLOCK_EMPTY = (byte) (0 + 1 * 16);
    protected static final byte BLOCK_POWERUP = (byte) (4 + 2 + 1 * 16);
    protected static final byte BLOCK_COIN = (byte) (4 + 1 + 1 * 16);
    protected static final byte GROUND = (byte) (1 + 9 * 16);
    protected static final byte ROCK = (byte) (9 + 0 * 16);
    protected static final byte COIN = (byte) (2 + 2 * 16);

    protected static final byte LEFT_GRASS_EDGE = (byte) (0 + 9 * 16);
    protected static final byte RIGHT_GRASS_EDGE = (byte) (2 + 9 * 16);
    protected static final byte RIGHT_UP_GRASS_EDGE = (byte) (2 + 8 * 16);
    protected static final byte LEFT_UP_GRASS_EDGE = (byte) (0 + 8 * 16);
    protected static final byte LEFT_POCKET_GRASS = (byte) (3 + 9 * 16);
    protected static final byte RIGHT_POCKET_GRASS = (byte) (3 + 8 * 16);

    protected static final byte HILL_FILL = (byte) (5 + 9 * 16);
    protected static final byte HILL_LEFT = (byte) (4 + 9 * 16);
    protected static final byte HILL_RIGHT = (byte) (6 + 9 * 16);
    protected static final byte HILL_TOP = (byte) (5 + 8 * 16);
    protected static final byte HILL_TOP_LEFT = (byte) (4 + 8 * 16);
    protected static final byte HILL_TOP_RIGHT = (byte) (6 + 8 * 16);

    protected static final byte HILL_TOP_LEFT_IN = (byte) (4 + 11 * 16);
    protected static final byte HILL_TOP_RIGHT_IN = (byte) (6 + 11 * 16);

    protected static final byte TUBE_TOP_LEFT = (byte) (10 + 0 * 16);
    protected static final byte TUBE_TOP_RIGHT = (byte) (11 + 0 * 16);

    protected static final byte TUBE_SIDE_LEFT = (byte) (10 + 1 * 16);
    protected static final byte TUBE_SIDE_RIGHT = (byte) (11 + 1 * 16);

    //The level's width and height
    public int width;
    public int height;

    //This map of WIDTH * HEIGHT that contains the level's design
    public byte[][] map;

    //This is a map of WIDTH * HEIGHT that contains the placement and type enemies
    public SpriteTemplate[][] spriteTemplates;

    //These are the place of the end of the level
    public int xExit;
    protected int yExit;

    public Level() {

    }

    public Level(int width, int height) {
        this.width = width;
        this.height = height;

        xExit = 10;
        yExit = 10;
        map = new byte[width][height];
        spriteTemplates = new SpriteTemplate[width][height];
    }

    public static void loadBehaviors(DataInputStream dis) throws IOException {
        dis.readFully(Level.TILE_BEHAVIORS);
    }

    public static void saveBehaviors(DataOutputStream dos) throws IOException {
        dos.write(Level.TILE_BEHAVIORS);
    }

    /**
     * Clone the level data so that we can load it when Mario dies
     *
     * @return clone of this level
     * @throws java.lang.CloneNotSupportedException
     */
    @Override
    public Level clone() throws CloneNotSupportedException {

        Level clone = new Level(width, height);

        clone.map = new byte[width][height];
        clone.spriteTemplates = new SpriteTemplate[width][height];
        clone.xExit = xExit;
        clone.yExit = yExit;

        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                clone.map[i][j] = map[i][j];
                clone.spriteTemplates[i][j] = spriteTemplates[i][j];
            }
        }

        return clone;

    }

    @Override
    public void tick() {
    }

    public byte getBlockCapped(int x, int y) {
        if (x < 0) {
            x = 0;
        }
        if (y < 0) {
            y = 0;
        }
        if (x >= width) {
            x = width - 1;
        }
        if (y >= height) {
            y = height - 1;
        }
        return map[x][y];
    }

    public byte getBlock(int x, int y) {
        if (x < 0) {
            x = 0;
        }
        if (y < 0) {
            return 0;
        }
        if (x >= width) {
            x = width - 1;
        }
        if (y >= height) {
            y = height - 1;
        }
        return map[x][y];
    }

    public void setBlock(int x, int y, byte b) {
        if (x < 0) {
            return;
        }
        if (y < 0) {
            return;
        }
        if (x >= width) {
            return;
        }
        if (y >= height) {
            return;
        }
        map[x][y] = b;
    }

    public boolean isBlocking(int x, int y, float xa, float ya) {
        byte block = getBlock(x, y);

        boolean blocking = ((TILE_BEHAVIORS[block & 0xff]) & BIT_BLOCK_ALL) > 0;
        blocking |= (ya > 0) && ((TILE_BEHAVIORS[block & 0xff]) & BIT_BLOCK_UPPER) > 0;
        blocking |= (ya < 0) && ((TILE_BEHAVIORS[block & 0xff]) & BIT_BLOCK_LOWER) > 0;

        return blocking;
    }

    public SpriteTemplate getSpriteTemplate(int x, int y) {
        if (x < 0) {
            return null;
        }
        if (y < 0) {
            return null;
        }
        if (x >= width) {
            return null;
        }
        if (y >= height) {
            return null;
        }
        return spriteTemplates[x][y];
    }

    public void setSpriteTemplate(int x, int y, SpriteTemplate spriteTemplate) {
        if (x < 0) {
            return;
        }
        if (y < 0) {
            return;
        }
        if (x >= width) {
            return;
        }
        if (y >= height) {
            return;
        }
        spriteTemplates[x][y] = spriteTemplate;
    }

    public SpriteTemplate[][] getSpriteTemplate() {
        return this.spriteTemplates;
    }

    public void resetSpriteTemplate() {
        for (SpriteTemplate[] spriteTemplate : spriteTemplates) {
            for (SpriteTemplate st : spriteTemplate) {
                if (st != null) {
                    st.isDead = false;
                }
            }
        }
    }

    public void print(byte[][] array) {
        for (byte[] array1 : array) {
            for (int j = 0; j < array1.length; j++) {
                System.out.print(array1[j]);
            }
            System.out.println();
        }
    }

    @Override
    public byte[][] getMap() {
        return map;
    }

    @Override
    public SpriteTemplate[][] getSpriteTemplates() {
        return spriteTemplates;
    }

    @Override
    public int getxExit() {
        return xExit;
    }

    @Override
    public int getyExit() {
        return yExit;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    /**
     *
     * @return this level's name
     */
    @Override
    public String getName() {
        return "";
    }

}
