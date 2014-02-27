    package dk.itu.mario.scene;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.*;


import Architect.ARCH_MESSAGE;
import Architect.Architect;


import dk.itu.mario.engine.Art;
import dk.itu.mario.engine.BgRenderer;
import dk.itu.mario.engine.DataRecorder;
import dk.itu.mario.engine.DifficultyRecorder;
import dk.itu.mario.engine.LevelRenderer;
import dk.itu.mario.engine.MarioComponent;
import level2.*;


import dk.itu.mario.engine.sonar.FixedSoundSource;
import dk.itu.mario.engine.sprites.*;


public class LevelScene extends Scene implements SpriteContext
{
    protected List<Sprite> sprites = new ArrayList<Sprite>();
    protected List<Sprite> spritesToAdd = new ArrayList<Sprite>();
    protected List<Sprite> spritesToRemove = new ArrayList<Sprite>();

    public Level level;
//    public Level levelTemp;
    public Mario mario;
    public float xCam, yCam, xCamO, yCamO;
    public static Image tmpImage;
    public double[][] transprobs = new double[3][3];
    public double[] playerModel = new double[3];
    
    protected int tick;

    protected LevelRenderer layer;
    protected BgRenderer[] bgLayer = new BgRenderer[2];
    protected Level currentLevel;
    protected GraphicsConfiguration graphicsConfiguration;

    public boolean paused = false;
    public int startTime = 0;
    public int timeLeft;
    public static int timeUsed;
    
    public Architect arch;

    //    private Recorder recorder = new Recorder();
    //    private Replayer replayer = null;

    protected long levelSeed;
    protected MarioComponent marioComponent;
    protected int levelType;
    protected int levelDifficulty;
    
    public double softMax_temperature;

    public static DataRecorder recorder;

    ARCH_MESSAGE m;
    public boolean gameStarted;

    public static boolean bothPlayed = false;

    private int []xPositionsArrow;
    private int []yPositionsArrow;
    private int widthArrow,heightArrow,tipWidthArrow;
    private int xArrow,yArrow;

    //Variables for storing received reward in playerModels (one for each difficulty level)
    public ArrayList playerModelDiff1 = new ArrayList(0);
    public ArrayList playerModelDiff4 = new ArrayList(0);
    public ArrayList playerModelDiff7 = new ArrayList(0);

    //Variables for tracking the difficulty level of each level segment
    public int currentLevelSegment;
    public ArrayList plannedDifficultyLevels = new ArrayList(0);                       
    public boolean nextSegmentAlreadyGenerated;
    
    public DifficultyRecorder dr = DifficultyRecorder.getInstance();
    
    
    
    public LevelScene(GraphicsConfiguration graphicsConfiguration, MarioComponent renderer, long seed, int levelDifficulty, int type)
    {
        this.graphicsConfiguration = graphicsConfiguration;
        this.levelSeed = seed;
        this.marioComponent = renderer;
        this.levelDifficulty = levelDifficulty;
        this.levelType = type;

        widthArrow = 25;
    	tipWidthArrow = 10;
    	heightArrow = 20;

    	xArrow = 160;
    	yArrow = 40;

    	xPositionsArrow = new int[]{xArrow+-widthArrow/2,xArrow+widthArrow/2-tipWidthArrow,xArrow+widthArrow/2-tipWidthArrow,xArrow+widthArrow/2,xArrow+widthArrow/2-tipWidthArrow,xArrow+widthArrow/2-tipWidthArrow,xArrow+-widthArrow/2};
    	yPositionsArrow = new int[]{yArrow+-heightArrow/4,yArrow+-heightArrow/4,yArrow+-heightArrow/2,yArrow+0,yArrow+heightArrow/2,yArrow+heightArrow/4,yArrow+heightArrow/4};
    }

    public void init()
    {

    }

    public int fireballsOnScreen = 0;

    List<Shell> shellsToCheck = new ArrayList<Shell>();

    public void checkShellCollide(Shell shell)
    {
        shellsToCheck.add(shell);
    }

    List<Fireball> fireballsToCheck = new ArrayList<Fireball>();

    public void checkFireballCollide(Fireball fireball)
    {
        fireballsToCheck.add(fireball);
    }

    public void tick(){
        timeLeft--;

        if( widthArrow < 0){
        	widthArrow*=-1;
        	tipWidthArrow*=-1;

        	xPositionsArrow = new int[]{xArrow+-widthArrow/2,xArrow+widthArrow/2-tipWidthArrow,xArrow+widthArrow/2-tipWidthArrow,xArrow+widthArrow/2,xArrow+widthArrow/2-tipWidthArrow,xArrow+widthArrow/2-tipWidthArrow,xArrow+-widthArrow/2};
        	yPositionsArrow = new int[]{yArrow+-heightArrow/4,yArrow+-heightArrow/4,yArrow+-heightArrow/2,yArrow+0,yArrow+heightArrow/2,yArrow+heightArrow/4,yArrow+heightArrow/4};

        }

        if (timeLeft==0)
        {
            mario.dieTime();
        }

        xCamO = xCam;
        yCamO = yCam;

        if (startTime > 0)
        {
            startTime++;
        }

        float targetXCam = mario.x - 160;

        xCam = targetXCam;

        if (xCam < 0) xCam = 0;
        if (xCam > level.getWidth() * 16 - 320) xCam = level.getWidth() * 16 - 320;

        /*      if (recorder != null)
         {
         recorder.addTick(mario.getKeyMask());
         }

         if (replayer!=null)
         {
         mario.setKeys(replayer.nextTick());
         }*/

        fireballsOnScreen = 0;

        for (Sprite sprite : sprites)
        {
            if (sprite != mario)
            {
                float xd = sprite.x - xCam;
                float yd = sprite.y - yCam;
                if (xd < -64 || xd > 320 + 64 || yd < -64 || yd > 240 + 64)
                {
                    removeSprite(sprite);
                }
                else
                {
                    if (sprite instanceof Fireball)
                    {
                        fireballsOnScreen++;
                    }
                }
            }
        }

        if (paused)
        {
            for (Sprite sprite : sprites)
            {
                if (sprite == mario)
                {
                    sprite.tick();
                }
                else
                {
                    sprite.tickNoMove();
                }
            }
        }
        else
        {

            tick++;
            level.tick();

            boolean hasShotCannon = false;
            int xCannon = 0;

            for (int x = (int) xCam / 16 - 1; x <= (int) (xCam + layer.width) / 16 + 1; x++)
                for (int y = (int) yCam / 16 - 1; y <= (int) (yCam + layer.height) / 16 + 1; y++)
                {
                    int dir = 0;

                    if (x * 16 + 8 > mario.x + 16) dir = -1;
                    if (x * 16 + 8 < mario.x - 16) dir = 1;

                    SpriteTemplate st = level.getSpriteTemplate(x, y);

                    if (st != null)
                    {
                        if (st.lastVisibleTick != tick - 1)
                        {
                            if (st.sprite == null || !sprites.contains(st.sprite))
                            {
                                st.spawn(this, x, y, dir);

							}
                        }

                        st.lastVisibleTick = tick;
                    }

                    if (dir != 0)
                    {
                        byte b = level.getBlock(x, y);
                        if (((Level.TILE_BEHAVIORS[b & 0xff]) & Level.BIT_ANIMATED) > 0)
                        {
                            if ((b % 16) / 4 == 3 && b / 16 == 0)
                            {
                                if ((tick - x * 2) % 100 == 0)
                                {
                                    xCannon = x;
                                    for (int i = 0; i < 8; i++)
                                    {
                                        addSprite(new Sparkle(x * 16 + 8, y * 16 + (int) (Math.random() * 16), (float) Math.random() * dir, 0, 0, 1, 5));
                                    }
                                    addSprite(new BulletBill(this, x * 16 + 8 + dir * 8, y * 16 + 15, dir));
                                    hasShotCannon = true;
                                }
                            }
                        }
                    }
                }

            if (hasShotCannon)
            {
                sound.play(Art.samples[Art.SAMPLE_CANNON_FIRE], new FixedSoundSource(xCannon * 16, yCam + 120), 1, 1, 1);
            }

            for (Sprite sprite : sprites)
            {
                sprite.tick();
            }

            for (Sprite sprite : sprites)
            {
                sprite.collideCheck();
            }

            for (Shell shell : shellsToCheck)
            {
                for (Sprite sprite : sprites)
                {
                    if (sprite != shell && !shell.dead)
                    {
                        if (sprite.shellCollideCheck(shell))
                        {
                            if (mario.carried == shell && !shell.dead)
                            {
                                mario.carried = null;
                                shell.die();
                            }
                        }
                    }
                }
            }
            shellsToCheck.clear();

            for (Fireball fireball : fireballsToCheck)
            {
                for (Sprite sprite : sprites)
                {
                    if (sprite != fireball && !fireball.dead)
                    {
                        if (sprite.fireballCollideCheck(fireball))
                        {
                            fireball.die();
                        }
                    }
                }
            }
            fireballsToCheck.clear();
        }

        sprites.addAll(0, spritesToAdd);
        sprites.removeAll(spritesToRemove);
        spritesToAdd.clear();
        spritesToRemove.clear();

        //TODO: THIS IS TEST FLIP
//        if(keys[Mario.KEY_UP] && tick%2 == 0)
//        	level.startFlipping = true;

//        if(level.canFlip)
//        	flip();
    }

//    protected void flip(){
//    	level.canFlip = false;
//
//    	level.flipWorld();
//    	layer.repaint(0,0,layer.width,layer.height);
//
//    	//flip the monsters
//    	for(int i=0;i<sprites.size();++i){
//    		Sprite sprite = sprites.get(i);
//
//    		int correction;
//
//    		correction = (level.flipped) ? 0:-1;
//   			sprite.x = correction+(level.width - sprite.x /16)*16;
//    	}
//    }

    private DecimalFormat df = new DecimalFormat("00");
    private DecimalFormat df2 = new DecimalFormat("000");
    private DecimalFormat df6 = new DecimalFormat("000000");

    public void render(Graphics g, float alpha)
    {
        int xCam = (int) (mario.xOld + (mario.x - mario.xOld) * alpha) - 160;
        int yCam = (int) (mario.yOld + (mario.y - mario.yOld) * alpha) - 120;
        //int xCam = (int) (xCamO + (this.xCam - xCamO) * alpha);
        //        int yCam = (int) (yCamO + (this.yCam - yCamO) * alpha);
        if (xCam < 0) xCam = 0;
        if (yCam < 0) yCam = 0;
        if (xCam > level.getWidth() * 16 - 320) xCam = level.getWidth() * 16 - 320;
        if (yCam > level.getHeight() * 16 - 240) yCam = level.getHeight() * 16 - 240;

        //      g.drawImage(Art.background, 0, 0, null);

        for (int i = 0; i < 2; i++)
        {
            bgLayer[i].setCam(xCam, yCam);
            bgLayer[i].render(g, tick, alpha);
        }

        g.translate(-xCam, -yCam);
        for (Sprite sprite : sprites)
        {
            if (sprite.layer == 0) sprite.render(g, alpha);
        }
        g.translate(xCam, yCam);

        ////////////THIS RENDERS THE LEVEL
        layer.setCam(xCam, yCam);
        layer.render(g, tick, paused?0:alpha);
        layer.renderExit0(g, tick, paused?0:alpha, mario.winTime==0);
        ////////////END OF LEVEL RENDER


        ////////////RENDERS SPRITES
        g.translate(-xCam, -yCam);
        for (Sprite sprite : sprites)
        {
            if (sprite.layer == 1) sprite.render(g, alpha);
        }
        g.translate(xCam, yCam);
        g.setColor(Color.BLACK);
        layer.renderExit1(g, tick, paused?0:alpha);
        ////////////END OF SPRITE RENDERING

        //Draw standard MARIO gameplay informatiom
        //drawStringDropShadow(g, "MARIO", 0, 0, 7);
        //drawStringDropShadow(g, " " + df.format(Mario.lives), 0, 1, 7);
        //drawStringDropShadow(g, "MARIO " + df.format(Mario.lives), 0, 0, 7);     
        
        drawStringDropShadow(g, "ENEMIES", 2, 0, 7);
        drawStringDropShadow(g, "  " + df2.format(Mario.enemieskilled), 2, 1, 7);
        
        drawStringDropShadow(g, "COINS", 12, 0, 7);
        drawStringDropShadow(g, " " + df2.format(Mario.coins), 12, 1, 7);
        //drawStringDropShadow(g, "COIN", 14, 0, 7);
        //drawStringDropShadow(g, " " + df.format(Mario.coins), 14, 1, 7);
        
        //drawStringDropShadow(g, "WORLD", 24, 0, 7);
        //drawStringDropShadow(g, " " + Mario.levelString, 24, 1, 7);

        drawStringDropShadow(g, "TIME", 20, 0, 7);
        //drawStringDropShadow(g, "TIME", 35, 0, 7);
        int time = (timeLeft+15-1)/15;
        if (time<0) time = 0;
        drawStringDropShadow(g, " " + df2.format(time), 20, 1, 7);
        //drawStringDropShadow(g, " " + df2.format(time), 35, 1, 7);

        //Draw player model as consisting of received rewards
        /*
        DecimalFormat df = new DecimalFormat("#.##");       
        drawStringDropShadow(g, "PLAYER MODEL", 25, 0, 7);
        drawStringDropShadow(g, " " + df.format(playerModel[0]), 25, 1, 7); //get average rewards received when using difficulty level 1 for this specific player
        drawStringDropShadow(g, " " + df.format(playerModel[1]), 30, 1, 7); //get average rewards received when using difficulty level 4 for this specific player
        drawStringDropShadow(g, " " + df.format(playerModel[2]), 35, 1, 7); //get average rewards received when using difficulty level 7 for this specific player
        */
        
        drawStringDropShadow(g, "SEGMENTS", 27, 0, 7);
        drawStringDropShadow(g, "LABELLED: " + arch.chunksGenerated, 27, 1, 7);
        //drawStringDropShadow(g, "TOTAL", 27, 2, 7);
        //drawStringDropShadow(g, "TIME: " + df6.format(timeUsed/15), 27, 3, 7);
        
        /*
        //Draw general POMDP statistics
        drawStringDropShadow(g, "DIFF", 0, 3, 7);
        drawStringDropShadow(g, plannedDifficultyLevels.toString(), 0, 4, 7);
        drawStringDropShadow(g, drawCurrentLevelSegmentArrow(), 0, 5, 7);
        */
        //drawStringDropShadow(g, " " + m.state, 0, 4, 7);
        //drawStringDropShadow(g, "APPR", 6, 3, 7);
        //drawStringDropShadow(g, " " + m.state[1], 6, 4, 7);
        //drawStringDropShadow(g, "RWD", 12, 3, 7);
        //drawStringDropShadow(g, " " + df.format(m.REWARD), 12, 4, 7);
        
        //Draw Transition probs
        //Returns Transition Probability of Action A in State S resulting in State S'
        //Format: getTransProb(action, current state s, future_state)
        //setTransProb(); //fill table transprobs[][]
        
        //drawStringDropShadow(g, "TRNS.probs (a/f_st)", 0, 6, 7);
        //drawStringDropShadow(g, " " + getTransProb(1, m.state, 1), 0, 7, 7);
        //drawStringDropShadow(g, " " + getTransProb(1, m.state, 4), 5, 7, 7);
        //drawStringDropShadow(g, " " + getTransProb(1, m.state, 7), 10, 7, 7);
        //drawStringDropShadow(g, " " + getTransProb(4, m.state, 1), 0, 8, 7);
        //drawStringDropShadow(g, " " + getTransProb(4, m.state, 4), 5, 8, 7);
        //drawStringDropShadow(g, " " + getTransProb(4, m.state, 7), 10, 8, 7);
        //drawStringDropShadow(g, " " + getTransProb(7, m.state, 1), 0, 9, 7);
        //drawStringDropShadow(g, " " + getTransProb(7, m.state, 4), 5, 9, 7);
        //drawStringDropShadow(g, " " + getTransProb(7, m.state, 7), 10, 9, 7);
        //Action 1 - Row 1
        //drawStringDropShadow(g, " " + getTransProb(1, m.state, 1, 0), 0, 7, 7);
        //drawStringDropShadow(g, " " + getTransProb(1, m.state, 1, 1), 5, 7, 7);
        //drawStringDropShadow(g, " " + getTransProb(1, m.state, 1, 2), 10, 7, 7);
        //Action 1 - Row 2
        //drawStringDropShadow(g, " " + getTransProb(1, m.state, 4, 0), 0, 8, 7);
        //drawStringDropShadow(g, " " + getTransProb(1, m.state, 4, 1), 5, 8, 7);
        //drawStringDropShadow(g, " " + getTransProb(1, m.state, 4, 2), 10, 8, 7);
        //Action 1 - Row 3
        //drawStringDropShadow(g, " " + getTransProb(1, m.state, 7, 0), 0, 9, 7);
        //drawStringDropShadow(g, " " + getTransProb(1, m.state, 7, 1), 5, 9, 7);
        //drawStringDropShadow(g, " " + getTransProb(1, m.state, 7, 2), 10, 9, 7);
        //Action 4 - Row 1
        //drawStringDropShadow(g, " " + getTransProb(4, m.state, 1, 0), 16, 7, 7);
        //drawStringDropShadow(g, " " + getTransProb(4, m.state, 1, 1), 21, 7, 7);
        //drawStringDropShadow(g, " " + getTransProb(4, m.state, 1, 2), 26, 7, 7);
        //Action 4 - Row 2
        //drawStringDropShadow(g, " " + getTransProb(4, m.state, 4, 0), 16, 8, 7);
        //drawStringDropShadow(g, " " + getTransProb(4, m.state, 4, 1), 21, 8, 7);
        //drawStringDropShadow(g, " " + getTransProb(4, m.state, 4, 2), 26, 8, 7);
        //Action 4 - Row 3
        //drawStringDropShadow(g, " " + getTransProb(4, m.state, 7, 0), 16, 9, 7);
        //drawStringDropShadow(g, " " + getTransProb(4, m.state, 7, 1), 21, 9, 7);
        //drawStringDropShadow(g, " " + getTransProb(4, m.state, 7, 2), 26, 9, 7);
        //Action 7 - Row 1
        //drawStringDropShadow(g, " " + getTransProb(7, m.state, 1, 0), 0, 11, 7);
        //drawStringDropShadow(g, " " + getTransProb(7, m.state, 1, 1), 5, 11, 7);
        //drawStringDropShadow(g, " " + getTransProb(7, m.state, 1, 2), 10, 11, 7);
        //Action 7 - Row 2
        //drawStringDropShadow(g, " " + getTransProb(7, m.state, 4, 0), 0, 12, 7);
        //drawStringDropShadow(g, " " + getTransProb(7, m.state, 4, 1), 5, 12, 7);
        //drawStringDropShadow(g, " " + getTransProb(7, m.state, 4, 2), 10, 12, 7);
        //Action 7 - Row 3
        //drawStringDropShadow(g, " " + getTransProb(7, m.state, 7, 0), 0, 13, 7);
        //drawStringDropShadow(g, " " + getTransProb(7, m.state, 7, 1), 5, 13, 7);
        //drawStringDropShadow(g, " " + getTransProb(7, m.state, 7, 2), 10, 13, 7);        
        
        //Draw Player Model
        //drawStringDropShadow(g, "Player model", 24, 6, 7);
        //drawStringDropShadow(g, " " + getPlayerModelElement(1), 24, 7, 7); //get appropriateness of difficulty level 1 for this specific player
        //drawStringDropShadow(g, " " + getPlayerModelElement(4), 29, 7, 7); //get appropriateness of difficulty level 1 for this specific player
        //drawStringDropShadow(g, " " + getPlayerModelElement(7), 34, 7, 7); //get appropriateness of difficulty level 1 for this specific player
        
        //Set Next Action
        //setNextAction();
        
        //OLD - Draw Q-Table statistics
        //Draw q-table
        /*
        drawStringDropShadow(g, "s", 0, 2, 7);
        drawStringDropShadow(g, " " + df.format(m.states[0].index), 1, 2, 7);
        drawStringDropShadow(g, " " + df.format(m.states[1].index), 5, 2, 7);
        drawStringDropShadow(g, " " + df.format(m.states[2].index), 9, 2, 7);
        
        drawStringDropShadow(g, " " + df.format(m.states[0].QValue[0]), 1, 3, 7);
        drawStringDropShadow(g, " " + df.format(m.states[0].QValue[1]), 1, 4, 7);
        drawStringDropShadow(g, " " + df.format(m.states[0].QValue[2]), 1, 5, 7);
        
        drawStringDropShadow(g, " " +df.format(m.states[1].QValue[0]), 6, 3, 7);
        drawStringDropShadow(g, " " +df.format(m.states[1].QValue[1]), 6, 4, 7);
        drawStringDropShadow(g, " " +df.format( m.states[1].QValue[2]), 6, 5, 7);
        
        drawStringDropShadow(g, " " +df.format( m.states[2].QValue[0]), 11, 3, 7);
        drawStringDropShadow(g, " " +df.format( m.states[2].QValue[1]), 11, 4, 7);
        drawStringDropShadow(g, " " +df.format( m.states[2].QValue[2]), 11, 5, 7);
        
        drawStringDropShadow(g, " " + df.format(m.states[0].PAction[0]), 1, 7, 7);
        drawStringDropShadow(g, " " + df.format(m.states[0].PAction[1]), 1, 8, 7);
        drawStringDropShadow(g, " " + df.format(m.states[0].PAction[2]), 1, 9, 7);
        
        drawStringDropShadow(g, " " +df.format(m.states[1].PAction[0]), 6, 7, 7);
        drawStringDropShadow(g, " " +df.format(m.states[1].PAction[1]), 6, 8, 7);
        drawStringDropShadow(g, " " +df.format( m.states[1].PAction[2]), 6, 9, 7);
        
        drawStringDropShadow(g, " " +df.format( m.states[2].PAction[0]), 11, 7, 7);
        drawStringDropShadow(g, " " +df.format( m.states[2].PAction[1]), 11, 8, 7);
        drawStringDropShadow(g, " " +df.format( m.states[2].PAction[2]), 11, 9, 7);

        drawStringDropShadow(g, "R", 22, 3, 7);
        drawStringDropShadow(g, " " + df.format(m.Reward), 22, 4, 7);
        */             
        renderDirectionArrow(g);

        if (startTime > 0)
        {
            float t = startTime + alpha - 2;
            t = t * t * 0.6f;
            renderBlackout(g, 160, 120, (int) (t));
        }
//        mario.x>level.xExit*16
        if (mario.winTime > 0)
        {
            float t = mario.winTime + alpha;
            t = t * t * 0.2f;

            if (t > 0){
                if(recorder != null){
                	recorder.stopRecord();
                	recorder.levelWon();
//                	recorder.printAll();
                }

            }

            if (t > 900)
            {

                	winActions();
                	return;


                //              replayer = new Replayer(recorder.getBytes());
//                init();
            }

            renderBlackout(g, (int) (mario.xDeathPos - xCam), (int) (mario.yDeathPos - yCam), (int) (320 - t));
        }

        if (mario.deathTime > 0)
        {
        	g.setColor(Color.BLACK);
            float t = mario.deathTime + alpha;
            t = t * t * 0.4f;

            if(t > 0 && Mario.lives <= 0){
                if(recorder != null){
                	recorder.stopRecord();
                }
            }

            if (t > 1800)
            {
            	//Mario.lives--;
            	deathActions();
           }

            renderBlackout(g, (int) (mario.xDeathPos - xCam), (int) (mario.yDeathPos - yCam), (int) (320 - t));
        }
    }
    
    
    public String drawCurrentLevelSegmentArrow() {
        //Compose simple string for on-screen marking of the currentLevelSegment
        String str = " ";
        for (int i = 0; i < currentLevelSegment; i++) {
            str += "   ";
        }       
        str += "^";
        return str;
    }
    
    
   public double[] softmax(double[] input) {
        //Return vector with softMax probabilities
        //double softMax_temperature = 1.0; //set globablly
        double output[] = new double[input.length];
        for (int i = 0; i < input.length; i++) {
            double div = 0.0;
            for (int j = 0; j < input.length; j++) {
                div += Math.exp( input[j] / softMax_temperature );
            }
            output[i] = Math.exp( input[i] / softMax_temperature ) / div;
        }
        return output;
   }    

   public double getAbandonmentProbability() {
       //Return probability of the user abandoning the game
       return Math.random();
   }  
   
   //Calculate most desirable action at this point
   public int setAction() {
       //Select the next action using the accumulated rewards as given in the player models
       //Now: select according to softMax probabilities
       System.out.println("");
       System.out.println("setAction() called...");
       //String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
       //System.out.println("setAction() called at " + timeStamp + "...");

       //Determine the softMax temperature on the basis of the probability of user abandonment
       double abandonmentProbability = getAbandonmentProbability();
       softMax_temperature = (1 - abandonmentProbability);
//       System.out.println("-probability of abandonment: " + abandonmentProbability );
//       System.out.println("-calling softmax with temperature of: " + softMax_temperature);
       
       //Select action according to softMax probabilities
       double[] softmaxProbs = softmax(playerModel);
//       System.out.println("-probability of action 1 (with average accumulated reward " + playerModel[0] + ") being selected is: " + softmaxProbs[0]);
//       System.out.println("-probability of action 4 (with average accumulated reward " + playerModel[1] + ") being selected is: " + softmaxProbs[1]);
//       System.out.println("-probability of action 7 (with average accumulated reward " + playerModel[2] + ") being selected is: " + softmaxProbs[2]);

       double p = Math.random();
       double cumulativeProbability = 0.0;
       int selectedAction = 0;
       for (int i = 0; i < softmaxProbs.length; i++) {
           cumulativeProbability += softmaxProbs[i];
           if (p <= cumulativeProbability) {
               selectedAction = i;
               break;
           }
       }
       //selectedAction = selectedAction * 3 + 1; //convert to 1,4,7 difficulty scale
       m.bestAction = selectedAction * 3 + 1; //convert to 1,4,7 difficulty scale;
//       System.out.println("-selected action is: " + m.bestAction);
       return m.bestAction;
       
       // OLD - another one (most recent)
       /*
       //Select according to playerModel probabilities
       //i.e., an action with appreciation probability of 60% has a 60% chance of being executed
       //System.out.println("");
       System.out.println("setNextAction() called");
       int bestAction = 0;
       double bestActionProb = 0.0;
       double lower, upper, result;
       for(int i=0; i < playerModel.length; i++){
              //System.out.println("-checking for i is: " + i);
              int offset = i*3 + 1;
              lower = 0.0;
              upper = getPlayerModelElement(offset);
              result = Math.random() * (upper - lower) + lower;
              System.out.println("-selection probability of action " + offset + " with appropriateness " + upper + " is " + result);
              if ( result > bestActionProb ) {
                  bestAction = i;
                  bestActionProb = result;
              }      
       }    
       m.bestAction = bestAction*3 + 1;
       m.bestActionProb = bestActionProb;
       System.out.println("-selected action is action " + m.bestAction + " with selection probability " + m.bestActionProb);       
       */

       //OLD - also
       /*
       //Greedy action selection:
       //use playerModel to determine bestAction with bestActionProb
       int bestAction = 0;
       double bestActionProb = 0.0;
       for(int i=0; i < playerModel.length; i++){
              //System.out.println("Checking for i is: " + i);
              int offset = i*3 + 1;
              if (  getPlayerModelElement(offset) > bestActionProb ) {
                    bestAction = i;
                    bestActionProb = getPlayerModelElement(offset);
              }      
       }    
       m.bestAction = bestAction*3 + 1;
       m.bestActionProb = bestActionProb;
       //System.out.println("Best action is action " + m.bestAction + " with appropriateness probability " + m.bestActionProb);
       */
       
       //OLD - SANDER
       /*
       // - select action with highest probability of leading to a state with appropriate difficulty (i.e., appro = 1)
       //Init
       double[] probApproAction = new double[3];
       
       //Sum probabilities of ending in Appropriate behaviour by executing Action 1, 2, 3, respectively
       //probApproAction[0] = getTransProb(1, m.state, 1, 1) + getTransProb(1, m.state, 4, 1) + getTransProb(1, m.state, 7, 1);
       //probApproAction[1] = getTransProb(4, m.state, 1, 1) + getTransProb(4, m.state, 4, 1) + getTransProb(4, m.state, 7, 1);
       //probApproAction[2] = getTransProb(7, m.state, 1, 1) + getTransProb(7, m.state, 4, 1) + getTransProb(7, m.state, 7, 1);
       
       //Verbose
       //System.out.println("-----");
       //System.out.println("probApproAction1: " + probApproAction[0]);
       //System.out.println("probApproAction4: " + probApproAction[1]);
       //System.out.println("probApproAction7: " + probApproAction[2]);
       
       //Calculate best action based on sum of probabilities
       int i = 0;
       int bestAction = 0;
       double bestActionProb = 0.0;
       while (i < probApproAction.length) {
           //System.out.println("Checking for i: " + i);
           if ( probApproAction[i] > bestActionProb ) {
               bestAction = i;
               bestActionProb = probApproAction[i];
           }                 
           i++;
       }
            
       //Convert to 0,1,2 to 1,4,7 scale
       bestAction = bestAction*3 + 1; //.DIFFICULTY*3 + 1;
       //System.out.println("Best action is action " + bestAction + " with appropriateness probability " + bestActionProb);
       m.bestAction = bestAction;
       m.bestActionProb = bestActionProb;
       */
}
      
   public void displayReceivedRewards() {
       //Display received rewards, as stored in playerModelDiff1,4,7
       //Display rewards stored in playerModelDiff1
       //for (int i = 0; i < playerModelDiff1.toString())
//       System.out.println("");
//       System.out.println("displayReceivedRewards() called...");
//       System.out.println("-playerModelDiff1: " + playerModelDiff1.toString());
//       System.out.println("-playerModelDiff4: " + playerModelDiff4.toString());
//       System.out.println("-playerModelDiff7: " + playerModelDiff7.toString());
   }
   
   public void updatePlayerModel() {
       //Update playerModel[] with actual average (!) rewards, using playerModelDiff1,4,7 as input     
       //Add some test data
       //playerModelDiff4.add(0.0);
       //playerModelDiff4.add(1.0);
       //playerModelDiff4.add(1.0);
       
       //Update playerModel[0] - difficulty 1
       double average = 0.0;
       for (int i = 0; i < playerModelDiff1.size(); i++) {
           average += (double) playerModelDiff1.get(i);
       }
       if ( playerModelDiff1.size() > 0 )
           average = average / playerModelDiff1.size();
       playerModel[0] = average;

       //Update playerModel[1] - difficulty 4
       average = 0.0;
       for (int i = 0; i < playerModelDiff4.size(); i++) {
           average += (double) playerModelDiff4.get(i);
       }
       if ( playerModelDiff4.size() > 0 )
           average = average / playerModelDiff4.size();
       playerModel[1] = average;

       //Update playerModel[2] - difficulty 7
       average = 0.0;
       for (int i = 0; i < playerModelDiff7.size(); i++) {
           average += (double) playerModelDiff7.get(i);
       }
       if ( playerModelDiff7.size() > 0 )
           average = average / playerModelDiff7.size();
       playerModel[2] = average;
   }
   
   public void initPlayerModel()
   {
       //Fill table playerModel[] with initial player model
       //NOTE, currently redundant as actually received rewards are used - no initialisation with historic data
       //I.e., playerModel[0] represents appropriateness of challenge-level 1 to the individual player, [1] of 4, and [2] of 7      
       //Initialise the displayed average accumulated reward for each difficulty level
       //Note, the actual rewards are maintained and update in the playerModelDiff1,4,7 arraylists
       //playerModel[0] = 0.0; //average reward at initialisation of difficulty level 1
       //playerModel[1] = 0.0; //average reward at initialisation of difficulty level 4
       //playerModel[2] = 0.0; //average reward at initialisation of difficulty level 7
   }

   public int getPlayerModelIndex(int difficultylevel)
   {
       //Determine correct index for lookup in table playermodel
       int index;
       switch (difficultylevel) {
            case 1:  index = 0; break;
            case 4:  index = 1; break;
            case 7:  index = 2; break;
            default: System.out.println("ERROR - Invalid action input in getPlayerModelIndex()"); index = 0; break;
        }       
      
       //Return requested index of player-model element corresponding to the input difficulty level (for external processing)
       return index;
   }  
   
   public double getPlayerModelElement(int difficultylevel)
   {
       //Determine correct index for lookup in table playermodel
       int index;
       switch (difficultylevel) {
            case 1:  index = 0; break;
            case 4:  index = 1; break;
            case 7:  index = 2; break;
            default: System.out.println("ERROR - Invalid action input in getPlayerModelElement()"); index = 0; break;
        }       
      
       //Return content of player model according to input difficulty level
       return playerModel[index];
   }
   
   public void setTransProb() 
   //Format: getTransProb(action, current state s, state s'[0], state s'[1])
   // - i.e., the last two parameters comprise state s' of which we want to know the transition probability
   {
           //Fill table of transition probabilities
           //Always 100% probability of action 1 leading to state 1, etc.
           transprobs[0][0] = 1.0;
           transprobs[0][1] = 0.0;
           transprobs[0][2] = 0.0;
           transprobs[1][0] = 0.0;
           transprobs[1][1] = 1.0;
           transprobs[1][2] = 0.0;
           transprobs[2][0] = 0.0;
           transprobs[2][1] = 0.0;
           transprobs[2][2] = 1.0;
                     
           //OLD SANDER
           //Fill table of transition probabilities
           // - format trans_appr1[0,1,2][future_appr], where 0 is decrease diff, 1 is increase diff, and 2 is no change in diff
           //double[][] transprobs = new double[9][3]; //defined as public
         
           //Currently Too easy (currentstate[1]=0)
           //transprobs[0][0] = 0.85; //decrease diff, prob of resulting in future_appr 0
           //transprobs[0][1] = 0.10; //decrease diff, prob of resulting in future_appr 1
           //transprobs[0][2] = 0.05; //decrease diff, prob of resulting in future_appr 2
           //transprobs[1][0] = 0.40; //increase diff, prob of resulting in future_appr 0
           //transprobs[1][1] = 0.50; //increase diff, prob of resulting in future_appr 1
           //transprobs[1][2] = 0.10; //increase diff, prob of resulting in future_appr 2
           //transprobs[2][0] = 0.85; //no change, prob of resulting in future_appr 0
           //transprobs[2][1] = 0.10; //no change, prob of resulting in future_appr 1
           //transprobs[2][2] = 0.05; //no change, prob of resulting in future_appr 2

           //Currently Already appropriate (currentstate[1]=1)
           //transprobs[3][0] = 0.70; //decrease diff, prob of resulting in future_appr 0
           //transprobs[3][1] = 0.20; //decrease diff, prob of resulting in future_appr 1
           //transprobs[3][2] = 0.10; //decrease diff, prob of resulting in future_appr 2
           //transprobs[4][0] = 0.10; //increase diff, prob of resulting in future_appr 0
           //transprobs[4][1] = 0.20; //increase diff, prob of resulting in future_appr 1
           //transprobs[4][2] = 0.70; //increase diff, prob of resulting in future_appr 2
           //transprobs[5][0] = 0.15; //no change, prob of resulting in future_appr 0
           //transprobs[5][1] = 0.70; //no change, prob of resulting in future_appr 1
           //transprobs[5][2] = 0.15; //no change, prob of resulting in future_appr 2

           //Currently Too difficult (currentstate[1]=2)
           //transprobs[6][0] = 0.10; //decrease diff, prob of resulting in future_appr 0
           //transprobs[6][1] = 0.50; //decrease diff, prob of resulting in future_appr 1
           //transprobs[6][2] = 0.40; //decrease diff, prob of resulting in future_appr 2
           //transprobs[7][0] = 0.05; //increase diff, prob of resulting in future_appr 0
           //transprobs[7][1] = 0.10; //increase diff, prob of resulting in future_appr 1
           //transprobs[7][2] = 0.85; //increase diff, prob of resulting in future_appr 2
           //transprobs[8][0] = 0.05; //no change, prob of resulting in future_appr 0
           //transprobs[8][1] = 0.10; //no change, prob of resulting in future_appr 1
           //transprobs[8][2] = 0.85; //no change, prob of resulting in future_appr 2
   }
    
   public double getTransProb(int action, int currentstate, int future_state)
   {   
       //Init
       int offset_x, offset_y;
       
       //Determine correct x offset for later lookup in table transprobs
       switch (action) {
            case 1:  offset_x = 0; break;
            case 4:  offset_x = 1; break;
            case 7:  offset_x = 2; break;
            default: System.out.println("ERROR - Invalid action input in getTransProb()"); offset_x=0; break;
        }       

       //Determine correct y offset for later lookup in table transprobs
       switch (future_state) {
            case 1:  offset_y = 0; break;
            case 4:  offset_y = 1; break;
            case 7:  offset_y = 2; break;
            default: System.out.println("ERROR - Invalid future_state input in getTransProb()"); offset_y=0; break;
        }       
       
       //Return transprobs[action][future_state]
       return transprobs[offset_x][offset_y];
       
       //OLD - SANDER
       /*
       if ( action != future_diff ) {
           //Zero probability of future difficulty being different from action (action==future_diff)
           return 0.00;
       }
       else {
           //Return probability from transprobs table
           //input was: getTransProb(int action, int[] currentstate, int future_diff, int future_appr){
           //Calculate decrease / increase / no change action (0 / 1 / 2, respectively)
           int diff_action = 0; //dummy initialise to zero
           if (future_diff > action) { diff_action = 0; } //represents: decrease diff
           if (future_diff < action) { diff_action = 1; } //represents: increase diff
           if (future_diff == action) { diff_action = 2; } //represents: no change
           //Calculate offset for selecting the correct row in transprobs table 
           int offset = 0; //offset for fetching values for action 1
           if (action==4) { offset = 3; } //offset for fetching values for action 4
           if (action==7) { offset = 6; } //offset for fetching values for action 7
           
           //Return probability for transprobs(offset, diff_action)
           //input was: getTransProb(int action, int[] currentstate, int future_diff, int future_appr){
           //call is b.v. getTransProb(4, m.state, 4, 2)
           return transprobs[offset + diff_action][future_appr];
       }   
       */
   }
    
    
    public void winActions(){

    }

    public void deathActions(){

    }

    public void resetTime()
    {
        timeUsed += 3000 - timeLeft;
    	timeLeft = 200*15;
    }
    
    private void renderDirectionArrow(Graphics g){
    	if(widthArrow<0)
    		g.setColor(new Color(0,0,255,150));
    	else
    		g.setColor(new Color(255,0,0,150));

    	g.fillPolygon(xPositionsArrow,yPositionsArrow,Math.min(xPositionsArrow.length,yPositionsArrow.length));
    	g.setColor(new Color(0,0,0,255));
    	g.drawPolygon(xPositionsArrow,yPositionsArrow,Math.min(xPositionsArrow.length,yPositionsArrow.length));
    }

    private void drawStringDropShadow(Graphics g, String text, int x, int y, int c)
    {
        drawString(g, text, x*8+5, y*8+5, 0);
        drawString(g, text, x*8+4, y*8+4, c);
    }

    private void drawString(Graphics g, String text, int x, int y, int c)
    {
        char[] ch = text.toCharArray();
        for (int i = 0; i < ch.length; i++)
        {
            g.drawImage(Art.font[ch[i] - 32][c], x + i * 8, y, null);
        }
    }

    float decrease = (float)0.03;
    float factor = 0;
    boolean in = true;
    String flipText = "FLIP! MOVE THE OTHER WAY!";

//    protected void renderFlip(Graphics g){
//
//        if(level.startFlipping){
//            if(in){
//            	factor += decrease;
//            	if(factor>=1){
//            		in = false;
//            		level.canFlip = true;
//            	}
//            }
//            else{
//
//            	factor -= decrease;
//
//            	if(factor<=0){
//            		in = true;
//            		level.startFlipping = false;
//            	}
//            }
//
//            int width = 320;
//            int height = 240;
//            int overlap = 20;
//
//        	g.setColor(Color.BLACK);
//        	g.fillRect(0,0,(int)((width/2)*factor) + overlap, height);
//
//        	g.setColor(Color.BLACK);
//        	g.fillRect(width - overlap -(int)((width/2)*factor),0,(int)((width/2)*factor) + overlap,height);
//
//        	//draw a box behind the string so you can see it
//        	int padding = 3;
//
//        	g.setColor(new Color(0,0,0,100));
//        	g.fillRect(width/2-flipText.length()*8/2-padding,height/2-padding,flipText.length()*8 + 2*padding,10 + 2*padding);
//        	drawString(g,flipText, width/2-flipText.length()*8/2+2, height/2+2, 0);
//        	drawString(g,flipText,width/2-flipText.length()*8/2,height/2,2);
//
//        }
//    }

    private void renderBlackout(Graphics g, int x, int y, int radius)
    {
        if (radius > 320) return;

        int[] xp = new int[20];
        int[] yp = new int[20];
        for (int i = 0; i < 16; i++)
        {
            xp[i] = x + (int) (Math.cos(i * Math.PI / 15) * radius);
            yp[i] = y + (int) (Math.sin(i * Math.PI / 15) * radius);
        }
        xp[16] = 320;
        yp[16] = y;
        xp[17] = 320;
        yp[17] = 240;
        xp[18] = 0;
        yp[18] = 240;
        xp[19] = 0;
        yp[19] = y;
        g.fillPolygon(xp, yp, xp.length);

        for (int i = 0; i < 16; i++)
        {
            xp[i] = x - (int) (Math.cos(i * Math.PI / 15) * radius);
            yp[i] = y - (int) (Math.sin(i * Math.PI / 15) * radius);
        }
        xp[16] = 320;
        yp[16] = y;
        xp[17] = 320;
        yp[17] = 0;
        xp[18] = 0;
        yp[18] = 0;
        xp[19] = 0;
        yp[19] = y;

        g.fillPolygon(xp, yp, xp.length);
    }


    public void addSprite(Sprite sprite)
    {
        spritesToAdd.add(sprite);
        sprite.tick();
    }

    public void removeSprite(Sprite sprite)
    {
        spritesToRemove.add(sprite);
    }

    public float getX(float alpha)
    {
        int xCam = (int) (mario.xOld + (mario.x - mario.xOld) * alpha) - 160;
        //        int yCam = (int) (mario.yOld + (mario.y - mario.yOld) * alpha) - 120;
        //int xCam = (int) (xCamO + (this.xCam - xCamO) * alpha);
        //        int yCam = (int) (yCamO + (this.yCam - yCamO) * alpha);
        if (xCam < 0) xCam = 0;
        //        if (yCam < 0) yCam = 0;
        //        if (yCam > 0) yCam = 0;
        return xCam + 160;
    }

    public float getY(float alpha)
    {
        return 0;
    }

    public void bump(int x, int y, boolean canBreakBricks)
    {
        byte block = level.getBlock(x, y);

        if ((Level.TILE_BEHAVIORS[block & 0xff] & Level.BIT_BUMPABLE) > 0)
        {
            bumpInto(x, y - 1);
            level.setBlock(x, y, (byte) 4);

            if (((Level.TILE_BEHAVIORS[block & 0xff]) & Level.BIT_SPECIAL) > 0)
            {
                sound.play(Art.samples[Art.SAMPLE_ITEM_SPROUT], new FixedSoundSource(x * 16 + 8, y * 16 + 8), 1, 1, 1);
                if (!Mario.large)
                {
                    addSprite(new Mushroom(this, x * 16 + 8, y * 16 + 8));
                }
                else
                {
                    addSprite(new FireFlower(this, x * 16 + 8, y * 16 + 8));
                }

                if(recorder != null){
                	recorder.blockPowerDestroyRecord();
                }
            }
            else
            {
            	//TODO should only record hidden coins (in boxes)
            	if(recorder != null){
            		recorder.blockCoinDestroyRecord();
            	}

                Mario.getCoin();
                sound.play(Art.samples[Art.SAMPLE_GET_COIN], new FixedSoundSource(x * 16 + 8, y * 16 + 8), 1, 1, 1);
                addSprite(new CoinAnim(x, y));
            }
        }

        if ((Level.TILE_BEHAVIORS[block & 0xff] & Level.BIT_BREAKABLE) > 0)
        {
            bumpInto(x, y - 1);
            if (canBreakBricks)
            {
            	if(recorder != null){
            		recorder.blockEmptyDestroyRecord();
            	}

                sound.play(Art.samples[Art.SAMPLE_BREAK_BLOCK], new FixedSoundSource(x * 16 + 8, y * 16 + 8), 1, 1, 1);
                level.setBlock(x, y, (byte) 0);
                for (int xx = 0; xx < 2; xx++)
                    for (int yy = 0; yy < 2; yy++)
                        addSprite(new Particle(x * 16 + xx * 8 + 4, y * 16 + yy * 8 + 4, (xx * 2 - 1) * 4, (yy * 2 - 1) * 4 - 8));
            }

        }
    }

    public void bumpInto(int x, int y)
    {
        byte block = level.getBlock(x, y);
        if (((Level.TILE_BEHAVIORS[block & 0xff]) & Level.BIT_PICKUPABLE) > 0)
        {
            Mario.getCoin();
            sound.play(Art.samples[Art.SAMPLE_GET_COIN], new FixedSoundSource(x * 16 + 8, y * 16 + 8), 1, 1, 1);
            level.setBlock(x, y, (byte) 0);
            addSprite(new CoinAnim(x, y + 1));

            //TODO no idea when this happens... maybe remove coin count
            if(recorder != null)
            	recorder.recordCoin();
        }

        for (Sprite sprite : sprites)
        {
            sprite.bumpCheck(x, y);
        }
    }
    public void setLevel(Level level){
    	this.level = level;
    }

	@Override
	public void mouseClicked(MouseEvent me) {
		// TODO Auto-generated method stub

	}
}
