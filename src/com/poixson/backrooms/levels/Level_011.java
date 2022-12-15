package com.poixson.backrooms.levels;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.generator.WorldInfo;

import com.poixson.backrooms.BackroomsPlugin;


// 11 | city
public class Level_011 extends BackroomsLevel {

	public static final int CITY_Y = 0;



	public Level_011(final BackroomsPlugin plugin) {
		super(plugin);
	}
	@Override
	public void unload() {
	}



	@Override
	public void generateSurface(
			final WorldInfo worldInfo, final Random random,
			final int chunkX, final int chunkZ, final ChunkData chunk) {
if (chunkX == -1 && chunkZ == 1) return;
if (chunkZ % 10 == 0) return;
		for (int z=0; z<16; z++) {
			for (int x=0; x<16; x++) {
				chunk.setBlock(x, 0, z, Material.BEDROCK);
				chunk.setBlock(x, 1, z, Material.STONE);
			}
		}
	}



}
