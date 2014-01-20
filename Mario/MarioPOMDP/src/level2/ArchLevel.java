package level2;

import java.util.Random;

import dk.itu.mario.MarioInterface.Constraints;
import dk.itu.mario.MarioInterface.LevelInterface;

import dk.itu.mario.engine.sprites.SpriteTemplate;
import dk.itu.mario.engine.sprites.Enemy;

import Architect.*;

import java.util.ArrayList;

public class ArchLevel extends Level {

    //Store information about the level
    public int DIFFICULTY_sander;
    public int ENEMIES = 0; //the number of enemies the level contains
    public int BLOCKS_EMPTY = 0; // the number of empty blocks
    public int BLOCKS_COINS = 0; // the number of coin blocks
    public int BLOCKS_POWER = 0; // the number of power blocks
    public int COINS = 0; //These are the coins in boxes that Mario collect

    public double[] playValues;

    Random random;

    private static final int STRAIGHT = 0;
    private static final int HILL_STRAIGHT = 1;
    private static final int TUBES = 2;
    private static final int JUMP = 3;
    private static final int CANNONS = 4;

    private static int MAX_ENEMIES;
    private static int MAX_COINS = 20;
    private static int MAX_TURTLES = 10;
    private static int GAP_SIZE = 4;

    public int[] odds = new int[6];

    private int totalOdds;

    private int difficulty;
    private int type;
    private int gaps;

    public ArchLevel(int width, int height) {
        super(width, height);
    }

    public ArchLevel(paramsPCG m) {
        this(m.width, m.height);
        this.createArchLevel(m);
    }

    public float getCustomRewards(String type) {
        float reward = 0;
        if ("coin".equals(type)) {
            reward = 5;
        }
        if ("turtle".equals(type)) {
            reward = 6 * ((float) this.MAX_TURTLES / 10) + 1;
        }

        if ("enemy".equals(type)) {
            reward = 6 * ((float) this.ENEMIES / 10) + 1;
        }
        if ("jump".equals(type)) {
            reward = 6 * ((float) this.JUMP / 10) + 1;
        }

        // System.out.println("type is" + type + " and reward is " + reward);
        return reward;
    }

    public void createArchLevel(paramsPCG m) {

        setGlobalVariablesTo(m);
        fixOddsArrayAndCalculateTotal();

        Section[] sectionBlueprints = createBlueprints();
        designLevelSection(sectionBlueprints);

        if (type == LevelInterface.TYPE_CASTLE
                || type == LevelInterface.TYPE_UNDERGROUND) {
            placeCeilingOverLevel();
        }

        fixWalls();
    }
    
    private Section[] createBlueprints() {
        int[] levelSeed = odds;
        int availableWidth = width-10;
        int scale = availableWidth / totalOdds;        
        ArrayList<Section> blueprintTemp = new ArrayList<>();
        for ( int i = 0; i < levelSeed.length; i++ ) {
            if ( levelSeed[i] != 0 ) {
                blueprintTemp.add(new Section( i, levelSeed[i]*scale ));
            }
        }
        Section[] blueprint = shuffleBlueprints( listToArray(blueprintTemp) );
        return blueprint;
    }
    
    private Section[] shuffleBlueprints(Section[] blueprint) {
        for ( int i = 0; i < blueprint.length; i++ ) {
            int j = random.nextInt(blueprint.length);
            Section remember = blueprint[i];
            blueprint[i] = blueprint[j];
            blueprint[j] = remember;
        }
        return blueprint;
    }

    private Section[] listToArray(ArrayList<Section> blueprintTemp) {
        Section[] blueprint = new Section[blueprintTemp.size()];
        for ( int i = 0; i < blueprintTemp.size(); i++ ) {
            blueprint[i] = blueprintTemp.get(i);
        }
        return blueprint;
    }

    private void placeCeilingOverLevel() {
        int ceiling = 0;
        int run = 0;
        for (int x = 0; x < width; x++) {
            if (run-- <= 0 && x > 4) {
                ceiling = random.nextInt(4);
                run = random.nextInt(4) + 4;
            }
            for (int y = 0; y < height; y++) {
                if ((x > 4 && y <= ceiling) || x < 1) {
                    setBlock(x, y, GROUND);
                }
            }
        }
    }

    private void designLevelSection(Section[] blueprints) {
        int lengthSoFar = 1;
        lengthSoFar += buildStraight(1, width, true, 5); // Beginning section
        for ( int i = 0; i < blueprints.length; i++ ) {
            int lengthRemaining = width-lengthSoFar;
            lengthSoFar += buildZone( lengthSoFar, lengthRemaining, 
                    blueprints[i].type, blueprints[i].length);
        }
        buildEndSection(lengthSoFar);
    }

    private void buildEndSection(int length) {
        //set the end piece
        int floor = height - 1 - random.nextInt(4);

        xExit = length + 8;
        yExit = floor;

        // fills the end piece
        for (int x = length; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (y >= floor) {
                    setBlock(x, y, GROUND);
                }
            }
        }
    }

    private void fixOddsArrayAndCalculateTotal() {
        for (int i = 0; i < odds.length; i++) {
            //failsafe (no negative odds)
            if (odds[i] < 0) {
                odds[i] = 0;
            }
            totalOdds += odds[i];
        }

        if (type != LevelInterface.TYPE_OVERGROUND) {
            odds[HILL_STRAIGHT] = 0;
        }
    }

    private void setGlobalVariablesTo(paramsPCG m) {
        odds[STRAIGHT] = (int) m.ODDS_STRAIGHT;
        odds[HILL_STRAIGHT] = (int) m.ODDS_HILL_STRAIGHT;
        odds[TUBES] = (int) m.ODDS_TUBES;
        odds[JUMP] = (int) m.ODDS_JUMP;
        odds[CANNONS] = (int) m.ODDS_CANNONS;
        difficulty = m.difficulty;

        //Upper Limits
        MAX_ENEMIES = m.MAX_ENEMIES;
        MAX_COINS = m.MAX_COINS;
        GAP_SIZE = m.ODDS_JUMP;

        random = new Random(m.seed);
    }

    private int buildZone(int x, int maxLength, 
            int blockType, int length) {

        switch (blockType) {
            case STRAIGHT:
                return buildStraight(x, maxLength, false, length); // Length = 1d10+2
            case HILL_STRAIGHT:
                return buildHillStraight(x, maxLength, length); // length 1d10+10
            case TUBES:
                return buildTubes(x, maxLength, length); // Length = 5
            case JUMP: {
                if (gaps < Constraints.gaps) {
                    return buildJump(x, maxLength, length); // Length = 1d4+6
                } else {
                    odds[JUMP]++;
                    totalOdds++;
                    return buildStraight(x, maxLength, false, length);
                }
            }
            case CANNONS:
                return buildCannons(x, maxLength, length); // Length = 5
        }
        return 0;
    }

    private int buildJump(int xo, int maxLength, int desiredLength) {
        gaps++;
        int length = desiredLength;
        if (length > maxLength) {
            length = maxLength;
        }
        int jumpLength = random.nextInt(GAP_SIZE) + 2;
        if (jumpLength > length) {
            jumpLength = length;
        }
        int blocksAtEitherSide = length - jumpLength;

        boolean hasStairs = random.nextInt(3) == 0;

        int floor = height - 1 - random.nextInt(4);
        //run from the start x position, for the whole length
        for (int x = xo; x < xo + length; x++) {
            if (x < xo + blocksAtEitherSide
                    || x > xo + length - blocksAtEitherSide - 1) {
                //run for all y's since we need to paint blocks upward
                //paint ground up until the floor
                for (int y = 0; y < height; y++) {
                    if (y >= floor) {
                        setBlock(x, y, GROUND);
                    } //if it is above ground, start making stairs of rocks
                    else if (hasStairs) {	//LEFT SIDE
                        if (x < xo + blocksAtEitherSide) {
                            //we need to max it out and level because it wont
                            //paint ground correctly unless two bricks are
                            //side by side
                            if (y >= floor - (x - xo) + 1) {
                                setBlock(x, y, ROCK);
                            }
                        } else { //RIGHT SIDE
                            if (y >= floor - ((xo + length) - x) + 2) {
                                setBlock(x, y, ROCK);
                            }
                        }
                    }
                }
            }
        }

        return length;
    }

    private int buildCannons(int xo, int maxLength, int desiredLength) {
        if (desiredLength > maxLength) {
            desiredLength = maxLength;
        }

        int floor = height - 1 - random.nextInt(4);
        int xCannon = xo + 1 + random.nextInt(4);
        for (int x = xo; x < xo + desiredLength; x++) {
            if (x > xCannon) {
                xCannon += 2 + random.nextInt(4);
            }
            if (xCannon == xo + desiredLength - 1) {
                xCannon += 10;
            }
            int cannonHeight = floor - random.nextInt(4) - 1;

            for (int y = 0; y < height; y++) {
                if (y >= floor) {
                    setBlock(x, y, GROUND);
                } else {
                    if (x == xCannon && y >= cannonHeight) {
                        if (y == cannonHeight) {
                            setBlock(x, y, (byte) (14 + 0 * 16));
                        } else if (y == cannonHeight + 1) {
                            setBlock(x, y, (byte) (14 + 1 * 16));
                        } else {
                            setBlock(x, y, (byte) (14 + 2 * 16));
                        }
                    }
                }
            }
        }

        return desiredLength;
    }

    private int buildHillStraight(int xo, int maxLength, int desiredLength) {
        int length = desiredLength;
        if (length > maxLength) {
            length = maxLength;
        }

        int floor = height - 1 - random.nextInt(4);
        for (int x = xo; x < xo + length; x++) {
            for (int y = 0; y < height; y++) {
                if (y >= floor) {
                    setBlock(x, y, GROUND);
                }
            }
        }

        addEnemyLine(xo + 0, xo + length - 0, floor - 1);

        int h = floor;

        boolean keepGoing = true;

        boolean[] occupied = new boolean[length];
        while (keepGoing) {
            h = h - 2 - random.nextInt(3);

            if (h <= 0) {
                keepGoing = false;
            } else {
                int l = random.nextInt(5) + 3;
                int xxo = random.nextInt(length - l - 2) + xo + 1;

                if (occupied[xxo - xo] || occupied[xxo - xo + l]
                        || occupied[xxo - xo - 1] || occupied[xxo - xo + l + 1]) {
                    keepGoing = false;
                } else {
                    occupied[xxo - xo] = true;
                    occupied[xxo - xo + l] = true;
                    addEnemyLine(xxo, xxo + l, h - 1);
                    if (random.nextInt(4) == 0) {
                        decorate(xxo - 1, xxo + l + 1, h);
                        keepGoing = false;
                    }
                    for (int x = xxo; x < xxo + l; x++) {
                        for (int y = h; y < floor; y++) {
                            int xx = 5;
                            if (x == xxo) {
                                xx = 4;
                            }
                            if (x == xxo + l - 1) {
                                xx = 6;
                            }
                            int yy = 9;
                            if (y == h) {
                                yy = 8;
                            }

                            if (getBlock(x, y) == 0) {
                                setBlock(x, y, (byte) (xx + yy * 16));
                            } else {
                                if (getBlock(x, y) == HILL_TOP_LEFT) {
                                    setBlock(x, y, HILL_TOP_LEFT_IN);
                                }
                                if (getBlock(x, y) == HILL_TOP_RIGHT) {
                                    setBlock(x, y, HILL_TOP_RIGHT_IN);
                                }
                            }
                        }
                    }
                }
            }
        }

        return length;
    }

    private void addEnemyLine(int x0, int x1, int y) {
        for (int x = x0; x < x1; x += 5) {
            if ((random.nextInt(5) < difficulty) && ENEMIES < MAX_ENEMIES) {
                //difficulty -=1;

                int enemyType = random.nextInt(4);

                if (difficulty < 2) {
                    enemyType = Enemy.ENEMY_GOOMBA;
                } else if (difficulty < 3) {
                    enemyType = random.nextInt(3);
                }

                setSpriteTemplate(x, y, new SpriteTemplate(enemyType,
                        random.nextInt(10) < difficulty));
                ENEMIES++;
            }
        }
    }

    private int buildTubes(int xo, int maxLength, int desiredLength) {
        int length = desiredLength;
        if (length > maxLength) {
            length = maxLength;
        }

        int floor = height - 1 - random.nextInt(4);
        int xTube = xo + 1 + random.nextInt(4);
        int tubeHeight = floor - random.nextInt(2) - 2;
        for (int x = xo; x < xo + length; x++) {
            if (x > xTube + 1) {
                xTube += 3 + random.nextInt(4);
                tubeHeight = floor - random.nextInt(2) - 2;
            }
            if (xTube >= xo + length - 2) {
                xTube += 10;
            }

            if (x == xTube && random.nextInt(11) < difficulty + 1) {
                setSpriteTemplate(x, tubeHeight,
                        new SpriteTemplate(Enemy.ENEMY_FLOWER, false));
                ENEMIES++;
            }

            for (int y = 0; y < height; y++) {
                if (y >= floor) {
                    setBlock(x, y, GROUND);

                } else {
                    if ((x == xTube || x == xTube + 1) && y >= tubeHeight) {
                        int xPic = 10 + x - xTube;

                        if (y == tubeHeight) {
                            //tube top
                            setBlock(x, y, (byte) (xPic + 0 * 16));
                        } else {
                            //tube side
                            setBlock(x, y, (byte) (xPic + 1 * 16));
                        }
                    }
                }
            }
        }

        return length;
    }

    private int buildStraight(int xo, int maxLength,
            boolean safe, int desiredLength) {
        int length = desiredLength;

        if (length > maxLength) {
            length = maxLength;
        }

        int floor = height - 1 - random.nextInt(4);

        //runs from the specified x position to the length of the segment
        for (int x = xo; x < xo + length; x++) {
            for (int y = 0; y < height; y++) {
                if (y >= floor) {
                    setBlock(x, y, GROUND);
                }
            }
        }

        if (!safe) {
            if (length > 5) {
                decorate(xo, xo + length, floor);
            }
        }

        return length;
    }

    private void decorate(int xStart, int xLength, int floor) {
        //if its at the very top, just return
        if (floor < 1) {
            return;
        }
        boolean rocks = true;

        //add an enemy line above the box
        addEnemyLine(xStart + 1, xLength - 1, floor - 1);

        int s = random.nextInt(4);
        int e = random.nextInt(4);

        if (floor - 2 > 0) {
            if ((xLength - 1 - e) - (xStart + 1 + s) > 1) {
                for (int x = xStart + 1 + s; x < xLength - 1 - e; x++) {
                    setBlock(x, floor - 2, COIN);
                    COINS++;
                }
            }
        }

        s = random.nextInt(4);
        e = random.nextInt(4);

        if (floor - 4 > 0) {
            if ((xLength - 1 - e) - (xStart + 1 + s) > 2) {
                for (int x = xStart + 1 + s; x < xLength - 1 - e; x++) {
                    if (rocks) {
                        if (x != xStart + 1 && x != xLength - 2
                                && random.nextInt(2) == 0) {
                            if (random.nextInt(2) == 0) {
                                setBlock(x, floor - 4, BLOCK_POWERUP);
                                BLOCKS_POWER++;
                            } else {
                                if (COINS < MAX_COINS) {
                                    COINS++;
                                    setBlock(x, floor - 4, BLOCK_COIN);
                                    BLOCKS_COINS++;

                                } else {
                                    setBlock(x, floor - 4, BLOCK_EMPTY);
                                    BLOCKS_EMPTY++;
                                }
                            }
                        } else if (random.nextInt(4) == 0) {
                            if (random.nextInt(4) == 0) {
                                setBlock(x, floor - 4, (byte) (2 + 1 * 16));
                            } else {
                                setBlock(x, floor - 4, (byte) (1 + 1 * 16));
                            }
                        } else {
                            setBlock(x, floor - 4, BLOCK_EMPTY);
                            BLOCKS_EMPTY++;
                        }
                    }
                }
            }
        }
    }

    private void fixWalls() {
        boolean[][] blockMap = new boolean[width + 1][height + 1];

        for (int x = 0; x < width + 1; x++) {
            for (int y = 0; y < height + 1; y++) {
                int blocks = 0;
                for (int xx = x - 1; xx < x + 1; xx++) {
                    for (int yy = y - 1; yy < y + 1; yy++) {
                        if (getBlockCapped(xx, yy) == GROUND) {
                            blocks++;
                        }
                    }
                }
                blockMap[x][y] = blocks == 4;
            }
        }
        blockify(this, blockMap, width + 1, height + 1);
    }

    private void blockify(Level level,
            boolean[][] blocks, int width, int height) {
        int to = 0;
        if (type == LevelInterface.TYPE_CASTLE) {
            to = 4 * 2;
        } else if (type == LevelInterface.TYPE_UNDERGROUND) {
            to = 4 * 3;
        }

        boolean[][] b = new boolean[2][2];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int xx = x; xx <= x + 1; xx++) {
                    for (int yy = y; yy <= y + 1; yy++) {
                        int _xx = xx;
                        int _yy = yy;
                        if (_xx < 0) {
                            _xx = 0;
                        }
                        if (_yy < 0) {
                            _yy = 0;
                        }
                        if (_xx > width - 1) {
                            _xx = width - 1;
                        }
                        if (_yy > height - 1) {
                            _yy = height - 1;
                        }
                        b[xx - x][yy - y] = blocks[_xx][_yy];
                    }
                }

                if (b[0][0] == b[1][0] && b[0][1] == b[1][1]) {
                    if (b[0][0] == b[0][1]) {
                        if (b[0][0]) {
                            level.setBlock(x, y, (byte) (1 + 9 * 16 + to));
                        } else {
                            // KEEP OLD BLOCK!
                        }
                    } else {
                        if (b[0][0]) {
                            //down grass top?
                            level.setBlock(x, y, (byte) (1 + 10 * 16 + to));
                        } else {
                            //up grass top
                            level.setBlock(x, y, (byte) (1 + 8 * 16 + to));
                        }
                    }
                } else if (b[0][0] == b[0][1] && b[1][0] == b[1][1]) {
                    if (b[0][0]) {
                        //right grass top
                        level.setBlock(x, y, (byte) (2 + 9 * 16 + to));
                    } else {
                        //left grass top
                        level.setBlock(x, y, (byte) (0 + 9 * 16 + to));
                    }
                } else if (b[0][0] == b[1][1] && b[0][1] == b[1][0]) {
                    level.setBlock(x, y, (byte) (1 + 9 * 16 + to));
                } else if (b[0][0] == b[1][0]) {
                    if (b[0][0]) {
                        if (b[0][1]) {
                            level.setBlock(x, y, (byte) (3 + 10 * 16 + to));
                        } else {
                            level.setBlock(x, y, (byte) (3 + 11 * 16 + to));
                        }
                    } else {
                        if (b[0][1]) {
                            //right up grass top
                            level.setBlock(x, y, (byte) (2 + 8 * 16 + to));
                        } else {
                            //left up grass top
                            level.setBlock(x, y, (byte) (0 + 8 * 16 + to));
                        }
                    }
                } else if (b[0][1] == b[1][1]) {
                    if (b[0][1]) {
                        if (b[0][0]) {
                            //left pocket grass
                            level.setBlock(x, y, (byte) (3 + 9 * 16 + to));
                        } else {
                            //right pocket grass
                            level.setBlock(x, y, (byte) (3 + 8 * 16 + to));
                        }
                    } else {
                        if (b[0][0]) {
                            level.setBlock(x, y, (byte) (2 + 10 * 16 + to));
                        } else {
                            level.setBlock(x, y, (byte) (0 + 10 * 16 + to));
                        }
                    }
                } else {
                    level.setBlock(x, y, (byte) (0 + 1 * 16 + to));
                }
            }
        }
    }

    @Override
    public ArchLevel clone() throws CloneNotSupportedException {

        ArchLevel clone = new ArchLevel(width, height);

        clone.xExit = xExit;
        clone.yExit = yExit;
        byte[][] thisMap = getMap();
        SpriteTemplate[][] st = getSpriteTemplate();

        for (int i = 0; i < thisMap.length; i++) {
            for (int j = 0; j < thisMap[i].length; j++) {
                clone.setBlock(i, j, thisMap[i][j]);
                clone.setSpriteTemplate(i, j, st[i][j]);
            }
        }
        clone.BLOCKS_COINS = BLOCKS_COINS;
        clone.BLOCKS_EMPTY = BLOCKS_EMPTY;
        clone.BLOCKS_POWER = BLOCKS_POWER;
        clone.ENEMIES = ENEMIES;
        clone.COINS = COINS;

        return clone;

    }

    private static class Section {

        int type;
        int length;
        public Section(int t, int l) {
            type = t; length = l;
        }
    }


}