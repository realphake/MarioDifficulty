package ch.idsia.mario.engine;

import ch.idsia.mario.engine.level.BgLevelGenerator;
import ch.idsia.mario.engine.level.Level;
import ch.idsia.mario.engine.level.LevelGenerator;
import ch.idsia.mario.engine.level.SpriteTemplate;
import ch.idsia.mario.engine.sprites.*;
import ch.idsia.mario.environments.Environment;
import ch.idsia.utils.MathX;
import Architect.*;
import static ch.idsia.mario.engine.Scene.keys;
import java.awt.*;
import java.io.DataInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ch.idsia.mario.engine.level.ArchLevel;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
public class LevelScene extends Scene implements SpriteContext
{
    // new vars
        public double softMax_temperature;
        public boolean gameStarted;
    	private double thresshold; //how large the distance from point to mario should be before switching
	private int point = -1;
	private int []checkPoints;
	private boolean isCustom;
        public double[] playerModel = new double[3];
        public ArrayList<double[]> valueArrayList = new ArrayList(0);//means of the gaussians, will contain all unique vectors used
	public ArrayList<double[]> rewardList = new ArrayList(0);//contains all rewards in same order as valueArrayList, corresponding to each vector, list for each vector
	public double[] vectorModel = new double[0];//appropriateness for vectors values in same order as valueArrayList, corresponding to each vector, one for each vector
	//moved to parent class for rendering;
	//public double [][] valueList = {startVector};//means of the gaussians, will contain all playvectors created, possibly including multiple of same
	private int newVectorInterval = 1;//interval for new vectors; i.e. 5 will create 5 vectors before setting selecting best vector
	private int newVectorCount = 0; //counter for newVectorInterval
	private boolean normalDiffMethods = false;//boolean to toggle normal difficulty calculations
	
    public boolean recording = false;
	public boolean l2 = true;
	public boolean l3 = false;
	public ArchLevel level2;
	public ArchLevel level2_reset;
	public ArchLevel level3;
	public ArchLevel level3_reset;
	public boolean gameover = false;
        protected MarioComponent marioComponent;
        
            //Variables for storing received reward in playerModels (one for each difficulty level)
    public ArrayList playerModelDiff1 = new ArrayList(0);
    public ArrayList playerModelDiff4 = new ArrayList(0);
    public ArrayList playerModelDiff7 = new ArrayList(0);

    //Variables for tracking the difficulty level of each level segment
    public int currentLevelSegment;
    public ArrayList plannedDifficultyLevels = new ArrayList(0);                       
    public boolean nextSegmentAlreadyGenerated;
    ARCH_MESSAGE m;
    // end new vars
    public static DataRecorder recorder;
    public List<Sprite> sprites = new ArrayList<Sprite>();
    private List<Sprite> spritesToAdd = new ArrayList<Sprite>();
    private List<Sprite> spritesToRemove = new ArrayList<Sprite>();
    ArrayList<Double> switchPoints;
    public double [] startVector = {10,10,6,0,25,5};//starting vector for the gaussian, first entry in valueList/valueArrayList
    public double [][] valueList = {startVector};//means of the gaussians, will contain all playvectors created, possibly including multiple of same
    
   public Level level;

    public Mario mario;
    public float xCam, yCam, xCamO, yCamO;
    public static Image tmpImage;
    public int tick;
    public Level currentLevel;
    public LevelRenderer layer;
    public BgRenderer[] bgLayer = new BgRenderer[2];

    public GraphicsConfiguration graphicsConfiguration;

    public boolean paused = false;
    public int startTime = 0;
    public int timeLeft;
    public int levelWidth = 100;
    private final int timeLimit = 200;
    public int getTotalTime() {  return totalTime; }

    public void setTotalTime(int totalTime) {  this.totalTime = totalTime; }

    private int totalTime = 200;

    //    private Recorder recorder = new Recorder();
    //    private Replayer replayer = null;

    public long levelSeed;
    public MarioComponent renderer;
    public int levelType;
    public int levelDifficulty;
    private int levelLength;
    public static int killedCreaturesTotal;
    public static int killedCreaturesByFireBall;
    public static int killedCreaturesByStomp;
    public static int killedCreaturesByShell;

    private static String[] LEVEL_TYPES = {"Overground(0)",
                                           "Underground(1)",
                                           "Castle(2)"};
	//General variables
	public boolean verbose = false;

	//Variables for Random Forest classification
	public RandomForest RF = new RandomForest();
	public Instances RF_trainingInstances;
	public Instances RF_testInstances;

       

	Architect arch;
        
        
        	public LevelScene(GraphicsConfiguration graphicsConfiguration, MarioComponent renderer, long seed, int levelDifficulty, int type){
		this.isCustom = isCustom;
	
                
    //super(graphicsConfiguration,renderer,seed,levelDifficulty,type);
        this.graphicsConfiguration = graphicsConfiguration;
        this.levelSeed = seed;
        this.renderer = renderer;
        this.levelDifficulty = levelDifficulty;
        this.levelType = type;
        this.levelLength = levelLength;
        this.setTotalTime(timeLimit);
        killedCreaturesTotal = 0;
        killedCreaturesByFireBall = 0;
        killedCreaturesByStomp = 0;
        killedCreaturesByShell = 0;
    }

    private String mapElToStr(int el)
    {
        String s = "";
        if  (el == 0 || el == 1)
            s = "##";
        s += (el == mario.kind) ? "#M.#" : el;
        while (s.length() < 4)
            s += "#";
        return s + " ";
    }

    private String enemyToStr(int el)
        {
            String s = "";
            if  (el == 0)
                s = "";
            s += (el == mario.kind) ? "-m" : el;
            while (s.length() < 2)
                s += "#";
            return s + " ";
        }
    public void reset() {
		System.out.println("");
		System.out.println("----------------------------------------");
		System.out.println("------------ Resetting game ------------");
		System.out.println("----------------------------------------");                               

		//Always reset POMDP stuff
		playerModelDiff1.clear();
		playerModelDiff4.clear();
		playerModelDiff7.clear();
		if(normalDiffMethods)
		{	
		updatePlayerModel();
		displayReceivedRewards();
		}
		int temp_diffsegment1;
		int temp_diffsegment2;
		if (currentLevelSegment == 0) {
			System.out.println("-you died in the first segment, resetting to how you just started");
			temp_diffsegment1 = (int) plannedDifficultyLevels.get(0);
			temp_diffsegment2 = (int) plannedDifficultyLevels.get(1);
		}
		else {
			System.out.println("-nextSegmentAlreadyGenerated:" + nextSegmentAlreadyGenerated);
			if (nextSegmentAlreadyGenerated) {
				//because the next segment is already generated (and so the previous does not exist anymore),
				temp_diffsegment1 = (int) plannedDifficultyLevels.get(currentLevelSegment);
				temp_diffsegment2 = (int) plannedDifficultyLevels.get(currentLevelSegment+1);
			}
			else {
				//because the next segment is not yet generated
				temp_diffsegment1 = (int) plannedDifficultyLevels.get(currentLevelSegment-1);
				temp_diffsegment2 = (int) plannedDifficultyLevels.get(currentLevelSegment);
			}
		}
		plannedDifficultyLevels.clear();

		System.out.println("-resetting to: " + temp_diffsegment1 + ", " + temp_diffsegment2);
		plannedDifficultyLevels.add(temp_diffsegment1);
		plannedDifficultyLevels.add(temp_diffsegment2);
		currentLevelSegment = 0;

		paused = false;
		Sprite.spriteContext = this;
		sprites.clear();

		try {
			level2 = level2_reset.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			level3 = level3_reset.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		conjoin();

		layer = new LevelRenderer(level, graphicsConfiguration, 320, 240);
		for (int i = 0; i < 2; i++)
		{
			int scrollSpeed = 4 >> i;
		int w = ((level.getWidth() * 16) - 320) / scrollSpeed + 320;
		int h = ((level.getHeight() * 16) - 240) / scrollSpeed + 240;
		Level bgLevel = BgLevelGenerator.createLevel(w / 32 + 1, h / 32 + 1, i == 0, levelType);
		bgLayer[i] = new BgRenderer(bgLevel, graphicsConfiguration, 320, 240, scrollSpeed);
		}

		double oldX = 0;
		if(mario!=null)
			oldX = mario.x;

		mario = new Mario(this);
		sprites.add(mario);
		startTime = 1;

		timeLeft = 200*15;

		tick = 0;

		/*
		 * SETS UP ALL OF THE CHECKPOINTS TO CHECK FOR SWITCHING
		 */
		 switchPoints = new ArrayList<Double>();

		//first pick a random starting waypoint from among ten positions
		int squareSize = 16; //size of one square in pixels
		int sections = 10;

		double startX = 32; //mario start position
		double endX = level.getxExit()*squareSize; //position of the end on the level
		//if(!isCustom && recorder==null)

			recorder = new DataRecorder(this,level2,keys);
			//System.out.println("\n enemies LEFT : " + recorder.level.COINS); //Sander disable
			//System.out.println("\n enemies LEFT : " + recorder.level.BLOCKS_COINS);
			//System.out.println("\n enemies LEFT : " + recorder.level.BLOCKS_POWER);
			gameStarted = false;
	}
    
        public void resetTime()
    {
    	timeLeft = 200*15;
    }
        
        
       public double[] softmax(double[] input) {
        //Return vector with softMax probabilities
        //double softMax_temperature = 1.0; //set globablly
        double output[] = new double[input.length];
        for (int i = 0; i < input.length; i++) {
            double div = 0.0;
            for (int j = 0; j < input.length; j++) {
                div += Math.exp( input[j] / softMax_temperature );
            }
            output[i] = Math.exp( input[i] / softMax_temperature ) / div;
        }
        return output;
   }  
       public double getAbandonmentProbability() {
       //Return probability of the user abandoning the game
       return Math.random();
   }  
    
       //Calculate most desirable action at this point
   public int setAction() {
       //Select the next action using the accumulated rewards as given in the player models
       //Now: select according to softMax probabilities
       System.out.println("");
       System.out.println("setAction() called...");
       //String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
       //System.out.println("setAction() called at " + timeStamp + "...");

       //Determine the softMax temperature on the basis of the probability of user abandonment
       double abandonmentProbability = getAbandonmentProbability();
       softMax_temperature = (1 - abandonmentProbability);
       System.out.println("-probability of abandonment: " + abandonmentProbability );
       System.out.println("-calling softmax with temperature of: " + softMax_temperature);
       
       //Select action according to softMax probabilities
       double[] softmaxProbs = softmax(playerModel);
       System.out.println("-probability of action 1 (with average accumulated reward " + playerModel[0] + ") being selected is: " + softmaxProbs[0]);
       System.out.println("-probability of action 4 (with average accumulated reward " + playerModel[1] + ") being selected is: " + softmaxProbs[1]);
       System.out.println("-probability of action 7 (with average accumulated reward " + playerModel[2] + ") being selected is: " + softmaxProbs[2]);

       double p = Math.random();
       double cumulativeProbability = 0.0;
       int selectedAction = 0;
       for (int i = 0; i < softmaxProbs.length; i++) {
           cumulativeProbability += softmaxProbs[i];
           if (p <= cumulativeProbability) {
               selectedAction = i;
               break;
           }
       }
       //selectedAction = selectedAction * 3 + 1; //convert to 1,4,7 difficulty scale
       m.bestAction = selectedAction * 3 + 1; //convert to 1,4,7 difficulty scale;
       System.out.println("-selected action is: " + m.bestAction);
       return m.bestAction;
       
       // OLD - another one (most recent)
       /*
       //Select according to playerModel probabilities
       //i.e., an action with appreciation probability of 60% has a 60% chance of being executed
       System.out.println("");
       System.out.println("setNextAction() called");
       int bestAction = 0;
       double bestActionProb = 0.0;
       double lower, upper, result;
       for(int i=0; i < playerModel.length; i++){
              //System.out.println("-checking for i is: " + i);
              int offset = i*3 + 1;
              lower = 0.0;
              upper = getPlayerModelElement(offset);
              result = Math.random() * (upper - lower) + lower;
              System.out.println("-selection probability of action " + offset + " with appropriateness " + upper + " is " + result);
              if ( result > bestActionProb ) {
                  bestAction = i;
                  bestActionProb = result;
              }      
       }    
       m.bestAction = bestAction*3 + 1;
       m.bestActionProb = bestActionProb;
       System.out.println("-selected action is action " + m.bestAction + " with selection probability " + m.bestActionProb);       
       */

       //OLD - also
       /*
       //Greedy action selection:
       //use playerModel to determine bestAction with bestActionProb
       int bestAction = 0;
       double bestActionProb = 0.0;
       for(int i=0; i < playerModel.length; i++){
              //System.out.println("Checking for i is: " + i);
              int offset = i*3 + 1;
              if (  getPlayerModelElement(offset) > bestActionProb ) {
                    bestAction = i;
                    bestActionProb = getPlayerModelElement(offset);
              }      
       }    
       m.bestAction = bestAction*3 + 1;
       m.bestActionProb = bestActionProb;
       //System.out.println("Best action is action " + m.bestAction + " with appropriateness probability " + m.bestActionProb);
       */
       
       //OLD - SANDER
       /*
       // - select action with highest probability of leading to a state with appropriate difficulty (i.e., appro = 1)
       //Init
       double[] probApproAction = new double[3];
       
       //Sum probabilities of ending in Appropriate behaviour by executing Action 1, 2, 3, respectively
       //probApproAction[0] = getTransProb(1, m.state, 1, 1) + getTransProb(1, m.state, 4, 1) + getTransProb(1, m.state, 7, 1);
       //probApproAction[1] = getTransProb(4, m.state, 1, 1) + getTransProb(4, m.state, 4, 1) + getTransProb(4, m.state, 7, 1);
       //probApproAction[2] = getTransProb(7, m.state, 1, 1) + getTransProb(7, m.state, 4, 1) + getTransProb(7, m.state, 7, 1);
       
       //Verbose
       //System.out.println("-----");
       //System.out.println("probApproAction1: " + probApproAction[0]);
       //System.out.println("probApproAction4: " + probApproAction[1]);
       //System.out.println("probApproAction7: " + probApproAction[2]);
       
       //Calculate best action based on sum of probabilities
       int i = 0;
       int bestAction = 0;
       double bestActionProb = 0.0;
       while (i < probApproAction.length) {
           //System.out.println("Checking for i: " + i);
           if ( probApproAction[i] > bestActionProb ) {
               bestAction = i;
               bestActionProb = probApproAction[i];
           }                 
           i++;
       }
            
       //Convert to 0,1,2 to 1,4,7 scale
       bestAction = bestAction*3 + 1; //.DIFFICULTY*3 + 1;
       //System.out.println("Best action is action " + bestAction + " with appropriateness probability " + bestActionProb);
       m.bestAction = bestAction;
       m.bestActionProb = bestActionProb;
       */
}
   
   public void updatePlayerModel() {
       //Update playerModel[] with actual average (!) rewards, using playerModelDiff1,4,7 as input     
       //Add some test data
       //playerModelDiff4.add(0.0);
       //playerModelDiff4.add(1.0);
       //playerModelDiff4.add(1.0);
       
       //Update playerModel[0] - difficulty 1
       double average = 0.0;
       for (int i = 0; i < playerModelDiff1.size(); i++) {
           average += (double) playerModelDiff1.get(i);
       }
       if ( playerModelDiff1.size() > 0 )
           average = average / playerModelDiff1.size();
       playerModel[0] = average;

       //Update playerModel[1] - difficulty 4
       average = 0.0;
       for (int i = 0; i < playerModelDiff4.size(); i++) {
           average += (double) playerModelDiff4.get(i);
       }
       if ( playerModelDiff4.size() > 0 )
           average = average / playerModelDiff4.size();
       playerModel[1] = average;

       //Update playerModel[2] - difficulty 7
       average = 0.0;
       for (int i = 0; i < playerModelDiff7.size(); i++) {
           average += (double) playerModelDiff7.get(i);
       }
       if ( playerModelDiff7.size() > 0 )
           average = average / playerModelDiff7.size();
       playerModel[2] = average;
   }
   
    public void displayReceivedRewards() {
       //Display received rewards, as stored in playerModelDiff1,4,7
       //Display rewards stored in playerModelDiff1
       //for (int i = 0; i < playerModelDiff1.toString())
       System.out.println("");
       System.out.println("displayReceivedRewards() called...");
       System.out.println("-playerModelDiff1: " + playerModelDiff1.toString());
       System.out.println("-playerModelDiff4: " + playerModelDiff4.toString());
       System.out.println("-playerModelDiff7: " + playerModelDiff7.toString());
   }
   

   
    private byte ZLevelMapElementGeneralization(byte el, int ZLevel)
    {
        if (el == 0)
            return 0;
        switch (ZLevel)
        {
            case(0):
                switch(el)
                {
                    case 16:  // brick, simple, without any surprise.
                    case 17:  // brick with a hidden coin
                    case 18:  // brick with a hidden flower
                        return 16; // prevents cheating
                    case 21:       // question brick, contains coin
                    case 22:       // question brick, contains flower/mushroom
                        return 21; // question brick, contains something
                }
                return el;
            case(1):
                switch(el)
                {
                    case 16:  // brick, simple, without any surprise.
                    case 17:  // brick with a hidden coin
                    case 18:  // brick with a hidden flower
                        return 16; // prevents cheating
                    case 21:       // question brick, contains coin
                    case 22:       // question brick, contains flower/mushroom
                        return 21; // question brick, contains something                    
                    case(-108):
                    case(-107):
                    case(-106):
                    case(15): // Sparcle, irrelevant
                    case(34): // Coin, irrelevant for the current contest
                        return 0;
                    case(-128):
                    case(-127):
                    case(-126):
                    case(-125):
                    case(-120):
                    case(-119):
                    case(-118):
                    case(-117):
                    case(-116):
                    case(-115):
                    case(-114):
                    case(-113):
                    case(-112):
                    case(-111):
                    case(-110):
                    case(-109):
                    case(-104):
                    case(-103):
                    case(-102):
                    case(-101):                        
                    case(-100):
                    case(-99):
                    case(-98):
                    case(-97):
                    case(-69):
                    case(-65):
                    case(-88):
                    case(-87):
                    case(-86):
                    case(-85):
                    case(-84):
                    case(-83):
                    case(-82):
                    case(-81):
                    case(4):  // kicked hidden brick
                    case(9):
                        return -10;   // border, cannot pass through, can stand on
//                    case(9):
//                        return -12; // hard formation border. Pay attention!
                    case(-124):
                    case(-123):
                    case(-122):
                    case(-76):
                    case(-74):
                        return -11; // half-border, can jump through from bottom and can stand on
                    case(10): case(11): case(26): case(27): // flower pot
                    case(14): case(30): case(46): // canon
                        return 20;  // angry flower pot or cannon
                }
                System.err.println("Unknown value el = " + el + " ; Please, inform the developers");
                return el;
            case(2):
                switch(el)
                {
                    //cancel out half-borders, that could be passed through
                    case(0):
                    case(-108):
                    case(-107):
                    case(-106):
                    case(34): // coins
                    case(15): // Sparcle, irrelevant
                        return 0;
                }
                return 1;  // everything else is "something", so it is 1
        }
        System.err.println("Unkown ZLevel Z" + ZLevel);
        return el; //TODO: Throw unknown ZLevel exception
    }


    private byte ZLevelEnemyGeneralization(byte el, int ZLevel)
    {
        switch (ZLevel)
        {
            case(0):
                switch(el)
                {
                    // cancell irrelevant sprite codes
                    case(Sprite.KIND_COIN_ANIM): 
                    case(Sprite.KIND_PARTICLE):
                    case(Sprite.KIND_SPARCLE):
                    case(Sprite.KIND_MARIO):
                        return Sprite.KIND_NONE;
                }
                return el;   // all the rest should go as is
            case(1):
                switch(el)
                {
                    case(Sprite.KIND_COIN_ANIM):
                    case(Sprite.KIND_PARTICLE):
                    case(Sprite.KIND_SPARCLE):
                    case(Sprite.KIND_MARIO):
                        return Sprite.KIND_NONE;
                    case (Sprite.KIND_FIRE_FLOWER):
                        return Sprite.KIND_FIRE_FLOWER;
                    case (Sprite.KIND_MUSHROOM):
                        return Sprite.KIND_MUSHROOM;
                    case(Sprite.KIND_FIREBALL):
                        return Sprite.KIND_FIREBALL;                    
                    case(Sprite.KIND_BULLET_BILL):
                    case(Sprite.KIND_GOOMBA):
                    case(Sprite.KIND_GOOMBA_WINGED):
                    case(Sprite.KIND_GREEN_KOOPA):
                    case(Sprite.KIND_GREEN_KOOPA_WINGED):
                    case(Sprite.KIND_RED_KOOPA):
                    case(Sprite.KIND_RED_KOOPA_WINGED):
                    case(Sprite.KIND_SHELL):
                        return Sprite.KIND_GOOMBA;
                    case(Sprite.KIND_SPIKY):
                    case(Sprite.KIND_ENEMY_FLOWER):
                    case(Sprite.KIND_SPIKY_WINGED):
                        return Sprite.KIND_SPIKY;
                }
                System.err.println("Z1 UNKOWN el = " + el);
                return el;
            case(2):
                switch(el)
                {
                    case(Sprite.KIND_COIN_ANIM):
                    case(Sprite.KIND_PARTICLE):
                    case(Sprite.KIND_SPARCLE):
                    case(Sprite.KIND_FIREBALL):
                    case(Sprite.KIND_MARIO):
                    case(Sprite.KIND_FIRE_FLOWER):
                    case(Sprite.KIND_MUSHROOM):
                        return Sprite.KIND_NONE;
                    case(Sprite.KIND_BULLET_BILL):
                    case(Sprite.KIND_GOOMBA):
                    case(Sprite.KIND_GOOMBA_WINGED):
                    case(Sprite.KIND_GREEN_KOOPA):
                    case(Sprite.KIND_GREEN_KOOPA_WINGED):
                    case(Sprite.KIND_RED_KOOPA):
                    case(Sprite.KIND_RED_KOOPA_WINGED):
                    case(Sprite.KIND_SHELL):
                    case(Sprite.KIND_SPIKY):
                    case(Sprite.KIND_ENEMY_FLOWER):
                        return 1;
                }
                System.err.println("Z2 UNKNOWNN el = " + el);
                return 1;
        }
        return el; //TODO: Throw unknown ZLevel exception
    }

    public byte[][] levelSceneObservation(int ZLevel)
    {
        byte[][] ret = new byte[Environment.HalfObsWidth*2][Environment.HalfObsHeight*2];
        //TODO: Move to constants 16
        int MarioXInMap = (int)mario.x/16;
        int MarioYInMap = (int)mario.y/16;

        for (int y = MarioYInMap - Environment.HalfObsHeight, obsX = 0; y < MarioYInMap + Environment.HalfObsHeight; y++, obsX++)
        {
            for (int x = MarioXInMap - Environment.HalfObsWidth, obsY = 0; x < MarioXInMap + Environment.HalfObsWidth; x++, obsY++)
            {
                if (x >=0 /*  && x <= level.xExit */ && y >= 0 && y < level.height)
                {
                    ret[obsX][obsY] = ZLevelMapElementGeneralization(level.map[x][y], ZLevel);
                }
                else
                    ret[obsX][obsY] = 0;
//                if (x == MarioXInMap && y == MarioYInMap)
//                    ret[obsX][obsY] = mario.kind;
            }
        }
        return ret;
    }

    public byte[][] enemiesObservation(int ZLevel)
    {
        byte[][] ret = new byte[Environment.HalfObsWidth*2][Environment.HalfObsHeight*2];
        //TODO: Move to constants 16
        int MarioXInMap = (int)mario.x/16;
        int MarioYInMap = (int)mario.y/16;

        for (int w = 0; w < ret.length; w++)
            for (int h = 0; h < ret[0].length; h++)
                ret[w][h] = 0;
//        ret[Environment.HalfObsWidth][Environment.HalfObsHeight] = mario.kind;
        for (Sprite sprite : sprites)
        {
            if (sprite.kind == mario.kind)
                continue;
            if (sprite.mapX >= 0 &&
                sprite.mapX > MarioXInMap - Environment.HalfObsWidth &&
                sprite.mapX < MarioXInMap + Environment.HalfObsWidth &&
                sprite.mapY >= 0 &&
                sprite.mapY > MarioYInMap - Environment.HalfObsHeight &&
                sprite.mapY < MarioYInMap + Environment.HalfObsHeight )
            {
                int obsX = sprite.mapY - MarioYInMap + Environment.HalfObsHeight;
                int obsY = sprite.mapX - MarioXInMap + Environment.HalfObsWidth;
                ret[obsX][obsY] = ZLevelEnemyGeneralization(sprite.kind, ZLevel);
            }
        }
        return ret;
    }

    public float[] enemiesFloatPos()
    {
        List<Float> poses = new ArrayList<Float>();
        for (Sprite sprite : sprites)
        {
            // check if is an influenceable creature
            if (sprite.kind >= Sprite.KIND_GOOMBA && sprite.kind <= Sprite.KIND_MUSHROOM)
            {
                poses.add((float)sprite.kind);
                poses.add(sprite.x);
                poses.add(sprite.y);
            }
        }

        float[] ret = new float[poses.size()];

        int i = 0;
        for (Float F: poses)
            ret[i++] = F;

        return ret;
    }

    public byte[][] mergedObservation(int ZLevelScene, int ZLevelEnemies)
    {
        byte[][] ret = new byte[Environment.HalfObsWidth*2][Environment.HalfObsHeight*2];
        //TODO: Move to constants 16
        int MarioXInMap = (int)mario.x/16;
        int MarioYInMap = (int)mario.y/16;

        for (int y = MarioYInMap - Environment.HalfObsHeight, obsX = 0; y < MarioYInMap + Environment.HalfObsHeight; y++, obsX++)
        {
            for (int x = MarioXInMap - Environment.HalfObsWidth, obsY = 0; x < MarioXInMap + Environment.HalfObsWidth; x++, obsY++)
            {
                if (x >=0 /*&& x <= level.xExit*/ && y >= 0 && y < level.height)
                {
                    
                    ret[obsX][obsY] = ZLevelMapElementGeneralization(level.map[x][y], ZLevelScene);
                }
                else
                    ret[obsX][obsY] = 0;
//                if (x == MarioXInMap && y == MarioYInMap)
//                    ret[obsX][obsY] = mario.kind;
            }
        }

//        for (int w = 0; w < ret.length; w++)
//            for (int h = 0; h < ret[0].length; h++)
//                ret[w][h] = -1;
//        ret[Environment.HalfObsWidth][Environment.HalfObsHeight] = mario.kind;
        for (Sprite sprite : sprites)
        {
            if (sprite.kind == mario.kind)
                continue;
            if (sprite.mapX >= 0 &&
                sprite.mapX > MarioXInMap - Environment.HalfObsWidth &&
                sprite.mapX < MarioXInMap + Environment.HalfObsWidth &&
                sprite.mapY >= 0 &&
                sprite.mapY > MarioYInMap - Environment.HalfObsHeight &&
                sprite.mapY < MarioYInMap + Environment.HalfObsHeight )
            {
                int obsX = sprite.mapY - MarioYInMap + Environment.HalfObsHeight;
                int obsY = sprite.mapX - MarioXInMap + Environment.HalfObsWidth;
                // quick fix TODO: handle this in more general way.
                if (ret[obsX][obsY] != 14)
                {
                    byte tmp = ZLevelEnemyGeneralization(sprite.kind, ZLevelEnemies);
                    if (tmp != Sprite.KIND_NONE)
                        ret[obsX][obsY] = tmp;
                }
            }
        }

        return ret;
    }

    private String encode(byte[][] state, Generalizer generalize)
    {
        String estate = "";

        return estate;
    }


    // Encode

    public String bitmapLevelObservation(int ZLevel)
    {
        String ret = "";
        int MarioXInMap = (int)mario.x/16;
        int MarioYInMap = (int)mario.y/16;

        char block = 0;
        byte bitCounter = 0;
        int totalBits = 0;
        int totalBytes = 0;
        for (int y = MarioYInMap - Environment.HalfObsHeight, obsX = 0; y < MarioYInMap + Environment.HalfObsHeight; y++, obsX++)
        {
            for (int x = MarioXInMap - Environment.HalfObsWidth, obsY = 0; x < MarioXInMap + Environment.HalfObsWidth; x++, obsY++)
            {
                ++totalBits;
                if (bitCounter > 15)
                {
                    // update a symbol and store the current one
                    ret += block;
                    ++totalBytes;
                    block = 0;
                    bitCounter = 0;
                }
                if (x >=0 && x <= level.xExit && y >= 0 && y < level.height)
                {
                    int temp = ZLevelMapElementGeneralization(level.map[x][y], ZLevel);
                    if (temp != 0)
                        block |= MathX.powsof2[bitCounter];
                }
                ++bitCounter;
            }
//            if (block != 0)
//            {
//                System.out.println("block = " + block);
//                show(block);
//            }

        }

        if (bitCounter > 0)
            ret += block;

//        try {
//            String s = new String(code, "UTF8");
//            System.out.println("s = " + s);
//            ret = s;
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }
//        System.out.println("totalBits = " + totalBits);
//        System.out.println("totalBytes = " + totalBytes);
//        System.out.println("ret = " + ret);

        return ret;
    }

    public String bitmapEnemiesObservation(int ZLevel)
    {
        String ret = "";
        byte[][] enemiesObservation = enemiesObservation(ZLevel);
        int MarioXInMap = (int)mario.x/16;
        int MarioYInMap = (int)mario.y/16;

        char block = 0;
        char bitCounter = 0;
        int totalBits = 0;
        int totalBytes = 0;
        for (int i = 0; i < enemiesObservation.length; ++i)
        {
            for (int j = 0; j < enemiesObservation[0].length; ++j)
            {
                ++totalBits;
                if (bitCounter > 7)
                {
                    // update a symbol and store the current one
                    ret += block;
                    ++totalBytes;
                    block = 0;
                    bitCounter = 0;
                }
                int temp = enemiesObservation[i][j] ;
                if (temp != -1)
                    block |= MathX.powsof2[bitCounter];
                ++bitCounter;
            }
//            if (block != 0)
//            {
//                System.out.println("block = " + block);
//                show(block);
//            }

        }

        if (bitCounter > 0)
            ret += block;

//        System.out.println("totalBits = " + totalBits);
//        System.out.println("totalBytes = " + totalBytes);
//        System.out.println("ret = " + ret);
        return ret;
    }


    public List<String> LevelSceneAroundMarioASCII(boolean Enemies, boolean LevelMap,
                                                   boolean mergedObservationFlag,
                                                   int ZLevelScene, int ZLevelEnemies){
//        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));//        bw.write("\nTotal world width = " + level.width);
        List<String> ret = new ArrayList<String>();
        if (level != null && mario != null)
        {
            ret.add("Total world width = " + level.width);
            ret.add("Total world height = " + level.height);
            ret.add("Physical Mario Position (x,y): (" + mario.x + "," + mario.y + ")");
            ret.add("Mario Observation Width " + Environment.HalfObsWidth*2);
            ret.add("Mario Observation Height " + Environment.HalfObsHeight*2);
            ret.add("X Exit Position: " + level.xExit);
            int MarioXInMap = (int)mario.x/16;
            int MarioYInMap = (int)mario.y/16;
            ret.add("Calibrated Mario Position (x,y): (" + MarioXInMap + "," + MarioYInMap + ")\n");

            byte[][] levelScene = levelSceneObservation(ZLevelScene);
            if (LevelMap)
            {
                ret.add("~ZLevel: Z" + ZLevelScene + " map:\n");
                for (int x = 0; x < levelScene.length; ++x)
                {
                    String tmpData = "";
                    for (int y = 0; y < levelScene[0].length; ++y)
                        tmpData += mapElToStr(levelScene[x][y]);
                    ret.add(tmpData);
                }
            }

            byte[][] enemiesObservation = null;
            if (Enemies || mergedObservationFlag)
            {
                enemiesObservation = enemiesObservation(ZLevelEnemies);
            }

            if (Enemies)
            {
                ret.add("~ZLevel: Z" + ZLevelScene + " Enemies Observation:\n");
                for (int x = 0; x < enemiesObservation.length; x++)
                {
                    String tmpData = "";
                    for (int y = 0; y < enemiesObservation[0].length; y++)
                    {
//                        if (x >=0 && x <= level.xExit)
                            tmpData += enemyToStr(enemiesObservation[x][y]);
                    }
                    ret.add(tmpData);
                }
            }

            if (mergedObservationFlag)
            {
//                ret.add("~ZLevel: Z" + ZLevelScene + "===========\nAll objects: (LevelScene[x,y], Sprite[x,y])==/* Mario ~> MM */=====\n");
//                for (int x = 0; x < levelScene.length; ++x)
//                {
//                    String tmpData = "";
//                    for (int y = 0; y < levelScene[0].length; ++y)
//                        tmpData += "(" + levelScene[x][y] + "," + enemiesObservation[x][y] + ")";
//                    ret.add(tmpData);
//                }

                byte[][] mergedObs = mergedObservation(ZLevelScene, ZLevelEnemies);
                ret.add("~ZLevelScene: Z" + ZLevelScene + " ZLevelEnemies: Z" + ZLevelEnemies + " ; Merged observation /* Mario ~> #M.# */");
                for (int x = 0; x < levelScene.length; ++x)
                {
                    String tmpData = "";
                    for (int y = 0; y < levelScene[0].length; ++y)
                        tmpData += mapElToStr(mergedObs[x][y]);
                    ret.add(tmpData);
                }
            }
        }
        else
            ret.add("~level or mario is not available");
        return ret;
    }
    
    	public void conjoin()
	{
		//fixborders();
		//INSERTED THIS CODE
		//to conjoin two levels into one
		int width = level2.width + level3.width;
		int height = level2.height;
		Level level4 = new Level(width, height);
		level4.map = new byte[width][height];
		// level4.data = new byte[width][height];
		level4.xExit = width - 5;
		int k = 0;
		for (int i = 0; i < width; i++)
		{
			if(i < level2.width)
			{
				level4.map[i] = level2.map[i].clone();
				//level4.data[i] = level2.data[i];
				level4.spriteTemplates[i] = level2.spriteTemplates[i];

			}
			else
			{
				level4.map[i] = level3.map[k].clone();
				//level4.data[i] = level3.data[k];
				level4.spriteTemplates[i] = level3.spriteTemplates[k];
				k++;
			}

		}
		level = level4;
	}
        
        
	public void fixborders()
	{    	    	
		for( int i = 0 ; i<15 ; i++)
		{
			level2.map[0][i] = (byte)(0);
			level2.map[level2.width-1][i] = (byte)(0);

			//if(level2.map[level2.width-1][i] == )
			if (level2.map[level2.width-2][i] == (byte)(-127))
			{
				level2.map[level2.width-2][i] = (byte)(-126);
			}

			if (level2.map[level2.width-2][i] == (byte)(-111))
			{
				level2.map[level2.width-2][i] = (byte)(-110);
			}

			if (level2.map[1][i] == (byte)(-127))
			{
				//change to corner
				level2.map[1][i] = (byte)(-128);

			}
			if(level2.map[1][i] == (byte)(-111))
			{
				level2.map[1][i] = (byte)(-112);
			}

			level3.map[0][i] = (byte)(0);
			level3.map[level3.width-1][i] = (byte)(0);

			//if(level2.map[level2.width-1][i] == )
			if (level3.map[level3.width-2][i] == (byte)(-127))
			{
				level3.map[level3.width-2][i] = (byte)(-126);
			}

			if (level3.map[level3.width-2][i] == (byte)(-111))
			{
				level3.map[level3.width-2][i] = (byte)(-110);
			}

			if (level3.map[1][i] == (byte)(-127))
			{
				//change to corner
				level3.map[1][i] = (byte)(-128);

			}
			if(level3.map[1][i] == (byte)(-111))
			{
				level3.map[1][i] = (byte)(-112);
			}
		}
	}
    public void init()
    {
        try
        {
            Level.loadBehaviors(new DataInputStream(LevelScene.class.getResourceAsStream("resources/tiles.dat")));
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.exit(0);
        }
        /*        if (replayer!=null)
         {
         level = LevelGenerator.createLevel(2048, 15, replayer.nextLong());
         }
         else
         {*/
//        level = LevelGenerator.createLevel(320, 15, levelSeed);
        
        
        	
	if(level == null)	
            currentLevel = new RandomLevel(levelWidth, 15, levelSeed, levelDifficulty, levelType); //it's my impression this level segment is not directly used, perhaps overwritten elsewhere?
                
        level = currentLevel;
        
        
                        
        paused = false;
        Sprite.spriteContext = this;
        sprites.clear();
        
        
        Random randomGenerator = new Random();
	int randomInt = randomGenerator.nextInt(100);
                
    
        //        }

        /*        if (recorder != null)
         {
         recorder.addLong(LevelGenerator.lastSeed);
         }*/
		fixborders();
		conjoin();
      
			level2_reset = level2;
		
			level3_reset = level3;
	
              

        
       
        
        
        layer = new LevelRenderer(level, graphicsConfiguration, 320, 240);
        for (int i = 0; i < 2; i++)
        {
            int scrollSpeed = 4 >> i;
            int w = ((level.width * 16) - 320) / scrollSpeed + 320;
            int h = ((level.height * 16) - 240) / scrollSpeed + 240;
            Level bgLevel = BgLevelGenerator.createLevel(w / 32 + 1, h / 32 + 1, i == 0, levelType);
            bgLayer[i] = new BgRenderer(bgLevel, graphicsConfiguration, 320, 240, scrollSpeed);
        }
        mario = new Mario(this);
        sprites.add(mario);
        startTime = 1;

        timeLeft = totalTime*15;

        tick = 0;
        
        /*
		 * SETS UP ALL OF THE CHECKPOINTS TO CHECK FOR SWITCHING
		 */
		 switchPoints = new ArrayList<Double>();

		//first pick a random starting waypoint from among ten positions
		 int squareSize = 16; //size of one square in pixels
		 int sections = 10;

		 double startX = 32; //mario start position
		 double endX = 10*squareSize; //position of the end on the level
		 //if(!isCustom && recorder==null)
    }

    public int fireballsOnScreen = 0;

    List<Shell> shellsToCheck = new ArrayList<Shell>();

    public void checkShellCollide(Shell shell)
    {
        shellsToCheck.add(shell);
    }

    List<Fireball> fireballsToCheck = new ArrayList<Fireball>();

    public void checkFireballCollide(Fireball fireball)
    {
        fireballsToCheck.add(fireball);
    }
	public void swap()
	{
              
		int k = 0;
		//The background info should change aswell                       
           
                        
		if (mario.x > level2.width*16)
		{  
			if(!l3)
			{
				
				
				//System.out.println(Arrays.deepToString(valueList));

				//Determine next action
				//System.out.println("");
				//System.out.println("Trying to switch to difficulty level... ");

				//Switch to new state
				//System.out.println("Switching state: best action is action " + m.bestAction + " with appropriateness probability " + m.bestActionProb);                                    
				//m.DIFFICULTY = arch.message.DIFFICULTY*3 + 1;
				//m.state[0] = getDifficulty();
				//m.state[1] = randomNumber(0,3);
				//m.DIFFICULTY = m.bestAction;
				//m.state = m.DIFFICULTY;
				//levelDifficulty = 7;
			

			

				l3 = true;
				l2 = false;
			}

		}
		else
		{
			// here u are in level 2
			//System.out.println("HERE YOU ARE IN LEVEL 2");

			if(!l2)
			{

				l3 = false;
				// here u are in level 3		    		
				//System.out.println("HERE YOU ARE IN LEVEL 3");
				


				l2 = true;
			}		
		}

		if(mario.x > (level2.width*16 + 50*16/2))
		{
			//if(swap_done==0)
				//{
			for (int i = 0; i < level.width; i++)
			{
				if(i < level2.width)
				{
					level2.map[i] = level.map[i];
					// level2.data[i] = level.data[i];
					level2.spriteTemplates[i] = level.spriteTemplates[i];

				}
				else
				{
					level3.map[k] = level.map[i];
					// level3.data[k] = level.data[i];
					level3.spriteTemplates[k] = level.spriteTemplates[i];
					k++;
				}

			}

			newchunk();
			fixborders();
			k = 0;


			for (int i = 0; i <level.width; i++)
			{
				if(i < level3.width)
				{
					level.map[i] = level3.map[i];
					// level.data[i] = level3.data[i];

					level.spriteTemplates[i] = level3.spriteTemplates[i];

				}
				else
				{
					level.map[i] = level2.map[k];
					// level.data[i] = level2.data[k];
					level.spriteTemplates[i] = level2.spriteTemplates[k];
					k++;
				}

			}
			for(int i = 0 ; i < sprites.size() ; i++)
			{
				//if(sprites.get(i).x < level2.width)sprites.get(i).release();
				sprites.get(i).x = sprites.get(i).x - level2.width*16;
			}
		

		}


	}
        
        	public static double [][] addToArray(double [][] array, double [] element)
	{	//quick method to add array to array of arrays
		double [][] newarray = new double [array.length+1][array[0].length];
		for (int i = 0;i < array.length;i++)
		{	
			newarray[i] = array[i];
			
		}
			newarray[array.length] = element;
			return newarray;
			
	}
	
                
        
        public void newchunk()
	{
		if(!gameover)
		{
			

			Random randomGenerator = new Random();
			int randomInt = randomGenerator.nextInt(100);
			//level2 = new CustomizedLevel(100, 15, levelSeed + randomInt, levelDifficulty,levelType, arch.message);
			// level2 = new RandomLevel(100, 15, levelSeed+randomInt , levelDifficulty , levelType);
			System.out.println("-newchunck called");
			 //level2 = LevelGenerator.createLevel(levelWidth, 15, levelSeed+randomInt, levelDifficulty, levelType);
			//Note: Using other constructor of ArchLevel, using recorder and valueList as inputs
			
			
				level2_reset = level3_reset;
			
			
				level3_reset = level2;
			

		}
		else
		{
			level.xExit = 105;
		}

	}
        
  
    public void tick()
    {   
        
        swap();
        if (GlobalOptions.TimerOn)
                timeLeft--;
        if (timeLeft==0)
        {
            mario.die();
        }
        xCamO = xCam;
        yCamO = yCam;

        if (startTime > 0)
        {
            startTime++;
        }

        float targetXCam = mario.x - 160;

        xCam = targetXCam;

        if (xCam < 0) xCam = 0;
        if (xCam > level.width * 16 - 320) xCam = level.width * 16 - 320;

        /*      if (recorder != null)
         {
         recorder.addTick(mario.getKeyMask());
         }
         
         if (replayer!=null)
         {
         mario.setKeys(replayer.nextTick());
         }*/

        fireballsOnScreen = 0;

        for (Sprite sprite : sprites)
        {
            if (sprite != mario)
            {
                float xd = sprite.x - xCam;
                float yd = sprite.y - yCam;
                if (xd < -64 || xd > 320 + 64 || yd < -64 || yd > 240 + 64)
                {
                    removeSprite(sprite);
                }
                else
                {
                    if (sprite instanceof Fireball)
                    {
                        fireballsOnScreen++;
                    }
                }
            }
        }

        if (paused)
        {
            for (Sprite sprite : sprites)
            {
                if (sprite == mario)
                {
                    sprite.tick();
                }
                else
                {
                    sprite.tickNoMove();
                }
            }
        }
        else
        {
            tick++;
            level.tick();

            boolean hasShotCannon = false;
            int xCannon = 0;

            for (int x = (int) xCam / 16 - 1; x <= (int) (xCam + layer.width) / 16 + 1; x++)
                for (int y = (int) yCam / 16 - 1; y <= (int) (yCam + layer.height) / 16 + 1; y++)
                {
                    int dir = 0;

                    if (x * 16 + 8 > mario.x + 16) dir = -1;
                    if (x * 16 + 8 < mario.x - 16) dir = 1;

                    SpriteTemplate st = level.getSpriteTemplate(x, y);

                    if (st != null)
                    {
                        if (st.lastVisibleTick != tick - 1)
                        {
                            if (st.sprite == null || !sprites.contains(st.sprite))
                            {
                                st.spawn(this, x, y, dir);
                            }
                        }

                        st.lastVisibleTick = tick;
                    }

                    if (dir != 0)
                    {
                        byte b = level.getBlock(x, y);
                        if (((Level.TILE_BEHAVIORS[b & 0xff]) & Level.BIT_ANIMATED) > 0)
                        {
                            if ((b % 16) / 4 == 3 && b / 16 == 0)
                            {
                                if ((tick - x * 2) % 100 == 0)
                                {
                                    xCannon = x;
                                    for (int i = 0; i < 8; i++)
                                    {
                                        addSprite(new Sparkle(x * 16 + 8, y * 16 + (int) (Math.random() * 16), (float) Math.random() * dir, 0, 0, 1, 5));
                                    }
                                    addSprite(new BulletBill(this, x * 16 + 8 + dir * 8, y * 16 + 15, dir));
                                    hasShotCannon = true;
                                }
                            }
                        }
                    }
                }

            for (Sprite sprite : sprites)
            {
                sprite.tick();
            }

            for (Sprite sprite : sprites)
            {
                sprite.collideCheck();
            }

            for (Shell shell : shellsToCheck)
            {
                for (Sprite sprite : sprites)
                {
                    if (sprite != shell && !shell.dead)
                    {
                        if (sprite.shellCollideCheck(shell))
                        {
                            if (mario.carried == shell && !shell.dead)
                            {
                                mario.carried = null;
                                shell.die();
                                ++this.killedCreaturesTotal;
                            }
                        }
                    }
                }
            }
            shellsToCheck.clear();

            for (Fireball fireball : fireballsToCheck)
            {
                for (Sprite sprite : sprites)
                {
                    if (sprite != fireball && !fireball.dead)
                    {
                        if (sprite.fireballCollideCheck(fireball))
                        {
                            fireball.die();
                        }
                    }
                }
            }
            fireballsToCheck.clear();
        }

        sprites.addAll(0, spritesToAdd);
        sprites.removeAll(spritesToRemove);
        spritesToAdd.clear();
        spritesToRemove.clear();
    }

    private DecimalFormat df = new DecimalFormat("00");
    private DecimalFormat df2 = new DecimalFormat("000");

    public void render(Graphics g, float alpha)
    {
        int xCam = (int) (mario.xOld + (mario.x - mario.xOld) * alpha) - 160;
        int yCam = (int) (mario.yOld + (mario.y - mario.yOld) * alpha) - 120;

        if (GlobalOptions.MarioAlwaysInCenter)
        {
        }
        else
        {
            //int xCam = (int) (xCamO + (this.xCam - xCamO) * alpha);
            //        int yCam = (int) (yCamO + (this.yCam - yCamO) * alpha);
            if (xCam < 0) xCam = 0;
            if (yCam < 0) yCam = 0;
            if (xCam > level.width * 16 - 320) xCam = level.width * 16 - 320;
            if (yCam > level.height * 16 - 240) yCam = level.height * 16 - 240;
        }
        //      g.drawImage(Art.background, 0, 0, null);

        for (int i = 0; i < 2; i++)
        {
            bgLayer[i].setCam(xCam, yCam);
            bgLayer[i].render(g, tick, alpha);
        }

        g.translate(-xCam, -yCam);

        for (Sprite sprite : sprites)
        {
            if (sprite.layer == 0) sprite.render(g, alpha);
        }

        g.translate(xCam, yCam);

        layer.setCam(xCam, yCam);
        layer.render(g, tick, paused?0:alpha);
        //layer.renderExit0(g, tick, paused?0:alpha, mario.winTime==0);

        g.translate(-xCam, -yCam);

        // TODO: Dump out of render!
        if (mario.cheatKeys[Mario.KEY_DUMP_CURRENT_WORLD])
            for (int w = 0; w < level.width; w++)
                for (int h = 0; h < level.height; h++)
                    level.observation[w][h] = -1;

        for (Sprite sprite : sprites)
        {
            if (sprite.layer == 1) sprite.render(g, alpha);
            if (mario.cheatKeys[Mario.KEY_DUMP_CURRENT_WORLD] && sprite.mapX >= 0 && sprite.mapX < level.observation.length &&
                    sprite.mapY >= 0 && sprite.mapY < level.observation[0].length)
                level.observation[sprite.mapX][sprite.mapY] = sprite.kind;

        }

        g.translate(xCam, yCam);
        g.setColor(Color.BLACK);
        //layer.renderExit1(g, tick, paused?0:alpha);

//        drawStringDropShadow(g, "MARIO: " + df.format(Mario.lives), 0, 0, 7);
//        drawStringDropShadow(g, "#########", 0, 1, 7);


        drawStringDropShadow(g, "DIFFICULTY:   " + df.format(this.levelDifficulty), 0, 0, this.levelDifficulty > 6 ? 1 : this.levelDifficulty > 2 ? 4 : 7 ); drawStringDropShadow(g, "CREATURES:" + (mario.world.paused ? "OFF" : "ON"), 19, 0, 7);
        drawStringDropShadow(g, "SEED:" + this.levelSeed, 0, 1, 7);
        drawStringDropShadow(g, "TYPE:" + LEVEL_TYPES[this.levelType], 0, 2, 7);                  drawStringDropShadow(g, "ALL KILLS: " + killedCreaturesTotal, 19, 1, 1);
        drawStringDropShadow(g, "LENGTH:" + (int)mario.x/16 + " of " + this.levelLength, 0, 3, 7); drawStringDropShadow(g, "by Fire  : " + killedCreaturesByFireBall, 19, 2, 1);
        drawStringDropShadow(g,"COINS    : " + df.format(Mario.coins), 0, 4, 4);                      drawStringDropShadow(g, "by Shell : " + killedCreaturesByShell, 19, 3, 1);
        drawStringDropShadow(g, "MUSHROOMS: " + df.format(Mario.gainedMushrooms), 0, 5, 4);                  drawStringDropShadow(g, "by Stomp : " + killedCreaturesByStomp, 19, 4, 1);
        drawStringDropShadow(g, "FLOWERS  : " + df.format(Mario.gainedFlowers), 0, 6, 4);


        drawStringDropShadow(g, "TIME", 32, 0, 7);
        int time = (timeLeft+15-1)/15;
        if (time<0) time = 0;
        drawStringDropShadow(g, " "+df2.format(time), 32, 1, 7);

        drawProgress(g);

        if (GlobalOptions.Labels)
        {
            g.drawString("xCam: " + xCam + "yCam: " + yCam, 70, 40);
            g.drawString("x : " + mario.x + "y: " + mario.y, 70, 50);
            g.drawString("xOld : " + mario.xOld + "yOld: " + mario.yOld, 70, 60);
        }

        if (startTime > 0)
        {
            float t = startTime + alpha - 2;
            t = t * t * 0.6f;
            renderBlackout(g, 160, 120, (int) (t));
        }
//        mario.x>level.xExit*16
        if (mario.winTime > 0)
        {
            float t = mario.winTime + alpha;
            t = t * t * 0.2f;

            if (t > 900)
            {
                //renderer.levelWon();
                //              replayer = new Replayer(recorder.getBytes());
//                init();
            }

            //renderBlackout(g, mario.xDeathPos - xCam, mario.yDeathPos - yCam, (int) (320 - t));
        }

        if (mario.deathTime > 0)
        {
//            float t = mario.deathTime + alpha;
//            t = t * t * 0.4f;
//
//            if (t > 1800)
//            {
                renderer.levelFailed();
                //              replayer = new Replayer(recorder.getBytes());
//                init();
//            }

//            renderBlackout(g, (int) (mario.xDeathPos - xCam), (int) (mario.yDeathPos - yCam), (int) (320 - t));
        }
    }

    private void drawProgress(Graphics g) {
        String entirePathStr = "......................................>";
        double physLength = (levelLength - 53)*16;
        int progressInChars = (int) (mario.x * (entirePathStr.length()/physLength));
        String progress_str = "";
        for (int i = 0; i < progressInChars - 1; ++i)
            progress_str += ".";
        progress_str += "M";
        try {
        drawStringDropShadow(g, entirePathStr.substring(progress_str.length()), progress_str.length(), 28, 0);
        } catch (StringIndexOutOfBoundsException e)
        {
//            System.err.println("warning: progress line inaccuracy");
        }
        drawStringDropShadow(g, progress_str, 0, 28, 2);
    }

    public static void drawStringDropShadow(Graphics g, String text, int x, int y, int c)
    {
        drawString(g, text, x*8+5, y*8+5, 0);
        drawString(g, text, x*8+4, y*8+4, c);
    }

    private static void drawString(Graphics g, String text, int x, int y, int c)
    {
        char[] ch = text.toCharArray();
        for (int i = 0; i < ch.length; i++)
        {
            g.drawImage(Art.font[ch[i] - 32][c], x + i * 8, y, null);
        }
    }

    private void renderBlackout(Graphics g, int x, int y, int radius)
    {
        if (radius > 320) return;

        int[] xp = new int[20];
        int[] yp = new int[20];
        for (int i = 0; i < 16; i++)
        {
            xp[i] = x + (int) (Math.cos(i * Math.PI / 15) * radius);
            yp[i] = y + (int) (Math.sin(i * Math.PI / 15) * radius);
        }
        xp[16] = 320;
        yp[16] = y;
        xp[17] = 320;
        yp[17] = 240;
        xp[18] = 0;
        yp[18] = 240;
        xp[19] = 0;
        yp[19] = y;
        g.fillPolygon(xp, yp, xp.length);

        for (int i = 0; i < 16; i++)
        {
            xp[i] = x - (int) (Math.cos(i * Math.PI / 15) * radius);
            yp[i] = y - (int) (Math.sin(i * Math.PI / 15) * radius);
        }
        xp[16] = 320;
        yp[16] = y;
        xp[17] = 320;
        yp[17] = 0;
        xp[18] = 0;
        yp[18] = 0;
        xp[19] = 0;
        yp[19] = y;

        g.fillPolygon(xp, yp, xp.length);
    }


    public void addSprite(Sprite sprite)
    {
        spritesToAdd.add(sprite);
        sprite.tick();
    }

    public void removeSprite(Sprite sprite)
    {
        spritesToRemove.add(sprite);
    }

    public float getX(float alpha)
    {
        int xCam = (int) (mario.xOld + (mario.x - mario.xOld) * alpha) - 160;
        //        int yCam = (int) (mario.yOld + (mario.y - mario.yOld) * alpha) - 120;
        //int xCam = (int) (xCamO + (this.xCam - xCamO) * alpha);
        //        int yCam = (int) (yCamO + (this.yCam - yCamO) * alpha);
        if (xCam < 0) xCam = 0;
        //        if (yCam < 0) yCam = 0;
        //        if (yCam > 0) yCam = 0;
        return xCam + 160;
    }

    public float getY(float alpha)
    {
        return 0;
    }

    public void bump(int x, int y, boolean canBreakBricks)
    {
        byte block = level.getBlock(x, y);

        if ((Level.TILE_BEHAVIORS[block & 0xff] & Level.BIT_BUMPABLE) > 0)
        {
            bumpInto(x, y - 1);
            level.setBlock(x, y, (byte) 4);
            level.setBlockData(x, y, (byte) 4);

            if (((Level.TILE_BEHAVIORS[block & 0xff]) & Level.BIT_SPECIAL) > 0)
            {
                if (!Mario.large)
                {
                    addSprite(new Mushroom(this, x * 16 + 8, y * 16 + 8));
                }
                else
                {
                    addSprite(new FireFlower(this, x * 16 + 8, y * 16 + 8));
                }
            }
            else
            {
                Mario.getCoin();
                addSprite(new CoinAnim(x, y));
            }
        }

        if ((Level.TILE_BEHAVIORS[block & 0xff] & Level.BIT_BREAKABLE) > 0)
        {
            bumpInto(x, y - 1);
            if (canBreakBricks)
            {
                level.setBlock(x, y, (byte) 0);
                for (int xx = 0; xx < 2; xx++)
                    for (int yy = 0; yy < 2; yy++)
                        addSprite(new Particle(x * 16 + xx * 8 + 4, y * 16 + yy * 8 + 4, (xx * 2 - 1) * 4, (yy * 2 - 1) * 4 - 8));
            }
            else
            {
                level.setBlockData(x, y, (byte) 4);
            }
        }
    }

    public void bumpInto(int x, int y)
    {
        byte block = level.getBlock(x, y);
        if (((Level.TILE_BEHAVIORS[block & 0xff]) & Level.BIT_PICKUPABLE) > 0)
        {
            Mario.getCoin();
            level.setBlock(x, y, (byte) 0);
            addSprite(new CoinAnim(x, y + 1));
        }

        for (Sprite sprite : sprites)
        {
            sprite.bumpCheck(x, y);
        }
    }

//    public void update(boolean[] action)
//    {
//        System.arraycopy(action, 0, mario.keys, 0, 6);
//    }

    public int getStartTime() {  return startTime / 15;    }

    public int getTimeLeft() {        return timeLeft / 15;    }

}