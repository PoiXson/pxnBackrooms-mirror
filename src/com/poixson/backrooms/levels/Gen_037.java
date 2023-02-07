package com.poixson.backrooms.levels;

import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.backrooms.BackroomsPlugin;


// 37 | Poolrooms
public class Gen_037 extends GenBackrooms {

	public static final Material POOLS_WALL       = Material.DARK_PRISMARINE;
	public static final Material POOLS_SUBFLOOR   = Material.DARK_PRISMARINE;
	public static final Material POOLS_SUBCEILING = Material.DARK_PRISMARINE;

	public final boolean buildroof;
	public final int subfloor;
	public final int subceiling;



	public Gen_037(final BackroomsPlugin plugin, final int level_y, final int level_h,
			final boolean buildroof, final int subfloor, final int subceiling) {
		super(plugin, level_y, level_h);
		this.buildroof  = buildroof;
		this.subfloor   = subfloor;
		this.subceiling = subceiling;
	}



	@Override
	public void generate(final PreGenData pregen,
			final ChunkData chunk, final int chunkX, final int chunkZ) {
		for (int z=0; z<16; z++) {
			for (int x=0; x<16; x++) {
//				final int xx = (chunkX * 16) + x;
//				final int zz = (chunkZ * 16) + z;
/*
				int y  = this.level_y;
				int cy = this.level_y + SUBFLOOR + this.level_h;
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
*/
			} // end x
		} // end z
	}



}
