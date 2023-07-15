package com.poixson.backrooms.worlds;

import static com.poixson.backrooms.BackroomsPlugin.LOG_PREFIX;
import static com.poixson.commonmc.tools.plugin.xJavaPlugin.LOG;

import java.util.LinkedList;

import org.bukkit.Location;
import org.bukkit.World;

import com.poixson.backrooms.BackroomsLevel;
import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.backrooms.dynmap.GeneratorTemplate;
import com.poixson.backrooms.gens.Gen_771;
import com.poixson.backrooms.listeners.Listener_771;
import com.poixson.commonmc.tools.plotter.BlockPlotter;
import com.poixson.commonmc.tools.worldstore.LocationStoreManager;
import com.poixson.utils.RandomUtils;


// 771 | Crossroads
public class Level_771 extends BackroomsLevel {

	public static final boolean ENABLE_GEN_771 = true;

	public static final int LEVEL_Y = -61;
	public static final int LEVEL_H = 360;

	// generators
	public final Gen_771 gen;

	// listeners
	protected final Listener_771 listener_771;

	// exit locations
	public final LocationStoreManager portal_ladder;
	public final LocationStoreManager portal_drop;
	public final LocationStoreManager portal_void;
	// loot
	public final LocationStoreManager loot_chests_upper;
	public final LocationStoreManager loot_chests_lower;



	public Level_771(final BackroomsPlugin plugin) {
		super(plugin, 771);
		// dynmap
		if (plugin.enableDynmapConfigGen()) {
			final GeneratorTemplate gen_tpl = new GeneratorTemplate(plugin, 0);
			gen_tpl.add(771, "crossroads", "Crossroads");
			gen_tpl.commit();
		}
		// generators
		this.gen = this.register(new Gen_771(this, LEVEL_Y, LEVEL_H));
		// listeners
		this.listener_771 = new Listener_771(plugin);
		// exit locations
		this.portal_ladder     = new LocationStoreManager("level771", "portal_ladder" ); // upper/lower ladder
		this.portal_drop       = new LocationStoreManager("level771", "portal_drop"   ); // shaft to lower bridge
		this.portal_void       = new LocationStoreManager("level771", "portal_void"   ); // shaft to void
		// loot
		this.loot_chests_upper = new LocationStoreManager("level771", "loot_upper"    );
		this.loot_chests_lower = new LocationStoreManager("level771", "loot_lower"    );
	}



	@Override
	public void register() {
		super.register();
		this.portal_ladder    .start(this.plugin); // upper/lower ladder
		this.portal_drop      .start(this.plugin); // shaft to lower bridge
		this.portal_void      .start(this.plugin); // shaft to void
		this.loot_chests_upper.start(this.plugin); // loot upper
		this.loot_chests_lower.start(this.plugin); // loot lower
		this.listener_771.register();
	}
	@Override
	public void unregister() {
		super.unregister();
		this.listener_771.unregister();
		this.portal_ladder    .saveAll();
		this.portal_drop      .saveAll();
		this.portal_void      .saveAll();
		this.loot_chests_upper.saveAll();
		this.loot_chests_lower.saveAll();
	}



	@Override
	public Location getNewSpawnArea(final int level) {
		final int distance = this.plugin.getSpawnDistance();
		final int y = this.getY(level);
		int x = RandomUtils.GetRandom(0-distance, distance);
		int z = RandomUtils.GetRandom(0-distance, distance);
		if (Math.abs(x) > Math.abs(z)) z = 0;
		else                           x = 0;
		final World world = this.plugin.getWorldFromLevel(level);
		if (world == null) throw new RuntimeException("Invalid backrooms level: "+Integer.toString(level));
		return this.getSpawnNear(world.getBlockAt(x, y, z).getLocation());
	}
	@Override
	public Location getSpawnNear(final Location spawn, final int distance) {
		final int distanceMin = Math.floorDiv(distance, 3);
		final float yaw = (float) RandomUtils.GetRandom(0, 360);
		final World world = spawn.getWorld();
		final int y = spawn.getBlockY();
		// true if north/south roads
		final boolean axis = (spawn.getBlockX() == 0);
		int x = 0;
		int z = 0;
		Location near, valid;
		for (int t=0; t<10; t++) {
			for (int iy=0; iy<10; iy++) {
				if (axis) z = spawn.getBlockZ() + RandomUtils.GetRandom(distanceMin, distance);
				else      x = spawn.getBlockX() + RandomUtils.GetRandom(distanceMin, distance);
				near = world.getBlockAt(x, y+iy, z).getLocation();
				valid = this.validateSpawn(near);
				if (valid != null) {
					valid.setYaw(yaw);
					return valid;
				}
			}
		}
		LOG.warning(LOG_PREFIX + "Failed to find a safe spawn location: " + spawn.toString());
		return spawn;
	}



	@Override
	public int getY(final int level) {
		return LEVEL_Y + LEVEL_H + 1;
	}
	@Override
	public int getMaxY(final int level) {
		return 320;
	}
	@Override
	public boolean containsLevel(final int level) {
		return (level == 771);
	}



	@Override
	protected void generate(final int chunkX, final int chunkZ,
			final ChunkData chunk, final LinkedList<BlockPlotter> plots) {
		this.gen.generate(null, chunk, plots, chunkX, chunkZ);
	}



}
