package Architect;

public class coefficients {
	
	//FUN
	
		//number of times the player kicked an opponent shell, ns (try over total number of shells)
		public double n_s = 0.345/1.871;
		// number of coin blocks pressed over the total number of coin blocks existent in the level, ncb
		public double n_cb = 0.311/1.871; 
		//total number of kills over total number of opponents, kT
		public double k_T = 0.311/1.871;
		//number of times the run button was pressed, nr
		public double n_r = 0.253/1.871;
		//percentage of time that the player is moving left, tL
		public double t_L = 0.237/1.871;
		//number of opponent kills minus number of deaths caused by opponents, kP
		public double k_P = 0.222/1.871;
		//percentage of time that the player is running, tr.
		public double t_r = 0.192/1.871;
		
		
		
		// CHALENGE
		// number of powerup blocks pressed over the total number of powerup blocks existent in the level
		public double n_p  = -0.480;
		// number of times the player was killed by jumping	into a gap over the total number of deaths
		public double d_j  =  0.469;
		// number of times the player was killed by jumping into a gap
		public double d_g  =  0.447;
		// number of jumps 	over gaps or without any purpose (e.g. to collect an item, to kill an opponent),
		//public double J_prime = 'N/A';
		// difference between J' (jumps over gaps or without any purpose) and the number of gaps G
		public double J_d  =  0.439;
		// average width of gaps
		public double EG_w =  0.409;
		// number of ducks
		public double n_d  = -0.368;
		// playing duration of last life over total time spent on the level
		public double t_ll = -0.312;
		// number of gaps
		public double G    = -0.287;
		
			
		//FRUSTRATION
		public double C = -0.826;
		// n_c number of coins collected over the total number of coins existent in the level
		public double n_c = -0.511;
		// number of collected items (coins, destroyed blocks and powerups) over total items existent in the level
		public double n_I = -0.544;
		// percentage of time that the player: is standing still
		public double t_s =  0.520;
		// number of opponents died from fire-shots over the total number of kills
		public double k_f = -0.515;

		public double fr_n_p = -0.815;
		public double fr_n_cb = -0.688;

}
