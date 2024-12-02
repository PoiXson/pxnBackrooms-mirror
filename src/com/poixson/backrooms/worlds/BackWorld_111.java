package com.poixson.backrooms.worlds;

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
import com.poixson.tools.worldstore.WorldStore_KeyVal;
import com.poixson.utils.MathUtils;


// 111 | Run For Your Life!
public class BackWorld_111 extends BackroomsWorld {
	public static final String KEY_NEXT_HALL_INDEX = "Next Hall Index";

	// generators
	public final Gen_111 gen_111;

	// listeners
	protected final Listener_111 listener_111;

	public final WorldStore_KeyVal keystore;



	public BackWorld_111(final BackroomsPlugin plugin) {
		super(plugin);
		// generators
		this.gen_111 = this.register(new Gen_111(this, this.seed));
		// listeners
		this.listener_111 = new Listener_111(plugin);
		// next hall
		this.keystore = new WorldStore_KeyVal(plugin, "level_111");
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
		this.keystore.start();
		// defaults
		final int hall_index = this.keystore.getInt(KEY_NEXT_HALL_INDEX);
		this.keystore.set(KEY_NEXT_HALL_INDEX, (hall_index>0 ? hall_index : 0));
	}
	@Override
	public void unregister() {
		super.unregister();
		this.listener_111.unregister();
		this.keystore.stop();
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
	public int[] getLevels() {
		return new int[] {
			111, // run for your life
		};
	}



	@Override
	public int getOpenY(final int level) {
		return this.gen_111.getOpenY();
	}

	@Override
	public int getMinY(final int level) {
		return this.gen_111.getMinY();
	}
	@Override
	public int getMaxY(final int level) {
		return this.gen_111.getMaxY();
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
		final int minor = 8; // z-order interleave
		final int major = 16;
		final int total = minor * major;
		final World world = this.plugin.getWorldFromLevel(level);
		if (world == null) throw new RuntimeException("Invalid backrooms level: "+Integer.toString(level));
		int hall_index = this.keystore.getInt(KEY_NEXT_HALL_INDEX);
		if (hall_index < 0) hall_index = 0;
		if (hall_index % total == 0) hall_index++;
		this.keystore.set(KEY_NEXT_HALL_INDEX, hall_index+1);
		final int inter_index = MathUtils.ZOrderInterleave(hall_index, minor, major);
		final int x = (inter_index * 16) + 7;
		final int y = this.getOpenY(level);
		this.log().info(String.format(
			"Run For Your Life!  index: #%d  hall: #%d  x: %d",
			Integer.valueOf(hall_index),
			Integer.valueOf(inter_index),
			Integer.valueOf(x)
		));
		return world.getBlockAt(x, y, 7).getLocation();
	}



	// -------------------------------------------------------------------------------
	// generate



	@Override
	protected void generate(final LinkedList<Tuple<BlockPlotter, StringBuilder[][]>> plots,
			final ChunkData chunk, final int chunkX, final int chunkZ) {
		this.gen_111.generate(null, plots, chunk, chunkX, chunkZ);
	}



}
