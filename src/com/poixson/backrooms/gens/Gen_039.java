package com.poixson.backrooms.gens;

import java.util.LinkedList;

import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.backrooms.BackroomsGen;
import com.poixson.backrooms.BackroomsLevel;
import com.poixson.backrooms.PreGenData;
import com.poixson.tools.abstractions.Tuple;
import com.poixson.tools.plotter.BlockPlotter;


// 39 | Metro
public class Gen_039 extends BackroomsGen {



	public Gen_039(final BackroomsLevel backlevel, final int seed,
			final int level_y, final int level_h) {
		super(backlevel, seed, level_y, level_h);
	}



	@Override
	public void generate(final PreGenData pregen,
			final LinkedList<Tuple<BlockPlotter, StringBuilder[][]>> plots,
			final ChunkData chunk, final int chunkX, final int chunkZ) {
//TODO
	}



	// -------------------------------------------------------------------------------
	// configs



	@Override
	protected void loadConfig() {
	}
	@Override
	public void configDefaults() {
	}



}
