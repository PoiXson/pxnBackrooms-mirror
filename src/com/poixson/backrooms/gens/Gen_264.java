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


// 264 | Museum
public class Gen_264 extends BackroomsGen {



	public Gen_264(final BackroomsLevel backlevel, final int seed,
			final int level_y, final int level_h) {
		super(backlevel, seed, level_y, level_h);
	// params
	public final boolean enable_gen;
	public final boolean enable_top;
		// params
		this.enable_gen = cfgParams.getBoolean("Enable-Gen"  );
		this.enable_top = cfgParams.getBoolean("Enable-Top"  );
	}



	@Override
	public int getLevelNumber() {
		return 264;
	}



	@Override
	public void generate(final PreGenData pregen,
			final LinkedList<Tuple<BlockPlotter, StringBuilder[][]>> plots,
			final ChunkData chunk, final int chunkX, final int chunkZ) {
		if (!this.enable_gen) return;
//TODO
		final int y = Level_011.Y_264;
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
		// params
		cfgParams.addDefault("Enable-Gen",   Boolean.TRUE                       );
		cfgParams.addDefault("Enable-Top",   Boolean.TRUE                       );
	}



}
