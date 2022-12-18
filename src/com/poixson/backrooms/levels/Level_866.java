package com.poixson.backrooms.levels;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.generator.WorldInfo;

import com.poixson.backrooms.BackroomsPlugin;


// 866 | Dirtfield
public class Level_866 extends BackroomsLevel {

	public static final int DIRTFIELD_Y = 0;
	public static final int SUBFLOOR = 3;

	public static final Material GROUND      = Material.RED_SANDSTONE;
	public static final Material GROUND_SLAB = Material.RED_SANDSTONE_SLAB;

	// generators
	public final Gen_866 gen_866;



	public Level_866(final BackroomsPlugin plugin) {
		super(plugin);
		// generators
		this.gen_866 = new Gen_866();
	}

	@Override
	public void unload() {
		this.gen_866.unload();
	}



	@Override
	public Location getSpawn(final int level) {
		if (level != 866) throw new RuntimeException("Invalid backrooms level: "+Integer.toString(level));
		final int x = (BackroomsPlugin.Rnd10K() * 2) - 10000;
		final int z = (BackroomsPlugin.Rnd10K() * 2) - 10000;
		return this.getSpawn(level, x, z);
	}
	@Override
	public Location getSpawn(final int level, final int x, final int z) {
		return this.getSpawn(level, 10, x, DIRTFIELD_Y, z);
	}

	@Override
	public int getLevelFromY(final int y) {
		return 866;
	}
	public int getYFromLevel(final int level) {
		return DIRTFIELD_Y;
	}
	public int getMaxYFromLevel(final int level) {
		return 255;
	}



	@Override
	public void generateSurface(
			final WorldInfo worldInfo, final Random random,
			final int chunkX, final int chunkZ, final ChunkData chunk) {
if (chunkX == 2 && chunkZ == 2) return;
if (chunkX % 20 == 0 || chunkZ % 20 == 0) return;
		int xx, zz;
		for (int z=0; z<16; z++) {
			zz = (chunkZ * 16) + z;
			for (int x=0; x<16; x++) {
				xx = (chunkX * 16) + x;
				this.gen_866.generateField(chunkX, chunkZ, chunk, x, z, xx, zz);
			}
		}
	}



}
