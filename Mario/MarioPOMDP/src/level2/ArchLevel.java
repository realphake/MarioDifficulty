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
    private static final int GAPS = 5;

    private static int MAX_ENEMIES;
    private static int MAX_COINS = 20;
    private static int MAX_TURTLES = 10;

    public int[] odds = new int[6];
    private int type;
    private int gaps;

    ArrayList<GameSection> gameSections = new ArrayList<>();

    public ArchLevel(int width, int height) {
        super(width, height);
    }

    public ArchLevel(paramsPCG m) {
        this(m.width, m.height);
        this.createArchLevel(m);
    }

    public int sectionTypeAtCoordinate(int xCoord) {
        for (GameSection gs : gameSections) {
            if (xCoord < gs.xEnd && xCoord >= gs.xStart) {
                return (gs.blockType);
            }
        }
        return -1;

    }

    public float getCustomRewards(String type) {
        float reward = 0;
        if ("coin".equals(type)) {
            reward = 5;
        }
        if ("turtle".equals(type)) {
            reward = 6 * ((float) MAX_TURTLES / 10) + 1;
        }

        if ("enemy".equals(type)) {
            reward = 6 * ((float) this.ENEMIES / 10) + 1;
        }
        if ("jump".equals(type)) {
            reward = 6 * ((float) JUMP / 10) + 1;
        }

        return reward;
    }

    public void createArchLevel(paramsPCG m) {
        
        setGlobalVariablesTo(m);
        fixOddsArrayAndCalculateTotal();

        Section[] sectionBlueprints = createBlueprints();
        System.out.println(sectionArrayToString(sectionBlueprints));
        designLevelSection(sectionBlueprints);

        if (type == LevelInterface.TYPE_CASTLE
                || type == LevelInterface.TYPE_UNDERGROUND) {
            placeCeilingOverLevel();
        }

        fixWalls();
    }

    private Section[] createBlueprints() {
        int[] levelSeed = odds;
        ArrayList<Section> blueprintTemp = new ArrayList<>();
        for (int i = 0; i < levelSeed.length - 1; i++) {
            int difficult = levelSeed[i];
            int length = 16; //HARDCODED! phew.
            blueprintTemp.add(new Section(i, length, difficult));
        }
        Section[] blueprint = shuffleBlueprints(listToArray(blueprintTemp));
        return blueprint;
    }

    private Section[] shuffleBlueprints(Section[] blueprint) {
        for (int i = 0; i < blueprint.length; i++) {
            int j = random.nextInt(blueprint.length);
            Section remember = blueprint[i];
            blueprint[i] = blueprint[j];
            blueprint[j] = remember;
        }
        return blueprint;
    }

    private Section[] listToArray(ArrayList<Section> blueprintTemp) {
        Section[] blueprint = new Section[blueprintTemp.size()];
        for (int i = 0; i < blueprintTemp.size(); i++) {
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
        int floor = height - 1 - random.nextInt(4);
        lengthSoFar += buildStraight(1, width, true, 16, 0, floor);
        for (Section blueprint : blueprints) {
            int lengthRemaining = width - lengthSoFar;
            floor = adjustFloor(floor);
            lengthSoFar += buildZone(lengthSoFar, lengthRemaining,
                    blueprint.type, blueprint.length, 
                    blueprint.difficulty, floor);
        }
        floor = adjustFloor(floor);
        buildEndSection(lengthSoFar, floor);
    }
    
    private int adjustFloor( int currentFloor ) {
        if ( currentFloor <= height - 1 - 3 ) {
            return currentFloor + random.nextInt(2);
        }
        if ( currentFloor >= height - 1 ) {
            return currentFloor - random.nextInt(2);
        }
        return currentFloor + random.nextInt(3) - 1;
    }

    private void buildEndSection(int length, int floor) {
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
        odds[GAPS] = (int) m.GAP_SIZE;

        //Upper Limits
        MAX_ENEMIES = m.MAX_ENEMIES;
        MAX_COINS = m.MAX_COINS;

        random = new Random(m.seed);
    }

    private int buildZone(int x, int maxLength,
            int blockType, int length, int diffic, int floor) {

        gameSections.add(new GameSection(x, x + length, blockType));
        switch (blockType) {
            case STRAIGHT:
                return buildStraight(x, maxLength, false, length, diffic, floor);
            case HILL_STRAIGHT:
                return buildHillStraight(x, maxLength, length, diffic, floor);
            case TUBES:
                return buildTubes(x, maxLength, length, diffic, floor);
            case JUMP: {
                if (gaps < Constraints.gaps) {
                    return buildJump(x, maxLength, length, diffic, floor);
                } else {
                    odds[JUMP]++;
                    return buildStraight(x, maxLength, false, length, diffic, floor);
                }
            }
            case CANNONS:
                return buildCannons(x, maxLength, length, diffic, floor);
        }
        return 0;
    }

    private int buildJump(int xo, int maxLength, int desiredLength,
            int diffic, int floor) {
        gaps++;
        int length = desiredLength;
        if (length > maxLength) {
            length = maxLength;
        }
        
        int jumpLength = diffic+1;
        if ( diffic == 0 ) jumpLength = 0;
        if (jumpLength > length) {
            jumpLength = length;
        }
                
        int blocksAtEitherSide = ( length - jumpLength ) / 2;

        boolean hasStairs = diffic > 3;

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

    private int buildCannons(int xo, int maxLength,
            int desiredLength, int diffic, int floor) {
        if (desiredLength > maxLength) {
            desiredLength = maxLength;
        }
        
        // Decide on the space between two cannons.
        int spaceBetween;
        if (diffic != 0) spaceBetween = desiredLength / diffic;
        else spaceBetween = 3 * desiredLength; // in lieu of infinite.

        // Decide on the position of the first cannon.
        int xCannon = xo + (spaceBetween / 2);
        
        putTheseCannonsInLevel(xo, desiredLength, xCannon, spaceBetween, floor);

        return desiredLength;
    }

    private void putTheseCannonsInLevel(int xo, int desiredLength, int xCannon,
            int spaceBetween, int floor) {
        for (int x = xo; x < xo + desiredLength; x++) {
            if (x > xCannon) {
                xCannon += spaceBetween;
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
    }

    private int buildHillStraight(int xo, int maxLength,
            int desiredLength, int diffic, int floor) {
        int length = desiredLength;
        if (length > maxLength) {
            length = maxLength;
        }

        for (int x = xo; x < xo + length; x++) {
            for (int y = 0; y < height; y++) {
                if (y >= floor) {
                    setBlock(x, y, GROUND);
                }
            }
        }

        int h = floor;
        int dynamicDifficulty = diffic;

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
                  
                    int decision = random.nextInt(3);
                    if ( decision == 0) {
                        decorate(xxo - 1, xxo + l + 1, h, 1);
                        dynamicDifficulty -= 1;
                        keepGoing = false;
                    }
                    if (decision == 1) {
                        addEnemyLine(xxo, xxo + l, h - 1, 1);
                        dynamicDifficulty -= 1;
                    }
                    setTheBlocksForAHill(xxo, l, h, floor);
                }
            }
        }
        if(dynamicDifficulty < 0) dynamicDifficulty = 0;
        addEnemyLine(xo + 0, xo + length - 0, floor - 1, dynamicDifficulty);
        

        return length;
    }

    private void setTheBlocksForAHill(int xxo, int l, int h, int floor) {
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

    private void addEnemyLine(int x0, int x1, int y, int diffic) {
        int availableSpace = x1-x0;
        int spaceBetween = availableSpace * 3;
        if (diffic != 0) spaceBetween = availableSpace/diffic;
        int firstEnemy = x0 + (spaceBetween / 2);
        if ( diffic == 4 ) firstEnemy += 1;
        if ( diffic == 5 ) spaceBetween = availableSpace/4;
        
        for (int x = firstEnemy; x < x1; x += spaceBetween) {
            if (ENEMIES < MAX_ENEMIES) {
                // The following 2 lines randomize after all, which is bad...
                int enemyType = chooseEnemyType(diffic);
                boolean isFlying = random.nextInt(diffic) >= 2;
                setSpriteTemplate(x, y, new SpriteTemplate(enemyType,
                        isFlying));
                ENEMIES++;
            }
        }
    }

    private int chooseEnemyType(int diffic) {
        int decision = random.nextInt(diffic+1); // from 0-5
        if (decision <= 1) return Enemy.ENEMY_GOOMBA;
        else if (decision == 2) return Enemy.ENEMY_GREEN_KOOPA;
        else if (decision == 3) return Enemy.ENEMY_RED_KOOPA;
        else return Enemy.ENEMY_SPIKY; // On a 4 or 5.
    }

    private int buildTubes(int xo, int maxLength,
            int desiredLength, int diffic, int floor) {
        int length = desiredLength;
        if (length > maxLength) {
            length = maxLength;
        }
        
        int spaceBetweenTubes = 3 * desiredLength, emptyTube = 0;
        if ( diffic != 0 ) {
            spaceBetweenTubes = desiredLength / diffic;
            emptyTube = random.nextInt( diffic );
        } 
        
        int xTube = xo + spaceBetweenTubes/2;
        if (diffic == 4) xTube -= 1;
        
        int tubeHeight = floor - random.nextInt(2) - 2;
        int tubeNumber = 0;
        for (int x = xo; x < xo + length; x++) {
            if (x > xTube + 1) {
                xTube += spaceBetweenTubes;
                tubeHeight = floor - random.nextInt(2) - 2;
                tubeNumber++;
            }
            if (xTube >= xo + length - 2) {
                xTube += 10;
            }

            if ( x == xTube && !(tubeNumber == emptyTube) ) {
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
            boolean safe, int desiredLength, int diffic, int floor) {
        int length = desiredLength;

        if (length > maxLength) {
            length = maxLength;
        }

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
                decorate(xo, xo + length, floor, diffic);
            }
        }

        return length;
    }

    private void decorate(int xStart, int xLength, int floor, int diffic) {
        //if its at the very top, just return
        if (floor < 1) {
            return;
        }
        boolean rocks = true;

        //add an enemy line above the box
        addEnemyLine(xStart + 1, xLength - 1, floor - 1, diffic);

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
                            if (random.nextInt(2) == 0 && BLOCKS_POWER < 2) {
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
        
        clone.odds = odds.clone();
        // pass gamesections as well for reset
        clone.gameSections = gameSections;
        return clone;

    }

    private String sectionArrayToString(Section[] blueprint) {
        String representation = "-begin";
        for (Section section : blueprint) {
            representation += ", " + section.toString();
        }
        return representation + ", end";
    }

    private static class Section {

        int type;
        int length;
        int difficulty;

        public Section(int t, int l, int d) {
            type = t;
            length = l;
            difficulty = d;
        }

        @Override
        public String toString() {
            return "" + blockToString(type) + "(" + length + "," + difficulty + ")";
        }

        private String blockToString(int type) {
            switch (type) {
                case STRAIGHT:
                    return "straight";
                case HILL_STRAIGHT:
                    return "hills";
                case TUBES:
                    return "tubes";
                case JUMP:
                    return "jump";
                case CANNONS:
                    return "cannons";
                default:
                    return "unknown type";
            }
        }
    }

    private static class GameSection {

        int xStart;
        int xEnd;
        int blockType;

        public GameSection(int xs, int xe, int bt) {
            xStart = xs;
            xEnd = xe;
            blockType = bt;
        }
        
        

    }

}
