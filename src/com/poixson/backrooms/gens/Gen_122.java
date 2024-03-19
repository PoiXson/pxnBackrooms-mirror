package com.poixson.backrooms.gens;

import java.util.LinkedList;

import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.backrooms.BackroomsGen;
import com.poixson.backrooms.BackroomsLevel;
import com.poixson.backrooms.PreGenData;
import com.poixson.backrooms.worlds.Level_011;
import com.poixson.tools.abstractions.Tuple;
import com.poixson.tools.plotter.BlockPlotter;


// 122 | Mall
public class Gen_122 extends BackroomsGen {



	public Gen_122(final BackroomsLevel backlevel, final int seed,
			final int level_y, final int level_h) {
		super(backlevel, seed, level_y, level_h);
	}



	@Override
	public int getLevelNumber() {
		return 122;
	}



	@Override
	public void generate(final PreGenData pregen,
			final LinkedList<Tuple<BlockPlotter, StringBuilder[][]>> plots,
			final ChunkData chunk, final int chunkX, final int chunkZ) {
//TODO
		final int y = Level_011.Y_122;
		for (int iz=0; iz<16; iz++) {
			for (int ix=0; ix<16; ix++)
				chunk.setBlock(ix, y, iz, Material.BEDROCK);
		}
	}



	// -------------------------------------------------------------------------------
	// configs



	@Override
	protected void loadConfig(final ConfigurationSection cfgParams, final ConfigurationSection cfgBlocks) {
	}
	@Override
	protected void configDefaults(final ConfigurationSection cfgParams, final ConfigurationSection cfgBlocks) {
	}



}
