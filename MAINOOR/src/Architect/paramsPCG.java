/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Architect;

import java.util.Random;

/**
 *
 * @author stathis
 */
public class paramsPCG {
    
    public int width;
    public int height;
    public int seed;
    
    
    public int ODDS_STRAIGHT ; //(1-5)
    public int ODDS_HILL_STRAIGHT ;//(1-5)
    public int ODDS_TUBES ;//(1-5)
    public int ODDS_JUMP ;//(1-5)
    public int ODDS_CANNONS ;//(1-5)
    public int difficulty;//(1-5);
    
    
    public int GAP_SIZE ; //(2-5)
    public int MAX_COINS ;//(10-100)
    public int MAX_ENEMIES;
    public String type;
    
    
    public double reward;
    
    Random randomGenerator = new Random();
   
    
    public  paramsPCG(){
        
        width = 50;
        height = 15;
        
        newSeed();
        
        ODDS_STRAIGHT = 5  ; //(1-5)
        ODDS_HILL_STRAIGHT = 5;//(1-5)
        ODDS_TUBES = 5 ;//(1-5)
        ODDS_JUMP = 1;//(1-5)
        ODDS_CANNONS = 2;//(1-5)
        difficulty = 2;//(1-5)
        
        GAP_SIZE = 5; //(2-5)
        MAX_COINS = 5;//(10-100)
        MAX_ENEMIES = 15;
        
        
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
    
    
    
}
