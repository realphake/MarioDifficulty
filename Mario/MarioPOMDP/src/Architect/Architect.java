/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Architect;

import Onlinedata.MainSendRequest;
import Statistics.WekaFunctions;
import dk.itu.mario.MarioInterface.GamePlay;
import java.util.ArrayList;
import java.util.Random;
import org.zeromq.ZMQ;

/**
 *
 * @author stathis
 */
public class Architect {

    //Reward
    public double REWARD;
    public double Reward;
    public double Reward_old;
    public int started = 0;
    //Observations
    public GamePlay Obs;

    //hill climbing parameters
    public int re = 50; //Probability the champion is re-evaluated
    public boolean smart_exploration = true;
    public double rs = 1;
    public double[] s;
    public double[] dk;
    public double f;

    public double dimensions = 8;
    public double prob = 1.0 / dimensions;
    public double[] i = {prob, prob, prob, prob, prob, prob, prob, prob};
    public double[] direction = {1, 1, 1, 1, 1, 1, 1, 1};
    public double stepSize;
    public double alpha = 0.8;
    
    // Hill climbing Linear Regression
    public int chunksGenerated = 0;
    public int epsilon = 100;
    public double difficultyAdjustment;

    //level generation parameters
    public ArrayList<paramsPCG> paramHistory;
    public paramsPCG params_new;
    public paramsPCG params_old;
    public paramsPCG params_champion;

    public int first_time = 1;
    //helpers
    Random randomGenerator = new Random();
    WekaFunctions sFunctions = new WekaFunctions();

    ZMQ.Context context = ZMQ.context(1);
    ZMQ.Socket socket = context.socket(ZMQ.REP);
    public int count = 10;
    public int small_count;
    public int type;
    public double[] reward_weights;
    public double reward_label;

    public Architect() {
        params_new = new paramsPCG();
        params_old = new paramsPCG();
        params_champion = new paramsPCG();
    }
    
    public Architect(boolean training, MainSendRequest request) {
        params_new = new paramsPCG();
        params_old = new paramsPCG();
        params_champion = new paramsPCG();
        
        if (!training){
            request.downloadData("trainingfile.arff");
            sFunctions.loadTrainInstance(request.download);
            sFunctions.buildLRcls();
            // or load basic model
            //sFunctions.loadModel("../../MAINOOR/traindata/LinRegressionModel.model");
        }
    }

    public paramsPCG paramsfromstring(String spoint) {
        paramsPCG p = new paramsPCG();
        String[] parts = spoint.split(" ");
        p.ODDS_STRAIGHT = (int) (5 * Double.parseDouble(parts[0]) + 1);
        p.ODDS_HILL_STRAIGHT = (int) (5 * Double.parseDouble(parts[1]) + 1);
        p.ODDS_TUBES = (int) (5 * Double.parseDouble(parts[2]) + 1);
        p.ODDS_JUMP = (int) (5 * Double.parseDouble(parts[3]) + 1);
        p.ODDS_CANNONS = (int) (5 * Double.parseDouble(parts[4]) + 1);
        p.GAP_SIZE = (int) (5 * Double.parseDouble(parts[5]) + 1);

        return p;

    }
    
    public int[] changeParamsBasedOnStats( double currentDiffEstimate ){
        System.out.println("Estimate difficulty and change parameters");
        System.out.println("Difficulty adjustment before: "+difficultyAdjustment);
        // Get ready for a lot of ugly if statements
        // experienced = 0.1    Guess based on personal experience
        // average = 0.3        Guess based on personal experience
        // We estimate how well the player goes past the level
        // if totallefttime is very small the player is considered experienced
        double diffEstimate = 0;
        int[] paramchanges = new int[6];
        double[] lrRatio = new double[5];
        double[] runPerc = new double[5];
        int[] deaths = new int[5];
        lrRatio[0] = (double)Obs.totalLeftTimeStraight/
                                Obs.totalRightTimeStraight;
        lrRatio[1] = (double)Obs.totalLeftTimeHills/
                                Obs.totalRightTimeHills;
        lrRatio[2] = (double)Obs.totalLeftTimeTubes/
                                Obs.totalRightTimeTubes;
        lrRatio[3] = (double)Obs.totalLeftTimeJump/
                                Obs.totalRightTimeJump;
        lrRatio[4] = (double)Obs.totalLeftTimeCannons/
                                Obs.totalRightTimeCannons;
        // We estimate willingness of risk taking by runtime percentage
        // strong risk taking cutoff is 0.8
        runPerc[0]  = (double)Obs.totalRunTimeStraight/
                                (Obs.totalLeftTimeStraight+
                                Obs.totalRightTimeStraight);
        runPerc[1]  = (double)Obs.totalRunTimeHills/
                                (Obs.totalLeftTimeHills+
                                Obs.totalRightTimeHills);
        runPerc[2]  = (double)Obs.totalRunTimeTubes/
                                (Obs.totalLeftTimeTubes+
                                Obs.totalRightTimeTubes);
        runPerc[3]  = (double)Obs.totalRunTimeJump/
                                (Obs.totalLeftTimeJump+
                                Obs.totalRightTimeJump);
        runPerc[4]  = (double)Obs.totalRunTimeCannons/
                                (Obs.totalLeftTimeCannons+
                                Obs.totalRightTimeCannons);
        // Deaths per section
        deaths[0] = Obs.timesOfDeathByArmoredTurtle+
                    Obs.timesOfDeathByGoomba+
                    Obs.timesOfDeathByGreenTurtle+
                    Obs.timesOfDeathByRedTurtle;
        deaths[1] = deaths[0];
        deaths[2] = Obs.timesOfDeathByJumpFlower+Obs.timesOfDeathByChompFlower;
        deaths[3] = (int)Obs.timesOfDeathByFallingIntoGap;
        deaths[4] = Obs.timesOfDeathByCannonBall;
        
        // Dying to one type with low risk taking should result in a decrease
        // of that parameter
        // while a lrRatio of 0.1 or less should increase by 2. except when the player was small a lot
        // and a lrRatio of 0.1-0.3 should increase by 1. except when the player was small a lot
        // lrRatio of > 0.3 and low running perc and death decreases the difficulty by a lot
        // ******** needs improvement
        // more subtle changes, 
        for (int i = 0;i<5;i++){
            if (lrRatio[i] < 0.1){
                if(runPerc[i] > 0.8 && deaths[i] == 0) paramchanges[i] = 2;
                else if (deaths[i] == 0) paramchanges[i] = 1;
                else if (deaths[i] > 0) paramchanges[i] = -1;
            } else if (lrRatio[i] < 0.3){
                if(runPerc[i] > 0.8 && deaths[i] == 0) paramchanges[i] = 1;
                else if (deaths[i] > 0) paramchanges[i] = -1;
                else paramchanges[i] = 0;
            } else {
                if(runPerc[i] > 0.8 && deaths[i] == 0) paramchanges[i] = 1;
                else if (deaths[i] == 0) paramchanges[i] = 0;
                else if (runPerc[i] > 0.8 && deaths[i] > 0) paramchanges[i] = -1;
                else if (deaths[i] > 0) paramchanges[i] = -2;
            }
            diffEstimate += 0.25 * paramchanges[i];
            
        }
        System.out.println("Parameter changes = "+  paramchanges[0]+", "+
                                                    paramchanges[1]+", "+
                                                    paramchanges[2]+", "+
                                                    paramchanges[3]+", "+
                                                    paramchanges[4]+", "+
                                                    paramchanges[5]);
        difficultyAdjustment = (diffEstimate + currentDiffEstimate) * 0.5; //update belief for difficulty adjustment
        System.out.println("Difficulty adjustment after: "+difficultyAdjustment);
        return paramchanges;
    }
    
    public int[] findBestEstimate(){
        System.out.println("Finding best estimate for adjustment "+difficultyAdjustment+".");
        double[] bestResult = new double[6];
        double[] currentSettings = params_new.getSettingsDouble();
        double target = 3+difficultyAdjustment;
        double minDifference = 10;
        double diff;
        
        // Basic limited grid search for now
        // should have some sort of gradient optimization for larger areas
        // or a direct lookup from weka (don't know if it exists)
        for (int i = 0;i<(int)(difficultyAdjustment*6);i++){
            if (difficultyAdjustment > 0 && currentSettings[i%6] != 5){
                //positive difficultyAdjustment
                currentSettings[i%6]+= 1;
            } else if (difficultyAdjustment < 0 && currentSettings[i%6] != 1){
                // negative difficultyAdjustment
                currentSettings[i%6]-= 1;
            }
            // get the predicted values for difficulty
            diff = Math.abs(target - sFunctions.predict(currentSettings));
            if(diff < minDifference){
                minDifference = diff;
                bestResult = currentSettings.clone();
            }
        }
        
        return new int[]{   (int)bestResult[0],
                            (int)bestResult[1],
                            (int)bestResult[2],
                            (int)bestResult[3],
                            (int)bestResult[4],
                            (int)bestResult[5]};
    }

    public void update(boolean training) {
        // Updates 'arch.params_new' with new parameters to explore in the training phase
        
        // Estimate difficulty offset -GO (Gradient Optimization of any type)
        // Determine Explore/Exploit -EE
        // IF train:
        //      explore with a certain pattern, maybe startpoint and a pattern based on that
        params_old = params_new.copy();
        if (training) {
            
            // test repo passwrd
            // if he chose the current level increment all otherwise increment a random parameter by a random value
            if(this.Obs.better == 1 && this.first_time > 0 )
                params_new.incrementAll();
            else params_new.incrementRandom();
            //params_new.setAllTo(1); //test for effect
            //params_new.randomizeParameters();
            this.first_time++;
            /*
            if (chunksGenerated % 6 == 0) {
                params_new.randomizeParameters();
            } else {
                params_new.incrementAll();
            }
            */
        } else {
        // IF online: epsilon greedy
            // Estimate challenge
            int[] paramchanges = changeParamsBasedOnStats(difficultyAdjustment);
            // Exploit estimation 
            if ((difficultyAdjustment >= 1 || difficultyAdjustment <= -1) 
                    && randomGenerator.nextInt(100) < epsilon){
                params_new.setSettingsInt(findBestEstimate());
            } else {
            // otherwise Explore
                params_new.adjustSettingsInt(paramchanges);
            }
        }
        chunksGenerated++;
        
                 

        // Update the reward given the latest observations
        // note : the observations gets updated externaly in the LevelSceneTest Class at every swap()
        //getAppropriatenessToUser(); 
        //paramHistory.add(params_new.copy());
            /*
         Reward = 0;
            
         double [] rewards = {0 ,0 ,0 ,0 ,0 ,0};
         for (int i = 0 ; i<5 ; i++)
         {
         if(i == reward_label)
         {
         rewards[i] = reward_weights[i] * 0.66;
         }
         else if(i == reward_label + 1 || i == reward_label - 1)
         {
         rewards[i] = reward_weights[i] * 0.33;
         }
         else rewards[i] = 0;
                
         Reward += rewards[i];
                
         }
         System.out.println("generating new segment");
         System.out.println(Reward);
         */
        params_new.newSeed();
            //params_new = getBayesOptNextStep();

            //hillClimb();
    }

    public paramsPCG getBayesOptNextStep() {
        paramsPCG params = new paramsPCG();
        //First send the reward for the last step
        String request = Double.toString(Reward);
        System.out.println("Sending Reward Feedback to BayesOpt...");
        socket.send(request.getBytes(), 0);

        byte[] reply = socket.recv(0);
        String spoint = new String(reply);
        System.out.println("Received next Point of Interest : " + spoint);

                //params = paramsfromstring(spoint);
        return params;
    }

    public void hillClimb() {

        paramsPCG params = new paramsPCG();
        int hsize = paramHistory.size();

        if (re < randomGenerator.nextInt(100) && (hsize > 0)) {
            historyGetChampion();
            params = params_champion.copy();
        } else {
            //Choose the starting point of the next exploration step
            if (hsize > 0) {
                if (Reward < params_old.reward) {
                    params = params_old.copy();
                } else {
                    params = mutate(params_old, alpha);
                }
                params_old = params.copy();
                paramHistory.add(params.copy());
            }

            //Take next exploration step
            if (smart_exploration) {
                i = setSmartMutationProbabilityPerElement();
                direction = setSmartDirection();
                stepSize = 1 - params.reward;
            } else {
                i = setUniformMutationProbability();
                direction = setProbabilisticDirection();
                stepSize = rs;
            }
            params_new = mutate(params, 1);

        }
    }

    public double[] setSmartMutationProbabilityPerElement() {
        double dimensions = 8;
        double prob = 1.0 / dimensions;
        double[] i = {prob, prob, prob, prob, prob, prob, prob, prob};
        return i;
    }

    public double[] setUniformMutationProbability() {
        double dimensions = 8;
        double prob = 1.0 / dimensions;
        double[] i = {prob, prob, prob, prob, prob, prob, prob, prob};
        return i;
    }

    public double[] setSmartDirection() {
        double[] direction = {1, 1, 1, 1, 1, 1, 1, 1};
        return direction;
    }

    public double[] setProbabilisticDirection() {
        double[] direction = {1, 1, 1, 1, 1, 1, 1, 1};
        return direction;
    }

    public paramsPCG mutate(paramsPCG params, double factor) {

        paramsPCG p = params.copy();

        if (randomGenerator.nextInt(100) < i[0]) {
            p.ODDS_STRAIGHT += direction[0] * stepSize * factor;
        }
        if (randomGenerator.nextInt(100) < i[1]) {
            p.ODDS_HILL_STRAIGHT += direction[1] * stepSize * factor;
        }
        if (randomGenerator.nextInt(100) < i[2]) {
            p.ODDS_TUBES += direction[2] * stepSize * factor;
        }
        if (randomGenerator.nextInt(100) < i[3]) {
            p.ODDS_JUMP += direction[3] * stepSize * factor;
        }
        if (randomGenerator.nextInt(100) < i[4]) {
            p.ODDS_CANNONS += direction[4] * stepSize * factor;
        }
        if (randomGenerator.nextInt(100) < i[5]) {
            p.GAP_SIZE += direction[5] * stepSize * factor;
        }
        if (randomGenerator.nextInt(100) < i[6]) {
            p.MAX_COINS += direction[6] * stepSize * factor;
        }
        if (randomGenerator.nextInt(100) < i[7]) {
            p.MAX_ENEMIES += direction[7] * stepSize * factor;
        }
        if (randomGenerator.nextInt(100) < i[8]) {
            p.difficulty += direction[8] * stepSize * factor;
        }

        return p;
    }

    public void heuristic_update() {
        System.out.println(Obs.jumpsNumber);
        params_new.seed = Obs.jumpsNumber;
//           
//                p.newVectorCount += 1; //a new vector is added
//                    newPlayValues[0] += -0.5+recorder.tr();
//                    newPlayValues[1] += 0.5-recorder.tr();
//                    newPlayValues[2] += recorder.getKills(SpriteTemplate.CHOMP_FLOWER)-recorder.getDeaths(SpriteTemplate.CHOMP_FLOWER)*5;
//                    newPlayValues[3] += recorder.J()-recorder.dg();
//                    newPlayValues[4] += recorder.getKills(SpriteTemplate.CANNON_BALL)-recorder.getDeaths(SpriteTemplate.CANNON_BALL)*5;//todo get kill ratio instead of kills
//                    int totalkills = 0;
//                    for(int i=0;i<4;i++)//variables for enemies
//                    {
//                        totalkills += recorder.getKills(i)*3 - recorder.getDeaths(i)*2;
//                        int kills = recorder.getKills(i)*3-recorder.getDeaths(i)*2;
//                        newPlayValues[6+i] += kills;
//                    }
//                    newPlayValues[5] += totalkills*3-recorder.dop();

    }

    public void getAppropriatenessToUser() {
        Reward_old = Reward;
        Reward = 10;
    }

    public void historyGetChampion() {
        int i = 0;
        params_champion = paramHistory.get(i);
    }

    public void close_socket() {
        socket.close();
        context.term();
    }

    public void init_socket() {
        // Socket to talk to server
        System.out.println("Connecting to hello world server");
        socket.bind("tcp://*:5555");
        byte[] reply = socket.recv(0);
        String spoint = new String(reply);
        System.out.println("Received next Point of Interest" + spoint);

        params_new = paramsfromstring(spoint);

    }
}
