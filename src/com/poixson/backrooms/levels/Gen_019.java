package com.poixson.backrooms.levels;

import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.utils.FastNoiseLiteD;
import com.poixson.utils.FastNoiseLiteD.CellularDistanceFunction;
import com.poixson.utils.FastNoiseLiteD.FractalType;
import com.poixson.utils.FastNoiseLiteD.NoiseType;


// 19 | Attic
public class Gen_019 extends GenBackrooms {

	public static final boolean ENABLED = false;

	public static final boolean BUILD_ROOF = Level_000.BUILD_ROOF;
	public static final int     SUBFLOOR   = Level_000.SUBFLOOR;
	public static final int     SUBCEILING = Level_000.SUBCEILING;

	public static final int ATTIC_Y = Level_000.Y_019;
	public static final int ATTIC_H = Level_000.H_019;
	public static final int ATTIC_M = Math.floorDiv(ATTIC_H, 2);

	public static final Material ATTIC_FLOOR = Material.SPRUCE_PLANKS;
	public static final Material ATTIC_WALLS = Material.SPRUCE_PLANKS;

	// noise
	protected final FastNoiseLiteD noiseAtticWalls;

	// populators
	public final Pop_019 atticPop;



	public Gen_019() {
		super();
		// attic walls
		this.noiseAtticWalls = new FastNoiseLiteD();
		this.noiseAtticWalls.setFrequency(0.02);
		this.noiseAtticWalls.setNoiseType(NoiseType.Cellular);
		this.noiseAtticWalls.setFractalType(FractalType.PingPong);
		this.noiseAtticWalls.setCellularDistanceFunction(CellularDistanceFunction.Manhattan);
		// populators
		this.atticPop = new Pop_019();
	}
	@Override
	public void unload() {
	}



	@Override
	public void setSeed(final int seed) {
		this.noiseAtticWalls.setSeed(seed);
	}



	public void generateAttic(
			final ChunkData chunk, final int chunkX, final int chunkZ,
			final int x, final int z, final int xx, final int zz) {
		if (!ENABLED) return;
		int y  = ATTIC_Y;
		int cy = ATTIC_Y + SUBFLOOR + ATTIC_H;
		// lobby floor
		chunk.setBlock(x, y, z, Material.BEDROCK);
		y++;
		for (int yy=0; yy<SUBFLOOR; yy++) {
			chunk.setBlock(x, y+yy, z, ATTIC_FLOOR);
		}
		y += SUBFLOOR;
		final double value = this.noiseAtticWalls.getNoiseRot(xx, cy, 0.25);
		if (value < -0.9 || value > 0.9) {
			for (int iy=0; iy<3; iy++) {
				chunk.setBlock(x, y+iy, z, ATTIC_WALLS);
			}
		}
		// second floor
		chunk.setBlock(x, y+ATTIC_M, z, ATTIC_WALLS);
		if (BUILD_ROOF) {
			cy++;
			for (int i=0; i<SUBCEILING; i++) {
				chunk.setBlock(x, cy+i, z, ATTIC_FLOOR);
			}
		}
	}



}
