package Architect;

import java.util.Random;

public class state {


	
		public int index;
		public double QValue[];
		public double PAction[];
                
                /*
                public int ODDS_STRAIGHT ; //(1-10)
                public int ODDS_HILL_STRAIGHT ;//(1-10)
                public int ODDS_TUBES ;//(1-10)
                public int ODDS_JUMP ;//(1-10)
                public int ODDS_CANNONS ;//(1-10)
                public int GAP_SIZE ; //(2-5)
                public int MAX_COINS ;//(10-100)
                public int MAX_TURTLES;                
                */
                
		/*//state
		int ODDS_STRAIGHT ;
		//action values
		//double ODDS_STRAIGHT_INC;
		//double ODDS_STRAIGHT_DEC;
		//action probabilities
		double ODDS_STRAIGHT_INC_P;
		double ODDS_STRAIGHT_DEC_P;
		
		int ODDS_HILL_STRAIGHT;
		//double ODDS_HILL_STRAIGHT_INC;
		//double ODDS_HILL_STRAIGHT_DEC;
		double ODDS_HILL_STRAIGHT_INC_P;
		double ODDS_HILL_STRAIGHT_DEC_P;
		
		int ODDS_TUBES ;
		//double ODDS_TUBES_INC;
		//double ODDS_TUBES_DEC;
		double ODDS_TUBES_INC_P;
		double ODDS_TUBES_DEC_P;
		
		int ODDS_JUMP;
		double ODDS_JUMP_INC;
		double ODDS_JUMP_DEC;
		double ODDS_JUMP_INC_P;
		double ODDS_JUMP_DEC_P;
		
		int ODDS_CANNONS;
		double ODDS_CANNONS_INC;
		double ODDS_CANNONS_DEC;
		double ODDS_CANNONS_INC_P;
		double ODDS_CANNONS_DEC_P;*/
		
		/*public int GAP_SIZE ; //(2-5)
		double GAP_SIZE_INC;
		double GAP_SIZE_DEC;
		double GAP_SIZE_INC_P;
		double GAP_SIZE_DEC_P;*/
		
		public int DIFFICULTY;//(1-10)
		public int DIFFICULTY_INC = 0;
		public int DIFFICULTY_DEC = 1;
		
		
		
		/*public int MAX_COINS ;//(1-10)
		public int MAX_COINS_INC = 2;
		public int MAX_COINS_DEC = 3;
		*/
		public int DoNothing = 2;
		
		public state()
		{
			//INITIALISE STATE
                        QValue = new double[3];
			PAction = new double[3];
			
			for( int i = 0 ; i <3 ; i++)
			{
				QValue[i] = 1;
				PAction[i] = 0.33;
			}
			
			DIFFICULTY = 1; //this is the initial difficulty, which should be overwritten quite directly -> at it seems not used anywhere in current code (i.e., levelDifficulty sets difficulty)
                        //DIFFICULTY = randomNumber(0,3); //initialise difficulty as either 0,1,2 - will be corrected to 1,4,7 in LevelSceneTest.init
                        //System.out.println("real difficulty set in state.java: " + DIFFICULTY);
			//MAX_COINS = 5;
			hash();
		}
		
        	private int randomNumber(int low, int high){
			return new Random(new Random().nextLong()).nextInt(high-low)+low;
		}		
		
		public void hash()
		{
			int A = DIFFICULTY;
			//int B = MAX_COINS;
			index = A;// * 10 + B ;
		}
		
		
		public state clone()
		{
			state temp = new state();
			temp.DIFFICULTY = this.DIFFICULTY;
			temp.DIFFICULTY_DEC = this.DIFFICULTY_DEC;
			temp.DIFFICULTY_INC = this.DIFFICULTY_INC;
			temp.DoNothing = this.DoNothing;
			temp.index = this.index;
			/*temp.MAX_COINS = this.MAX_COINS;
			temp.MAX_COINS_DEC = this.MAX_COINS_DEC;
			temp.MAX_COINS_INC = this.MAX_COINS_INC;*/
			
			for ( int i = 0 ; i < 3 ; i++)
				{
					temp.PAction[i] = this.PAction[i];
					temp.QValue[i] = this.QValue[i];
				}
			
			return temp;
					
		}
		
		
	
}
