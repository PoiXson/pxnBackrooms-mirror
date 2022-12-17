package com.poixson.backrooms.levels;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.generator.WorldInfo;

import com.poixson.backrooms.BackroomsPlugin;


// 78 | Space
public class Level_078 extends BackroomsLevel {



	public Level_078(final BackroomsPlugin plugin) {
		super(plugin);
	}
	@Override
	public void unload() {
	}



//TODO
	@Override
	public Location getSpawn(final int level) {
		if (level != 78) throw new RuntimeException("Invalid backrooms level: "+Integer.toString(level));
		return this.getSpawn(level, 0, 0);
	}

	@Override
	public int getLevelFromY(final int y) {
return 0;
	}
	public int getYFromLevel(final int level) {
return 255;
	}
	public int getMaxYFromLevel(final int level) {
return 255;
	}



	@Override
	public void generateSurface(
			final WorldInfo worldInfo, final Random random,
			final int chunkX, final int chunkZ, final ChunkData chunk) {
	}



}
