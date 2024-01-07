package com.poixson.backrooms.worlds;

import java.io.IOException;
import java.util.LinkedList;

import org.bukkit.Location;
import org.bukkit.World;

import com.poixson.backrooms.BackroomsLevel;
import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.backrooms.dynmap.GeneratorTemplate;
import com.poixson.backrooms.gens.Gen_033;
import com.poixson.backrooms.listeners.Listener_033;
import com.poixson.tools.plotter.BlockPlotter;
import com.poixson.tools.worldstore.VarStore;


// 33 | Run For Your Life!
public class Level_033 extends BackroomsLevel {
	public static final String KEY_NEXT_HALL_X = "next_hall_x";

	public static final boolean ENABLE_GEN_033 = true;
	public static final boolean ENABLE_TOP_033 = true;

	public static final int LEVEL_Y = 50;
	public static final int LEVEL_H = 8;

	// generators
	public final Gen_033 gen;

	// listeners
	protected final Listener_033 listener_033;

	protected final VarStore varstore;



	public Level_033(final BackroomsPlugin plugin) {
		super(plugin);
		// dynmap
		if (plugin.enableDynmapConfigGen()) {
			final GeneratorTemplate gen_tpl = new GeneratorTemplate(plugin, 33);
			gen_tpl.add(33, "run", "Run For Your Life", LEVEL_Y+LEVEL_H+1);
		}
		// generators
		this.gen = this.register(new Gen_033(this, this.seed, LEVEL_Y, LEVEL_H));
		// listeners
		this.listener_033 = new Listener_033(plugin);
		// next hall
		this.varstore = new VarStore("level33");
		this.varstore.start(plugin);
	}



	@Override
	public void register() {
		super.register();
		this.listener_033.register();
	}
	@Override
	public void unregister() {
		super.unregister();
		this.listener_033.unregister();
		try {
			this.varstore.save();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}



	// -------------------------------------------------------------------------------
	// locations



	@Override
	public int getMainLevel() {
		return 33; // run for your life
	}
	@Override
	public boolean containsLevel(final int level) {
		return (level == 33);
	}



	@Override
	public int getY(final int level) {
		return LEVEL_Y + 1;
	}
	@Override
	public int getMaxY(final int level) {
		return LEVEL_Y + LEVEL_H;
	}



	// -------------------------------------------------------------------------------
	// spawn



	@Override
	public Location getNewSpawnArea(final int level) {
		throw new UnsupportedOperationException();
	}
	@Override
	public Location getSpawnNear(final int level, final Location area) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Location getSpawnLocation(final int level) {
		final World world = this.plugin.getWorldFromLevel(level);
		if (world == null) throw new RuntimeException("Invalid backrooms level: "+Integer.toString(level));
		int x = this.varstore.getInt(KEY_NEXT_HALL_X);
		if (x == Integer.MIN_VALUE) x = 0;
		final int y = this.getY(level);
		this.varstore.set(KEY_NEXT_HALL_X, (Math.floorDiv(x, 16)+1) * 16);
		return world.getBlockAt(x+7, y, 7).getLocation();
	}



	// -------------------------------------------------------------------------------
	// generate



	@Override
	protected void generate(final int chunkX, final int chunkZ,
			final ChunkData chunk, final LinkedList<BlockPlotter> plots) {
		this.gen.generate(null, chunk, plots, chunkX, chunkZ);
	}



}
