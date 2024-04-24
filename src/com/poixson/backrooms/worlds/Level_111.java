package com.poixson.backrooms.worlds;

import java.io.IOException;
import java.util.LinkedList;

import org.bukkit.Location;
import org.bukkit.World;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.backrooms.BackroomsWorld;
import com.poixson.backrooms.dynmap.GeneratorTemplate;
import com.poixson.backrooms.gens.Gen_111;
import com.poixson.backrooms.listeners.Listener_111;
import com.poixson.tools.abstractions.Tuple;
import com.poixson.tools.plotter.BlockPlotter;
import com.poixson.tools.worldstore.VarStore;


// 111 | Run For Your Life!
public class Level_111 extends BackroomsWorld {
	public static final String KEY_NEXT_HALL_X = "next_hall_x";

	// generators
	public final Gen_111 gen_111;

	// listeners
	protected final Listener_111 listener_111;

	protected final VarStore varstore;



	public Level_111(final BackroomsPlugin plugin) {
		super(plugin);
		// generators
		this.gen_111 = this.register(new Gen_111(this, this.seed));
		// listeners
		this.listener_111 = new Listener_111(plugin);
		// next hall
		this.varstore = new VarStore("level_111");
		this.varstore.start(plugin);
		// dynmap
		if (plugin.enableDynmapConfigGen()) {
			final GeneratorTemplate gen_tpl = new GeneratorTemplate(plugin, 111);
			gen_tpl.add(111, "run", "Run For Your Life", this.gen_111.level_y+this.gen_111.level_h+1);
		}
	}



	@Override
	public void register() {
		super.register();
		this.listener_111.register();
	}
	@Override
	public void unregister() {
		super.unregister();
		this.listener_111.unregister();
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
		return 111; // run for your life
	}
	@Override
	public boolean containsLevel(final int level) {
		return (level == 111);
	}



	@Override
	public int getY(final int level) {
		return this.gen_111.level_y + 1;
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
	protected void generate(final LinkedList<Tuple<BlockPlotter, StringBuilder[][]>> plots,
			final ChunkData chunk, final int chunkX, final int chunkZ) {
		this.gen_111.generate(null, plots, chunk, chunkX, chunkZ);
	}



}
