package com.poixson.backrooms.gens;

import java.util.LinkedList;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.backrooms.BackroomsGen;
import com.poixson.backrooms.BackroomsLevel;
import com.poixson.backrooms.PreGenData;
import com.poixson.tools.abstractions.Tuple;
import com.poixson.tools.plotter.BlockPlotter;


// 39 | Metro
public class Gen_039 extends BackroomsGen {

	// default params
	public static final int DEFAULT_LEVEL_H    = 8;
	public static final int DEFAULT_SUBFLOOR   = 3;
	public static final int DEFAULT_SUBCEILING = 3;

	// params
	public final boolean enable_gen;
	public final boolean enable_top;
	public final int     level_y;
	public final int     level_h;
	public final int     subfloor;
	public final int     subceiling;



	public Gen_039(final BackroomsLevel backlevel, final int seed, final BackroomsGen gen_below) {
		super(backlevel, gen_below, seed);
		final int level_number = this.getLevelNumber();
		final ConfigurationSection cfgParams = this.plugin.getConfigLevelParams(level_number);
		// params
		this.enable_gen = cfgParams.getBoolean("Enable-Gen"  );
		this.enable_top = cfgParams.getBoolean("Enable-Top"  );
		this.level_y    = cfgParams.getInt(    "Level-Y"     );
		this.level_h    = cfgParams.getInt(    "Level-Height");
		this.subfloor   = cfgParams.getInt(    "SubFloor"    );
		this.subceiling = cfgParams.getInt(    "SubCeiling"  );
	}



	@Override
	public int getLevelNumber() {
		return 39;
	}

	@Override
	public int getNextY() {
		return this.level_y + this.bedrock_barrier + this.subfloor + this.level_h + this.subceiling + 2;
	}



	@Override
	public void generate(final PreGenData pregen,
			final LinkedList<Tuple<BlockPlotter, StringBuilder[][]>> plots,
			final ChunkData chunk, final int chunkX, final int chunkZ) {
		if (!this.enable_gen) return;
//TODO
	}



	// -------------------------------------------------------------------------------
	// configs



	@Override
	protected void configDefaults(final ConfigurationSection cfgParams, final ConfigurationSection cfgBlocks) {
		cfgParams.addDefault("Enable-Gen",   Boolean.TRUE                       );
		cfgParams.addDefault("Enable-Top",   Boolean.TRUE                       );
		cfgParams.addDefault("Level-Y",      Integer.valueOf(this.getDefaultY()));
		cfgParams.addDefault("Level-Height", Integer.valueOf(DEFAULT_LEVEL_H   ));
		cfgParams.addDefault("SubFloor",     Integer.valueOf(DEFAULT_SUBFLOOR  ));
		cfgParams.addDefault("SubCeiling",   Integer.valueOf(DEFAULT_SUBCEILING));
	}



}
