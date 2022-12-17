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



	@Override
	public Location getSpawn(final int level) {
//TODO
return null;
//		if (level != 78) throw new RuntimeException("Invalid level: "+Integer.toString(level));
//		final int x = (BackroomsPlugin.Rnd10K() * 2) - 10000;
//		final int z = (BackroomsPlugin.Rnd10K() * 2) - 10000;
//		return this.getSpawn(level, 10, x, SUBURBS_Y, z);
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
