package com.poixson.backrooms.levels;

import java.util.LinkedList;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.backrooms.dynmap.GeneratorTemplate;
import com.poixson.commonmc.tools.plotter.BlockPlotter;
import com.poixson.backrooms.listeners.Listener_078;


// 78 | Space
public class Level_078 extends LevelBackrooms {

	public static final boolean ENABLE_GEN_078 = true;

	// generators
	public final Gen_078 gen;

	// listeners
	protected final Listener_078 listener_078;



	public Level_078(final BackroomsPlugin plugin) {
		super(plugin, 78);
		// dynmap
		if (plugin.enableDynmapConfigGen()) {
			final GeneratorTemplate gen_tpl = new GeneratorTemplate(plugin, 0);
			gen_tpl.add(78, "space", "Space");
		}
		// generators
		this.gen = this.register(new Gen_078(this, 0, 0));
		// listeners
		this.listener_078 = new Listener_078(plugin);
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
		this.gen.generate(null, chunk, plots, chunkX, chunkZ);
	}



}
