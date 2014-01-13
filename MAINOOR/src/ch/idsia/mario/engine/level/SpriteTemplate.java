package ch.idsia.mario.engine.level;

import ch.idsia.mario.engine.LevelScene;
import ch.idsia.mario.engine.sprites.Enemy;
import ch.idsia.mario.engine.sprites.FlowerEnemy;
import ch.idsia.mario.engine.sprites.Sprite;

public class SpriteTemplate
{
    public int lastVisibleTick = -1;
    public Sprite sprite;
    public boolean isDead = false;
    private boolean winged;
    
    // new vars
    	public static final int RED_TURTLE		= 0;
	public static final int GREEN_TURTLE	= 1;
	public static final int GOOMPA			= 2;
	public static final int ARMORED_TURTLE	= 3;
	public static final int JUMP_FLOWER		= 4;
	public static final int CANNON_BALL		= 5;
	public static final int CHOMP_FLOWER	= 6;
    // end new vars
    public int getType() {
        return type;
    }

    private int type;
    
    public SpriteTemplate(int type, boolean winged)
    {
        this.type = type;
        this.winged = winged;
    }
    
    public void spawn(LevelScene world, int x, int y, int dir)
    {
        if (isDead) return;

        if (type==Enemy.ENEMY_FLOWER)
        {
            sprite = new FlowerEnemy(world, x*16+15, y*16+24, x, y);
        }
        else
        {
//            sprite = new Enemy(world, x*16+8, y*16+15, dir, type, winged);
            sprite = new Enemy(world, x*16+8, y*16+15, dir, type, winged, x, y);
        }
        sprite.spriteTemplate = this;
        world.addSprite(sprite);
    }
}