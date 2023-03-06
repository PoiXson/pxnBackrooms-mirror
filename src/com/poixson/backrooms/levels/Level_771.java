package com.poixson.backrooms.levels;

import static com.poixson.utils.RandomUtils.Rnd10K;

import java.util.LinkedList;

import org.bukkit.Location;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.backrooms.dynmap.GeneratorTemplate;
import com.poixson.commonmc.tools.plotter.BlockPlotter;
import com.poixson.backrooms.listeners.Listener_771;


// 771 | Crossroads
public class Level_771 extends LevelBackrooms {

	public static final int LEVEL_Y = -61;
	public static final int LEVEL_H = 360;

	// generators
	public final Gen_771 gen;

	// listeners
	protected final Listener_771 listener_771;



	public Level_771(final BackroomsPlugin plugin) {
		super(plugin, 771);
		// dynmap
		if (plugin.enableDynmapConfigGen()) {
			final GeneratorTemplate gen_tpl = new GeneratorTemplate(plugin, 0);
			gen_tpl.add(771, "crossroads", "Crossroads");
		}
		// generators
		this.gen = this.register(new Gen_771(this, LEVEL_Y, LEVEL_H));
		// listeners
		this.listener_771 = new Listener_771(plugin);
	}



	@Override
	public void register() {
		super.register();
		this.listener_771.register();
	}
	@Override
	public void unregister() {
		super.unregister();
		this.listener_771.unregister();
	}



	@Override
	public Location getSpawn(final int level) {
		int x = (Rnd10K() * 2) - 10000;
		int z = (Rnd10K() * 2) - 10000;
		if (Math.abs(x) > Math.abs(z)) {
			z = 0;
		} else {
			x = 0;
		}
		return this.getSpawn(level, x, z);
	}
	@Override
	public Location getSpawn(final int level, final int x, final int z) {
		return this.getSpawn(level, 10, x, LEVEL_Y+LEVEL_H, z);
	}

	@Override
	public int getY(final int level) {
		return LEVEL_Y + LEVEL_H;
	}
	@Override
	public int getMaxY(final int level) {
		return LEVEL_Y + LEVEL_H + 20;
	}



	@Override
	protected void generate(final int chunkX, final int chunkZ,
			final ChunkData chunk, final LinkedList<BlockPlotter> plots) {
		this.gen.generate(null, chunk, plots, chunkX, chunkZ);
	}



}
