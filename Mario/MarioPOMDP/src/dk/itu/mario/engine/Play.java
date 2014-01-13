package dk.itu.mario.engine;
import java.awt.*;

import javax.swing.*;

public class Play {
	    
	public static void main(String[] args)
	    {

	    	JFrame frame = new JFrame("Mario POMDP");
	    	MarioComponent mario = new MarioComponent(640, 480, false);

                // Get a DifficultyRecorder instance
                DifficultyRecorder dr = DifficultyRecorder.getInstance();
                // Set current game frame
                dr.setFrame(frame);
                // Append or prepend difficulty data to the POMDP string
                dr.setLogStrategy(DifficultyRecorder.LOG_APPEND);
                // Should the difficulty be recorded each time mario dies?
                dr.setRecordAfterDeath(false);
                // Set current game thread
                dr.setMariocomponent(mario);
                
	    	frame.setContentPane(mario);
	    	frame.setResizable(false);
	        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        frame.pack();

	        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	        frame.setLocation((screenSize.width-frame.getWidth())/2, (screenSize.height-frame.getHeight())/2);

	        frame.setVisible(true);

	        mario.start();
	   
	}

}
