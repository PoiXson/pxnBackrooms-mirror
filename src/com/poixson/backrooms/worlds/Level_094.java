/*
package com.poixson.backrooms.worlds;

import java.util.LinkedList;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.backrooms.dynmap.GeneratorTemplate;
import com.poixson.commonmc.tools.plotter.BlockPlotter;


// 94 | Motion
public class Level_094 extends BackroomsLevel {

	public static final boolean ENABLE_GEN_094 = true;

	// generators
	public final Gen_094 gen;

	// listeners
	protected final Listener_094 listener_094;



	public Level_094(final BackroomsPlugin plugin) {
		super(plugin, 94);
		// dynmap
		if (plugin.enableDynmapConfigGen()) {
			final GeneratorTemplate gen_tpl = new GeneratorTemplate(plugin, 0);
			gen_tpl.add(94, "motion", "Motion");
		}
		// generators
		this.gen = this.register(new Gen_094(this, 0, 0));
	}



	@Override
	public void register() {
		super.register();
		this.listener_094.register();
	}
	@Override
	public void unregister() {
		super.unregister();
		this.listener_094.unregister();
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
		return (level == 94);
	}



	@Override
	protected void generate(final int chunkX, final int chunkZ,
			final ChunkData chunk, final LinkedList<BlockPlotter> plots) {
		this.gen.generate(null, chunk, plots, chunkX, chunkZ);
	}



}
*/
