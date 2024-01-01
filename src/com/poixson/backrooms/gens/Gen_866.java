/*
package com.poixson.backrooms.gens;


// 866 | Dirtfield
public class Gen_866 extends BackroomsGen {

//	// noise
//	public final FastNoiseLiteD noiseField;

//	protected int rndLast = 0;



	public Gen_866(final BackroomsLevel backlevel, final int level_y, final int level_h) {
		super(backlevel, level_y, level_h);
		// field
//		this.noiseField = this.register(new FastNoiseLiteD());
//		this.noiseField.setFrequency(0.006);
//		this.noiseField.setFractalOctaves(1);
	}



	@Override
	public void generate(final PreGenData pregen, final ChunkData chunk,
			final LinkedList<BlockPlotter> plots, final int chunkX, final int chunkZ) {
/ *
		if (!ENABLE_GEN_866) return;
		for (int iz=0; iz<16; iz++) {
			for (int ix=0; ix<16; ix++) {
//TODO
				final int xx = (chunkX * 16) + ix;
				final int zz = (chunkZ * 16) + iz;
				// subfloor
				chunk.setBlock(ix, 0, iz, Material.BEDROCK);
				chunk.setBlock(ix, 1, iz, Material.ACACIA_PLANKS);
				chunk.setBlock(ix, 2, iz, Material.SMOOTH_RED_SANDSTONE);
				final double value = this.noiseField.getNoise(xx, zz);
//				final double mx = ((double)xx) + value;
//				final double mz = ((double)zz) + (value * 5.0);
//				final double value2 = this.noiseField.getNoise(mx, z) + 1.0;
				final double h = value * 1.2;
				final int ih = (int) Math.floor(h);
				final double hh = h - ((double)ih);
				final boolean isHalf = (hh > 0.6);
				int y = this.level_y + 3;
				for (int iy=0; iy<ih; iy++)
					chunk.setBlock(ix, y+iy, iz, Material.SMOOTH_RED_SANDSTONE);
				y += ih;
				if (isHalf) {
					chunk.setBlock(ix, y, iz, Material.SMOOTH_RED_SANDSTONE_SLAB);
				} else {
//					final int rnd = NumberUtils.GetNewRandom(0, 999, this.rndLast) % 100;
//					this.rndLast = rnd;
//					if (rnd < 2) {
//						chunk.setBlock(x, y, z, Material.DEAD_BUSH);
//					} else
//					if (rnd < 5) {
//						chunk.setBlock(x, y, z, Material.GRASS);
//					}
				}
			} // end ix
		} // end iz
* /
	}



	// -------------------------------------------------------------------------------
	// configs



	@Override
	protected void loadConfig() {
	}
	public static void ConfigDefaults(final FileConfiguration cfg) {
	}



}
*/
