package Architect;

public class ARCH_MESSAGE {
	
	public double REWARD; //0.0 +
        public int DIFFICULTY ;//(1-10)
    	public int ODDS_STRAIGHT ; //(1-10)
	public int ODDS_HILL_STRAIGHT ;//(1-10)
	public int ODDS_TUBES ;//(1-10)
	public int ODDS_JUMP ;//(1-10)
	public int ODDS_CANNONS ;//(1-10)
	public int GAP_SIZE ; //(2-5)
	public int MAX_COINS ;//(10-100)
	public int MAX_TURTLES;
	public double Reward;
	public state states[];
        //public int state[];
        public int state;
        public double trans[][];
        public int bestAction;
        public double bestActionProb;      
        //public double playerModel[];
	
	public ARCH_MESSAGE()
	{
		//playerModel = new double[3];
                states = new state[3]; //old - from stahis
                //state = new int[2]; //game state sander
                state = 4;
                trans = new double[3][3]; //transition probabilities               
                bestAction = 1;
                bestActionProb = 0;

                REWARD = 0.0;
		 //DIFFICULTY = 1;//(1-10) //do not init, set elsewhere
		 ODDS_STRAIGHT = 5  ; //(1-10)
		 ODDS_HILL_STRAIGHT = 5;//(1-10)
		 ODDS_TUBES = 5 ;//(1-10)
		 ODDS_JUMP = 5;//(1-10)
		 ODDS_CANNONS = 5;//(1-10)
		 GAP_SIZE = 5; //(2-5)
		 MAX_COINS = 5;//(10-100)
		 MAX_TURTLES = 10;
	}

}
