package com.poixson.backrooms.levels;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.WorldInfo;

import com.poixson.backrooms.BackroomsPlugin;


// 771 | Crossroads
public class Level_771 extends BackroomsLevel {

	public static final int ROAD_Y = 200;

	// generators
	public final Gen_771 gen_771;



	public Level_771(final BackroomsPlugin plugin) {
		super(plugin);
		// generators
		this.gen_771 = new Gen_771();
	}
	@Override
	public void unload() {
		this.gen_771.unload();
	}



	@Override
	public void generateSurface(
			final WorldInfo worldInfo, final Random random,
			final int chunkX, final int chunkZ, final ChunkData chunk) {
		final int seed = Long.valueOf(worldInfo.getSeed()).intValue();
		this.gen_771.setSeed(seed);
		final boolean centerX = (chunkX == 0 || chunkX == -1);
		final boolean centerZ = (chunkZ == 0 || chunkZ == -1);
		// world center
		if (centerX && centerZ) {
			this.gen_771.generateRoadCenter(chunk, chunkX, chunkZ);
			this.gen_771.generateCenterArch(chunk, chunkX, chunkZ, true);
			this.gen_771.generateCenterArch(chunk, chunkX, chunkZ, false);
		} else
		// road
		if (centerX || centerZ) {
			this.gen_771.generateRoad(chunk, chunkX, chunkZ);
		}
	}



	@Override
	public List<BlockPopulator> getDefaultPopulators(final World world) {
		return Arrays.asList(
			this.gen_771.crossPop
		);
	}



}