package com.poixson.backrooms.gens;

import java.util.LinkedList;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.backrooms.BackroomsGen;
import com.poixson.backrooms.BackroomsLevel;
import com.poixson.backrooms.PreGenData;
import com.poixson.tools.plotter.BlockPlotter;


// 4 | Abandoned Office
public class Gen_004 extends BackroomsGen {



	public Gen_004(final BackroomsLevel backlevel, final int level_y, final int level_h) {
		super(backlevel, level_y, level_h);
	}



	@Override
	public void generate(final PreGenData pregen, final ChunkData chunk,
			final LinkedList<BlockPlotter> plots, final int chunkX, final int chunkZ) {
//TODO
	}



	// -------------------------------------------------------------------------------
	// configs



	@Override
	protected void loadConfig() {
	}
	public static void ConfigDefaults(final FileConfiguration cfg) {
	}



}
