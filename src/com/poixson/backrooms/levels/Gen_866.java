package com.poixson.backrooms.levels;

import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.utils.FastNoiseLiteD;


// 866 | Dirtfield
public class Gen_866 extends BackroomsGenerator {

	public static final int DIRTFIELD_Y = Level_866.DIRTFIELD_Y;
	public static final int SUBFLOOR    = Level_866.SUBFLOOR;

	// noise
	protected final FastNoiseLiteD noiseField;

	protected int rndLast = 0;



	public Gen_866() {
		super();
		// field
		this.noiseField = new FastNoiseLiteD();
		this.noiseField.setFrequency(0.006);
		this.noiseField.setFractalOctaves(1);
	}
	@Override
	public void unload() {
	}



	@Override
	public void setSeed(final int seed) {
		this.noiseField.setSeed(seed);
	}



	public void generateField(
			final ChunkData chunk, final int chunkX, final int chunkZ,
			final int x, final int z, final int xx, final int zz) {
		// subfloor
		chunk.setBlock(x, 0, z, Material.BEDROCK);
		chunk.setBlock(x, 1, z, Material.ACACIA_PLANKS);
		chunk.setBlock(x, 2, z, Material.SMOOTH_RED_SANDSTONE);
		final double value = this.noiseField.getNoise(xx, zz);
//		final double mx = ((double)xx) + value;
//		final double mz = ((double)zz) + (value * 5.0);
//		final double value2 = this.noiseField.getNoise(mx, z) + 1.0;
		final double h = value * 1.2;
		final int ih = (int) Math.floor(h);
		final double hh = h - ((double)ih);
		final boolean isHalf = (hh > 0.6);
		int y = DIRTFIELD_Y + 3;
		for (int iy=0; iy<ih; iy++) {
			chunk.setBlock(x, y+iy, z, Material.SMOOTH_RED_SANDSTONE);
		}
		y += ih;
		if (isHalf) {
			chunk.setBlock(x, y, z, Material.SMOOTH_RED_SANDSTONE_SLAB);
		} else {
//			final int rnd = NumberUtils.GetNewRandom(0, 999, this.rndLast) % 100;
//			this.rndLast = rnd;
//			if (rnd < 2) {
//				chunk.setBlock(x, y, z, Material.DEAD_BUSH);
//			} else
//			if (rnd < 5) {
//				chunk.setBlock(x, y, z, Material.GRASS);
//			}
		}
	}



}
