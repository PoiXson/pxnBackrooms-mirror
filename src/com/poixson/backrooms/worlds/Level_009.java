/*
package com.poixson.backrooms.worlds;

import java.util.LinkedList;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.backrooms.dynmap.GeneratorTemplate;
import com.poixson.pluginlib.tools.plotter.BlockPlotter;


// 9 | Suburbs
public class Level_009 extends BackroomsLevel {

	public static final boolean ENABLE_GEN_009 = true;
	public static final boolean ENABLE_TOP_009 = true;

	public static final int LEVEL_Y = 0;

	// generators
	public final Gen_009 gen_009;



	public Level_009(final BackroomsPlugin plugin) {
		super(plugin, 9);
		// generators
		this.gen_009 = this.register(new Gen_009(this, this.seed, LEVEL_Y, 0));
		// dynmap
		if (plugin.enableDynmapConfigGen()) {
			final GeneratorTemplate gen_tpl = new GeneratorTemplate(plugin, 0);
			gen_tpl.add(9, "suburbs", "Suburbs");
		}
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
		return (level == 9);
	}



	@Override
	protected void generate(final int chunkX, final int chunkZ,
			final ChunkData chunk, final LinkedList<BlockPlotter> plots) {
		this.gen_009.generate(null, chunk, plots, chunkX, chunkZ);
	}



}
*/
