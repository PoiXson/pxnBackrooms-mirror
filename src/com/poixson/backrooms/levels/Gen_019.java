package com.poixson.backrooms.levels;

import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.utils.FastNoiseLiteD;
import com.poixson.utils.FastNoiseLiteD.CellularDistanceFunction;
import com.poixson.utils.FastNoiseLiteD.FractalType;
import com.poixson.utils.FastNoiseLiteD.NoiseType;


// 19 | Attic
public class Gen_019 extends GenBackrooms {

	public static final boolean ENABLE_GENERATE = true;
	public static final boolean ENABLE_ROOF     = true;

	public static final Material ATTIC_FLOOR = Material.SPRUCE_PLANKS;
	public static final Material ATTIC_WALLS = Material.SPRUCE_PLANKS;

	public final int subfloor;
	public final int subceiling;
	public final int level_m;

	// noise
	protected final FastNoiseLiteD noiseAtticWalls;

	// populators
	public final Pop_019 popAttic;



	public Gen_019(final BackroomsPlugin plugin,
			final int level_y, final int level_h,
			final int subfloor, final int subceiling) {
		super(plugin, level_y, level_h);
		this.subfloor   = subfloor;
		this.subceiling = subceiling;
		this.level_m = Math.floorDiv(this.level_h, 2);
		// attic walls
		this.noiseAtticWalls = this.register(new FastNoiseLiteD());
		this.noiseAtticWalls.setFrequency(0.02);
		this.noiseAtticWalls.setNoiseType(NoiseType.Cellular);
		this.noiseAtticWalls.setFractalType(FractalType.PingPong);
		this.noiseAtticWalls.setCellularDistanceFunction(CellularDistanceFunction.Manhattan);
		// populators
		this.popAttic = new Pop_019(this);
	}



	@Override
	public void generate(final PreGenData pregen,
			final ChunkData chunk, final int chunkX, final int chunkZ) {
		if (!ENABLE_GENERATE) return;
		final int y  = this.level_y + this.subfloor + 1;
		final int cy = this.level_y + this.subfloor + this.level_h + 1;
		for (int z=0; z<16; z++) {
			for (int x=0; x<16; x++) {
				final int xx = (chunkX * 16) + x;
				final int zz = (chunkZ * 16) + z;
				// lobby floor
				chunk.setBlock(x, this.level_y, z, Material.BEDROCK);
				for (int yy=0; yy<this.subfloor; yy++) {
					chunk.setBlock(x, this.level_y+yy+1, z, ATTIC_FLOOR);
				}
				final double value = this.noiseAtticWalls.getNoiseRot(xx, zz, 0.25);
				if (value < -0.9 || value > 0.9) {
					for (int iy=0; iy<3; iy++) {
						chunk.setBlock(x, y+iy, z, ATTIC_WALLS);
					}
				}
				// second floor
				chunk.setBlock(x, y+this.level_m, z, ATTIC_WALLS);
				if (ENABLE_ROOF) {
					for (int i=0; i<this.subceiling; i++) {
						chunk.setBlock(x, cy+i+1, z, ATTIC_FLOOR);
					}
				}
			} // end x
		} // end z
	}



}
