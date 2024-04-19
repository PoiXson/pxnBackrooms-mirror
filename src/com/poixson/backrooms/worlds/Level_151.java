/*
package com.poixson.backrooms.worlds;

import java.util.LinkedList;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.backrooms.dynmap.GeneratorTemplate;
import com.poixson.pluginlib.tools.plotter.BlockPlotter;


// 151 | Dollhouse
public class Level_151 extends BackroomsWorld {

	public static final boolean ENABLE_GEN_151 = true;
	public static final boolean ENABLE_TOP_151 = true;

	public static final int SUBFLOOR   = 3;
	public static final int SUBCEILING = 3;

	public static final int LEVEL_Y = 100;
	public static final int LEVEL_H = 100;

	// generators
	public final Gen_151 gen_151;



	public Level_151(final BackroomsPlugin plugin) {
		super(plugin, 151);
		// dynmap
		if (plugin.enableDynmapConfigGen()) {
			final GeneratorTemplate gen_tpl = new GeneratorTemplate(plugin, 0);
			gen_tpl.add(151, "dollhouse", "Dollhouse", LEVEL_Y+LEVEL_H+SUBFLOOR+1);
		}
		// generators
		this.gen_151 = this.register(new Gen_151(this, this.seed, LEVEL_Y, LEVEL_H, SUBFLOOR, SUBCEILING));
	}



	@Override
	public int getY(final int level) {
		return LEVEL_Y;
	}
	@Override
	public int getMaxY(final int level) {
		return 319;
	}
	@Override
	public boolean containsLevel(final int level) {
		return (level == 151);
	}



	@Override
	protected void generate(final int chunkX, final int chunkZ,
			final ChunkData chunk, final LinkedList<BlockPlotter> plots) {
		this.gen_151.generate(null, chunk, plots, chunkX, chunkZ);
	}



}
*/
