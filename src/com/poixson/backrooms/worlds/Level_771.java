package com.poixson.backrooms.worlds;

import java.util.LinkedList;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.backrooms.BackroomsWorld;
import com.poixson.backrooms.dynmap.GeneratorTemplate;
import com.poixson.backrooms.gens.Gen_771;
import com.poixson.backrooms.listeners.Listener_771;
import com.poixson.tools.abstractions.Tuple;
import com.poixson.tools.plotter.BlockPlotter;
import com.poixson.tools.worldstore.LocationStoreManager;


// 771 | Crossroads
public class Level_771 extends BackroomsWorld {

	// generators
	public final Gen_771 gen_771;

	// listeners
	protected final Listener_771 listener_771;

	// exit locations
	public final LocationStoreManager portal_ladder;
	public final LocationStoreManager portal_drop;
	public final LocationStoreManager portal_void;
	// loot
	public final LocationStoreManager loot_chests_upper;
	public final LocationStoreManager loot_chests_lower;



	public Level_771(final BackroomsPlugin plugin)
			throws InvalidConfigurationException {
		super(plugin);
		// dynmap
		if (plugin.enableDynmapConfigGen()) {
			final GeneratorTemplate gen_tpl = new GeneratorTemplate(plugin, 771);
			gen_tpl.add(771, "crossroads", "Crossroads");
			gen_tpl.commit();
		}
		// generators
		this.gen_771 = this.register(new Gen_771(this, this.seed));
		// listeners
		this.listener_771 = new Listener_771(plugin);
		// exit locations
		this.portal_ladder     = new LocationStoreManager(plugin, "level_771", "portal_ladder"); // upper/lower ladder
		this.portal_drop       = new LocationStoreManager(plugin, "level_771", "portal_drop"  ); // shaft to lower bridge
		this.portal_void       = new LocationStoreManager(plugin, "level_771", "portal_void"  ); // shaft to void
		// loot
		this.loot_chests_upper = new LocationStoreManager(plugin, "level_771", "loot_upper"   ); // upper path loot chests
		this.loot_chests_lower = new LocationStoreManager(plugin, "level_771", "loot_lower"   ); // lower path loot chests
	}



	@Override
	public void register() {
		super.register();
		this.portal_ladder    .start();
		this.portal_drop      .start();
		this.portal_void      .start();
		this.loot_chests_upper.start();
		this.loot_chests_lower.start();
		this.listener_771.register();
	}
	@Override
	public void unregister() {
		super.unregister();
		this.listener_771.unregister();
		this.portal_ladder    .stop();
		this.portal_drop      .stop();
		this.portal_void      .stop();
		this.loot_chests_upper.stop();
		this.loot_chests_lower.stop();
	}



	// -------------------------------------------------------------------------------
	// locations



	@Override
	public int getMainLevel() {
		return 771; // crossroads
	}
	@Override
	public boolean containsLevel(final int level) {
		return (level == 771);
	}



	@Override
	public int getOpenY(final int level) {
		return this.gen_771.getOpenY();
	}

	@Override
	public int getMinY(final int level) {
		return this.gen_771.getMinY();
	}
	@Override
	public int getMaxY(final int level) {
		return this.gen_771.getMaxY();
	}



	// -------------------------------------------------------------------------------
	// spawn



	@Override
	public Location getNewSpawnArea(final int level) {
		final int distance = this.plugin.getSpawnDistance();
		int x = this.random.nextInt(0-distance, distance);
		int z = this.random.nextInt(0-distance, distance);
		if (Math.abs(x) > Math.abs(z)) z = 0;
		else                           x = 0;
		final World world = this.plugin.getWorldFromLevel(level);
		if (world == null) throw new RuntimeException("Invalid backrooms level: "+Integer.toString(level));
		return world.getBlockAt(x, 0, z).getLocation();
	}
	@Override
	public Location getSpawnNear(final int level, final Location spawn) {
		final int max_y = this.getMaxY(level);
		final int distance_near = DEFAULT_SPAWN_NEAR_DISTANCE;
		final int distance_min  = Math.floorDiv(distance_near, 3);
		final float yaw = (float) this.random.nextDbl(0.0, 360.0);
		final World world = spawn.getWorld();
		final int y = this.getOpenY(level);
		final int h = max_y - y;
		// true if north/south roads
		final boolean axis = (spawn.getBlockX() == 0);
		int x = 0;
		int z = 0;
		Location near, valid;
		for (int tries=0; tries<20; tries++) {
			for (int iy=0; iy<h; iy++) {
				if (axis) z = spawn.getBlockZ() + this.random.nextInt(distance_min, distance_near);
				else      x = spawn.getBlockX() + this.random.nextInt(distance_min, distance_near);
				near = world.getBlockAt(x, y+iy, z).getLocation();
				valid = this.validSpawn(near);
				if (valid != null) {
					valid.setYaw(yaw);
					return valid;
				}
			}
		}
		this.log().warning("Failed to find a safe spawn location: "+spawn.toString());
		return spawn;
	}



	// -------------------------------------------------------------------------------
	// generate



	@Override
	protected void generate(final LinkedList<Tuple<BlockPlotter, StringBuilder[][]>> plots,
			final ChunkData chunk, final int chunkX, final int chunkZ) {
		this.gen_771.generate(null, plots, chunk, chunkX, chunkZ);
	}



}
