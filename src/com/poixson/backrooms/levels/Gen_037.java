package com.poixson.backrooms.levels;

import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator.ChunkData;


// 37 | Poolrooms
public class Gen_037 extends BackroomsGenerator {

	public static final boolean BUILD_ROOF = Level_000.BUILD_ROOF;
	public static final int     SUBFLOOR   = Level_000.SUBFLOOR;
	public static final int     SUBCEILING = Level_000.SUBCEILING;

	public static final int POOLS_Y = Level_000.Y_037;
	public static final int POOLS_H = Level_000.H_037;

	public static final Material POOLS_WALL       = Material.DARK_PRISMARINE;
	public static final Material POOLS_SUBFLOOR   = Material.DARK_PRISMARINE;
	public static final Material POOLS_SUBCEILING = Material.DARK_PRISMARINE;



	public Gen_037() {
		super();
	}
	@Override
	public void unload() {
	}



	@Override
	public void setSeed(final int seed) {
	}



	public void generatePools(final int chunkX, final int chunkZ, final ChunkData chunk,
			final int x, final int z, final int xx, final int zz) {
		int y  = POOLS_Y;
		int cy = POOLS_Y + SUBFLOOR + POOLS_H;
		// lobby floor
		chunk.setBlock(x, y, z, Material.BEDROCK);
		y++;
		for (int yy=0; yy<SUBFLOOR; yy++) {
			chunk.setBlock(x, y+yy, z, POOLS_SUBFLOOR);
		}
		y += SUBFLOOR;
//TODO
		if (BUILD_ROOF) {
			cy++;
			for (int i=0; i<SUBCEILING; i++) {
				chunk.setBlock(x, cy+i, z, POOLS_SUBCEILING);
			}
		}
	}



}
