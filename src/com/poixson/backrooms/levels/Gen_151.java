package com.poixson.backrooms.levels;

import static com.poixson.backrooms.levels.Level_151.SUBCEILING;
import static com.poixson.backrooms.levels.Level_151.SUBFLOOR;

import java.util.LinkedList;

import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.commonmc.tools.plotter.BlockPlotter;
import com.poixson.utils.FastNoiseLiteD;
import com.poixson.utils.FastNoiseLiteD.CellularDistanceFunction;
import com.poixson.utils.FastNoiseLiteD.FractalType;
import com.poixson.utils.FastNoiseLiteD.NoiseType;


// 151 | Dollhouse
public class Gen_151 extends GenBackrooms {

	public static final boolean ENABLE_GENERATE = true;
	public static final boolean ENABLE_ROOF     = true;

	public static final Material HOUSE_FLOOR = Material.SPRUCE_PLANKS;
	public static final Material HOUSE_WALLS = Material.SPRUCE_PLANKS;

	public final int level_m;

	// noise
	protected final FastNoiseLiteD noiseHouseWalls;



	public Gen_151(final LevelBackrooms backlevel,
			final int level_y, final int level_h,
			final int subfloor, final int subceiling) {
		super(backlevel, level_y, level_h);
		this.level_m = Math.floorDiv(this.level_h, 2);
		// attic walls
		this.noiseHouseWalls = this.register(new FastNoiseLiteD());
		this.noiseHouseWalls.setFrequency(0.02);
		this.noiseHouseWalls.setNoiseType(NoiseType.Cellular);
		this.noiseHouseWalls.setFractalType(FractalType.PingPong);
		this.noiseHouseWalls.setCellularDistanceFunction(CellularDistanceFunction.Manhattan);
	}



	@Override
	public void generate(final PreGenData pregen, final ChunkData chunk,
			final LinkedList<BlockPlotter> plots, final int chunkX, final int chunkZ) {
		if (!ENABLE_GENERATE) return;
		for (int z=0; z<16; z++) {
			for (int x=0; x<16; x++) {
//				final int xx = (chunkX * 16) + x;
//				final int zz = (chunkZ * 16) + z;
				chunk.setBlock(x, 0, z, Material.BEDROCK);
				chunk.setBlock(x, 1, z, Material.STONE);
/*
				int y  = this.level_y + SUBFLOOR + 1;
				int cy = this.level_y + SUBFLOOR + this.level_h + 1;
				// lobby floor
				chunk.setBlock(x, y, z, Material.BEDROCK);
				y++;
				for (int yy=0; yy<SUBFLOOR; yy++)
					chunk.setBlock(x, y+yy, z, HOUSE_FLOOR);
				y += SUBFLOOR;
				final double value = this.noiseHouseWalls.getNoiseRot(xx, cy, 0.25);
				if (value < -0.9 || value > 0.9) {
					for (int iy=0; iy<3; iy++) {
						chunk.setBlock(x, y+iy, z, HOUSE_WALLS);
					}
				}
				if (BUILD_ROOF) {
					cy++;
					for (int i=0; i<SUBCEILING; i++)
						chunk.setBlock(x, cy+i, z, HOUSE_FLOOR);
				}
*/
			} // end x
		} // end z
	}



}
