/*
package com.poixson.backrooms.worlds;

import java.util.LinkedList;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.backrooms.dynmap.GeneratorTemplate;
import com.poixson.pluginlib.tools.plotter.BlockPlotter;


// 10 | Field of Wheat
public class Level_010 extends BackroomsLevel {

	public static final boolean ENABLE_GEN_010 = true;

	public static final int LEVEL_Y = 0;

	// generators
	public final Gen_010 gen;



	public Level_010(final BackroomsPlugin plugin) {
		super(plugin, 10);
		// dynmap
		if (plugin.enableDynmapConfigGen()) {
			final GeneratorTemplate gen_tpl = new GeneratorTemplate(plugin, 0);
			gen_tpl.add(10, "wheat", "Field of Wheat");
		}
		// generators
		this.gen = this.register(new Gen_010(this, this.seed, LEVEL_Y, 0));
	}



	@Override
	public int getY(final int level) {
		return LEVEL_Y;
	}
	@Override
	public int getMaxY(final int level) {
		return 255;
	}
	@Override
	public boolean containsLevel(final int level) {
		return (level == 10);
	}



	@Override
	protected void generate(final int chunkX, final int chunkZ,
			final ChunkData chunk, final LinkedList<BlockPlotter> plots) {
		this.gen.generate(null, chunk, plots, chunkX, chunkZ);
	}



}
*/
