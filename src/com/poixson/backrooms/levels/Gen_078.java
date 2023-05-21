package com.poixson.backrooms.levels;

import static com.poixson.backrooms.levels.Level_078.ENABLE_GEN_078;

import java.util.LinkedList;

import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.commonmc.tools.plotter.BlockPlotter;


// 78 | Space
public class Gen_078 extends GenBackrooms {



	public Gen_078(final LevelBackrooms backlevel,
			final int level_y, final int level_h) {
		super(backlevel, level_y, level_h);
	}



	@Override
	public void generate(final PreGenData pregen, final ChunkData chunk,
			final LinkedList<BlockPlotter> plots, final int chunkX, final int chunkZ) {
		if (!ENABLE_GEN_078) return;
		for (int iz=0; iz<16; iz++) {
			for (int ix=0; ix<16; ix++) {
//				final int xx = (chunkX * 16) + ix;
//				final int zz = (chunkZ * 16) + iz;
			} // end ix
		} // end iz
	}



}
