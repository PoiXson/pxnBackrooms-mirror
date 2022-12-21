package com.poixson.backrooms.levels;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.generator.WorldInfo;

import com.poixson.backrooms.BackroomsPlugin;


// 78 | Space
public class Level_078 extends BackroomsLevel {

	// generators
	public final Gen_078 gen_078;



	public Level_078(final BackroomsPlugin plugin) {
		super(plugin);
		// generators
		this.gen_078 = new Gen_078(plugin);
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
	public Location getSpawn(final int level, final int x, final int z) {
		return this.getSpawn(level, 255, x, 0, z);
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
