/*
package com.poixson.backrooms.gens;

import static com.poixson.backrooms.worlds.Level_033.ENABLE_GEN_033;

import java.util.LinkedList;

import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.commonmc.tools.plotter.BlockPlotter;


// 33 | Run For Your Life!
public class Gen_033 extends BackroomsGen {



	public Gen_033(final BackroomsLevel backlevel,
			final int level_y, final int level_h) {
		super(backlevel, level_y, level_h);
	}



	@Override
	public void generate(final PreGenData pregen, final ChunkData chunk,
			final LinkedList<BlockPlotter> plots, final int chunkX, final int chunkZ) {
		if (!ENABLE_GEN_033) return;
		for (int iz=0; iz<16; iz++) {
			for (int ix=0; ix<16; ix++) {
//				final int xx = (chunkX * 16) + ix;
//				final int zz = (chunkZ * 16) + iz;
				switch (ix) {
				case 0:
				case 15:
					for (int iy=0; iy<this.level_h; iy++)
						chunk.setBlock(ix, iy+this.level_y+2, iz, Material.BEDROCK);
					break;
				case 1:
				case 14:
					for (int iy=0; iy<this.level_h; iy++)
						chunk.setBlock(ix, iy+this.level_y+2, iz, Material.BLACKSTONE);
					break;
				default: break;
				}
			} // end ix
		} // end iz
	}



}
*/
