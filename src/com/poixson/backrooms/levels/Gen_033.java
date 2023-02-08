package com.poixson.backrooms.levels;

import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.backrooms.BackroomsPlugin;


// 33 | Run For Your Life!
public class Gen_033 extends GenBackrooms {

	public static final boolean ENABLE_GENERATE = true;
	public static final boolean ENABLE_ROOF     = true;

	public final int subfloor;
	public final int subceiling;



	public Gen_033(final BackroomsPlugin plugin,
			final int level_y, final int level_h,
			final int subfloor, final int subceiling) {
		super(plugin, level_y, level_h);
		this.subfloor   = subfloor;
		this.subceiling = subceiling;
	}



	@Override
	public void generate(final PreGenData pregen,
			final ChunkData chunk, final int chunkX, final int chunkZ) {
		if (!ENABLE_GENERATE) return;
		for (int z=0; z<16; z++) {
			for (int x=0; x<16; x++) {
//				final int xx = (chunkX * 16) + x;
//				final int zz = (chunkZ * 16) + z;
				switch (x) {
				case 0:
				case 15:
					for (int y=0; y<this.level_h; y++) {
						chunk.setBlock(x, y+this.level_y+2, z, Material.BEDROCK);
					}
					break;
				case 1:
				case 14:
					for (int y=0; y<this.level_h; y++) {
						chunk.setBlock(x, y+this.level_y+2, z, Material.BLACKSTONE);
					}
					break;
				default: break;
				}
			} // end x
		} // end z
	}



}
