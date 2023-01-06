package com.poixson.backrooms.levels;

import static com.poixson.utils.NumberUtils.Rnd10K;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.generator.WorldInfo;

import com.poixson.backrooms.BackroomsPlugin;


// 10 | Field of Wheat
public class Level_010 extends BackroomsLevel {

	public static final int WHEAT_Y = 0;

	// generators
	public final Gen_010 gen_010;



	public Level_010(final BackroomsPlugin plugin) {
		super(plugin);
		// generators
		this.gen_010 = new Gen_010();
	}
	@Override
	public void unload() {
		this.gen_010.unload();
	}



	@Override
	public Location getSpawn(final int level) {
		if (level != 10) throw new RuntimeException("Invalid backrooms level: "+Integer.toString(level));
		final int x = (Rnd10K() * 2) - 10000;
		final int z = (Rnd10K() * 2) - 10000;
		return this.getSpawn(level, x, z);
	}
	@Override
	public Location getSpawn(final int level, final int x, final int z) {
		return this.getSpawn(level, 10, x, WHEAT_Y, z);
	}

	@Override
	public int getLevelFromY(final int y) {
		return 10;
	}
	public int getYFromLevel(final int level) {
		return WHEAT_Y;
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
