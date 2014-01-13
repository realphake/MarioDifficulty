/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Architect;

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
        
        //Observations
        public GamePlay Observations;
       
        //hill climbing parameters
        public int re = 50; //Probability the champion is re-evaluated
        public boolean smart_exploration = true;
        public double rs = 1;
        public double[] s;
        public double[] dk;
        public double f;
              
        public double dimensions = 8;
        public double prob = 1.0/dimensions;
        public double[] i = {prob , prob ,prob , prob ,prob , prob ,prob , prob};
        public double[] direction = {1 ,1 ,1 ,1 ,1 ,1 ,1 ,1 } ;
        public double stepSize;
        public double alpha = 0.8;
        
        //level generation parameters
        public ArrayList<paramsPCG> paramHistory;
        public paramsPCG params_new;
        public paramsPCG params_old;
        public paramsPCG params_champion;
        
        public int first = 1;
        //helpers
        Random randomGenerator = new Random();
        
        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket socket = context.socket(ZMQ.REP);
        public int count=10;
        public int small_count;
        public int type;
        public double[] reward_weights;
        public double reward_label;
                
	
	public Architect()
	{
		params_new = new paramsPCG();
                params_old = new paramsPCG();
                params_champion = new paramsPCG();
                    
                //Socket to talk to server
                init_socket();
        }
        
        
        public paramsPCG paramsfromstring(String spoint)
        {
            paramsPCG p = new paramsPCG();
            String[] parts = spoint.split(" ");
            p.ODDS_STRAIGHT = (int) (5 * Double.parseDouble(parts[0])+1);
            p.ODDS_HILL_STRAIGHT =  (int) (5 * Double.parseDouble(parts[1])+1);
            p.ODDS_TUBES =  (int) (5 *Double.parseDouble(parts[2])+1);
            p.ODDS_JUMP =  (int) (5 *Double.parseDouble(parts[3])+1);
            p.ODDS_CANNONS = (int) (5 *Double.parseDouble(parts[4])+1);
            p.GAP_SIZE =  (int) (5 *Double.parseDouble(parts[5])+1);
            
            return p;
            
        }
        public void update()
        {
                        
            // Update the reward given the latest observations
            // note : the observations get updated externaly in the LevelSceneTest Class at every swap()
            //getAppropriatenessToUser(); 
            //paramHistory.add(params_new.copy());
            Reward = 0;
            
            double [] rewards = {0 ,0 ,0 ,0 ,0 ,0};
            for (int i = 0 ; i<5 ; i++)
            {
                if(i == reward_label){
                    rewards[i] = reward_weights[i] * 0.66;
                }
                else if(i == reward_label +1 || i == reward_label - 1)
                {
                    rewards[i] = reward_weights[i] * 0.33;
                }
                else rewards[i] = 0;
                
                Reward += rewards[i];
                
            }
            System.out.println("generating new segment");
            System.out.println(Reward);
            params_new.newSeed();
            params_new = getBayesOptNextStep();
            
            //hillClimb();
            
        }
        
        public paramsPCG getBayesOptNextStep()
        {
                paramsPCG params = new paramsPCG();
                //First send the reward for the last step
                String request = Double.toString(Reward);
                System.out.println("Sending Reward Feedback to BayesOpt...");
                socket.send(request.getBytes (), 0);
                
                
                byte[] reply = socket.recv(0);
                String spoint = new String(reply);
                System.out.println("Received next Point of Interest : " + spoint);
                
                params = paramsfromstring(spoint);
                
                return params;
        }
               
                
        public void hillClimb()
        {
                   
            paramsPCG params = new paramsPCG();
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
                        params = mutate(params_old,alpha);
                    }
                    params_old = params.copy();
                    paramHistory.add(params.copy());
                }
                
                //Take next exploration step
                if(smart_exploration){
                    i = setSmartMutationProbabilityPerElement();
                    direction = setSmartDirection();
                    stepSize = 1- params.reward;
                }
                else{
                    i = setUniformMutationProbability();
                    direction = setProbabilisticDirection();
                    stepSize = rs;
                }
                params_new = mutate(params,1);
                            
            }
        }
        
        
        public double[] setSmartMutationProbabilityPerElement()
        {
            double dimensions = 8;
            double prob = 1.0/dimensions;
            double[] i = {prob , prob ,prob , prob ,prob , prob ,prob , prob};
            return i;
        }
        
        public double[] setUniformMutationProbability()
        {
            double dimensions = 8;
            double prob = 1.0/dimensions;
            double[] i = {prob , prob ,prob , prob ,prob , prob ,prob , prob};
            return i;
        }
        
        public double[] setSmartDirection()
        {
            double[] direction = {1 ,1 ,1 ,1 ,1 ,1 ,1 ,1 } ;
            return direction;
        }
        
        public double[] setProbabilisticDirection()
        {
            double[] direction = {1 ,1 ,1 ,1 ,1 ,1 ,1 ,1 } ;
            return direction;
        }
        
              
        
        public paramsPCG mutate(paramsPCG params , double factor)
        {
            
            paramsPCG p = params.copy();
            
            if(randomGenerator.nextInt(100)<i[0])
                p.ODDS_STRAIGHT += direction[0]*stepSize*factor;
            if(randomGenerator.nextInt(100)<i[1])
                p.ODDS_HILL_STRAIGHT += direction[1]*stepSize*factor;
            if(randomGenerator.nextInt(100)<i[2])
                p.ODDS_TUBES += direction[2]*stepSize*factor;
            if(randomGenerator.nextInt(100)<i[3])
                p.ODDS_JUMP += direction[3]*stepSize*factor;
            if(randomGenerator.nextInt(100)<i[4])
                p.ODDS_CANNONS += direction[4]*stepSize*factor;
            if(randomGenerator.nextInt(100)<i[5])
                p.GAP_SIZE += direction[5]*stepSize*factor;
            if(randomGenerator.nextInt(100)<i[6])
                p.MAX_COINS += direction[6]*stepSize*factor;
            if(randomGenerator.nextInt(100)<i[7])
                p.MAX_ENEMIES += direction[7]*stepSize*factor;
            if(randomGenerator.nextInt(100)<i[8])
                p.difficulty += direction[8]*stepSize*factor;
            
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



        
       


    


