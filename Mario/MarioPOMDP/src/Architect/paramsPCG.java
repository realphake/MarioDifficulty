/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Architect;

import java.util.Random;

/**
 *
 * @author stathis
 * We know. We can see that in the commit history. Also we don't really care.
 */
public class paramsPCG {
    
    public int width;
    public int height;
    public int seed;
    
    
    public int ODDS_STRAIGHT ; //(0-5)
    public int ODDS_HILL_STRAIGHT ;//(0-5)
    public int ODDS_TUBES ;//(0-5)
    public int ODDS_JUMP ;//(0-5)
    public int ODDS_CANNONS ;//(0-5)
    public int difficulty;

    
    
    public int GAP_SIZE; //(2-5)
    public int MAX_COINS ;//(10-100)
    public int MAX_ENEMIES;
    
    
    public double reward;
    
    Random randomGenerator = new Random();
   
    
    public  paramsPCG(){
        
        width = 7*16;
        height = 15;
        
        newSeed();
        
        //SANDER - these are the default values for the first segments
        //BLA BLA BLA
        ODDS_TUBES = 0;         //(0-5)
        ODDS_JUMP = 0;          //(0-5)       
        ODDS_HILL_STRAIGHT = 0; //(0-5)
        ODDS_CANNONS = 0;       //(0-5)       
        ODDS_STRAIGHT = 0;      //(0-5)
        
        //possibly redundant parameters
        difficulty = 1;//(0-5)       
        GAP_SIZE = 5; //(2-5)
        MAX_COINS = 5;//(10-100)
        MAX_ENEMIES = 20;
        
        
        //hill climbing params
        reward = 0;
    }  
    
    
    public void newSeed()
    {
        seed = randomGenerator.nextInt(100);
    }

    public paramsPCG copy()
    {
        paramsPCG c = new paramsPCG();
        
        c.width = width;
        c.height = height;
        c.ODDS_STRAIGHT = ODDS_STRAIGHT;
        c.ODDS_HILL_STRAIGHT = ODDS_HILL_STRAIGHT;
        c.ODDS_TUBES = ODDS_TUBES;
        c.ODDS_JUMP = ODDS_JUMP;
        c.ODDS_CANNONS = ODDS_CANNONS;
        c.GAP_SIZE = GAP_SIZE;
        c.MAX_COINS = MAX_COINS;
        c.MAX_ENEMIES = MAX_ENEMIES;
        c.difficulty = difficulty;
        c.seed = seed;
        c.reward = reward;
        
        return c;
    }
    
    public void randomizeParameters(){
        ODDS_STRAIGHT = randomGenerator.nextInt(6); //(0-5)
        ODDS_HILL_STRAIGHT = randomGenerator.nextInt(6);//(0-5)
        ODDS_TUBES = randomGenerator.nextInt(6);//(0-5)
        ODDS_JUMP = randomGenerator.nextInt(6);//(0-5)
        ODDS_CANNONS = randomGenerator.nextInt(6);//(0-5)
        //GAP_SIZE = randomGenerator.nextInt(4)+2;//(2-5) //not used, no need to update
    }
    
    public void incrementAll(){
        ODDS_STRAIGHT++;
        ODDS_HILL_STRAIGHT++;
        ODDS_TUBES++;
        ODDS_JUMP++;
        ODDS_CANNONS++;//(0-5)
        //GAP_SIZE = (GAP_SIZE +1)%4+2;//(2-5) //hier stond eerste -1 //gap size wordt niet meer gebruikt verder als het goed is
        clampValues();
    }

    public void decrementAll(){
        ODDS_STRAIGHT = (ODDS_STRAIGHT-1)%6; //(0-5)
        ODDS_HILL_STRAIGHT = (ODDS_HILL_STRAIGHT-1)%6;//(0-5)
        ODDS_TUBES = (ODDS_TUBES-1)%6;//(0-5)
        ODDS_JUMP = (ODDS_JUMP-1)%6;//(0-5)
        ODDS_CANNONS = (ODDS_CANNONS-1)%6;//(0-5)
        //GAP_SIZE = (GAP_SIZE -1)%4+2;//(2-5)
    }    
    
    public void incrementRandomorSpecific(boolean reverse, paramsPCG reverseValues, int param, boolean specific, boolean phase3, boolean decreaseOnDeath){
        // could not use the dictionary/list variable type
        // sorry for sloppy programming
        if(reverse){
            ODDS_JUMP = reverseValues.ODDS_JUMP;
            ODDS_TUBES = reverseValues.ODDS_TUBES;
            ODDS_CANNONS = reverseValues.ODDS_CANNONS;
            ODDS_STRAIGHT = reverseValues.ODDS_STRAIGHT;
            ODDS_HILL_STRAIGHT = reverseValues.ODDS_HILL_STRAIGHT;

            //printAll();
            //GAP_SIZE = reverseValues.GAP_SIZE;
        }
        
        // value to increment by
        int increment_value;
        int parameter_increment;
        if (!phase3) {
            //wide range randomization during training phase
            increment_value = randomGenerator.nextInt(6)+1;
        }
        else {
            //while in actual gameplay, increment by either 0 or 1, 
            //to balance giving the user a change to learn from his mistake, and decreasing the difficulty
            //increment_value = randomGenerator.nextInt(2);
            
            //always decrease by one;
            increment_value = 1;
            //System.out.println("-phase3 increment_value: " + increment_value);
        }
        
        // parameter to be incremented
        if (specific) {
            parameter_increment = param;
            
            if (phase3) {
                //how to decrement at Mario death during Online Personalisation (Phase3)
                
                if (decreaseOnDeath) {
                    // Decrease parameter values for specific content on Mario Death - for adaption in CORRECT direction
                    switch(parameter_increment){
                       case 0: ODDS_STRAIGHT = ODDS_STRAIGHT - increment_value; break;
                       case 1: ODDS_HILL_STRAIGHT = ODDS_HILL_STRAIGHT - increment_value; break;
                       case 2: ODDS_TUBES = ODDS_TUBES - increment_value; break;
                       case 3: ODDS_JUMP = ODDS_JUMP - increment_value; break;
                       case 4: ODDS_CANNONS = ODDS_CANNONS - increment_value; break;
                       case 5: GAP_SIZE = increment_value; 
                    }
                }
                else {
                    // Increase parameter values for specific content on Mario Death - for adaptation in WRONG direction
                    switch(parameter_increment){
                       case 0: ODDS_STRAIGHT = ODDS_STRAIGHT + increment_value; break;
                       case 1: ODDS_HILL_STRAIGHT = ODDS_HILL_STRAIGHT + increment_value; break;
                       case 2: ODDS_TUBES = ODDS_TUBES + increment_value; break;
                       case 3: ODDS_JUMP = ODDS_JUMP + increment_value; break;
                       case 4: ODDS_CANNONS = ODDS_CANNONS + increment_value; break;
                       case 5: GAP_SIZE = increment_value; 
                    }                    
                }
            }
            else {
                //how George coded the decrement for the training phase
                 switch(parameter_increment){
                    case 0: ODDS_STRAIGHT = increment_value = randomGenerator.nextInt(ODDS_STRAIGHT); break;
                    case 1: ODDS_HILL_STRAIGHT = increment_value = randomGenerator.nextInt(ODDS_HILL_STRAIGHT); break;
                    case 2: ODDS_TUBES = increment_value = randomGenerator.nextInt(ODDS_TUBES); break;
                    case 3: ODDS_JUMP = increment_value = randomGenerator.nextInt(ODDS_JUMP); break;
                    case 4: ODDS_CANNONS = increment_value = randomGenerator.nextInt(ODDS_CANNONS); break;
                    case 5: GAP_SIZE = increment_value; 
                 }
            }

             System.out.println("-decremented parameter " + parameter_increment + " by " + increment_value);
        }
        else {            
            parameter_increment = randomGenerator.nextInt(5);
            switch(parameter_increment){
                case 0: ODDS_STRAIGHT = increment_value; break;
                case 1: ODDS_HILL_STRAIGHT = increment_value; break;
                case 2: ODDS_TUBES = increment_value; break;
                case 3: ODDS_JUMP = increment_value; break;
                case 4: ODDS_CANNONS = increment_value; break;
                //case 5: GAP_SIZE = increment_value; 
            }

            System.out.println("-changed parameter " + parameter_increment + " to " + increment_value);
        }

        // clamp the values out of bounds
        clampValues();
        
        // print outcome      
        //printAll();
    }
    
    public void setAllTo(int value){
        //SANDER
        ODDS_STRAIGHT = value; //(0-5)
        ODDS_HILL_STRAIGHT = value; //(0-5)
        ODDS_TUBES = value; //(0-5)
        ODDS_JUMP = value; //(0-5)
        ODDS_CANNONS = value; //(0-5)
        GAP_SIZE = value; //(2-5)
    }
    
    public void printAll(){
    
    System.out.println("ODDS_STRAIGHT: " + ODDS_STRAIGHT);
    System.out.println("ODDS_HILL_STRAIGHT: " + ODDS_HILL_STRAIGHT);
    System.out.println("ODDS_TUBES: " + ODDS_TUBES);
    System.out.println("ODDS_JUMP: " + ODDS_JUMP);
    System.out.println("ODDS_CANNONS: " + ODDS_CANNONS);
    
    
    }
    
    public void clampValues(){
        if (ODDS_STRAIGHT > 5){
            ODDS_STRAIGHT = 5; //(0-5)
        } else if (ODDS_STRAIGHT < 0){
            ODDS_STRAIGHT = 0; //(0-5)
        }
        if (ODDS_HILL_STRAIGHT > 5){
            ODDS_HILL_STRAIGHT = 5; //(0-5)
        } else if (ODDS_HILL_STRAIGHT < 0){
            ODDS_HILL_STRAIGHT = 0; //(0-5)
        }
        if (ODDS_TUBES > 5){
            ODDS_TUBES = 5; //(0-5)
        } else if (ODDS_TUBES < 0){
            ODDS_TUBES = 0; //(0-5)
        }
        if (ODDS_JUMP > 5){
            ODDS_JUMP = 5; //(0-5)
        } else if (ODDS_JUMP < 0){
            ODDS_JUMP = 0; //(0-5)
        }
        if (ODDS_CANNONS > 5){
            ODDS_CANNONS = 5; //(0-5)
        } else if (ODDS_CANNONS < 0){
            ODDS_CANNONS = 0; //(0-5)
        }
        if (GAP_SIZE > 5){
            GAP_SIZE = 5; //(2-5)
        } else if (GAP_SIZE < 2){
            GAP_SIZE = 2; //(2-5)
        }
    }
    
    public int[] getSettingsInt(){
        return new int[]{   ODDS_STRAIGHT,
                            ODDS_HILL_STRAIGHT,
                            ODDS_TUBES,
                            ODDS_JUMP,
                            ODDS_CANNONS,
                            //GAP_SIZE
                        };
    }
    
    public double[] getSettingsDouble(){
        return new double[]{ODDS_STRAIGHT,
                            ODDS_HILL_STRAIGHT,
                            ODDS_TUBES,
                            ODDS_JUMP,
                            ODDS_CANNONS,
                            //GAP_SIZE
                            };
    }
    
    public void setSettingsInt(int[] settings){
        ODDS_STRAIGHT = settings[0];
        ODDS_HILL_STRAIGHT = settings[1];
        ODDS_TUBES = settings[2];
        ODDS_JUMP = settings[3];
        ODDS_CANNONS = settings[4];
        //GAP_SIZE = settings[5];
        clampValues();
    }
    
    public void adjustSettingsInt(int[] settings){
        ODDS_STRAIGHT += settings[0];
        ODDS_HILL_STRAIGHT += settings[1];
        ODDS_TUBES += settings[2];
        ODDS_JUMP += settings[3];
        ODDS_CANNONS += settings[4];
        //GAP_SIZE += settings[5];
        clampValues();
    }
}
