package dk.itu.mario.scene;

import dk.itu.mario.engine.DifficultyRecorder;
import java.awt.GraphicsConfiguration;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Random;
import java.io.BufferedReader;
import java.io.FileReader;
import weka.core.Instances;
import Architect.*;
import Onlinedata.MainSendRequest;

import weka.core.Instance;
import weka.core.Instances;
import weka.classifiers.trees.RandomForest;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.classifiers.Evaluation;

import weka.estimators.KernelEstimator;

import level2.ArchLevel;
import level2.BgLevelGenerator;
import dk.itu.mario.MarioInterface.GamePlay;
import dk.itu.mario.engine.sonar.FixedSoundSource;
import dk.itu.mario.engine.sprites.CoinAnim;
import dk.itu.mario.engine.sprites.FireFlower;
import dk.itu.mario.engine.sprites.Mario;
import dk.itu.mario.engine.sprites.Mushroom;
import dk.itu.mario.engine.sprites.Particle;
import dk.itu.mario.engine.sprites.Sprite;
import dk.itu.mario.engine.util.FileHandler;

import dk.itu.mario.engine.Art;
import dk.itu.mario.engine.BgRenderer;
import dk.itu.mario.engine.DataRecorder;
import dk.itu.mario.engine.LevelRenderer;
import dk.itu.mario.engine.MarioComponent;
import level2.*;
import level2.generator.CustomizedLevelGenerator;
import dk.itu.mario.engine.Play;
import dk.itu.mario.res.ResourcesManager;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.logging.Logger;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.converters.ArffSaver;
import weka.filters.unsupervised.attribute.NumericToNominal;

public class LevelSceneTest extends LevelScene {

    
    
    private boolean isTraining = true;
    ArrayList<Double> switchPoints;
    private ArrayList<SectionOfGame> sections = new ArrayList<SectionOfGame>();

    public ArrayList<SectionOfGame> getSections() {
        return sections;
    }

    public void setSections(ArrayList<SectionOfGame> sections) {
        this.sections = sections;
    }
    private int previousSection = -1;
       
    private boolean readGaze = false;
    
    public ArrayList<double[]> valueArrayList = new ArrayList(0);//means of the gaussians, will contain all unique vectors used
    public ArrayList<double[]> rewardList = new ArrayList(0);//contains all rewards in same order as valueArrayList, corresponding to each vector, list for each vector
    public double[] vectorModel = new double[0];//appropriateness for vectors values in same order as valueArrayList, corresponding to each vector, one for each vector
    //moved to parent class for rendering;
    //public double [][] valueList = {startVector};//means of the gaussians, will contain all playvectors created, possibly including multiple of same
    private int newVectorInterval = 1;//interval for new vectors; i.e. 5 will create 5 vectors before setting selecting best vector
    private int newVectorCount = 0; //counter for newVectorInterval
    private boolean normalDiffMethods = false;//boolean to toggle normal difficulty calculations

    private boolean firstRun;
    private int[] newDifficulties;
    public int tries = 3; //tries for a chunk before asking for feedback;
    public boolean recording = false;
    public boolean l2 = true;
    public boolean l3 = false;
    public ArchLevel level2;
    public ArchLevel level2_reset;
    public ArchLevel level3;
    public ArchLevel level3_reset;
    public boolean gameover = false;

    //General variables
    public boolean verbose = false;

    //alpha factor (emotion variance related)
    private  float alphaFactor;
    
    //Variables for Random Forest classification
    public RandomForest RF = new RandomForest();
    public Instances RF_trainingInstances;
    public Instances RF_testInstances;

    public int levelWidth = 50;

    public boolean training = true;
    MainSendRequest request = new MainSendRequest();
    boolean online = true;

    public LevelSceneTest(GraphicsConfiguration graphicsConfiguration,
            MarioComponent renderer, long seed, int levelDifficulty, int type) {
        super(graphicsConfiguration, renderer, seed, levelDifficulty, type);

    }

    public void init() {
        try {
            Level.loadBehaviors(new DataInputStream(ResourcesManager.class.getResourceAsStream("res/tiles.dat")));
        } catch (IOException e) {
            e.printStackTrace();
            //System.exit(0);
        }
        this.firstRun = true;
        this.alphaFactor =0;
        int diffParis = 1;
        int[] temp = {diffParis,diffParis,diffParis,diffParis,diffParis,diffParis};
        //set difficulties for sections aswell.
        for(SectionOfGame section : this.sections){
            section.setPreviousDifficulty(temp[0]);
        }
        this.newDifficulties = temp;
        
        //System.out.println("paris implementation");
        //create list of sections.
        /**
            * STRAIGHT = 0; HILL_STRAIGHT = 1; TUBES = 2; JUMP = 3; CANNONS = 4;
         */
        for (int i = 0; i < 5; i++) {
            SectionOfGame section = new SectionOfGame(i);
            sections.add(section);
        }

        System.out.println("----------------------------------------");
        System.out.println("---------- Initialising game -----------");
        System.out.println("----------------------------------------");

        System.out.println("");
        System.out.println("Generating first two segments...");
        currentLevelSegment = 0;
        nextSegmentAlreadyGenerated = false;

        paused = false;
        Sprite.spriteContext = this;
        sprites.clear();

        Random randomGenerator = new Random();
        int randomInt = randomGenerator.nextInt(100);

        arch = new Architect(training, request);
        //paris TEST OLD 
        //int[] parisParameters = new int[6];
        //get parameters based on emotions
        //parisParameters = probabilitiesToParameters(emotions);
        //arch.params_new.setSettingsInt(parisParameters);

        level2 = new ArchLevel(arch.params_new);
        plannedDifficultyLevels.add(level2.DIFFICULTY_sander);

        randomInt = randomGenerator.nextInt(100);
        arch.params_new.seed = randomInt;

        level3 = new ArchLevel(arch.params_new);
        plannedDifficultyLevels.add(level3.DIFFICULTY_sander);

        fixborders();
        conjoin();

        try {
            level2_reset = level2.clone();
        } catch (CloneNotSupportedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            level3_reset = level3.clone();
        } catch (CloneNotSupportedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        layer = new LevelRenderer(level, graphicsConfiguration, 320, 240);
        for (int i = 0; i < 2; i++) {
            int scrollSpeed = 4 >> i;
            int w = ((level.getWidth() * 16) - 320) / scrollSpeed + 320;
            int h = ((level.getHeight() * 16) - 240) / scrollSpeed + 240;
            Level bgLevel = BgLevelGenerator.createLevel(w / 32 + 1, h / 32 + 1, i == 0, levelType);
            bgLayer[i] = new BgRenderer(bgLevel, graphicsConfiguration, 320, 240, scrollSpeed);
        }

        double oldX = 0;
        if (mario != null) {
            oldX = mario.x;
        }

        mario = new Mario(this);
        sprites.add(mario);
        startTime = 1;
        timeLeft = 200 * 15;

        tick = 0;

        /*
         * SETS UP ALL OF THE CHECKPOINTS TO CHECK FOR SWITCHING
         */
        switchPoints = new ArrayList<Double>();

        //first pick a random starting waypoint from among ten positions
        int squareSize = 16; //size of one square in pixels
        int sections = 10;

        double startX = 32; //mario start position
        double endX = level.getxExit() * squareSize; //position of the end on the level
        //if(!isCustom && recorder==null)

        recorder = new DataRecorder(this, level2, keys);
        ////System.out.println("\n enemies LEFT : " + recorder.level.COINS); //SANDER disable
        ////System.out.println("\n enemies LEFT : " + recorder.level.BLOCKS_COINS);
        ////System.out.println("\n enemies LEFT : " + recorder.level.BLOCKS_POWER);
        gameStarted = false;
    }

    //get a vector of probabilities and convert it to parameter scale.
    public int[] probabilitiesToParameters(float[] emotions) {
        int[] params = new int[6];
        for (int i = 0; i < params.length; i++) {
            System.out.println("emotion " + (i + 1) + ": " + emotions[i + 1]);
            //ignore the first parameter (neutral face)
            //convert from [0..1] to [1..5] by param=emotion*(max-min)+min
            params[i] = Math.round(emotions[i + 1] * (5 - 1) + 1);
            System.out.println("parameter " + i + ":" + params[i]);
        }
        return params;
    }

    // Locally for Random Forest Classifier -- Currently Unused
    public void loadTrainingInstances(boolean verbose) {
        try {
            //Load training instances into data
            //System.out.println("");
            //System.out.println("Loading training instances into RandomForest classifier...");
            BufferedReader reader = new BufferedReader(
                    new FileReader("../../MAINOOR/traindata/MarioPOMDP-traininginstances.arff"));
            Instances data = new Instances(reader);
            reader.close();
            // setting class attribute
            data.setClassIndex(data.numAttributes() - 2);            //2nd to last attribute is used for classification (last is timestamp)

            //Filter out timestamp string data
            String[] options = new String[2];
            options[0] = "-R";                                       // "range"
            options[1] = "48";                                       // last timestamp attribute
            Remove remove = new Remove();                            // new instance of filter
            remove.setOptions(options);                              // set options
            remove.setInputFormat(data);                             // inform filter about dataset **AFTER** setting options
            RF_trainingInstances = Filter.useFilter(data, remove);   // apply filter

            //Build RandomForest classifier
            String[] options_RF = new String[1];
            options_RF[0] = "-D";          // debug output
            //RandomForest RF = new RandomForest(); //declared as public
            //RF.setOptions(options_RF);
            RF.buildClassifier(RF_trainingInstances);

            if (verbose) {
                //Get classification of example data
                //System.out.println(RF.toString());
                Evaluation eTest = new Evaluation(data);
                //eTest.evaluateModel(RF, RF_trainingInstances);
                //eTest.crossValidateModel(RF, data, 10, new Random());
                int folds = 10;
                Random rand = new Random(0);  // using seed = 0 (should be 1?)
                eTest.crossValidateModel(RF, RF_trainingInstances, folds, rand);

                // Print the result à la Weka explorer:
                String strSummary = eTest.toSummaryString();
//                                    System.out.println(strSummary);
                //System.out.println(eTest.toClassDetailsString());

                // Get the confusion matrix
                //double[][] cmMatrix = eTest.confusionMatrix();                                
                //System.out.println(eTest.toMatrixString());
            }

//                                System.out.println("-done loading " + RF_trainingInstances.numInstances() + " training instance(s)");
        } catch (Exception e) {
            //Error reading file
//                                System.out.println("ERROR!!! - In function loadTrainingInstances()...");
//                                System.out.println("-" + e);
        }
    }

    public void loadTestInstances(boolean verbose) {
        try {
            //Load test instances into data
            //System.out.println("");
            //System.out.println("Loading test instances...");
            BufferedReader reader = new BufferedReader(
                    new FileReader("../../MAINOOR/traindata/MarioPOMDP-testinstances.arff"));
            Instances data = new Instances(reader);
            reader.close();
            // setting class attribute
            data.setClassIndex(data.numAttributes() - 2);        //2nd to last attribute is used for classification (last is timestamp)

            //Filter out string data
            String[] options = new String[2];
            options[0] = "-R";                                   // "range"
            options[1] = "48";                                   // last timestamp attribute
            Remove remove = new Remove();                        // new instance of filter
            remove.setOptions(options);                          // set options
            remove.setInputFormat(data);                 // inform filter about dataset **AFTER** setting options
            RF_testInstances = Filter.useFilter(data, remove);   // apply filter                                

//                                System.out.println("-done loading " + RF_testInstances.numInstances() + " test instance(s)");
        } catch (Exception e) {
            //Error reading file
//                                System.out.println("ERROR!!! - In function loadTestInstances()...");
//                                System.out.println("-" + e);
        }
    }

    public Instance selectTestInstance() {
                            //Select last instance from loaded set of Test Instances

        //Create test instance
        //Instance testInstance = new Instance(newDataTest.firstInstance());
        //Instance testInstance = new Instance(newDataTest.instance(0));
        Instance testInstance = new Instance(RF_testInstances.lastInstance());
                            //System.out.println("-selecting last instance in test set RF_testInstances, done");

        // Specify that the instance belong to the training set 
        // in order to inherit from the set description                                
        testInstance.setDataset(RF_trainingInstances);
//                            System.out.println("-selected last instance in test set: " + testInstance.toString() );

        return testInstance;
    }

    public Instance selectTrainingInstance(int index) {
                            //Select last instance from loaded set of Test Instances

        //Create test instance
        //Instance trainingInstance = new Instance(RF_trainingInstances.firstInstance());
        //Instance trainingInstance = new Instance(RF_trainingInstances.lastInstance());
        Instance trainingInstance = new Instance(RF_trainingInstances.instance(index));

        // Specify that the instance belong to the training set 
        // in order to inherit from the set description                                
        trainingInstance.setDataset(RF_trainingInstances);
//                            System.out.println("-processing instance # " + index + " in training set: " + trainingInstance.toString() );

        return trainingInstance;
    }

    public double classifyInstance(Instance testInstance, boolean verbose) {
        try {
			//Classify one particular instance from loaded set of Test Instances

            //Create test instance
            //Instance testInstance = new Instance(newDataTest.firstInstance());
            //Instance testInstance = new Instance(newDataTest.instance(0));
            //Instance testInstance = new Instance(RF_testInstances.lastInstance());
            ////System.out.println("-selecting last instance in test set RF_testInstances, done");
            // Specify that the instance belong to the training set 
            // in order to inherit from the set description                                
            //testInstance.setDataset(RF_trainingInstances);
            // Get the likelihood of each classes 
            // fDistribution[0] is the probability of being positive
            // fDistribution[1] is the probability of being negative 
            double[] fDistribution = RF.distributionForInstance(testInstance);

            if (verbose) {
                //System.out.println("");
                //System.out.println("Classifying selected test instance...");                               
                //System.out.println("-probability of instance being appropriate     (1): " + fDistribution[1]);
                //System.out.println("-probability of instance being non-appropriate (0): " + fDistribution[0]);
                //System.out.println("-returning appropriateness probability of: " + fDistribution[1]);
            }

            return fDistribution[1];
        } catch (Exception e) {
            //Error reading file
            //System.out.println("ERROR!!! - In function classifyInstance()...");
            //System.out.println("-" + e);
            return 0.0; //dummy value
        }
    }

    public double[] classifyInstance() {
        double[] fDistribution = {0.3, 0.2, 0.1, 0.3, 0.3};
        return fDistribution;
    }

    public DifficultyRecorder getUserOpinion() {
        dr.startRecordDifficulty(false);
        while (!dr.isFinished()) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
                Logger.getLogger(LevelSceneTest.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            }
        }
        return dr;
    }

    public int getCurrentSectionType(int xcoord) {
        // returns 0-4 for section types
        // returns -1 if the coordinate is < 0
        //      meaning that the mario sprite is on the previous chunk
        if (arch.chunksGenerated == 0) {
            int l2length = level2.map.length;
            if (xcoord > l2length) {
                return level3.sectionTypeAtCoordinate(xcoord - l2length);
            } else {
                return level2.sectionTypeAtCoordinate(xcoord);
            }
        } else {
            return level2.sectionTypeAtCoordinate(xcoord - level3.map.length);
        }
        
    }

    public int getDifficulty() {

        //return m.DIFFICULTY; //this is outdated - lets return the actual values as also displaye on the screen
        //    public int currentLevelSegment;
        //    public ArrayList plannedDifficultyLevels = new ArrayList(0);                       
        return (int) plannedDifficultyLevels.get(currentLevelSegment);
    }

    public int getAppropriateness() {
        return randomNumber(0, 3);
    }

    public void updateReward(Instance selectedInstance, boolean doBernoulliRewards, boolean isTrainingInstance, boolean verbose) {
        //Update reward in the vector playerModel[]
        if (verbose) {
            System.out.println("");
            System.out.println("updateReward called()");
        }

        //Determine difficulty level associated to this instance
        int difficultyLevel;
        if (isTrainingInstance) {
            difficultyLevel = Integer.parseInt(selectedInstance.toString(45)); //in this attribute the difficulty level is stored
        } else {
            difficultyLevel = getDifficulty();  //now: m.DIFFICULTY. perhaps it should be m.state ?
        }
        if (verbose) {
            System.out.println("-calculating reward for previous level segment with difficulty level: " + difficultyLevel);
        }
        //double probsAppro = getProbsAppropriateness_ObservationStr(observation_str, false);
        double probsAppro = classifyInstance(selectedInstance, false);
        if (verbose) {
            System.out.println("-difficulty of level segment was deemed appropriate with a probability of: " + probsAppro);
        }

        //Determine rewards according to Bernoulli scheme / proportional reward
        double reward = 0.0;
        if (doBernoulliRewards) {
            if (verbose) {
                System.out.println("-returning reward of 1 with probablity of " + probsAppro + ", else reward of 0 (Bernoulli rewards)");
            }
            boolean returnBernoulliReward;
            if (Math.random() <= probsAppro) {
                returnBernoulliReward = true;
            } else {
                returnBernoulliReward = false;
            }

            if (verbose) {
                System.out.println("-boolean returnBernoulliReward: " + returnBernoulliReward);
            }
            if (returnBernoulliReward) {
                reward = 1.0;
            } else {
                reward = 0.0;
            }
        } else {
            if (verbose) {
                System.out.println("-returning reward " + probsAppro + " (regular non-Bernoulli rewards)");
            }
            reward = probsAppro;
        }

        //if (verbose) System.out.println("-adding reward of " + reward + " to arraylist playerModelDiff" + difficultyLevel);
        switch (difficultyLevel) {
            case 0:
                System.out.println("Set to 0, not adding any reward");
                break;
            case 1:
                playerModelDiff1.add(reward);
                break;
            case 4:
                playerModelDiff4.add(reward);
                break;
            case 7:
                playerModelDiff7.add(reward);
                break;
            default:
                System.out.println("-ERROR! DifficultyLevel=" + difficultyLevel + " Cannot add reward to concerning playerModelDiff1,4,7 due to incorrect input of difficultyLevel");
                break;
        }
        //Note, updating the display of average rewards is performed by updatePlayerModel()
        //int index = getPlayerModelIndex(difficultyLevel);
        //System.out.println("-updating playerModel[" + index + "] with reward: " + reward);
        //playerModel[index] += reward;
        if (verbose) {
            System.out.println("-done");
        }

                            //OLD
        //Increase reward proportionally to appropriateness of current difficulty level to the specific player
        //As determed by probabilities in player model
                            /*
         //System.out.println("");
         System.out.println("updateReward called()");
         double reward = getPlayerModelElement(m.DIFFICULTY);
         System.out.println("-increasing reward by: " + reward);
         m.REWARD += reward;
         System.out.println("-new reward is now: " + m.REWARD);
         */
        //OLD OLD
                            /*
         if (m.state == 1) { //SANDER UPDATE - NOT CORRECT AT THE MOMENT
         //Appropriate difficulty - Increase reward
         int rangeMin = 0;
         int rangeMax = 1;
         Random r = new Random();
         double randomValue = rangeMin + (rangeMax - rangeMin) * r.nextDouble();
         System.out.println("Increasing reward by: " + randomValue);
         m.REWARD += randomValue;
         System.out.println("New cummulative reward: " + m.REWARD);
         }
         else {
         //No appropriate difficulty - Do not increase reward for this level block
         m.REWARD += 0;                              
         }
         */
    }

    public void newchunk() {
        if (!gameover) {
            //System.out.println("-newchunck called");
            System.out.println("");
            System.out.println("Generating next segment...");

            //Update the next levels parameters according to exploration policy
            arch.update(training);

            //Note: Using other constructor of ArchLevel, using recorder and valueList as inputs
            level2 = new ArchLevel(arch.params_new);
            nextSegmentAlreadyGenerated = true;
            //System.out.println("-setting nextSegmentAlreadyGenerated to: " + nextSegmentAlreadyGenerated);

            try {
                level2_reset = level3_reset.clone();
            } catch (CloneNotSupportedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try {
                level3_reset = level2.clone();
            } catch (CloneNotSupportedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } else {
            level.xExit = 105;
        }

    }

    private int convertTime(int time) {
        return (int) Math.floor((time + 15 - 1) / 15);
    }

    public void swap() throws Exception {

        int k = 0;
        //The background info should change aswell               
        
        if (mario.x > (level2.width * 16 + level3.width * 16 - (10 * 16))) {
            tries = 3;
            recorder.endTime();
            marioComponent.pause();

                    
            //keep track of neutral, happy and angry  
            ArrayList<float[]> neutralHappyAngry = new ArrayList<>();
            
            //read emotions and create text files
            //readEmotions() also normalizes the emotions table for each section
            readEmotions();

            //get the variance for each emotion during this segment.
            float emotionVarianceThisSegment = getEmotionVariance();
            //System.out.println(emotionVarianceThisSegment);
            //show more emotions = smoother change in difficulties.
            //show less emotions = cause more significant change in difficulties.
            if(this.firstRun){
                this.alphaFactor = 1-emotionVarianceThisSegment;
                System.out.println("alpha Factor : "+ this.alphaFactor);
                this.firstRun=false;
                
                
            //write initial Difficulties to file
            StringBuilder line2 = new StringBuilder();
            for(SectionOfGame section:sections){
                line2.append("1");
                line2.append(" ");
            }

            try {
                String filename = "difficulties.txt";
                PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filename, true)));
                out.println(line2);
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            }
            
            
            //paris: calculate stuff during segments and update difficulties.
            for (SectionOfGame section : sections) {
                
                
                /**
                 * System.out.println(section.getId()); section.printEmotions();
                 * if(section.getEmotions()[0]>0.2){
                 * this.newDifficulties[section.getId()] ++;
                    }
                 */
                
                //System.out.println(section.getId()); 
                //section.printEmotions();
                
                
                //get neutral, happy and angry feelings and add them to a list.
                float[] nha = {section.getEmotions()[0],section.getEmotions()[1],section.getEmotions()[3]};
                neutralHappyAngry.add(nha);
                
                 //section.printAllEmotions();
                //reset also calculates next difficulty.
                section.reset(this.alphaFactor);
                this.newDifficulties[section.getId()] = section.getNextDifficulty();

            }
            //calculate neutral,happy and angry for whole section, and write to file.
            float[] tempSumNha={0,0,0};
            
            for(int ind=0;ind<5;ind++){
                tempSumNha[0] +=neutralHappyAngry.get(ind)[0]; 
                tempSumNha[1] +=neutralHappyAngry.get(ind)[1];
                tempSumNha[2] +=neutralHappyAngry.get(ind)[2];
            }
            
            for(int nhaInd=0;nhaInd<3;nhaInd++){
                tempSumNha[nhaInd]/=5;
                System.out.println("emotion "+nhaInd+" "+tempSumNha[nhaInd]);
            }
            
            //write neutral,happy,angry to file
            StringBuilder line = new StringBuilder();
            
            line.append(String.valueOf(tempSumNha[0]));
            line.append(" ");
            line.append(String.valueOf(tempSumNha[1]));
            line.append(" ");
            line.append(String.valueOf(tempSumNha[2]));
            try {
                String filename = "neutralHappyAngry.txt";
                PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filename, true)));
                out.println(line);
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            //write new Difficulties to file
            StringBuilder line2 = new StringBuilder();
            for(SectionOfGame section:sections){
                line2.append(String.valueOf(this.newDifficulties[section.getId()]));
                line2.append(" ");
            }

            try {
                String filename = "difficulties.txt";
                PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filename, true)));
                out.println(line2);
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            
                    //classification Paris
            
            //gather all the data.
            
            //for each section:
            
            //readDataFromFile
            //get new instance
            //get estimate.
            
            
            //if we need to train, get user opinion.
            if(isTraining){
                getUserOpinion();

            }
            //test for 1 section
            sections.get(0).initializeModel(); //WORKS
            sections.get(0).readDataFromFile(); //WORKS
            sections.get(0).addInstance(dr.likert);//WORKS
            sections.get(0).buildClassifier();//WORKS
            double q = sections.get(0).getEstimate();//WORKS  
            sections.get(0).saveDataToFile(); //WORKS
            
            
            
            
            
//            int likert = dr.likert;
//          //  FastVector attributes = new FastVector();
//            int diff = sections.get(0).getPreviousDifficulty();
//            float yaw = sections.get(0).getGazeMatrix()[0]; //yaw,pitch,roll
//            float pitch = sections.get(0).getGazeMatrix()[1];
//            float roll = sections.get(0).getGazeMatrix()[2];
//            float neutral = sections.get(0).getPreviousEmotions()[0];
//            float happy = sections.get(0).getPreviousEmotions()[1];
//            float surprised = sections.get(0).getPreviousEmotions()[2];
//            float angry = sections.get(0).getPreviousEmotions()[3];
//            float disgusted = sections.get(0).getPreviousEmotions()[4];
//            float afraid = sections.get(0).getPreviousEmotions()[5];
//            float sad = sections.get(0).getPreviousEmotions()[6];
//            
//            FastVector nom = new FastVector(5);
//            nom.addElement("1");
//            nom.addElement("2");
//            nom.addElement("3");
//            nom.addElement("4");
//            nom.addElement("5");
//            Attribute likertAttr = new Attribute("aNominal",nom);
//           
//            
//            attributes.addElement(new Attribute("diff"));
//            attributes.addElement(new Attribute("yaw"));
//            attributes.addElement(new Attribute("pitch"));
//            attributes.addElement(new Attribute("roll"));
//            attributes.addElement(new Attribute("neutral"));
//            attributes.addElement(new Attribute("happy"));
//            attributes.addElement(new Attribute("surprised"));
//            attributes.addElement(new Attribute("angry"));
//            attributes.addElement(new Attribute("disgusted"));
//            attributes.addElement(new Attribute("afraid"));
//            attributes.addElement(new Attribute("sad"));
//            
//            if(isTraining){
//                attributes.addElement(likertAttr);
//            }

            
//            Instances data = new Instances("myRelation",attributes,12);

//            Instance newInstance = new Instance(12);
//            newInstance.setValue((Attribute)attributes.elementAt(0),diff);
//            newInstance.setValue((Attribute)attributes.elementAt(1),yaw);
//            newInstance.setValue((Attribute)attributes.elementAt(2),pitch);
//            newInstance.setValue((Attribute)attributes.elementAt(3),roll);
//            newInstance.setValue((Attribute)attributes.elementAt(4),neutral);
//            newInstance.setValue((Attribute)attributes.elementAt(5),happy);
//            newInstance.setValue((Attribute)attributes.elementAt(6),surprised);
//            newInstance.setValue((Attribute)attributes.elementAt(7),angry);
//            newInstance.setValue((Attribute)attributes.elementAt(8),disgusted);
//            newInstance.setValue((Attribute)attributes.elementAt(9),afraid);
//            newInstance.setValue((Attribute)attributes.elementAt(10),sad);
//            newInstance.setValue((Attribute)attributes.elementAt(11),Integer.toString(likert));
            
            
            
//            newInstance.setDataset(data);
//            data.add(newInstance);
//            data.setClassIndex(data.numAttributes()-1);
            
            //section.saveDataToFile
//            //save to arff
//            ArffSaver saver = new ArffSaver();
//            saver.setInstances(data);
//            saver.setFile(new File("test.arff"));
//            saver.writeBatch();
          
            //section.readDataFromFile
//            BufferedReader reader = new BufferedReader(
//            new FileReader("test.arff"));
//            Instances data2 = new Instances(reader);
//            reader.close();
            
            
          //  System.out.println("DATA    "+data);
//
//            RandomForest tree = new RandomForest();
//            try {
//                tree.buildClassifier(data);
//            } catch (Exception ex) {
//                System.out.println(ex);
//            }
            
//            Instance newInstance2 = new Instance(12);
//            newInstance2.setValue((Attribute)attributes.elementAt(0),diff);
//            newInstance2.setValue((Attribute)attributes.elementAt(1),yaw);
//            newInstance2.setValue((Attribute)attributes.elementAt(2),pitch);
//            newInstance2.setValue((Attribute)attributes.elementAt(3),roll);
//            newInstance2.setValue((Attribute)attributes.elementAt(4),neutral);
//            newInstance2.setValue((Attribute)attributes.elementAt(5),happy);
//            newInstance2.setValue((Attribute)attributes.elementAt(6),surprised);
//            newInstance2.setValue((Attribute)attributes.elementAt(7),angry);
//            newInstance2.setValue((Attribute)attributes.elementAt(8),disgusted);
//            newInstance2.setValue((Attribute)attributes.elementAt(9),afraid);
//            newInstance2.setValue((Attribute)attributes.elementAt(10),sad);
//            newInstance2.setDataset(data);
//            newInstance2.setMissing(11);
//            
//            double result = 0;
//            try {
//                result = tree.classifyInstance(newInstance2);
//            } catch (Exception ex) {
//                Logger.getLogger(LevelSceneTest.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//            }
//            System.out.println("RISALTZZZZ++   +     "+result);//RETURNS THE INDEX OF NOMINAL CLASS OF ATTRIBUTE (0-4)
//            
//            
            
            sections.get(0).resetGazeMeasurements();
            
        
            
            
            
            arch.params_new.setSettingsInt(this.newDifficulties);

            //Swapping level segment
            //System.out.println("");
            //System.out.println("----------------------------------------");
            //System.out.println("-------- Swapping level segment --------");
            //System.out.println("----------------------------------------");
            // Difficulty Popup here -DP1
            //System.out.println("-pausing");
            //arch.Obs = recorder.fillGamePlayMetrics(getUserOpinion(), verbose, request, online); //write metrics at swapping to new level segment
            //Load test instances and select last instance for classification
            //Update in which level segment the player currently is
            //arch.reward_label = level3.getCustomRewards("coin");
            //arch.reward_weights = classifyInstance();
            currentLevelSegment++;

            nextSegmentAlreadyGenerated = false;

            //Update planned difficulty for the upcoming level segment
            plannedDifficultyLevels.add(levelDifficulty); //more efficient code as if statement has become redundant
            recorder.levelScene.resetTime();

            recorder.reset();
            recorder.startTime();

            for (int i = 0; i < level.width; i++) {
                if (i < level2.width) {
                    level2.map[i] = level.map[i];
                    level2.spriteTemplates[i] = level.spriteTemplates[i];

                } else {
                    level3.map[k] = level.map[i];
                    level3.spriteTemplates[k] = level.spriteTemplates[i];
                    k++;
                }

            }

            newchunk();
            recorder.level = level2;
            fixborders();
            k = 0;

            for (int i = 0; i < level.width; i++) {
                if (i < level3.width) {
                    level.map[i] = level3.map[i];;
                    level.spriteTemplates[i] = level3.spriteTemplates[i];
                } else {
                    level.map[i] = level2.map[k];
                    level.spriteTemplates[i] = level2.spriteTemplates[k];
                    k++;
                }

            }
            for (int i = 0; i < sprites.size(); i++) {
                sprites.get(i).x = sprites.get(i).x - level2.width * 16;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(LevelSceneTest.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            }
            marioComponent.resume();
        }

    }
    // }

    public void save() {
        try {
            level2_reset = level2.clone();
        } catch (CloneNotSupportedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            level3_reset = level3.clone();
        } catch (CloneNotSupportedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //paris: calculate the variance of emotions (averaging over all emotions over all sections).
    public float getEmotionVariance() {

        float[] totalVariances = {0, 0, 0, 0, 0, 0, 0};
        float sum = 0;

        for (SectionOfGame section : sections) {
            ArrayList<float[]> allEmotions = section.getAllEmotions();
            float[] emotionsMean = section.getEmotions();
            float[] temp = {0, 0, 0, 0, 0, 0, 0};
            float[] variances = {0, 0, 0, 0, 0, 0, 0};

            //for each vactor of emotions (==each game frame)
            for (int i = 0; i < allEmotions.size(); i++) {
                //for each emotion
                for (int j = 0; j < 7; j++) {
                    temp[j] += (emotionsMean[j] - allEmotions.get(i)[j]) * (emotionsMean[j] - allEmotions.get(i)[j]);
                }
            }

            //create variances table
            for (int k = 0; k < 7; k++) {
                variances[k] = temp[k] / section.getTimes();
            }
            //save variances in class variable
            section.setEmotionVariance(variances);

            //calculate totalVariances
            for (int q = 0; q < 7; q++) {
                totalVariances[q] += variances[q];
            }
        }

        for (int w = 0; w < 7; w++) {
            totalVariances[w] /= 5;
            //System.out.println(totalVariances[w]);
            sum += totalVariances[w];
        }

        return sum / 7;
    }

    public void conjoin() {
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
        for (int i = 0; i < width; i++) {
            if (i < level2.width) {
                level4.map[i] = level2.map[i].clone();
                //level4.data[i] = level2.data[i];
                level4.spriteTemplates[i] = level2.spriteTemplates[i];

            } else {
                level4.map[i] = level3.map[k].clone();
                //level4.data[i] = level3.data[k];
                level4.spriteTemplates[i] = level3.spriteTemplates[k];
                k++;
            }

        }
        level = level4;
    }

    public void fixborders() {
        for (int i = 0; i < 15; i++) {
            level2.map[0][i] = (byte) (0);
            level2.map[level2.width - 1][i] = (byte) (0);

            //if(level2.map[level2.width-1][i] == )
            if (level2.map[level2.width - 2][i] == (byte) (-127)) {
                level2.map[level2.width - 2][i] = (byte) (-126);
            }

            if (level2.map[level2.width - 2][i] == (byte) (-111)) {
                level2.map[level2.width - 2][i] = (byte) (-110);
            }

            if (level2.map[1][i] == (byte) (-127)) {
                //change to corner
                level2.map[1][i] = (byte) (-128);

            }
            if (level2.map[1][i] == (byte) (-111)) {
                level2.map[1][i] = (byte) (-112);
            }

            level3.map[0][i] = (byte) (0);
            level3.map[level3.width - 1][i] = (byte) (0);

            //if(level2.map[level2.width-1][i] == )
            if (level3.map[level3.width - 2][i] == (byte) (-127)) {
                level3.map[level3.width - 2][i] = (byte) (-126);
            }

            if (level3.map[level3.width - 2][i] == (byte) (-111)) {
                level3.map[level3.width - 2][i] = (byte) (-110);
            }

            if (level3.map[1][i] == (byte) (-127)) {
                //change to corner
                level3.map[1][i] = (byte) (-128);

            }
            if (level3.map[1][i] == (byte) (-111)) {
                level3.map[1][i] = (byte) (-112);
            }
        }
    }

    public void tick() {
        try {
            swap();
        } catch (Exception ex) {
            Logger.getLogger(LevelSceneTest.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        super.tick();

        sectionCalculations();

        if (recorder != null && !gameStarted) {
            recorder.startLittleRecord();
            recorder.startTime();
            gameStarted = true;
        }
        if (recorder != null) {
            recorder.tickRecord();
        }
    }

    public void winActions() {
        DifficultyRecorder dr = DifficultyRecorder.getInstance();
        if (recorder != null) {
            dr.startRecordDifficulty(false);
            while (!dr.isFinished()) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                    Logger.getLogger(LevelSceneTest.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                }
            }
        }
        marioComponent.win();
    }

    public void deathActions() {
        

        //Reset general mario stuff
        DifficultyRecorder dr = DifficultyRecorder.getInstance();
        
        getUserOpinion();
        System.out.println(dr.likert+ " LIKERT");

        if (Mario.lives <= 0) { //has no more lives
            if (recorder != null) {
                dr.startRecordDifficulty(false);
                while (!dr.isFinished()) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(LevelSceneTest.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                    }
                }
            }
            marioComponent.lose();
        } else // mario still has lives to play :)--> have a new beginning
        {
            if (recorder != null) {
                if (dr.isRecordAfterDeath()) {
                    dr.startRecordDifficulty(false);
                    while (!dr.isFinished()) {
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(LevelSceneTest.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                        }
                    }
                }
            }
            tries--;
            if (tries == 0) {
                tries = 3;
              //  recorder.fillGamePlayMetrics(getUserOpinion(), verbose, request, online);
            }
            //Mario.lives--; //Infinite amount of lives
            reset();
        }
    }

    public void bump(int x, int y, boolean canBreakBricks) {
        byte block = level.getBlock(x, y);

        if ((Level.TILE_BEHAVIORS[block & 0xff] & Level.BIT_BUMPABLE) > 0) {
            bumpInto(x, y - 1);
            level.setBlock(x, y, (byte) 4);

            if (((Level.TILE_BEHAVIORS[block & 0xff]) & Level.BIT_SPECIAL) > 0) {
                sound.play(Art.samples[Art.SAMPLE_ITEM_SPROUT], new FixedSoundSource(x * 16 + 8, y * 16 + 8), 1, 1, 1);
                if (!Mario.large) {
                    addSprite(new Mushroom(this, x * 16 + 8, y * 16 + 8));
                } else {
                    addSprite(new FireFlower(this, x * 16 + 8, y * 16 + 8));
                }

                if (recorder != null) {
                    recorder.blockPowerDestroyRecord();
                }
            } else {
                //TODO should only record hidden coins (in boxes)
                if (recorder != null) {
                    recorder.blockCoinDestroyRecord();
                }

                Mario.getCoin();
                sound.play(Art.samples[Art.SAMPLE_GET_COIN], new FixedSoundSource(x * 16 + 8, y * 16 + 8), 1, 1, 1);
                addSprite(new CoinAnim(x, y));
            }
        }

        if ((Level.TILE_BEHAVIORS[block & 0xff] & Level.BIT_BREAKABLE) > 0) {
            bumpInto(x, y - 1);
            if (canBreakBricks) {
                if (recorder != null) {
                    recorder.blockEmptyDestroyRecord();
                }

                sound.play(Art.samples[Art.SAMPLE_BREAK_BLOCK], new FixedSoundSource(x * 16 + 8, y * 16 + 8), 1, 1, 1);
                level.setBlock(x, y, (byte) 0);
                for (int xx = 0; xx < 2; xx++) {
                    for (int yy = 0; yy < 2; yy++) {
                        addSprite(new Particle(x * 16 + xx * 8 + 4, y * 16 + yy * 8 + 4, (xx * 2 - 1) * 4, (yy * 2 - 1) * 4 - 8));
                    }
                }
            }

        }
    }

    public void bumpInto(int x, int y) {
        byte block = level.getBlock(x, y);
        if (((Level.TILE_BEHAVIORS[block & 0xff]) & Level.BIT_PICKUPABLE) > 0) {
            Mario.getCoin();
            sound.play(Art.samples[Art.SAMPLE_GET_COIN], new FixedSoundSource(x * 16 + 8, y * 16 + 8), 1, 1, 1);
            level.setBlock(x, y, (byte) 0);
            addSprite(new CoinAnim(x, y + 1));

            //TODO no idea when this happens... maybe remove coin count
            if (recorder != null) {
                recorder.recordCoin();
            }
        }

        for (Sprite sprite : sprites) {
            sprite.bumpCheck(x, y);
        }
    }

    private int randomNumber(int low, int high) {
        return new Random(new Random().nextLong()).nextInt(high - low) + low;
    }

    private int toBlock(float n) {
        return (int) (n / 16);
    }

    private int toBlock(double n) {
        return (int) (n / 16);
    }

    private float toReal(int b) {
        return b * 16;
    }

    public void reset() {
        
        
        
        System.out.println("");
        System.out.println("----------------------------------------");
        System.out.println("------------ Resetting game ------------");
        System.out.println("----------------------------------------");

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        System.out.println("reset Time: "+calendar.getTimeInMillis() / 1000);
        sections.get(mario.getDeathSection()).calculateDeathEmotions(calendar.getTimeInMillis()/1000);
        

        
        
        //Always reset POMDP stuff
        playerModelDiff1.clear();
        playerModelDiff4.clear();
        playerModelDiff7.clear();
        updatePlayerModel();
        displayReceivedRewards();

        int temp_diffsegment1;
        int temp_diffsegment2;
        if (currentLevelSegment == 0) {
            //System.out.println("-you died in the first segment, resetting to how you just started");
            temp_diffsegment1 = (int) plannedDifficultyLevels.get(0);
            temp_diffsegment2 = (int) plannedDifficultyLevels.get(1);
        } else {
            //System.out.println("-nextSegmentAlreadyGenerated:" + nextSegmentAlreadyGenerated);
            if (nextSegmentAlreadyGenerated) {
                //because the next segment is already generated (and so the previous does not exist anymore),
                temp_diffsegment1 = (int) plannedDifficultyLevels.get(currentLevelSegment);
                temp_diffsegment2 = (int) plannedDifficultyLevels.get(currentLevelSegment + 1);
            } else {
                //because the next segment is not yet generated
                temp_diffsegment1 = (int) plannedDifficultyLevels.get(currentLevelSegment - 1);
                temp_diffsegment2 = (int) plannedDifficultyLevels.get(currentLevelSegment);
            }
        }
        plannedDifficultyLevels.clear();

        //System.out.println("-resetting to: " + temp_diffsegment1 + ", " + temp_diffsegment2);
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

        //paris: update Difficulties on death if necessary.
        int deathSection = mario.getDeathSection();
        float[] deathEmotions = sections.get(deathSection).getDeathEmotions();
        int[] difficultiesAfterDeath = this.newDifficulties;
        boolean isAngry = true;
    
        
                
//        System.out.println("----EMOTIONS BEFORE DEATH");
//        for(int z=0;z<7;z++){
//            System.out.println(sections.get(deathSection).getEmotionsBeforeDeath()[z]);
//        }
//        System.out.println("----EMOTIONS AT DEATH");
//        for(int z=0;z<7;z++){
//            System.out.println(sections.get(deathSection).getDeathEmotions()[z]);
//        }
//        
        
        //old implementation: angry > all else
//        for(float emotion:deathEmotions){
//            if(deathEmotions[3]<emotion){
//                isAngry = false;
//            }
//        }

        
        //new implementation heuristic : angry>0.3?
        if(deathEmotions[3]<0.1){
            isAngry = false;
        }
        
        if(isAngry==true){
            int decreaseAmount = Math.round(deathEmotions[3]*5);
        if(this.firstRun){
            System.out.println("reducing previous difficulty.");
            sections.get(mario.getDeathSection()).reducePreviousDifficulty(1);
        }

            
            System.out.println("user is angry, decreasing difficulty for section: "+deathSection);
            difficultiesAfterDeath[deathSection]-=decreaseAmount;
            
            sections.get(deathSection).setWasReduced(true);
            
            if(difficultiesAfterDeath[deathSection]<0){
                difficultiesAfterDeath[deathSection]=0;
            }
        }
        
        arch.params_new.setSettingsInt(difficultiesAfterDeath);
            
            //write new Difficulties to file
            StringBuilder line2 = new StringBuilder();
            for(SectionOfGame section:sections){
                line2.append(String.valueOf(difficultiesAfterDeath[section.getId()]));
                line2.append(" ");
            }

            try {
                String filename = "difficulties.txt";
                PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filename, true)));
                out.println(line2);
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        
        level2 = new ArchLevel(arch.params_new);
        level3 = new ArchLevel(arch.params_new);
        
        conjoin();

        layer = new LevelRenderer(level, graphicsConfiguration, 320, 240);
        for (int i = 0; i < 2; i++) {
            int scrollSpeed = 4 >> i;
            int w = ((level.getWidth() * 16) - 320) / scrollSpeed + 320;
            int h = ((level.getHeight() * 16) - 240) / scrollSpeed + 240;
            Level bgLevel = BgLevelGenerator.createLevel(w / 32 + 1, h / 32 + 1, i == 0, levelType);
            bgLayer[i] = new BgRenderer(bgLevel, graphicsConfiguration, 320, 240, scrollSpeed);
        }

        double oldX = 0;
        if (mario != null) {
            oldX = mario.x;
        }

        mario = new Mario(this);

        if (oldX > level3.map.length * 16) {
            mario.x = level3.map.length * 16 + 32;
        }

        sprites.add(mario);
        startTime = 1;

        resetTime();

        tick = 0;

//        float[] zeros = {0,0,0,0,0,0,0};
//        sections.get(deathSection).setDeathEmotions(zeros);
//        sections.get(deathSection).setEmotionsBeforeDeath(zeros);
//        
        
        
        /*
         * SETS UP ALL OF THE CHECKPOINTS TO CHECK FOR SWITCHING
         */
        switchPoints = new ArrayList<Double>();

        //first pick a random starting waypoint from among ten positions
        int squareSize = 16; //size of one square in pixels
        int sections = 10;

        double startX = 32; //mario start position
        double endX = level.getxExit() * squareSize; //position of the end on the level
        //if(!isCustom && recorder==null)

        recorder.reset();
        recorder.level = level2;
        //System.out.println("\n enemies LEFT : " + recorder.level.COINS); //Sander disable
        //System.out.println("\n enemies LEFT : " + recorder.level.BLOCKS_COINS);
        //System.out.println("\n enemies LEFT : " + recorder.level.BLOCKS_POWER);
        gameStarted = false;
        

        
        
    }

    public void sectionCalculations() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        //System.out.println(calendar.getTimeInMillis() / 1000);
        //get current subsection (paris)
        double mariox = (double) mario.x;
        int currentSection = getCurrentSectionType((int) mariox / 16);
        //System.out.println("s1 : " + currentSection);
        //System.out.println(currentSection);
        
        //if mario is in a valid section.
        if (currentSection != -1) {
            //check if the section has already started. if not, set its startTime 
            if (!sections.get(currentSection).isHasStarted()) {
                //      System.out.println("section" + sections.get(currentSection).getId() + "has started");
                sections.get(currentSection).setStartTime(calendar.getTimeInMillis() / 1000);
                sections.get(currentSection).setHasStarted(true);
            }

            //if mario has already completed a similar segment
            if (sections.get(currentSection).isHasEnded()) {
                if (!sections.get(currentSection).isHasStarted2()) {
                    //       System.out.println("section2 " + sections.get(currentSection).getId() + " has started");
                    sections.get(currentSection).setStartTime2(calendar.getTimeInMillis() / 1000);
                    sections.get(currentSection).setHasStarted2(true);
                }
            }

            //if game passed into a new section, set the end time for the past section.
            if (currentSection != previousSection) {
                if (previousSection == -1) {
                    //        System.out.println("First section ended.");
                    previousSection = currentSection;
                } else {
                    //set the end time of the previous section
                    if (!sections.get(previousSection).isHasEnded()) {
//                        System.out.println("section" + sections.get(previousSection).getId() + " has ended.");
                        sections.get(previousSection).setEndTime(calendar.getTimeInMillis() / 1000);
                        sections.get(previousSection).setHasEnded(true);
                        previousSection = currentSection;

                    } //if the previous has ended, it means that we are in the 2nd segment
                    else {
                        if (!sections.get(previousSection).isHasEnded2()) {
                            //   System.out.println("section2 " + sections.get(previousSection).getId() + " has ended.");
                            sections.get(previousSection).setHasEnded2(true);
                            sections.get(previousSection).setEndTime2(calendar.getTimeInMillis() / 1000);
                            previousSection = currentSection;
                        }
                    }
                }
            }
            //check if we reached the end of segment (currentSection==-1)
        } else {
            if (previousSection != -1) {
                //check if we are in the first or 2nd segment.
                if (!sections.get(previousSection).isHasEnded()) {
                    sections.get(previousSection).setHasEnded(true);
                    sections.get(previousSection).setEndTime(calendar.getTimeInMillis() / 1000);
                    //     System.out.println("section " + sections.get(previousSection).getId() + " has finished");
                    previousSection = -1;
                } //we are in the second segment.
                else if (!sections.get(previousSection).isHasEnded2()) {
                    sections.get(previousSection).setHasEnded2(true);
                    sections.get(previousSection).setEndTime2(calendar.getTimeInMillis() / 1000);
                    //    System.out.println("section2 : " + sections.get(previousSection).getId() + " has ended");
                    previousSection = -1;
                }

            }
        }
    }

    public void readEmotions() {

        /**
         * emotions is a size[7] float probability array. 0:neutral 1:happy
         * 2:surprised 3:angry 4:disgusted 5:afraid 6:sad
         *
         * startEndTimes is a 2d array that should contain the start and end
         * time in milliseconds for each section of the game.
         *
         * allGameEmotions is a list that contains all the emotion vectors for
         * the whole durations of the segment.
         * 
         * 
         * 
         * NEW : ALSO READS YAW PITCH AND ROLL (with this sequence)
         * 
         */
        float[] emotions = new float[7];
        double[][] startEndTimes = new double[5][4];
        ArrayList<float[]> allGameEmotions = new ArrayList<float[]>();

        //fill the startEndTimes array
        for (SectionOfGame section : sections) {
            startEndTimes[section.getId()][0] = section.getStartTime();
            startEndTimes[section.getId()][1] = section.getEndTime();
            startEndTimes[section.getId()][2] = section.getStartTime2();
            startEndTimes[section.getId()][3] = section.getEndTime2();
        }

        int counter = 0;
        try {
            //read the file generated by inSight line by line.
            String currentLine;
            int a = 0;
            //System.out.println(System.getProperty("user.dir"));
            BufferedReader br = new BufferedReader(new FileReader("../../../output/emotions.txt"));
            try {
                while ((currentLine = br.readLine()) != null) {

                    if (currentLine.startsWith("---")) {
                        //read timestamp
                        String timeStampS = currentLine.substring(7, currentLine.length());
                        Double temp = Double.valueOf(timeStampS);
                        double timeStamp = temp.doubleValue();
                        a = getSectionFromTimeStamp(startEndTimes, timeStamp);
                        this.readGaze=false;

                    } else if(this.readGaze==false){
                        //convert string to float and add it to the correct position.
                            emotions[counter] += Float.parseFloat(currentLine);
                            counter++;
                        
                        
                        //if counter exceeds array dimensions
                        if (counter >6) {
//                            float t = Float.valueOf(currentLine);
//                            System.out.println("last line   "+t);
                            if (a != -1) {
                                sections.get(a).addEmotions(emotions);
                                sections.get(a).increaseTimes();

                                float[] temp = new float[7];
                                for (int p = 0; p < emotions.length; p++) {
                                    temp[p] = emotions[p];
                                }
                                //add emotion to the total list
                                allGameEmotions.add(temp);
                                
                                   /**
                                    * gaze matrix = [Yaw, Pitch, Roll]
                                    */
                                float[] gazeMeasurements = {0,0,0};
                                
                                for(int kk=0;kk<3;kk++){
                                    currentLine = br.readLine();
                                    gazeMeasurements[kk] = Float.parseFloat(currentLine);
                                    
                                }
                                sections.get(a).addGazeMeasurements(gazeMeasurements);  
                                this.readGaze = true;
                            }

                            //reset the emotions table.
                            for (int k = 0; k < 7; k++) {
                                emotions[k] = 0;
                            }
                            counter = 0;
                        }
                    }

                }

                
                
                
                //write emotions to file
                for (int j = 0; j < allGameEmotions.size(); j++) {
                    StringBuilder line = new StringBuilder();
                    for (int i = 0; i < allGameEmotions.get(j).length; i++) {
                        line.append(String.valueOf(allGameEmotions.get(j)[i]));
                        line.append(" ");
                        //System.out.println(allGameEmotions.get(j)[i]);
                    }
                    try {
                        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("allEmotions.txt", true)));
                        out.println(line);
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                //normalize the emotions array for each section and print it to the console
                for (SectionOfGame section : sections) {
                    section.normalizeGazeMeasurements();
                    section.normalizeEmotions();
                    System.out.println("------" + section.getId() + "-------");
                    section.printEmotions();
                    section.writeSectionEmotionsToFile();
                }

                //System.out.println(System.getProperty("user.dir"));// get pwd.
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }

    }

    public int getSectionFromTimeStamp(double[][] times, double timeStamp) {
        for (int i = 0; i < 5; i++) {
            if ((timeStamp >= times[i][0] && timeStamp < times[i][1]) || (timeStamp >= times[i][2] && timeStamp < times[i][3])) {
                return i;
            }
        }
        return -1;
    }
}
