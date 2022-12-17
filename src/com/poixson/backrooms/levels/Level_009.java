package com.poixson.backrooms.levels;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.generator.WorldInfo;

import com.poixson.backrooms.BackroomsPlugin;


// 9 | Suburbs
public class Level_009 extends BackroomsLevel {

	public static final int SUBURBS_Y = 0;



	public Level_009(final BackroomsPlugin plugin) {
		super(plugin);
	}
	@Override
	public void unload() {
	}



	@Override
	public Location getSpawn(final int level) {
		if (level != 9) throw new RuntimeException("Invalid backrooms level: "+Integer.toString(level));
		final int x = (BackroomsPlugin.Rnd10K() * 2) - 10000;
		final int z = (BackroomsPlugin.Rnd10K() * 2) - 10000;
		return this.getSpawn(level, x, z);
	}
	@Override
	public Location getSpawn(final int level, final int x, final int z) {
		return this.getSpawn(level, 10, x, SUBURBS_Y, z);
	}

	@Override
	public int getLevelFromY(final int y) {
		return 9;
	}
	public int getYFromLevel(final int level) {
		return SUBURBS_Y;
	}
	public int getMaxYFromLevel(final int level) {
		return 255;
	}



	@Override
	public void generateSurface(
			final WorldInfo worldInfo, final Random random,
			final int chunkX, final int chunkZ, final ChunkData chunk) {
//if (chunkX == 2 && chunkZ == 2) return;
//if (chunkX % 20 == 0 || chunkZ % 20 == 0) return;
		for (int z=0; z<16; z++) {
			for (int x=0; x<16; x++) {
				chunk.setBlock(x, 0, z, Material.BEDROCK);
				chunk.setBlock(x, 1, z, Material.STONE);
			}
		}
	}



}
