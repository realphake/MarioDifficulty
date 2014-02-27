package ch.idsia.mario.engine;

import Architect.Architect;
import ch.idsia.ai.agents.Agent;
import ch.idsia.ai.agents.human.CheaterKeyboardAgent;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;
import ch.idsia.tools.EvaluationInfo;
import ch.idsia.tools.GameViewer;
import ch.idsia.tools.tcp.ServerAgent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.image.VolatileImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class MarioComponent extends JComponent implements Runnable, /*KeyListener,*/ FocusListener, Environment {
    private static final long serialVersionUID = 790878775993203817L;
    public static final int TICKS_PER_SECOND = 24;

    private boolean running = false;
    private int width, height;
    private GraphicsConfiguration graphicsConfiguration;
    private Scene scene;
    private boolean focused = false;

    int frame;
    int delay;
    Thread animator;

    private int ZLevelEnemies = 1;
    private int ZLevelSceneTest = 1;

    public void setGameViewer(GameViewer gameViewer) {
        this.gameViewer = gameViewer;
    }

    private GameViewer gameViewer = null;

    public Agent agent = null;
    private CheaterKeyboardAgent cheatAgent = null;

    private KeyAdapter prevHumanKeyBoardAgent;
    private Mario mario = null;
    private LevelSceneTest levelScene = null;
                    public long startTime;
                    public float time;
                    private boolean paused = false; 
    public MarioComponent(int width, int height) {
        adjustFPS();

        this.setFocusable(true);
        this.setEnabled(true);
        this.width = width;
        this.height = height;
        
        Dimension size = new Dimension(width, height);

        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);

        setFocusable(true);

        if (this.cheatAgent == null)
        {
            this.cheatAgent = new CheaterKeyboardAgent();
            this.addKeyListener(cheatAgent);
        }        

        GlobalOptions.registerMarioComponent(this);
        
    }

    public void adjustFPS() {
        int fps = GlobalOptions.FPS;
        delay = (fps > 0) ? (fps >= GlobalOptions.InfiniteFPS) ? 0 : (1000 / fps) : 100;
//        System.out.println("Delay: " + delay);
    }

    public void paint(Graphics g) {
    }

    public void update(Graphics g) {
    }

    public void init() {
        graphicsConfiguration = getGraphicsConfiguration();
//        if (graphicsConfiguration != null) {
            Art.init(graphicsConfiguration);
//        }
    }
    
               
                    public void pause()
                    {
                        paused = true;
                        
                       
                       
                        
                    }
                    
                    public void resume()
                    {
                        paused = false;
                        
                    }

    public void start() {
        
        if (!running) {
            running = true;
            animator = new Thread(this, "Game Thread");
            animator.start();
        }
    }

    public void stop() {
        running = false;
    }

    public void run() {

    }

    public EvaluationInfo run1(int currentTrial, int totalNumberOfTrials) {
        
       
        running = true;
        adjustFPS();
        EvaluationInfo evaluationInfo = new EvaluationInfo();

        VolatileImage image = null;
        Graphics g = null;
        Graphics og = null;

        image = createVolatileImage(320, 240);
        g = getGraphics();
        og = image.getGraphics();

        if (!GlobalOptions.VisualizationOn) {
            String msgClick = "Vizualization is not available";
            drawString(og, msgClick, 160 - msgClick.length() * 4, 110, 1);
            drawString(og, msgClick, 160 - msgClick.length() * 4, 110, 7);
        }

        addFocusListener(this);

        // Remember the starting time
        long tm = System.currentTimeMillis();
        long tick = tm;
        int marioStatus = Mario.STATUS_RUNNING;

        mario = ((LevelSceneTest) scene).mario;
        int totalActionsPerfomed = 0;
// TODO: Manage better place for this:
        Mario.resetCoins();

        while (/*Thread.currentThread() == animator*/ running) {
            
                if (paused)
                                  
            {
                levelScene.paused = true;
            } else  levelScene.paused = false;
            // Display the next frame of animation.
//                repaint();
            scene.tick();
            if (gameViewer != null && gameViewer.getContinuousUpdatesState())
                gameViewer.tick();

            float alpha = 0;

//            og.setColor(Color.RED);
            if (GlobalOptions.VisualizationOn) {
                og.fillRect(0, 0, 320, 240);
                scene.render(og, alpha);
            }

            if (agent instanceof ServerAgent && !((ServerAgent) agent).isAvailable()) {
                System.err.println("Agent became unavailable. Simulation Stopped");
                running = false;
                break;
            }

            boolean[] action = agent.getAction(this/*DummyEnvironment*/);
            if (action != null)
            {
                for (int i = 0; i < Environment.numberOfButtons; ++i)
                    if (action[i])
                    {
                        ++totalActionsPerfomed;
                        break;
                    }
            }
            else
            {
                System.err.println("Null Action received. Skipping simulation...");
                stop();
            }


            //Apply action;
//            scene.keys = action;
            ((LevelSceneTest) scene).mario.keys = action;
            ((LevelSceneTest) scene).mario.cheatKeys = cheatAgent.getAction(null);

            if (GlobalOptions.VisualizationOn) {

                String msg = "Agent: " + agent.getName();
                LevelSceneTest.drawStringDropShadow(og, msg, 0, 7, 5);

                msg = "Selected Actions: ";
                LevelSceneTest.drawStringDropShadow(og, msg, 0, 8, 6);

                msg = "";
                if (action != null)
                {
                    for (int i = 0; i < Environment.numberOfButtons; ++i)
                        msg += (action[i]) ? scene.keysStr[i] : "      ";
                }
                else
                    msg = "NULL";                    
                drawString(og, msg, 6, 78, 1);

                if (!this.hasFocus() && tick / 4 % 2 == 0) {
                    String msgClick = "CLICK TO PLAY";
//                    og.setColor(Color.YELLOW);
//                    og.drawString(msgClick, 320 + 1, 20 + 1);
                    drawString(og, msgClick, 160 - msgClick.length() * 4, 110, 1);
                    drawString(og, msgClick, 160 - msgClick.length() * 4, 110, 7);
                }
                og.setColor(Color.DARK_GRAY);
                LevelSceneTest.drawStringDropShadow(og, "FPS: ", 32, 2, 7);
                LevelSceneTest.drawStringDropShadow(og, ((GlobalOptions.FPS > 99) ? "\\infty" : GlobalOptions.FPS.toString()), 32, 3, 7);

                msg = totalNumberOfTrials == -2 ? "" : currentTrial + "(" + ((totalNumberOfTrials == -1) ? "\\infty" : totalNumberOfTrials) + ")";

                LevelSceneTest.drawStringDropShadow(og, "Trial:", 33, 4, 7);
                LevelSceneTest.drawStringDropShadow(og, msg, 33, 5, 7);

                if (width != 320 || height != 240) {
                        g.drawImage(image, 0, 0, 640 * 2, 480 * 2, null);
                } else {
                    g.drawImage(image, 0, 0, null);
                }
            } else {
                // Win or Die without renderer!! independently.
                marioStatus = ((LevelSceneTest) scene).mario.getStatus();
                if (marioStatus != Mario.STATUS_RUNNING)
                    stop();
            }
            // Delay depending on how far we are behind.
            if (delay > 0)
                try {
                    tm += delay;
                    Thread.sleep(Math.max(0, tm - System.currentTimeMillis()));
                } catch (InterruptedException e) {
                    break;
                }
            // Advance the frame
            frame++;
        }
//=========
        evaluationInfo.agentType = agent.getClass().getSimpleName();
        evaluationInfo.agentName = agent.getName();
        evaluationInfo.marioStatus = mario.getStatus();
        evaluationInfo.livesLeft = mario.lives;
        evaluationInfo.lengthOfLevelPassedPhys = mario.x;
        evaluationInfo.lengthOfLevelPassedCells = mario.mapX;
        evaluationInfo.totalLengthOfLevelCells = levelScene.level.getWidthCells();
        evaluationInfo.totalLengthOfLevelPhys = levelScene.level.getWidthPhys();
        evaluationInfo.timeSpentOnLevel = levelScene.getStartTime();
        evaluationInfo.timeLeft = levelScene.getTimeLeft();
        evaluationInfo.totalTimeGiven = levelScene.getTotalTime();
        evaluationInfo.numberOfGainedCoins = Mario.coins;
//        evaluationInfo.totalNumberOfCoins   = -1 ; // TODO: total Number of coins.
        evaluationInfo.totalActionsPerfomed = totalActionsPerfomed; // Counted during the play/simulation process
        evaluationInfo.totalFramesPerfomed = frame;
        evaluationInfo.marioMode = mario.getMode();
        evaluationInfo.killsTotal = mario.world.killedCreaturesTotal;
//        evaluationInfo.Memo = "Number of attempt: " + Mario.numberOfAttempts;
        if (agent instanceof ServerAgent && mario.keys != null /*this will happen if client quits unexpectedly in case of Server mode*/)
            ((ServerAgent)agent).integrateEvaluationInfo(evaluationInfo);
        return evaluationInfo;
    }

    private void drawString(Graphics g, String text, int x, int y, int c) {
        char[] ch = text.toCharArray();
        for (int i = 0; i < ch.length; i++) {
            g.drawImage(Art.font[ch[i] - 32][c], x + i * 8, y, null);
        }
    }

    public void startLevel(long seed, int difficulty, int type, int levelLength, int timeLimit) {
        scene = new LevelSceneTest(graphicsConfiguration, this, seed, difficulty, type);
        
      
        levelScene = ((LevelSceneTest) scene);
        scene.init();
    }
    
    public void startLevel(long seed, int difficulty, int type, int levelLength, int timeLimit , Architect arch) {
        scene = new LevelSceneTest(graphicsConfiguration, this, seed, difficulty, type);
        
      
        levelScene = ((LevelSceneTest) scene);
        levelScene.arch = arch;
        scene.init();
    }

    public void levelFailed() {
//        scene = mapScene;
        //Mario.lives--;
        stop();
    }

    public void focusGained(FocusEvent arg0) {
        focused = true;
    }

    public void focusLost(FocusEvent arg0) {
        focused = false;
    }

    public void levelWon() {
        stop();
//        scene = mapScene;
//        mapScene.levelWon();
    }

    public void toTitle() {
//        Mario.resetStatic();
//        scene = new TitleScene(this, graphicsConfiguration);
//        scene.init();
    }

    public List<String> getTextObservation(boolean Enemies, boolean LevelMap, boolean Complete, int ZLevelMap, int ZLevelEnemies) {
        if (scene instanceof LevelSceneTest)
            return ((LevelSceneTest) scene).LevelSceneAroundMarioASCII(Enemies, LevelMap, Complete, ZLevelMap, ZLevelEnemies);
        else {
            return new ArrayList<String>();
        }
    }

    public String getBitmapEnemiesObservation()
    {
        if (scene instanceof LevelSceneTest)
            return ((LevelSceneTest) scene).bitmapEnemiesObservation(1);
        else {
            //
            return new String();
        }                
    }

    public String getBitmapLevelObservation()
    {
        if (scene instanceof LevelSceneTest)
            return ((LevelSceneTest) scene).bitmapLevelObservation(1);
        else {
            //
            return null;
        }
    }

    // Chaning ZLevel during the game on-the-fly;
    public byte[][] getMergedObservationZ(int zLevelSceneTest, int zLevelEnemies) {
        if (scene instanceof LevelSceneTest)
            return ((LevelSceneTest) scene).mergedObservation(zLevelSceneTest, zLevelEnemies);
        return null;
    }

    public byte[][] getLevelSceneTestObservationZ(int zLevelSceneTest) {
        if (scene instanceof LevelSceneTest)
            return ((LevelSceneTest) scene).levelSceneObservation(zLevelSceneTest);
        return null;
    }



    public int getKillsTotal() {
        return mario.world.killedCreaturesTotal;
    }

    public int getKillsByFire() {
        return mario.world.killedCreaturesByFireBall;
    }

    public int getKillsByStomp() {
        return mario.world.killedCreaturesByStomp;
    }

    public int getKillsByShell() {
        return mario.world.killedCreaturesByShell;
    }

    public boolean canShoot() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public byte[][] getCompleteObservation() {
        if (scene instanceof LevelSceneTest)
            return ((LevelSceneTest) scene).mergedObservation(this.ZLevelSceneTest, this.ZLevelEnemies);
        return null;
    }

    public byte[][] getEnemiesObservation() {
        if (scene instanceof LevelSceneTest)
            return ((LevelSceneTest) scene).enemiesObservation(this.ZLevelEnemies);
        return null;
    }

    public byte[][] getLevelSceneTestObservation() {
        if (scene instanceof LevelSceneTest)
            return ((LevelSceneTest) scene).levelSceneObservation(this.ZLevelSceneTest);
        return null;
    }

    public boolean isMarioOnGround() {
        return mario.isOnGround();
    }

    public boolean mayMarioJump() {
        return mario.mayJump();
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
        if (agent instanceof KeyAdapter) {
            if (prevHumanKeyBoardAgent != null)
                this.removeKeyListener(prevHumanKeyBoardAgent);
            this.prevHumanKeyBoardAgent = (KeyAdapter) agent;
            this.addKeyListener(prevHumanKeyBoardAgent);
        }
    }

    public void setMarioInvulnerable(boolean invulnerable)
    {
        Mario.isMarioInvulnerable = invulnerable;
    }

    public void setPaused(boolean paused) {
        levelScene.paused = paused;
    }

    public void setZLevelEnemies(int ZLevelEnemies) {
        this.ZLevelEnemies = ZLevelEnemies;
    }

    public void setZLevelScene(int ZLevelSceneTest) {
        this.ZLevelSceneTest = ZLevelSceneTest;
    }

    public float[] getMarioFloatPos()
    {
        return new float[]{this.mario.x, this.mario.y};
    }

    public float[] getEnemiesFloatPos()
    {
        if (scene instanceof LevelSceneTest)
            return ((LevelSceneTest) scene).enemiesFloatPos();
        return null;
    }

    public int getMarioMode()
    {
        return mario.getMode();
    }

    public boolean isMarioCarrying()
    {
        return mario.carried != null;
    }



    public byte[][] getLevelSceneObservationZ(int zLevelScene) {
        if (scene instanceof LevelScene)
            return ((LevelScene) scene).levelSceneObservation(zLevelScene);
        return null;
    }

   
    public byte[][] getEnemiesObservationZ(int zLevelEnemies) {
        if (scene instanceof LevelScene)
            return ((LevelScene) scene).enemiesObservation(zLevelEnemies);
        return null;
    }

    @Override
    public byte[][] getLevelSceneObservation() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}