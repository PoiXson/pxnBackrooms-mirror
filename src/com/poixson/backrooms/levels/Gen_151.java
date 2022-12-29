package com.poixson.backrooms.levels;

import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.utils.FastNoiseLiteD;
import com.poixson.utils.FastNoiseLiteD.CellularDistanceFunction;
import com.poixson.utils.FastNoiseLiteD.FractalType;
import com.poixson.utils.FastNoiseLiteD.NoiseType;


// 151 | Dollhouse
public class Gen_151 extends BackroomsGenerator {

	public static final boolean BUILD_ROOF = Level_151.BUILD_ROOF;
	public static final int     SUBFLOOR   = Level_151.SUBFLOOR;
	public static final int     SUBCEILING = Level_151.SUBCEILING;

	public static final int HOUSE_Y = Level_151.HOUSE_Y;
	public static final int HOUSE_H = Level_151.HOUSE_H;
	public static final int HOUSE_M = Math.floorDiv(HOUSE_H, 2);

	public static final Material HOUSE_FLOOR = Material.SPRUCE_PLANKS;
	public static final Material HOUSE_WALLS = Material.SPRUCE_PLANKS;

	// noise
	protected final FastNoiseLiteD noiseHouseWalls;



	public Gen_151() {
		super();
		// attic walls
		this.noiseHouseWalls = new FastNoiseLiteD();
		this.noiseHouseWalls.setFrequency(0.02);
		this.noiseHouseWalls.setNoiseType(NoiseType.Cellular);
		this.noiseHouseWalls.setFractalType(FractalType.PingPong);
		this.noiseHouseWalls.setCellularDistanceFunction(CellularDistanceFunction.Manhattan);
	}
	@Override
	public void unload() {
	}



	@Override
	public void setSeed(final int seed) {
		this.noiseHouseWalls.setSeed(seed);
	}



	public void generateHouse(
			final int chunkX, final int chunkZ, final ChunkData chunk,
			final int x, final int z, final int xx, final int zz) {
		int y  = HOUSE_Y;
		int cy = HOUSE_Y + SUBFLOOR + HOUSE_H;
		// lobby floor
		chunk.setBlock(x, y, z, Material.BEDROCK);
		y++;
		for (int yy=0; yy<SUBFLOOR; yy++) {
			chunk.setBlock(x, y+yy, z, HOUSE_FLOOR);
		}
		y += SUBFLOOR;
		final double value = this.noiseHouseWalls.getNoiseRot(xx, cy, 0.25);
		if (value < -0.9 || value > 0.9) {
			for (int iy=0; iy<3; iy++) {
				chunk.setBlock(x, y+iy, z, HOUSE_WALLS);
			}
		}
		if (BUILD_ROOF) {
			cy++;
			for (int i=0; i<SUBCEILING; i++) {
				chunk.setBlock(x, cy+i, z, HOUSE_FLOOR);
			}
		}
	}



}
