/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Architect;

import ch.idsia.mario.MarioInterface.GamePlay;
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
        
        //Observations
        public GamePlay Observations;
       
        //hill climbing parameters
        public int re = 50; //Probability the champion is re-evaluated
        public boolean smart_exploration = true;
        public double rs = 1;
        public double[] s;
        public double[] dk;
        public double f;
              
        public double dimensions = 6;
        public double prob = 1.0/dimensions;
        public double[] i = {prob , prob ,prob , prob ,prob, prob};
        public double[] direction = {1 ,-1} ;
        public double stepSize;
        public double alpha = 0.8;
        
        //level generation parameters
        public ArrayList<paramsPCG> paramHistory = new ArrayList<paramsPCG>();
        public paramsPCG params_new;
        public paramsPCG params_old;
        public paramsPCG params_champion;
        
        public int first = 1;
        //helpers
        Random randomGenerator = new Random();
        
        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket socket = context.socket(ZMQ.REP);
        public int count=2000;
        public int small_count = 0;
        public int type;
        public double[] reward_weights;
        public int reward_label;
        double [] rewards = {0.0 ,0.33 ,1 ,0.33 ,0.0 };
        public String[] types  = {"coin" , "enemy"};
	
	public Architect()
	{
		params_new = new paramsPCG();
                params_old = new paramsPCG();
                params_champion = new paramsPCG();
                    
                // Socket to talk to server
                //init_socket();
        }
        
        
        public paramsPCG paramsfromstring(String spoint)
        {
            paramsPCG p = new paramsPCG();
            String[] parts = spoint.split(" ");
            p.ODDS_STRAIGHT =  (int) (Double.parseDouble(parts[0]));
            p.ODDS_HILL_STRAIGHT =  (int) (Double.parseDouble(parts[1]));
            p.ODDS_TUBES =  (int) (Double.parseDouble(parts[2]));
            p.ODDS_JUMP =  (int) (Double.parseDouble(parts[3]));
            p.ODDS_CANNONS =  (int) (Double.parseDouble(parts[4]));
            p.difficulty =  (int) (Double.parseDouble(parts[5]));
            
            return p;
            
        }
        public void update()
        {
                        
            // Update the reward given the latest observations
            // note : the observations get updated externaly in the LevelSceneTest Class at every swap()
            //getAppropriatenessToUser(); 
            //paramHistory.add(params_new.copy());
            
            
            
            if(reward_label>5)reward_label = 5;
            if(reward_label<1)reward_label = 1;
            //System.out.println(reward_label);
            Reward =rewards[reward_label-1];
                        
            
//            for (int i = 0 ; i<5 ; i++)
//            {
//                if(i == reward_label){
//                    rewards[i] = reward_weights[i] * 0.66;
//                }
//                else if(i == reward_label +1 || i == reward_label - 1)
//                {
//                    rewards[i] = reward_weights[i] * 0.33;
//                }
//                else rewards[i] = 0;
//                
//                Reward += rewards[i];
//                
//            }
            //System.out.println("generating new segment");
            //System.out.println(Reward);
            params_new.newSeed();
            //params_new = hillClimb();
            //params_new = getBayesOptNextStep();
            
            //System.out.println(params_new.ODDS_STRAIGHT + " | " + params_new.ODDS_HILL_STRAIGHT + " | " + params_new.ODDS_TUBES + " | " + params_new.ODDS_JUMP + " | " + params_new.ODDS_CANNONS + " | " + params_new.difficulty + " | ");


            //hillClimb();
            
        }
        
        public paramsPCG getBayesOptNextStep()
        {
                paramsPCG params = new paramsPCG();
                //First send the reward for the last step
                String request = Double.toString(-Reward);
                System.out.println("Sending Reward Feedback to BayesOpt...:" + -Reward);
                socket.send(request.getBytes (), 0);
                
                
                byte[] reply = socket.recv(0);
                //String spoint = "1 1 1 1 1 1";
                
                String spoint =  new String(reply);
                params = paramsfromstring(spoint);
                //System.out.println("Received next Point of Interest : " + spoint);
                
                
                
                return params;
        }
               
                
        public paramsPCG hillClimb()
        {
                  
            paramsPCG params = new paramsPCG();
            paramHistory.add(params_new.copy());
            int hsize = paramHistory.size();
            
            if( re < randomGenerator.nextInt(100) && (hsize>0) ){
                historyGetChampion();
                params = params_champion.copy();
            }
            else{
                //Choose the starting point of the next exploration step
                if( hsize>0 ){
                    if(Reward < params_old.reward){
                        params = params_old.copy();
                    }
                    else{
                        System.out.println("Print params new " + params_new.ODDS_STRAIGHT);      
                        params = mutateNew(params_old,params_new,alpha);
                        params_old = params_new.copy();
                        paramHistory.add(params.copy());
                    }

                }
                
                //Take next exploration step
                if(smart_exploration){
                    System.out.println("Clled smart exploration");
                    i = setSmartMutationProbabilityPerElement();
                    direction = setSmartDirection();
                    stepSize = 1- params.reward;
                }
                else{
                    System.out.println("Clled old exploration");
                    i = setUniformMutationProbability();
                    direction = setProbabilisticDirection();
                    stepSize = rs;
                }
                
                params_new = mutate(params_new,1);
                  
                            
            }
            //System.out.println("Print params new " + params.ODDS_STRAIGHT);
            if(params_new.ODDS_STRAIGHT > 5)
                params_new.ODDS_STRAIGHT = 5;
            if(params_new.ODDS_STRAIGHT < 1)
                params_new.ODDS_STRAIGHT = 1;
            if(params_new.ODDS_HILL_STRAIGHT > 5)
                params_new.ODDS_HILL_STRAIGHT = 5;
            if(params_new.ODDS_HILL_STRAIGHT < 1)
                params_new.ODDS_HILL_STRAIGHT = 1;
            if(params_new.ODDS_TUBES > 5)
                params_new.ODDS_TUBES = 5;
            if(params_new.ODDS_TUBES < 1)
                params_new.ODDS_TUBES = 1;
            if(params_new.ODDS_JUMP > 5)
                params_new.ODDS_JUMP = 5;
            if(params_new.ODDS_JUMP < 1)
                params_new.ODDS_JUMP = 1;
            if(params_new.ODDS_CANNONS > 5)
                params_new.ODDS_CANNONS = 5;
            if(params_new.ODDS_CANNONS < 1)
                params_new.ODDS_CANNONS = 1;
            if(params_new.difficulty > 5)
                params_new.difficulty = 5;
            if(params_new.difficulty < 1)
                params_new.difficulty = 1;
            
            return params_new;
        }
        
        
        public double[] setSmartMutationProbabilityPerElement()
        {
            double dimensions = 6;
            double prob = 1.0/dimensions;
            double[] i = {prob , prob ,prob , prob ,prob , prob };
            return i;
        }
        
        public double[] setUniformMutationProbability()
        {
            double dimensions = 6;
            double prob = 1.0/dimensions;
            double[] i = {prob , prob ,prob , prob ,prob , prob};
            return i;
        }
        
        public double[] setSmartDirection()
        {
            double[] direction = {1 , -1};
            return direction;
        }
        
        public double[] setProbabilisticDirection()
        {
            double[] direction = {1 ,-1} ;
            return direction;
        }
        
              
        
        public paramsPCG mutate(paramsPCG params , double factor)
        {
            
            paramsPCG p = params.copy();
            
            int dir = randomGenerator.nextInt(2);
            if(randomGenerator.nextInt(100)<i[0]*100)
                p.ODDS_STRAIGHT += Math.round(direction[dir]*stepSize*factor);
            dir = randomGenerator.nextInt(2);
            if(randomGenerator.nextInt(100)<i[1]*100){
                p.ODDS_HILL_STRAIGHT += Math.round(direction[dir]*stepSize*factor);
            }
            dir = randomGenerator.nextInt(2);
            if(randomGenerator.nextInt(100)<i[2]*100)
                p.ODDS_TUBES += Math.round(direction[dir]*stepSize*factor);
            dir = randomGenerator.nextInt(2);
            if(randomGenerator.nextInt(100)<i[3]*100)
                p.ODDS_JUMP += Math.round(direction[dir]*stepSize*factor);
            dir = randomGenerator.nextInt(2);
            if(randomGenerator.nextInt(100)<i[4]*100)
                p.ODDS_CANNONS += Math.round(direction[dir]*stepSize*factor);
            dir = randomGenerator.nextInt(2);
            if(randomGenerator.nextInt(100)<i[5]*100)
                p.difficulty += Math.round(direction[dir]*stepSize*factor);
            
            return p;
        }
        
        public paramsPCG mutateNew(paramsPCG params_old , paramsPCG params_new, double factor )
        {
            
            paramsPCG p = params_old.copy();
       
            
                //System.out.println("Old p value = " + p.ODDS_STRAIGHT);
                p.ODDS_STRAIGHT +=  Math.round((params_new.ODDS_STRAIGHT - params_old.ODDS_STRAIGHT)*factor);
                //System.out.println("new val should be added " + (params_new.ODDS_STRAIGHT - params_old.ODDS_STRAIGHT)*factor );
                
                
                p.ODDS_HILL_STRAIGHT += Math.round((params_new.ODDS_HILL_STRAIGHT - params_old.ODDS_HILL_STRAIGHT)*factor);
            
                p.ODDS_TUBES += Math.round((params_new.ODDS_TUBES - params_old.ODDS_TUBES)*factor);
            
                p.ODDS_JUMP += Math.round((params_new.ODDS_JUMP - params_old.ODDS_JUMP)*factor);
            
                p.ODDS_CANNONS += Math.round((params_new.ODDS_CANNONS - params_old.ODDS_CANNONS)*factor);

            
                p.difficulty += Math.round((params_new.difficulty - params_old.difficulty)*factor);
            
            return p;
        }
        
        public void heuristic_update()
        {
            System.out.println(Observations.jumpsNumber);
            params_new.seed = Observations.jumpsNumber;
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
        
        public void getAppropriatenessToUser()
        {
            Reward_old = Reward;
            Reward = 10;
        }
        
        public void historyGetChampion()
        {
            int i = 0;
            params_champion = paramHistory.get(i);
        }
        
         
        public void close_socket()
        {
            socket.close();
            context.term();
        }
        
        public void init_socket()
        {
            // Socket to talk to server
                System.out.println("Connecting to hello world server");
                socket.bind ("tcp://*:5555");
                byte[] reply = socket.recv(0);
                String spoint = new String(reply);
                System.out.println("Received next Point of Interest" + spoint);
                
                params_new = paramsfromstring(spoint);
                
        }
}



        
       


    


