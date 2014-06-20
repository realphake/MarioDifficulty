package dk.itu.mario.engine;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextField;

/*
 * DifficultyRecorder class.
 * Used to prompt the player for demographic questions and how he perceives
 * difficulty through the game.
 */
public class DifficultyRecorder {

    private JFrame frame;
    private MarioComponent mariocomponent; // Stored to access game thread variables
    private boolean recordAfterDeath = false; // Record after each death?
    private int logStrategy = 1;
    private boolean finished = true; // Variable used to wait in the main thread
    private long startTime = 0;
    public int age = 0;
    public int gender = 0;
    public String nationality;
    public int hasPlayedVideoGame = 0;
    public int hasPlayedMarioBefore = 0;
    public int hoursPerWeek = 0;
    public int engagement = 0;
    public int boredom = 0;
    public int frustration = 0;
    public int apathy = 0;
    public int flow = 0;
    public int challenge = 0;
    public int better = 0;
    public static final int LOG_APPEND = 0;
    public static final int LOG_PREPEND = 1;

    private DifficultyRecorder() {
    }

    /* 
     * Private intern class only loaded into memory once at the first call.
     * Thread safe.
     */
    private static class DifficultyRecorderHolder {

        private final static DifficultyRecorder instance = new DifficultyRecorder();
    }

    public static DifficultyRecorder getInstance() {
        return DifficultyRecorderHolder.instance;
    }

    public JFrame getFrame() {
        return frame;
    }

    public void removeMinMaxClose(Component comp) {
        if (comp instanceof AbstractButton) {
            comp.getParent().remove(comp);
        }
        if (comp instanceof Container) {
            Component[] comps = ((Container) comp).getComponents();
            for (int x = 0, y = comps.length; x < y; x++) {
                removeMinMaxClose(comps[x]);
            }
        }
    }

    public void setFrame(JFrame frame) {
        this.frame = frame;
    }

    public void setLogStrategy(int logStrategy) {
        this.logStrategy = logStrategy;
    }

    public void setRecordAfterDeath(boolean recordAfterDeath) {
        this.recordAfterDeath = recordAfterDeath;
    }

    public boolean isRecordAfterDeath() {
        return recordAfterDeath;
    }

    public MarioComponent getMariocomponent() {
        return mariocomponent;
    }

    public void setMariocomponent(MarioComponent mariocomponent) {
        this.mariocomponent = mariocomponent;
    }

    public boolean isFinished() {
        return finished;
    }

    /*
     * Append or preprend (depending on the logstrategy)
     * saved metrics to the existing pomdp string.
     */
    public String fillPOMDPMetrics(String pomdp, boolean putFirstQuestions) {
        String tmp = "";

        if (putFirstQuestions) {
            tmp += age + ", ";
            tmp += nationality + ", ";
            tmp += gender + ", ";
            tmp += hasPlayedVideoGame + ", ";
            tmp += hasPlayedMarioBefore + ", ";
            tmp += hoursPerWeek;
        }

        if (putFirstQuestions) {
            tmp += ", ";
        }
        tmp += engagement + ", ";
        tmp += frustration + ", ";
        tmp += challenge + ", ";
        tmp += better;
        //tmp += challenge;

        if (logStrategy == LOG_APPEND) {
            pomdp += ", " + tmp;
        } else {
            pomdp = tmp + ", " + pomdp;
        }

        return pomdp;
    }

    /*
     * Create a new window to ask the questions
     * If true then ask demographic questions, if false then ask difficulty
     * questions.
     */
    public void startRecordDifficulty(boolean isFirstQuestions) {
        finished = false;
        startTime = System.nanoTime(); // Beginning of the recording

        // Create new frame on top of the main window
        JFrame tempFrame = new JFrame("Measuring Perceived Difficulty");
        tempFrame.setUndecorated(true);
        DisplayerComponent dc = new DisplayerComponent(800, 600);

        //this.mariocomponent.pause();
        setFrame(tempFrame);
        tempFrame.setContentPane(dc);
        tempFrame.setResizable(false);
        //tempFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        tempFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Do nothing. User has to submit his answers.
            }
        });
        tempFrame.pack();

        if (isFirstQuestions) {
            //loadFirstQuestions(dc);
            loadSwapLevelQuestions(dc, true);
        } else {
            loadSwapLevelQuestions(dc, false);
        }

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        tempFrame.setLocation((screenSize.width - frame.getWidth()) / 2, (screenSize.height - frame.getHeight()) / 2);
        tempFrame.setVisible(true);
    }

    /*
     * Stop recording difficulty. Destroy the window and resume the game.
     */
    private void stopRecordDifficulty() {
        System.out.println("-finished recording game experience");
        //System.out.println("-nationality=" + nationality + " age=" + age + " gender=" + gender
        //        + " hasPlayedVideoGame=" + hasPlayedVideoGame + " hasPlayedMarioBefore=" + hasPlayedMarioBefore
        //        + " hoursPerWeek=" + hoursPerWeek + " engagement=" + engagement
        //        + " frustration=" + frustration + " challenge=" + challenge);
        //System.out.println("-engagement=" + engagement + " frustration=" + frustration + " challenge=" + challenge);
        System.out.println("-challenge = " + challenge + ", better = " + better);
        this.frame.dispose();

        /* It's not possible to stop the game's timer...
         * We trick it by saving how long the game was stopped and add this
         * time to the startTime mainthread variable. The tick counter is therefor
         * almost unchanged and shouldn't disrupt the gameplay.
         */
        this.mariocomponent.startTime += (System.nanoTime() - startTime);
        this.mariocomponent.time = (System.nanoTime() - this.mariocomponent.startTime) / 1000000000f;
        this.mariocomponent.resume();
        finished = true;
    }

    /*
     * Method in charge of displaying demographic questions in the newly
     * created window 'dc'.
     */
    private void loadFirstQuestions(DisplayerComponent dc) {
        JButton buttonSubmit = new JButton("Submit");

        JLabel labelGreetings = new JLabel("Hey there! Before playing,");
        JLabel labelGreetings2 = new JLabel("please answer these questions and click submit.");
        JLabel labelNationality = new JLabel("Nationality:");
        JLabel labelAge = new JLabel("Age:");
        JLabel labelGender = new JLabel("Gender:");
        JLabel labelHasPlayedVideoGame = new JLabel("Have you already played video games?");
        JLabel labelHasPlayedMarioBefore = new JLabel("Have you already played Super Mario?");
        JLabel labelHoursPerWeek = new JLabel("How many hours do you generally");
        JLabel labelHoursPerWeek2 = new JLabel("spend playing video games per week?");

        final JTextField textNationality = new JTextField(10);
        final JTextField textAge = new JTextField(2);

        ButtonGroup groupGender = new ButtonGroup();
        final JRadioButton buttonGenderMale = new JRadioButton("Male");
        final JRadioButton buttonGenderFemale = new JRadioButton("Female");

        ButtonGroup groupHasPlayedVideoGame = new ButtonGroup();
        final JRadioButton buttonHasPlayedVideoGameYes = new JRadioButton("Yes");
        final JRadioButton buttonHasPlayedVideoGameNo = new JRadioButton("No");

        ButtonGroup groupHasPlayedMarioBefore = new ButtonGroup();
        final JRadioButton buttonHasPlayedMarioBeforeYes = new JRadioButton("Yes");
        final JRadioButton buttonHasPlayedMarioBeforeNo = new JRadioButton("No");

        final ButtonGroup groupHoursPerWeek = new ButtonGroup();
        final JRadioButton buttonHoursPerWeekInt1 = new JRadioButton("0-5");
        final JRadioButton buttonHoursPerWeekInt2 = new JRadioButton("5-10");
        final JRadioButton buttonHoursPerWeekInt3 = new JRadioButton("10-15");
        final JRadioButton buttonHoursPerWeekInt4 = new JRadioButton("15-20");
        final JRadioButton buttonHoursPerWeekInt5 = new JRadioButton("20+");

        GridBagConstraints c = new GridBagConstraints();

        // ---------------------------------------------------------------------
        groupGender.add(buttonGenderMale);
        groupGender.add(buttonGenderFemale);

        groupHasPlayedVideoGame.add(buttonHasPlayedVideoGameYes);
        groupHasPlayedVideoGame.add(buttonHasPlayedVideoGameNo);

        groupHasPlayedMarioBefore.add(buttonHasPlayedMarioBeforeYes);
        groupHasPlayedMarioBefore.add(buttonHasPlayedMarioBeforeNo);

        groupHoursPerWeek.add(buttonHoursPerWeekInt1);
        groupHoursPerWeek.add(buttonHoursPerWeekInt2);
        groupHoursPerWeek.add(buttonHoursPerWeekInt3);
        groupHoursPerWeek.add(buttonHoursPerWeekInt4);
        groupHoursPerWeek.add(buttonHoursPerWeekInt5);

        /*
         * Grid system. See GridBagLayout documentation.
         */
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(0, 0, 0, 0); // Padding
        dc.add(labelGreetings, c);
        c.gridy = 1;
        dc.add(labelGreetings2, c);

        c.gridx = 0;
        c.gridy = 2;
        c.insets = new Insets(25, 0, 0, 0);
        dc.add(labelNationality, c);
        c.gridx = 1;
        dc.add(textNationality, c);

        c.gridx = 0;
        c.gridy = 3;
        dc.add(labelAge, c);
        c.gridx = 1;
        dc.add(textAge, c);

        c.gridx = 0;
        c.gridy = 4;
        dc.add(labelGender, c);
        c.gridx = 1;
        c.anchor = GridBagConstraints.LINE_START; // Where the element should  be displayed in his cell
        c.insets = new Insets(15, 20, 0, 0);
        dc.add(buttonGenderMale, c);
        c.gridx = 2;
        dc.add(buttonGenderFemale, c);

        c.gridx = 0;
        c.gridy = 5;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(15, 0, 0, 0);
        dc.add(labelHasPlayedVideoGame, c);
        c.gridx = 1;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(15, 20, 0, 0);
        dc.add(buttonHasPlayedVideoGameYes, c);
        c.gridx = 2;
        dc.add(buttonHasPlayedVideoGameNo, c);

        c.gridx = 0;
        c.gridy = 6;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(15, 0, 0, 0);
        dc.add(labelHasPlayedMarioBefore, c);
        c.gridx = 1;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(15, 20, 0, 0);
        dc.add(buttonHasPlayedMarioBeforeYes, c);
        c.gridx = 2;
        dc.add(buttonHasPlayedMarioBeforeNo, c);

        c.gridx = 0;
        c.gridy = 7;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(15, 0, 0, 0);
        dc.add(labelHoursPerWeek, c);
        c.gridx = 1;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(15, 20, 0, 0);
        dc.add(buttonHoursPerWeekInt1, c);
        c.gridx = 2;
        dc.add(buttonHoursPerWeekInt2, c);
        c.gridy = 8;
        c.gridx = 0;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(0, 0, 0, 0);
        dc.add(labelHoursPerWeek2, c);
        c.gridx = 1;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(0, 20, 0, 0);
        dc.add(buttonHoursPerWeekInt3, c);
        c.gridx = 2;
        dc.add(buttonHoursPerWeekInt4, c);
        c.gridy = 9;
        c.gridx = 1;
        dc.add(buttonHoursPerWeekInt5, c);

        c.gridx = 0;
        c.gridy = 10;
        c.insets = new Insets(50, 230, 0, 0);
        dc.add(buttonSubmit, c);

        // Action when the button 'Submit' is clicked
        buttonSubmit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (textAge.getText() != null) {
                    try {
                        age = Integer.parseInt(textAge.getText());
                    } catch (Exception ex) {
                        System.out.println(ex);
                        return;
                    }
                } else {
                    return;
                }

                if (buttonGenderMale.isSelected()) {
                    gender = 0;
                } else if (buttonGenderFemale.isSelected()) {
                    gender = 1;
                } else {
                    return;
                }

                if (textNationality.getText() != null) {
                    nationality = textNationality.getText();
                } else {
                    return;
                }

                if (buttonHasPlayedVideoGameYes.isSelected()) {
                    hasPlayedVideoGame = 1;
                } else if (buttonHasPlayedVideoGameNo.isSelected()) {
                    hasPlayedVideoGame = 0;
                } else {
                    return;
                }

                if (buttonHasPlayedMarioBeforeYes.isSelected()) {
                    hasPlayedMarioBefore = 1;
                } else if (buttonHasPlayedMarioBeforeNo.isSelected()) {
                    hasPlayedMarioBefore = 0;
                } else {
                    return;
                }

                for (Enumeration<AbstractButton> buttons = groupHoursPerWeek.getElements(); buttons.hasMoreElements();) {
                    // 1 to 5 depending on the selected button
                    hoursPerWeek++;
                    AbstractButton button = buttons.nextElement();
                    if (button.isSelected()) {
                        break;
                    }
                }

                if (nationality.length() <= 2) {
                    stopRecordDifficulty();
                }
            }
        });
    }

    /*
     * Method in charge of displaying difficulty questions in the newly
     * created window 'dc'.
     */
    private void loadSwapLevelQuestions(DisplayerComponent dc, boolean firstIteration) {
        JButton buttonSubmit = new JButton("Continue...");

        JLabel labelGreetings = new JLabel("");
        JLabel labelGreetings2 = new JLabel("Please answer these questions...");
        //JLabel labelEngagement = new JLabel("");
        JLabel labelBoredom = new JLabel("Was the previous segment boring?");
        JLabel labelApathy = new JLabel("How important does it feel to play this game?");
        JLabel labelFlow = new JLabel("Do you feel like playing more?");
        JLabel labelFrustration = new JLabel("Did the game elements in the last segment frustrate you?");
        //JLabel labelChallenge = new JLabel("The challenge level of the part that I just played is...");
        //JLabel labelBetter = new JLabel("I prefer the challenge level of...");
        
        // Hashtables are obsolete collections but it won't accept a HashMap...
        Hashtable labelTableEandF = new Hashtable();
        labelTableEandF.put(new Integer(1), new JLabel(""));
        labelTableEandF.put(new Integer(3), new JLabel(""));
        labelTableEandF.put(new Integer(5), new JLabel(""));

        Hashtable labelTableChallenge = new Hashtable();
        labelTableChallenge.put(new Integer(1), new JLabel("Too easy"));
        labelTableChallenge.put(new Integer(3), new JLabel("Just right"));
        labelTableChallenge.put(new Integer(5), new JLabel("Too hard"));
        
        Hashtable labelTableBoredom = new Hashtable();
        labelTableBoredom.put(new Integer(0), new JLabel("Not Set"));
        labelTableBoredom.put(new Integer(1), new JLabel("Not at all"));
        labelTableBoredom.put(new Integer(3), new JLabel("It's OK"));
        labelTableBoredom.put(new Integer(5), new JLabel("Really boring"));
        
        Hashtable labelTableFrustration = new Hashtable();
        labelTableFrustration.put(new Integer(0), new JLabel("Not Set"));
        labelTableFrustration.put(new Integer(1), new JLabel("Not at all"));
        labelTableFrustration.put(new Integer(3), new JLabel("A fair bit"));
        labelTableFrustration.put(new Integer(5), new JLabel("Really frustrating"));
        
        Hashtable labelTableApathy = new Hashtable();
        labelTableApathy.put(new Integer(0), new JLabel("Not Set"));
        labelTableApathy.put(new Integer(1), new JLabel("Not at all"));
        labelTableApathy.put(new Integer(3), new JLabel("A fair bit"));
        labelTableApathy.put(new Integer(5), new JLabel("Really important"));
        
        Hashtable labelTableFlow = new Hashtable();
        labelTableFlow.put(new Integer(0), new JLabel("Not Set"));
        labelTableFlow.put(new Integer(1), new JLabel("Ready to quit"));
        labelTableFlow.put(new Integer(3), new JLabel("I'll keep going"));
        labelTableFlow.put(new Integer(5), new JLabel("Give me more!"));

        Hashtable labelTableBetter = new Hashtable();
        labelTableBetter.put(new Integer(0), new JLabel("The part before"));
        labelTableBetter.put(new Integer(1), new JLabel("This part"));
        
        final JSlider sliderBoredom = new JSlider(0, 5);
        sliderBoredom.setMajorTickSpacing(1);
        sliderBoredom.setPaintTicks(true);
        sliderBoredom.setLabelTable(labelTableBoredom);
        sliderBoredom.setPaintLabels(true);
        sliderBoredom.setPreferredSize(new Dimension(400, 50));
        sliderBoredom.setValue(0);
        
        final JSlider sliderFrustration = new JSlider(0, 5);
        sliderFrustration.setMajorTickSpacing(1);
        sliderFrustration.setPaintTicks(true);
        sliderFrustration.setLabelTable(labelTableFrustration);
        sliderFrustration.setPaintLabels(true);
        sliderFrustration.setPreferredSize(new Dimension(400, 50));
        sliderFrustration.setValue(0);
        
        final JSlider sliderApathy = new JSlider(0, 5);
        sliderApathy.setMajorTickSpacing(1);
        sliderApathy.setPaintTicks(true);
        sliderApathy.setLabelTable(labelTableApathy);
        sliderApathy.setPaintLabels(true);
        sliderApathy.setPreferredSize(new Dimension(400, 50));
        sliderApathy.setValue(0);
        
        final JSlider sliderFlow = new JSlider(0, 5);
        sliderFlow.setMajorTickSpacing(1);
        sliderFlow.setPaintTicks(true);
        sliderFlow.setLabelTable(labelTableFlow);
        sliderFlow.setPaintLabels(true);
        sliderFlow.setPreferredSize(new Dimension(400, 50));
        sliderFlow.setValue(0);
        /*
        final JSlider sliderChallenge = new JSlider(1, 5);
        sliderChallenge.setMajorTickSpacing(3);
        sliderChallenge.setPaintTicks(true);
        sliderChallenge.setLabelTable(labelTableChallenge);
        sliderChallenge.setPaintLabels(true);
        
        final JSlider sliderBetter = new JSlider(0, 1);
        sliderBetter.setMajorTickSpacing(1);
        sliderBetter.setPaintTicks(true);
        sliderBetter.setLabelTable(labelTableBetter);
        sliderBetter.setPaintLabels(true);
        sliderBetter.setValue(1);
        */
        GridBagConstraints c = new GridBagConstraints();

        // ---------------------------------------------------------------------
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(0, 0, 0, 0);
        dc.add(labelGreetings, c);
        c.gridy = 1;
        c.insets = new Insets(0, 0, 50, 0);
        dc.add(labelGreetings2, c);

        //c.gridy = 2;
        //c.gridx = 0;
        //c.insets = new Insets(15, 0, 0, 0);
        //dc.add(labelEngagement, c);
        //c.gridx = 1;
        //dc.add(sliderEngagement, c);
        c.gridy = 2;
        c.gridx = 0;
        dc.add(labelBoredom, c);
        c.gridy = 2;
        c.gridx = 1;
        dc.add(sliderBoredom, c);
        
        c.gridy = 3;
        c.gridx = 0;
        dc.add(labelFrustration, c);
        c.gridy = 3;
        c.gridx = 1;
        dc.add(sliderFrustration, c);
        
        c.gridy = 4;
        c.gridx = 0;
        dc.add(labelApathy, c);
        c.gridy = 4;
        c.gridx = 1;
        dc.add(sliderApathy, c);
        
        c.gridy = 5;
        c.gridx = 0;
        dc.add(labelFlow, c);
        c.gridy = 5;
        c.gridx = 1;
        dc.add(sliderFlow, c);
        
        /*
        if (firstIteration) {
            c.gridy = 6;
            c.gridx = 0;
            c.insets = new Insets(0, 0, 0, 0);        
            //c.insets = new Insets(50, 215, 0, 0);
            dc.add(buttonSubmit, c);                           
        } else {
            c.gridy = 6;
            c.gridx = 0;
            dc.add(labelBetter, c);
            c.gridy = 7;
            c.gridx = 0;
            dc.add(sliderBetter, c);

            c.gridy = 9;
            c.gridx = 0;
            c.insets = new Insets(0, 0, 0, 0);        
            //c.insets = new Insets(50, 215, 0, 0);
            dc.add(buttonSubmit, c);                       
        }*/
        
        c.gridy = 6;
        c.gridx = 0;
        c.insets = new Insets(0, 0, 0, 0);        
        //c.insets = new Insets(50, 215, 0, 0);
        dc.add(buttonSubmit, c); 


        /* //Add a hotkey for submit
         // *****************************************************
         ActionMap actionMap = new ActionMapUIResource();
         actionMap.put("action_save", new AbstractAction() {
         @Override
         public void actionPerformed(ActionEvent e) {
         System.out.println("Save action performed.");
         }
         });
         actionMap.put("action_exit", new AbstractAction() {
         @Override
         public void actionPerformed(ActionEvent e) {
         System.out.println("Exit action performed.");
         }
         });

         InputMap keyMap = new ComponentInputMap(p);
         keyMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S,
         java.awt.Event.CTRL_MASK), "action_save");
         keyMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q,
         java.awt.Event.CTRL_MASK), "action_exit");
         SwingUtilities.replaceUIActionMap(p, actionMap);
         SwingUtilities.replaceUIInputMap(p, JComponent.WHEN_IN_FOCUSED_WINDOW,
         keyMap);
         // *****************************************************
         */
        // Action when the button 'Submit' is clicked 
        buttonSubmit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                boredom = sliderBoredom.getValue();
                frustration = sliderFrustration.getValue();
                apathy = sliderApathy.getValue();
                flow = sliderFlow.getValue();
                //requires check here if values are filled in
                if (boredom != 0 && frustration != 0 && apathy != 0 && flow != 0){
                    stopRecordDifficulty();
                }
            }
        });
    }

    /*
     * Class used as the content of the newly created JFrame.
     * Uses GridBagLayout to displays the widgets.
     */
    private class DisplayerComponent extends JPanel {

        private int width, height;

        public DisplayerComponent(int width, int height) {
            this.setLayout(new GridBagLayout());
            this.setFocusable(true);
            this.setEnabled(true);
            this.width = width;
            this.height = height;
            Dimension size = new Dimension(width, height);
            setPreferredSize(size);
            setMinimumSize(size);
            setMaximumSize(size);
        }

    }
}
