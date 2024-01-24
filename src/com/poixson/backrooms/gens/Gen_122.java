package com.poixson.backrooms.gens;

import java.util.LinkedList;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.backrooms.BackroomsGen;
import com.poixson.backrooms.BackroomsLevel;
import com.poixson.backrooms.PreGenData;
import com.poixson.tools.abstractions.Tuple;
import com.poixson.tools.plotter.BlockPlotter;


// 122 | Mall
public class Gen_122 extends BackroomsGen {



	public Gen_122(final BackroomsLevel backlevel, final int seed,
			final int level_y, final int level_h) {
		super(backlevel, seed, level_y, level_h);
	}



	@Override
	public void generate(final PreGenData pregen,
			final LinkedList<Tuple<BlockPlotter, StringBuilder[][]>> plots,
			final ChunkData chunk, final int chunkX, final int chunkZ) {
	}



	// -------------------------------------------------------------------------------
	// configs



	@Override
	protected void loadConfig() {
	}
	public static void ConfigDefaults(final FileConfiguration cfg) {
	}



}
