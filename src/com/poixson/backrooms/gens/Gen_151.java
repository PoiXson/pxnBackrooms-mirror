/*
package com.poixson.backrooms.gens;


// 151 | Dollhouse
public class Gen_151 extends BackroomsGen {

//	public static final Material HOUSE_FLOOR = Material.SPRUCE_PLANKS;
//	public static final Material HOUSE_WALLS = Material.SPRUCE_PLANKS;

//	// noise
//	public final FastNoiseLiteD noiseHouseWalls;



	public Gen_151(final BackroomsLevel backlevel, final int seed,
			final int level_y, final int level_h,
			final int subfloor, final int subceiling) {
		super(backlevel, seed, level_y, level_h);
/ *
		// attic walls
		this.noiseHouseWalls = this.register(new FastNoiseLiteD());
		this.noiseHouseWalls.setFrequency(0.02);
		this.noiseHouseWalls.setNoiseType(NoiseType.Cellular);
		this.noiseHouseWalls.setFractalType(FractalType.PingPong);
		this.noiseHouseWalls.setCellularDistanceFunction(CellularDistanceFunction.Manhattan);
* /
	}



	@Override
	public int getLevelNumber() {
		return 151;
	}



	@Override
	public void generate(final PreGenData pregen,
			final LinkedList<Tuple<BlockPlotter, StringBuilder[][]>> plots,
			final ChunkData chunk, final int chunkX, final int chunkZ) {
/ *
		if (!ENABLE_GEN_151) return;
		for (int iz=0; iz<16; iz++) {
			for (int ix=0; ix<16; ix++) {
//TODO
				final int xx = (chunkX * 16) + ix;
//				final int zz = (chunkZ * 16) + iz;
				chunk.setBlock(ix, 0, iz, Material.BEDROCK);
				chunk.setBlock(ix, 1, iz, Material.STONE);
				int y  = this.level_y + SUBFLOOR + 1;
				int cy = this.level_y + SUBFLOOR + this.level_h + 1;
				// lobby floor
				chunk.setBlock(ix, y, iz, Material.BEDROCK);
				y++;
				for (int yy=0; yy<SUBFLOOR; yy++)
					chunk.setBlock(ix, y+yy, iz, HOUSE_FLOOR);
				y += SUBFLOOR;
				final double value = this.noiseHouseWalls.getNoiseRot(xx, cy, 0.25);
				if (value < -0.9 || value > 0.9) {
					for (int iy=0; iy<3; iy++)
						chunk.setBlock(ix, y+iy, iz, HOUSE_WALLS);
				}
				if (ENABLE_TOP_151) {
					cy++;
					for (int i=0; i<SUBCEILING; i++)
						chunk.setBlock(ix, cy+i, iz, HOUSE_FLOOR);
				}
			} // end ix
		} // end iz
* /
	}



	// -------------------------------------------------------------------------------
	// configs



	@Override
	protected void loadConfig(final ConfigurationSection cfgParams, final ConfigurationSection cfgBlocks) {
	}
	@Override
	protected void configDefaults(final ConfigurationSection cfgParams, final ConfigurationSection cfgBlocks) {
	}



}
*/
