package ch.idsia.mario.engine;

import java.util.*;

import ch.idsia.mario.engine.level.*;

public abstract class LevelFactory {

	private static Map<String, Level> levels = new HashMap<String, Level>();

	public static void register(String name,Level level){
		levels.put(name, level);
	}

	public static Level retrieve(String name){
		return levels.get(name);
	}

	public static Iterator<String> getKeyset(){
		return levels.keySet().iterator();
	}

	public static Map getLevelsMap(){
		return levels;
	}
}
