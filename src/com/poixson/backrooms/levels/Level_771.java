package com.poixson.backrooms.levels;

import static com.poixson.utils.NumberUtils.Rnd10K;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
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
	public Location getSpawn(final int level) {
		if (level != 771) throw new RuntimeException("Invalid backrooms level: "+Integer.toString(level));
		int x = (Rnd10K() * 2) - 10000;
		int z = (Rnd10K() * 2) - 10000;
		if (Math.abs(x) > Math.abs(z)) {
			z = 0;
		} else {
			x = 0;
		}
		return this.getSpawn(level, x, z);
	}
	@Override
	public Location getSpawn(final int level, final int x, final int z) {
		return this.getSpawn(level, 10, x, ROAD_Y, z);
	}

	@Override
	public int getLevelFromY(final int y) {
		return 771;
	}
	public int getYFromLevel(final int level) {
		return ROAD_Y;
	}
	public int getMaxYFromLevel(final int level) {
		return 255;
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
