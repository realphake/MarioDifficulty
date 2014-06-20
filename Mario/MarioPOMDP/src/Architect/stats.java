package Architect;

import dk.itu.mario.MarioInterface.GamePlay;
import java.util.Random;
import dk.itu.mario.MarioInterface.*;

public class stats {
	
	//FUN
	
			//number of times the player kicked an opponent shell, ns
			public double n_s;
			// number of coin blocks pressed over the total number of coin blocks existent in the level, ncb
			public double n_cb ; 
			//total number of kills over total number of opponents, kT
			public double k_T ;
			//number of times the run button was pressed, nr
			public double n_r ;
			//percentage of time that the player is moving left, tL
			public double t_L;
			
			// NOT TESTED
			//number of opponent kills minus number of deaths caused by opponents, kP
			public double k_P;
			
			
			//percentage of time that the player is running, tr.
			public double t_r;
			
			
			
			// CHALENGE
			// number of powerup blocks pressed over the total number of powerup blocks existent in the level
			public double n_p ;
			
			public double C;
			// NOT TESTED
			// number of times the player was killed by jumping	into a gap over the total number of deaths
			public double d_j ;
			// number of times the player was killed by jumping into a gap
			public double d_g ;
			// number of jumps 	over gaps or without any purpose (e.g. to collect an item, to kill an opponent),
			//public double J_prime = 'N/A';
			// difference between J' (jumps over gaps or without any purpose) and the number of gaps G
			public double J_d ;
			// average width of gaps
			public double EG_w ;
			
			
			
			// number of ducks
			public double n_d  ;
			// playing duration of last life over total time spent on the level
			
			// ???????
			public double t_ll;
			
			
			// number of gaps
			public double G   ;
			
				
			//FRUSTRATION
			// n_c number of coins collected over the total number of coins existent in the level
			public double n_c ;
			// number of collected items (coins, destroyed blocks and powerups) over total items existent in the level
			public double n_I ;
			// percentage of time that the player: is standing still
			public double t_s ;
			// number of opponents died from fire-shots over the total number of kills
			public double k_f;
			
			
			
			
			public void fill(GamePlay gmp)
			{
				//This function will translate 10 kind of gameplay statistics to player expreience ,
				//which will the be passed on to Calculate Reward.
				
				
				// FUN
				//number of times the player kicked an opponent shell, ns
				this.n_s = gmp.kickedShells;
				// number of coin blocks pressed over the total number of coin blocks existent in the level, ncb
				this.n_cb = gmp.percentageCoinBlocksDestroyed;
				//total number of kills over total number of opponents, kT
				this.k_T = gmp.k_T;
				//number of times the run button was pressed, nr
				this.n_r = gmp.timesPressedRun;
				//percentage of time that the player is moving left, tL
				this.t_L = gmp.t_L;
				//number of opponent kills minus number of deaths caused by opponents, kP
				this.k_P = gmp.k_P;
				//percentage of time that the player is running, tr.
				this.t_r = gmp.t_r;
				
				
				
				// CHALENGE
				// number of powerup blocks pressed over the total number of powerup blocks existent in the level
				this.n_p  = gmp.percentagePowerBlockDestroyed;
				// number of times the player was killed by jumping	into a gap over the total number of deaths
				this.d_j  = gmp.d_j;
				// number of times the player was killed by jumping into a gap
				this.d_g  = gmp.timesOfDeathByFallingIntoGap;
				// number of jumps 	over gaps or without any purpose (e.g. to collect an item, to kill an opponent),
				//this.J_prime = 'N/A';
				// difference between J' (jumps over gaps or without any purpose) and the number of gaps G
				this.J_d  =  0.439;
				// average width of gaps
				this.EG_w =  0.409;
				// number of ducks
				this.n_d  = gmp.duckNumber;
				// playing duration of last life over total time spent on the level
				this.t_ll = gmp.succesfulRunTime;
				// number of gaps
				this.G    = -0.287;
				
								
					
				//FRUSTRATION
				// n_c number of coins collected over the total number of coins existent in the level
				this.n_c = gmp.coinsCollected;
				// number of collected items (coins, destroyed blocks and powerups) over total items existent in the level
				this.n_I = gmp.n_I;
				// percentage of time that the player: is standing still
				this.t_s = gmp.t_s;
				// number of opponents died from fire-shots over the total number of kills
				this.k_f = gmp.k_f;
			}
			
			
			public stats clone()
			{
				stats temp = new stats();
				// FUN
				//number of times the player kicked an opponent shell, ns
				temp.n_s = this.n_s;
				// number of coin blocks pressed over the total number of coin blocks existent in the level, ncb
				temp.n_cb = this.n_cb ;
				//total number of kills over total number of opponents, kT
				temp.k_T = this.k_T;
				//number of times the run button was pressed, nr
				temp.n_r = this.n_r;
				//percentage of time that the player is moving left, tL
				temp.t_L = this.t_L;
				//number of opponent kills minus number of deaths caused by opponents, kP
				temp.k_P = this.k_P;
				//percentage of time that the player is running, tr.
				temp.t_r = this.t_r;
				
				
				
				// CHALENGE
				// number of powerup blocks pressed over the total number of powerup blocks existent in the level
				temp.n_p  = this.n_p;
				// number of times the player was killed by jumping	into a gap over the total number of deaths
				temp.d_j  = this.d_j ;
				// number of times the player was killed by jumping into a gap
				temp.d_g  = this.d_g ;
				// number of jumps 	over gaps or without any purpose (e.g. to collect an item, to kill an opponent),
				//this.J_prime = 'N/A';
				// difference between J' (jumps over gaps or without any purpose) and the number of gaps G
				temp.J_d  = this.J_d ;
				// average width of gaps
				temp.EG_w =  this.EG_w;
				// number of ducks
				temp.n_d  = this.n_d ;
				// playing duration of last life over total time spent on the level
				temp.t_ll = this.t_ll;
				// number of gaps
				temp.G    = this.G;
				
					
				//FRUSTRATION
				// n_c number of coins collected over the total number of coins existent in the level
				temp.n_c = this.n_c ;
				// number of collected items (coins, destroyed blocks and powerups) over total items existent in the level
				temp.n_I = this.n_I ;
				// percentage of time that the player: is standing still
				temp.t_s = 	this.t_s ;
				// number of opponents died from fire-shots over the total number of kills
				temp.k_f = this.k_f ;
				
				
				return temp;
			}
			
			
			

}
