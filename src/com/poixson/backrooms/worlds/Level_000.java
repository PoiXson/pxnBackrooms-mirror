package com.poixson.backrooms.worlds;

import java.util.HashMap;
import java.util.LinkedList;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.backrooms.BackroomsWorld;
import com.poixson.backrooms.PreGenData;
import com.poixson.backrooms.dynmap.GeneratorTemplate;
import com.poixson.backrooms.gens.Gen_000;
import com.poixson.backrooms.gens.Gen_000.LobbyData;
import com.poixson.backrooms.gens.Gen_001;
import com.poixson.backrooms.gens.Gen_001.BasementData;
import com.poixson.backrooms.gens.Gen_002;
import com.poixson.backrooms.gens.Gen_002.PipeData;
import com.poixson.backrooms.gens.Gen_004;
import com.poixson.backrooms.gens.Gen_004.DuctData;
import com.poixson.backrooms.gens.Gen_005;
import com.poixson.backrooms.gens.Gen_005.HotelData;
import com.poixson.backrooms.gens.Gen_006;
import com.poixson.backrooms.gens.Gen_019;
import com.poixson.backrooms.gens.Gen_023;
import com.poixson.backrooms.gens.Gen_037;
import com.poixson.backrooms.gens.Gen_037.PoolData;
import com.poixson.backrooms.gens.Gen_188;
import com.poixson.backrooms.gens.Gen_309;
import com.poixson.backrooms.gens.Pop_001;
import com.poixson.backrooms.gens.Pop_005;
import com.poixson.backrooms.gens.Pop_037;
import com.poixson.backrooms.gens.Pop_309;
import com.poixson.backrooms.listeners.Listener_023;
import com.poixson.tools.abstractions.Tuple;
import com.poixson.tools.dao.Iab;
import com.poixson.tools.plotter.BlockPlotter;
import com.poixson.tools.worldstore.LocationStoreManager;


// 309 | Radio Station
//  19 | Attic
//   5 | Hotel
//  37 | Poolrooms
//   6 | Lights Out
//   4 | Ductwork
//   0 | Lobby
//   2 | Pipe Dreams
//  23 | Overgrowth
//   1 | Basement
// 188 | The Windows
public class Level_000 extends BackroomsWorld {

	// generators
	public final Gen_001 gen_001;
	public final Gen_023 gen_023;
	public final Gen_002 gen_002;
	public final Gen_000 gen_000;
	public final Gen_004 gen_004;
	public final Gen_006 gen_006;
	public final Gen_037 gen_037;
	public final Gen_005 gen_005;
	public final Gen_019 gen_019;
	public final Gen_309 gen_309;
	public final Gen_188 gen_188;

	// populators
	public final Pop_001 pop_001;
	public final Pop_005 pop_005;
	public final Pop_037 pop_037;
	public final Pop_309 pop_309;

	// listeners
	protected final Listener_023 listener_023;

	// exit locations
	public final LocationStoreManager portal_000_to_001;
	public final LocationStoreManager portal_000_to_002;
	public final LocationStoreManager portal_000_to_004;
	public final LocationStoreManager portal_000_to_006;
	public final LocationStoreManager portal_006_to_111;
	public final LocationStoreManager portal_000_to_037;
	public final LocationStoreManager portal_001_well;
	public final LocationStoreManager portal_005_to_019;
	public final LocationStoreManager portal_005_to_037;
	public final LocationStoreManager portal_019_to_309;
	public final LocationStoreManager portal_309_stairs;
	public final LocationStoreManager portal_309_doors;
	public final LocationStoreManager cheese_rooms;
	// loot
	public final LocationStoreManager loot_0;



	public Level_000(final BackroomsPlugin plugin)
			throws InvalidConfigurationException {
		super(plugin);
		// generators
		this.gen_001 = this.register(new Gen_001(this, this.seed                      )); // basement
		this.gen_023 = this.register(new Gen_023(this, this.seed, this.gen_001        )); // overgrowth
		this.gen_002 = this.register(new Gen_002(this, this.seed, this.gen_023        )); // pipe dreams
		this.gen_000 = this.register(new Gen_000(this, this.seed, this.gen_002        )); // lobby
		this.gen_004 = this.register(new Gen_004(this, this.seed, this.gen_000        )); // ductwork
		this.gen_006 = this.register(new Gen_006(this, this.seed, this.gen_004        )); // lights out
		this.gen_037 = this.register(new Gen_037(this, this.seed, this.gen_006        )); // poolrooms
		this.gen_005 = this.register(new Gen_005(this, this.seed, this.gen_037        )); // hotel
		this.gen_019 = this.register(new Gen_019(this, this.seed, this.gen_005        )); // attic
		this.gen_309 = this.register(new Gen_309(this, this.seed, this.gen_019        )); // radio station
		this.gen_188 = this.register(new Gen_188(this, this.seed, this.gen_001.level_y)); // the windows
		// populators
		this.pop_001 = this.register(new Pop_001(this)); // basement
		this.pop_005 = this.register(new Pop_005(this)); // hotel
		this.pop_037 = this.register(new Pop_037(this)); // poolrooms
		this.pop_309 = this.register(new Pop_309(this)); // radio station
		// listeners
		this.listener_023 = new Listener_023(plugin);
		// exit locations
		this.portal_000_to_001 = new LocationStoreManager(plugin, "level_000", "portal_000_to_001"); // lobby to basement
		this.portal_000_to_002 = new LocationStoreManager(plugin, "level_000", "portal_000_to_002"); // lobby to pipe dreams
		this.portal_000_to_004 = new LocationStoreManager(plugin, "level_000", "portal_000_to_004"); // lobby to ductwork
		this.portal_000_to_006 = new LocationStoreManager(plugin, "level_000", "portal_000_to_006"); // lobby to lights out
		this.portal_006_to_111 = new LocationStoreManager(plugin, "level_000", "portal_006_to_111"); // run for your life button
		this.portal_000_to_037 = new LocationStoreManager(plugin, "level_000", "portal_000_to_037"); // lobby to poolrooms
		this.portal_001_well   = new LocationStoreManager(plugin, "level_000", "portal_001_well"  ); // basement well
		this.portal_005_to_019 = new LocationStoreManager(plugin, "level_000", "portal_005_to_019"); // hotel to attic
		this.portal_005_to_037 = new LocationStoreManager(plugin, "level_000", "portal_005_to_037"); // hotel to poolrooms
		this.portal_019_to_309 = new LocationStoreManager(plugin, "level_000", "portal_019_to_309"); // attic to forest
		this.portal_309_stairs = new LocationStoreManager(plugin, "level_000", "portal_309_stairs"); // stairs in the forest
		this.portal_309_doors  = new LocationStoreManager(plugin, "level_000", "portal_309_doors" ); // doors in the forest
		this.cheese_rooms      = new LocationStoreManager(plugin, "level_000", "cheese_rooms"     ); // cheese hotel room
		// loot
		this.loot_0            = new LocationStoreManager(plugin, "level_000", "loot_000"         ); // loot chests
		// dynmap
		if (plugin.enableDynmapConfigGen()) {
			final GeneratorTemplate gen_tpl = new GeneratorTemplate(plugin, 0);
			gen_tpl.add(  1, "basement",   "Basement",    this.gen_001.getLampY() - 1);
			gen_tpl.add( 23, "overgrow",   "Overgrowth",  this.gen_023.getOpenY() + 3);
			gen_tpl.add(  2, "pipedreams", "Pipe Dreams", this.gen_002.getOpenY()    );
			gen_tpl.add(  0, "lobby",      "Lobby",       this.gen_000.getOpenY() + 1);
			gen_tpl.add(  4, "ductwork",   "Ductwork",    this.gen_004.getOpenY() + 1);
			gen_tpl.add(  6, "lightsout",  "Lights Out",  this.gen_006.getOpenY() + 1);
			gen_tpl.add( 37, "poolrooms",  "Poolrooms",   this.gen_037.getOpenY() + this.gen_037.water_depth + 1);
			gen_tpl.add( 05, "hotel",      "Hotel",       this.gen_005.getOpenY() + 2);
			gen_tpl.add( 19, "attic",      "Attic",       this.gen_019.getOpenY()    );
			gen_tpl.add(309, "radio",      "Radio Station"                           );
			gen_tpl.commit();
		}
	}



	@Override
	public void register() {
		super.register();
		this.portal_000_to_001.start();
		this.portal_000_to_002.start();
		this.portal_000_to_004.start();
		this.portal_000_to_006.start();
		this.portal_006_to_111.start();
		this.portal_000_to_037.start();
		this.portal_001_well  .start();
		this.portal_005_to_019.start();
		this.portal_005_to_037.start();
		this.portal_019_to_309.start();
		this.portal_309_stairs.start();
		this.portal_309_doors .start();
		this.cheese_rooms     .start();
		this.loot_0           .start();
		this.listener_023.register();
	}
	@Override
	public void unregister() {
		super.unregister();
		this.listener_023.unregister();
		this.portal_000_to_001.stop();
		this.portal_000_to_002.stop();
		this.portal_000_to_004.stop();
		this.portal_000_to_006.stop();
		this.portal_006_to_111.stop();
		this.portal_000_to_037.stop();
		this.portal_001_well  .stop();
		this.portal_005_to_019.stop();
		this.portal_005_to_037.stop();
		this.portal_019_to_309.stop();
		this.portal_309_stairs.stop();
		this.portal_309_doors .stop();
		this.cheese_rooms     .stop();
		this.loot_0           .stop();
	}



	// -------------------------------------------------------------------------------
	// locations



	@Override
	public int getMainLevel() {
		return 0; // lobby
	}

	@Override
	public int getLevel(final Location loc) {
		final int x = loc.getBlockX();
		final int y = loc.getBlockY();
		final int z = loc.getBlockZ();
		// the windows
		if (y < this.gen_309.level_y
		&&  x >= -46 && x <= 62
		&&  z >= -46 && z <= 62)
			return 188;
		return this.getLevel(y);
	}
	@Override
	public int getLevel(final int y) {
		if (y < this.getMaxY( 1)) return  1; // basement
		if (y < this.getMaxY(23)) return 23; // overgrowth
		if (y < this.getMaxY( 2)) return  2; // pipe dreams
		if (y < this.getMaxY( 0)) return  0; // lobby
		if (y < this.getMaxY( 4)) return  4; // ductwork
		if (y < this.getMaxY( 6)) return  6; // lights out
		if (y < this.getMaxY(37)) return 37; // poolrooms
		if (y < this.getMaxY( 5)) return  5; // hotel
		if (y < this.getMaxY(19)) return 19; // attic
		return 309;                          // radio station
	}

	@Override
	public boolean containsLevel(final int level) {
		switch (level) {
		case   1: // basement
		case  23: // overgrowth
		case   2: // pipe dreams
		case   0: // lobby
		case   4: // ductwork
		case   6: // lights out
		case  37: // poolrooms
		case   5: // hotel
		case  19: // attic
		case 309: // radio station
		case 188: // the windows
			return true;
		default: return false;
		}
	}



	@Override
	public int getOpenY(final int level) {
		switch (level) {
		case   1: return this.gen_001.getOpenY(); // basement
		case  23: return this.gen_023.getOpenY(); // overgrowth
		case   2: return this.gen_002.getOpenY(); // pipe dreams
		case   0: return this.gen_000.getOpenY(); // lobby
		case   4: return this.gen_004.getOpenY(); // ductworrk
		case   6: return this.gen_006.getOpenY(); // lights out
		case  37: return this.gen_037.getOpenY(); // poolrooms
		case   5: return this.gen_005.getOpenY(); // hotel
		case  19: return this.gen_019.getOpenY(); // attic
		case 309: return this.gen_309.getOpenY(); // radio station
		default: throw new RuntimeException("Invalid backrooms level: "+Integer.toString(level));
		}
	}

	@Override
	public int getMinY(final int level) {
		switch (level) {
		case   1: return this.gen_001.getMinY(); // basement
		case  23: return this.gen_023.getMinY(); // overgrowth
		case   2: return this.gen_002.getMinY(); // pipe dreams
		case   0: return this.gen_000.getMinY(); // lobby
		case   4: return this.gen_004.getMinY(); // ductwork
		case   6: return this.gen_006.getMinY(); // lights out
		case  37: return this.gen_037.getMinY(); // poolrooms
		case   5: return this.gen_005.getMinY(); // hotel
		case  19: return this.gen_019.getMinY(); // attic
		case 309: return 320;                    // radio station
		case 188: return this.gen_188.getMinY(); // the windows
		default: throw new RuntimeException("Invalid backrooms level: "+Integer.toString(level));
		}
	}
	@Override
	public int getMaxY(final int level) {
		switch (level) {
		case   1: return this.gen_001.getMaxY(); // basement
		case  23: return this.gen_023.getMaxY(); // overgrowth
		case   2: return this.gen_002.getMaxY(); // pipe dreams
		case   0: return this.gen_000.getMaxY(); // lobby
		case   4: return this.gen_004.getMaxY(); // ductwork
		case   6: return this.gen_006.getMaxY(); // lights out
		case  37: return this.gen_037.getMaxY(); // poolrooms
		case   5: return this.gen_005.getMaxY(); // hotel
		case  19: return this.gen_019.getMaxY(); // attic
		case 309: return 320;                    // radio station
		case 188: return this.gen_188.getMaxY(); // the windows
		default: throw new RuntimeException("Invalid backrooms level: "+Integer.toString(level));
		}
	}



	// -------------------------------------------------------------------------------
	// spawn



	@Override
	public Location getSpawnArea(final int level) {
		switch (level) {
		case 188: return super.getSpawnArea(188);
		case 309: return super.getSpawnArea(309);
		default:  return super.getSpawnArea(  0);
		}
	}
	@Override
	public Location getNewSpawnArea(final int level) {
		switch (level) {
		case  1: // basement
		case 23: // overgrowth
		case  2: // pipe dreams
		case  0: // lobby
		case  4: // ductwork
		case  6: // lights out
		case 37: // poolrooms
		case  5: // hotel
		case 19: // attic
			return super.getNewSpawnArea(level);
		// radio station
		case 309: {
			final int distance = this.plugin.getSpawnDistance();
			final int z = this.random.nextInt(0, distance);
			final int x = this.gen_309.getPathX(z);
			final World world = this.plugin.getWorldFromLevel(level);
			if (world == null) throw new RuntimeException("Invalid backrooms level: "+Integer.toString(level));
			return world.getBlockAt(x, 0, z).getLocation();
		}
		// the windows
		case 188: {
			final World world = this.plugin.getWorldFromLevel(level);
			if (world == null) throw new RuntimeException("Invalid backrooms level: "+Integer.toString(level));
			return world.getBlockAt(0, this.gen_188.getOpenY(), 0).getLocation();
		}
		default: throw new RuntimeException("Invalid backrooms level: "+Integer.toString(level));
		}
	}



	@Override
	public int getSpawnDistanceNear(final int level) {
		switch (level) {
		case 188: return 45;
		default: break;
		}
		return DEFAULT_SPAWN_NEAR_DISTANCE;
	}



	// -------------------------------------------------------------------------------
	// generate



	public class Pregen_Level_000 implements PreGenData {
		public final HashMap<Iab, LobbyData>    lobby    = new HashMap<Iab, LobbyData>();
		public final HashMap<Iab, PipeData>     pipes    = new HashMap<Iab, PipeData>();
		public final HashMap<Iab, DuctData>     ducts    = new HashMap<Iab, DuctData>();
		public final HashMap<Iab, BasementData> basement = new HashMap<Iab, BasementData>();
		public final HashMap<Iab, HotelData>    hotel    = new HashMap<Iab, HotelData>();
		public final HashMap<Iab, PoolData>     pools    = new HashMap<Iab, PoolData>();
		public Pregen_Level_000() {}
	}



	@Override
	protected void generate(final LinkedList<Tuple<BlockPlotter, StringBuilder[][]>> plots,
			final ChunkData chunk, final int chunkX, final int chunkZ) {
		// level 188 - the windows
		if (chunkX >-4 && chunkZ >-4
		&&  chunkX < 4 && chunkZ < 4) {
			this.gen_188.generate(null, null, chunk, chunkX, chunkZ);
		// other levels
		} else {
			// pre-generate
			final Pregen_Level_000 pregen = new Pregen_Level_000();
			this.gen_000.pregenerate(pregen.lobby,    chunkX, chunkZ); // lobby
			this.gen_002.pregenerate(pregen.pipes,    chunkX, chunkZ); // pipedreams
			this.gen_004.pregenerate(pregen.ducts,    chunkX, chunkZ); // ductwork
			this.gen_001.pregenerate(pregen.basement, chunkX, chunkZ); // basement
			this.gen_005.pregenerate(pregen.hotel,    chunkX, chunkZ); // hotel
			this.gen_037.pregenerate(pregen.pools,    chunkX, chunkZ); // poolrooms
			// generate
			this.gen_001.generate(pregen, plots, chunk, chunkX, chunkZ); // basement
			this.gen_023.generate(pregen, plots, chunk, chunkX, chunkZ); // overgrowth
			this.gen_002.generate(pregen, plots, chunk, chunkX, chunkZ); // pipedreams
			this.gen_000.generate(pregen, plots, chunk, chunkX, chunkZ); // lobby
			this.gen_004.generate(pregen, plots, chunk, chunkX, chunkZ); // ductwork
			this.gen_006.generate(pregen, plots, chunk, chunkX, chunkZ); // lights out
			this.gen_037.generate(pregen, plots, chunk, chunkX, chunkZ); // poolrooms
			this.gen_005.generate(pregen, plots, chunk, chunkX, chunkZ); // hotel
			this.gen_019.generate(pregen, plots, chunk, chunkX, chunkZ); // attic
		}
		this.gen_309.generate(null, null, chunk, chunkX, chunkZ); // radio station
	}



}
