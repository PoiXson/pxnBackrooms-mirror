/*
package com.poixson.backrooms.worlds;

import java.util.LinkedList;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.backrooms.dynmap.GeneratorTemplate;
import com.poixson.backrooms.listeners.Listener_078;
import com.poixson.pluginlib.tools.plotter.BlockPlotter;


// 78 | Space
public class Level_078 extends BackroomsWorld {

	public static final boolean ENABLE_GEN_078 = true;

	// generators
	public final Gen_078 gen_078;

	// listeners
	protected final Listener_078 listener_078;



	public Level_078(final BackroomsPlugin plugin) {
		super(plugin, 78);
		// generators
		this.gen_078 = this.register(new Gen_078(this, this.seed, 0, 0));
		// listeners
		this.listener_078 = new Listener_078(plugin);
		// dynmap
		if (plugin.enableDynmapConfigGen()) {
			final GeneratorTemplate gen_tpl = new GeneratorTemplate(plugin, 0);
			gen_tpl.add(78, "space", "Space");
		}
	}



	@Override
	public void register() {
		super.register();
		this.listener_078.register();
	}
	@Override
	public void unregister() {
		super.unregister();
		this.listener_078.unregister();
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
		return (level == 78);
	}



	@Override
	protected void generate(final int chunkX, final int chunkZ,
			final ChunkData chunk, final LinkedList<BlockPlotter> plots) {
		this.gen_078.generate(null, chunk, plots, chunkX, chunkZ);
	}



}
*/
