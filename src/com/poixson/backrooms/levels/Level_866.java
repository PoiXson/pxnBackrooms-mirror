package com.poixson.backrooms.levels;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.generator.WorldInfo;

import com.poixson.backrooms.BackroomsPlugin;


// dirtfield
public class Level_866 extends BackroomsLevel {

	public static final int DIRTFIELD_Y = 0;
	public static final int SUBFLOOR = 3;



	public Level_866(final BackroomsPlugin plugin) {
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
//if (chunkX % 10 == 0 || chunkZ % 10 == 0) return;
		for (int z=0; z<16; z++) {
			for (int x=0; x<16; x++) {
				chunk.setBlock(x, 0, z, Material.BEDROCK);
				chunk.setBlock(x, 1, z, Material.STONE);
			}
		}
	}



}
