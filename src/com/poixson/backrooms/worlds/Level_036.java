/*
package com.poixson.backrooms.worlds;

import java.util.LinkedList;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.backrooms.dynmap.GeneratorTemplate;
import com.poixson.pluginlib.tools.plotter.BlockPlotter;


// 36 | Airport
public class Level_036 extends BackroomsLevel {

	public static final boolean ENABLE_GEN_036 = true;

	// generators
	public final Gen_036 gen;



	public Level_036(final BackroomsPlugin plugin) {
		super(plugin, 36);
		// dynmap
		if (plugin.enableDynmapConfigGen()) {
			final GeneratorTemplate gen_tpl = new GeneratorTemplate(plugin, 0);
			gen_tpl.add(36, "airport", "Airport");
		}
		// generators
		this.gen = this.register(new Gen_036(this, 0, 0));
	}



	@Override
	public void register() {
		super.register();
		this.listener_036.register();
	}
	@Override
	public void unregister() {
		super.unregister();
		this.listener_036.unregister();
	}



	@Override
	public int getY(final int level) {
		return 255;
	}
	@Override
	public int getMaxY(final int level) {
		return 319;
	}
	@Override
	public boolean containsLevel(final int level) {
		return (level == 36);
	}



	@Override
	protected void generate(final int chunkX, final int chunkZ,
			final ChunkData chunk, final LinkedList<BlockPlotter> plots) {
		this.gen.generate(null, chunk, plots, chunkX, chunkZ);
	}



}
*/
