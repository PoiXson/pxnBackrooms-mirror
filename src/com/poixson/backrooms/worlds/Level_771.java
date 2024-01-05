package com.poixson.backrooms.worlds;

import static com.poixson.backrooms.BackroomsPlugin.LOG_PREFIX;

import java.util.LinkedList;

import org.bukkit.Location;
import org.bukkit.World;

import com.poixson.backrooms.BackroomsLevel;
import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.backrooms.dynmap.GeneratorTemplate;
import com.poixson.backrooms.gens.Gen_771;
import com.poixson.tools.xRand;
import com.poixson.tools.plotter.BlockPlotter;
import com.poixson.tools.worldstore.LocationStoreManager;


// 771 | Crossroads
public class Level_771 extends BackroomsLevel {

	public static final boolean ENABLE_GEN_771 = true;

	public static final int LEVEL_Y = -61;
	public static final int LEVEL_H = 360;

	// generators
	public final Gen_771 gen;

	// exit locations
	public final LocationStoreManager portal_ladder;
	public final LocationStoreManager portal_drop;
	public final LocationStoreManager portal_void;
	// loot
	public final LocationStoreManager loot_chests_upper;
	public final LocationStoreManager loot_chests_lower;



	public Level_771(final BackroomsPlugin plugin) {
		super(plugin);
		// dynmap
		if (plugin.enableDynmapConfigGen()) {
			final GeneratorTemplate gen_tpl = new GeneratorTemplate(plugin, 771);
			gen_tpl.add(771, "crossroads", "Crossroads");
			gen_tpl.commit();
		}
		// generators
		this.gen = this.register(new Gen_771(this, LEVEL_Y, LEVEL_H));
		// exit locations
		this.portal_ladder     = new LocationStoreManager(plugin, "level771", "portal_ladder"); // upper/lower ladder
		this.portal_drop       = new LocationStoreManager(plugin, "level771", "portal_drop"  ); // shaft to lower bridge
		this.portal_void       = new LocationStoreManager(plugin, "level771", "portal_void"  ); // shaft to void
		// loot
		this.loot_chests_upper = new LocationStoreManager(plugin, "level771", "loot_upper"   ); // upper path loot chests
		this.loot_chests_lower = new LocationStoreManager(plugin, "level771", "loot_lower"   ); // lower path loot chests
	}



	@Override
	public void register() {
		super.register();
		this.portal_ladder    .start();
		this.portal_drop      .start();
		this.portal_void      .start();
		this.loot_chests_upper.start();
		this.loot_chests_lower.start();
	}
	@Override
	public void unregister() {
		super.unregister();
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
	public int getY(final int level) {
		return LEVEL_Y + LEVEL_H + 1;
	}
	@Override
	public int getMaxY(final int level) {
		return 320;
	}



	// -------------------------------------------------------------------------------
	// spawn



	@Override
	public Location getNewSpawnArea(final int level) {
		final int distance = this.plugin.getSpawnDistance();
		final xRand rnd = xRand.Get(0-distance, distance);
		final int y = this.getY(level);
		int x = rnd.nextInt();
		int z = rnd.nextInt();
		if (Math.abs(x) > Math.abs(z)) z = 0;
		else                           x = 0;
		final World world = this.plugin.getWorldFromLevel(level);
		if (world == null) throw new RuntimeException("Invalid backrooms level: "+Integer.toString(level));
		return this.getSpawnNear(level, world.getBlockAt(x, y, z).getLocation());
	}
	@Override
	public Location getSpawnNear(final int level, final Location spawn) {
		final int max_y = this.getMaxY(level);
		final int distance_near = DEFAULT_SPAWN_NEAR_DISTANCE;
		final int distance_min  = Math.floorDiv(distance_near, 3);
		final xRand rnd = xRand.Get(distance_min, distance_near);
		final float yaw = (float) xRand.Get(0, 360).nextInt();
		final World world = spawn.getWorld();
		final int y = spawn.getBlockY();
		final int h = max_y - y;
		// true if north/south roads
		final boolean axis = (spawn.getBlockX() == 0);
		int x = 0;
		int z = 0;
		Location near, valid;
		for (int tries=0; tries<20; tries++) {
			for (int iy=0; iy<h; iy++) {
				if (axis) z = spawn.getBlockZ() + rnd.nextInt();
				else      x = spawn.getBlockX() + rnd.nextInt();
				near = world.getBlockAt(x, y+iy, z).getLocation();
				valid = this.validSpawn(near);
				if (valid != null) {
					valid.setYaw(yaw);
					return valid;
				}
			}
		}
		this.log().warning(String.format("%sFailed to find a safe spawn location: %s", LOG_PREFIX, spawn.toString()));
		return spawn;
	}



	// -------------------------------------------------------------------------------
	// generate



	@Override
	protected void generate(final int chunkX, final int chunkZ,
			final ChunkData chunk, final LinkedList<BlockPlotter> plots) {
		this.gen.generate(null, chunk, plots, chunkX, chunkZ);
	}



}
