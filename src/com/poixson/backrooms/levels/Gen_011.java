package com.poixson.backrooms.levels;

import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.backrooms.BackroomsPlugin;


// 11 | City
public class Gen_011 extends GenBackrooms {



	public Gen_011(final BackroomsPlugin plugin, final int level_y, final int level_h) {
		super(plugin, level_y, level_h);
	}



	@Override
	public void generate(final PreGenData pregen,
			final ChunkData chunk, final int chunkX, final int chunkZ) {
		for (int z=0; z<16; z++) {
			for (int x=0; x<16; x++) {
				final int xx = (chunkX * 16) + x;
				final int zz = (chunkZ * 16) + z;
				
				
				
			} // end x
		} // end z
	}



}
