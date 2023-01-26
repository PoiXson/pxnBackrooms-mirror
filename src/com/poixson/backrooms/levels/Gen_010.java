package com.poixson.backrooms.levels;

import java.util.Map;

import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.tools.dao.Dxy;


// 10 | Field of Wheat
public class Gen_010 extends GenBackrooms {



	public Gen_010(final BackroomsPlugin plugin, final int level_y, final int level_h) {
		super(plugin, level_y, level_h);
	}



	@Override
	public void generate(final Map<Dxy, ? extends PreGenData> datamap,
			final ChunkData chunk, final int chunkX, final int chunkZ) {
		for (int z=0; z<16; z++) {
			for (int x=0; x<16; x++) {
				final int xx = (chunkX * 16) + x;
				final int zz = (chunkZ * 16) + z;
				
				
				
			} // end x
		} // end z
	}



}
