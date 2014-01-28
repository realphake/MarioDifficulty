package dk.itu.mario.engine;

import java.awt.*;

import javax.swing.*;


// DOES NOT WORK!!!
public class PlayApplet extends JApplet {

    MarioComponent mario;
//empty constructor

    public PlayApplet() {
        System.out.println("Java");
    }

    //init
    public void init() {
        mario = new MarioComponent(640, 480, false);
        // Get a DifficultyRecorder instance
        DifficultyRecorder dr = DifficultyRecorder.getInstance();
        // Set current game frame
        // Append or prepend difficulty data to the POMDP string
        dr.setLogStrategy(DifficultyRecorder.LOG_APPEND);
        // Should the difficulty be recorded each time mario dies?
        dr.setRecordAfterDeath(false);
        // Set current game thread
        dr.setMariocomponent(mario);
        getContentPane().add(new Panel().add(mario));
        
        //setContentPane(mario);

    }

    //start
    public void start() {
        System.out.println("Applet starting");
        mario.start();
    }

    //stop
    public void stop() {
        System.out.println("Applet stopping");
    }

    //destroy
    public void destroy() {
        System.out.println("Applet destroyed");
        System.exit(0);
    }

    //Panel
    public class panel extends JPanel {

        public panel() {
            //this is where the diplay items go
        }
    }

    public static void main(String[] args) {

        JFrame frame = new JFrame("Mario POMDP");
        frame.setSize(640, 480);
        PlayApplet   p = new PlayApplet();

        p.init();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((screenSize.width - frame.getWidth()) / 2, (screenSize.height - frame.getHeight()) / 2);

        p.start();
        frame.setVisible(true);

    }
}
