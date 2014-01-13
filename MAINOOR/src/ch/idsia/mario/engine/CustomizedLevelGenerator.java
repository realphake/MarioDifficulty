package ch.idsia.mario.engine;

import java.util.Random;

import ch.idsia.mario.MarioInterface.Constraints;
import ch.idsia.mario.MarioInterface.GamePlay;
import ch.idsia.mario.MarioInterface.LevelGenerator;
import ch.idsia.mario.MarioInterface.LevelInterface;
import ch.idsia.mario.engine.level.CustomizedLevel;

import Architect.ARCH_MESSAGE;

public class CustomizedLevelGenerator implements LevelGenerator{

	public LevelInterface generateLevel(GamePlay playerMetrics) {
		//SANDER
                //THIS CODE SEEMS OBSOLETE
                //LevelInterface level = new CustomizedLevel(320,15,new Random().nextLong(),1,1,playerMetrics);
                //System.out.println("HEREEEE");
                ARCH_MESSAGE message_dummy = new ARCH_MESSAGE();
                LevelInterface level = new CustomizedLevel(320,15,new Random().nextLong(),1,1,message_dummy);
		return level;
	}

	@Override
	public LevelInterface generateLevel(String detailedInfo) {
		// TODO Auto-generated method stub
		return null;
	}

}
