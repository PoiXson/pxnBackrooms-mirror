package com.poixson.backrooms.levels;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.tools.dao.Dxy;


// 33 | Run For Your Life!
public class Gen_033 extends GenBackrooms {

	public final boolean buildroof;
	public final int subfloor;
	public final int subceiling;



	public Gen_033(final BackroomsPlugin plugin, final int level_y, final int level_h,
			final boolean buildroof, final int subfloor, final int subceiling) {
		super(plugin, level_y, level_h);
		this.buildroof  = buildroof;
		this.subfloor   = subfloor;
		this.subceiling = subceiling;
	}



	@Override
	public void generate(final Map<Dxy, ? extends PreGenData> datamap,
			final ChunkData chunk, final int chunkX, final int chunkZ) {
		for (int z=0; z<16; z++) {
			for (int x=0; x<16; x++) {
				final int xx = (chunkX * 16) + x;
				final int zz = (chunkZ * 16) + z;
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
