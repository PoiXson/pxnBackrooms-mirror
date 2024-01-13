package com.poixson.backrooms.worlds;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.World;

import com.poixson.backrooms.BackroomsLevel;
import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.backrooms.PreGenData;
import com.poixson.backrooms.dynmap.GeneratorTemplate;
import com.poixson.backrooms.gens.Gen_000;
import com.poixson.backrooms.gens.Gen_000.LobbyData;
import com.poixson.backrooms.gens.Gen_001;
import com.poixson.backrooms.gens.Gen_001.BasementData;
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
import com.poixson.tools.xRand;
import com.poixson.tools.dao.Iab;
import com.poixson.tools.plotter.BlockPlotter;
import com.poixson.tools.worldstore.LocationStoreManager;


// 309 | Radio Station
// 188 | The Windows
//  19 | Attic
//   5 | Hotel
//  37 | Poolrooms
//   6 | Lights Out
//   0 | Lobby
//  23 | Overgrowth
//   1 | Basement
public class Level_000 extends BackroomsLevel {

	public static final boolean ENABLE_GEN_309 = true;
	public static final boolean ENABLE_GEN_019 = true;
	public static final boolean ENABLE_GEN_005 = true;
	public static final boolean ENABLE_GEN_037 = true;
	public static final boolean ENABLE_GEN_006 = true;
	public static final boolean ENABLE_GEN_000 = true;
	public static final boolean ENABLE_GEN_023 = true;
	public static final boolean ENABLE_GEN_001 = true;

	public static final boolean ENABLE_TOP_309 = true;
	public static final boolean ENABLE_TOP_005 = true;
	public static final boolean ENABLE_TOP_037 = true;
	public static final boolean ENABLE_TOP_000 = true;
	public static final boolean ENABLE_TOP_023 = true;
	public static final boolean ENABLE_TOP_001 = true;

	public static final int SUBFLOOR   = 3;
	public static final int SUBCEILING = 3;

	// basement
	public static final int Y_001 = 0;
	public static final int H_001 = 30;
	// overgrowth
	public static final int Y_023 = Y_001 + H_001 + SUBFLOOR + 2;
	public static final int H_023 = 5;
	// lobby
	public static final int Y_000 = Y_023 + H_023 + SUBFLOOR + SUBCEILING + 3;
	public static final int H_000 = 5;
	// lights out
	public static final int Y_006 = Y_000 + H_000 + SUBFLOOR + SUBCEILING + 3;
	public static final int H_006 = 5;
	// pools
	public static final int Y_037 = Y_006 + H_006 + 1;
	public static final int H_037 = 10;
	// hotel
	public static final int Y_005 = Y_037 + H_037 + SUBFLOOR + SUBCEILING + 3;
	public static final int H_005 = 5;
	// attic
	public static final int Y_019 = Y_005 + H_005 + SUBFLOOR + SUBCEILING + 1;
	public static final int H_019 = 10;
	// radio station
	public static final int Y_309 = Y_019 + H_019 + SUBFLOOR + 1;
	// the windows
	public static final int Y_188 = Y_001;
	public static final int H_188 = Y_309;

	// generators
	public final Gen_001 gen_001;
	public final Gen_023 gen_023;
	public final Gen_000 gen_000;
	public final Gen_006 gen_006;
	public final Gen_037 gen_037;
	public final Gen_005 gen_005;
	public final Gen_019 gen_019;
	public final Gen_188 gen_188;
	public final Gen_309 gen_309;

	// populators
	public final Pop_001 pop_001;
	public final Pop_005 pop_005;
	public final Pop_037 pop_037;
	public final Pop_309 pop_309;

	// listeners
	protected final Listener_023 listener_023;

	// exit locations
	public final LocationStoreManager portal_0_to_1;
	public final LocationStoreManager portal_0_to_6;
	public final LocationStoreManager portal_6_to_33;
	public final LocationStoreManager portal_0_to_37;
	public final LocationStoreManager portal_1_well;
	public final LocationStoreManager portal_5_to_19;
	public final LocationStoreManager portal_5_to_37;
	public final LocationStoreManager portal_19_to_309;
	public final LocationStoreManager portal_309_stairs;
	public final LocationStoreManager portal_309_doors;
	public final LocationStoreManager cheese_rooms;
	// loot
	public final LocationStoreManager loot_chests_0;



	public Level_000(final BackroomsPlugin plugin) {
		super(plugin);
		// dynmap
		if (plugin.enableDynmapConfigGen()) {
			final GeneratorTemplate gen_tpl = new GeneratorTemplate(plugin, 0);
			gen_tpl.add(  1, "basement",  "Basement",   Y_001              +10);
			gen_tpl.add( 23, "overgrow",  "Overgrowth", Y_023               +8);
			gen_tpl.add(  0, "lobby",     "Lobby",      Y_000+H_000+SUBFLOOR+1);
			gen_tpl.add(  6, "lightsout", "Lights Out", Y_006+H_006           );
			gen_tpl.add( 37, "poolrooms", "Poolrooms",  Y_037+H_037         +1);
			gen_tpl.add( 05, "hotel",     "Hotel",      Y_005+H_005+SUBFLOOR+1);
			gen_tpl.add( 19, "attic",     "Attic",      Y_019      +SUBFLOOR+1);
			gen_tpl.add(309, "radio",     "Radio Station"                     );
			gen_tpl.commit();
		}
		// generators
		this.gen_001 = this.register(new Gen_001(this, this.seed, Y_001, H_001)); // basement
		this.gen_023 = this.register(new Gen_023(this, this.seed, Y_023, H_023)); // overgrowth
		this.gen_000 = this.register(new Gen_000(this, this.seed, Y_000, H_000)); // lobby
		this.gen_006 = this.register(new Gen_006(this, this.seed, Y_006, H_006)); // lights out
		this.gen_037 = this.register(new Gen_037(this, this.seed, Y_037, H_037)); // pools
		this.gen_005 = this.register(new Gen_005(this, this.seed, Y_005, H_005)); // hotel
		this.gen_019 = this.register(new Gen_019(this, this.seed, Y_019, H_019)); // attic
		this.gen_188 = this.register(new Gen_188(this, this.seed, Y_188, H_188)); // the windows
		this.gen_309 = this.register(new Gen_309(this, this.seed, Y_309,     0)); // radio station
		// populators
		this.pop_001 = this.register(new Pop_001(this)); // basement
		this.pop_005 = this.register(new Pop_005(this)); // hotel
		this.pop_037 = this.register(new Pop_037(this)); // pools
		this.pop_309 = this.register(new Pop_309(this)); // radio station
		// listeners
		this.listener_023 = new Listener_023(plugin);
		// exit locations
		this.portal_0_to_1     = new LocationStoreManager(plugin, "level0", "portal_0_to_1"    ); // lobby to basement
		this.portal_0_to_6     = new LocationStoreManager(plugin, "level0", "portal_0_to_6"    ); // lobby to lights out
		this.portal_6_to_33    = new LocationStoreManager(plugin, "level0", "portal_6_to_33"   ); // run for your life button
		this.portal_0_to_37    = new LocationStoreManager(plugin, "level0", "portal_0_to_37"   ); // lobby to pools
		this.portal_1_well     = new LocationStoreManager(plugin, "level0", "portal_1_well"    ); // basement well
		this.portal_5_to_19    = new LocationStoreManager(plugin, "level0", "portal_5_to_19"   ); // hotel to attic
		this.portal_5_to_37    = new LocationStoreManager(plugin, "level0", "portal_5_to_37"   ); // hotel to pools
		this.portal_19_to_309  = new LocationStoreManager(plugin, "level0", "portal_19_to_309" ); // attic to forest
		this.portal_309_stairs = new LocationStoreManager(plugin, "level0", "portal_309_stairs"); // stairs in the forest
		this.portal_309_doors  = new LocationStoreManager(plugin, "level0", "portal_309_doors" ); // doors in the forest
		this.cheese_rooms      = new LocationStoreManager(plugin, "level0", "cheese_rooms"     ); // cheese hotel room
		// loot
		this.loot_chests_0     = new LocationStoreManager(plugin, "level0", "loot_0"           ); // loot chests
	}



	@Override
	public void register() {
		super.register();
		this.portal_0_to_1    .start();
		this.portal_0_to_6    .start();
		this.portal_6_to_33   .start();
		this.portal_0_to_37   .start();
		this.portal_1_well    .start();
		this.portal_5_to_19   .start();
		this.portal_5_to_37   .start();
		this.portal_19_to_309 .start();
		this.portal_309_stairs.start();
		this.portal_309_doors .start();
		this.cheese_rooms     .start();
		this.loot_chests_0    .start();
		this.listener_023.register();
	}
	@Override
	public void unregister() {
		super.unregister();
		this.listener_023.unregister();
		this.portal_0_to_1    .stop();
		this.portal_0_to_6    .stop();
		this.portal_6_to_33   .stop();
		this.portal_0_to_37   .stop();
		this.portal_1_well    .stop();
		this.portal_5_to_19   .stop();
		this.portal_5_to_37   .stop();
		this.portal_19_to_309 .stop();
		this.portal_309_stairs.stop();
		this.portal_309_doors .stop();
		this.cheese_rooms     .stop();
		this.loot_chests_0    .stop();
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
		if (y < Y_309
		&&  x >= -46 && x <= 62
		&&  z >= -46 && z <= 62)
			return 188;
		if (y < Y_023) return  1; // basement
		if (y < Y_000) return 23; // overgrowth
		if (y < Y_006) return  0; // lobby
		if (y < Y_037) return  6; // lights out
		if (y < Y_005) return 37; // pools
		if (y < Y_019) return  5; // hotel
		if (y < Y_309) return 19; // attic
		return 309;               // radio station
	}
	@Override
	public boolean containsLevel(final int level) {
		switch (level) {
		case   1: // basement
		case  23: // overgrowth
		case   0: // lobby
		case   6: // lights out
		case  37: // pools
		case   5: // hotel
		case  19: // attic
		case 309: // radio station
		case 188: // the windows
			return true;
		default: return false;
		}
	}



	@Override
	public int getY(final int level) {
		switch (level) {
		case   1: return Y_001; // basement
		case  23: return Y_023; // overgrowth
		case   0: return Y_000; // lobby
		case   6: return Y_006; // lights out
		case  37: return Y_037; // pools
		case   5: return Y_005; // hotel
		case  19: return Y_019; // attic
		case 309: return Y_309; // radio station
		case 188: return Y_188; // the windows
		default: throw new RuntimeException("Invalid backrooms level: "+Integer.toString(level));
		}
	}
	@Override
	public int getMaxY(final int level) {
		switch (level) {
		case   1: return Y_023 - 1; // basement
		case  23: return Y_000 - 1; // overgrowth
		case   0: return Y_006 - 1; // lobby
		case   6: return Y_037 - 1; // lights out
		case  37: return Y_005 - 1; // pools
		case   5: return Y_019 - 1; // hotel
		case  19: return Y_309 - 1; // attic
		case 309: return 320;       // radio station
		case 188: return Y_309 - 1; // the windows
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
		case  0: // lobby
		case  6: // lights out
		case 37: // pools
		case  5: // hotel
		case 19: // attic
			return super.getNewSpawnArea(level);
		// the windows
		case 188: {
			final int x = 0;
			final int z = 0;
			final World world = this.plugin.getWorldFromLevel(level);
			if (world == null) throw new RuntimeException("Invalid backrooms level: "+Integer.toString(level));
			return world.getBlockAt(x, 0, z).getLocation();
		}
		// radio station
		case 309: {
			final int distance = this.plugin.getSpawnDistance();
			final int z = xRand.Get(0, distance).nextInt();
			final int x = this.gen_309.getPathX(z);
			final World world = this.plugin.getWorldFromLevel(level);
			if (world == null) throw new RuntimeException("Invalid backrooms level: "+Integer.toString(level));
			return world.getBlockAt(x, 0, z).getLocation();
		}
		default: throw new RuntimeException("Invalid backrooms level: "+Integer.toString(level));
		}
	}

	@Override
	public Location getFixedSpawnLocation(final World world, final Random random) {
		return world.getBlockAt(100, this.getY(0), 100).getLocation();
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



	public class PregenLevel0 implements PreGenData {
		public final HashMap<Iab, LobbyData>    lobby    = new HashMap<Iab, LobbyData>();
		public final HashMap<Iab, BasementData> basement = new HashMap<Iab, BasementData>();
		public final HashMap<Iab, HotelData>    hotel    = new HashMap<Iab, HotelData>();
		public final HashMap<Iab, PoolData>     pools    = new HashMap<Iab, PoolData>();
		public PregenLevel0() {}
	}



	@Override
	protected void generate(final int chunkX, final int chunkZ,
			final ChunkData chunk, final LinkedList<BlockPlotter> plots) {
		// level 188 - the windows
		if (chunkX >-4 && chunkZ >-4
		&&  chunkX < 4 && chunkZ < 4) {
			this.gen_188.generate(null, chunk, null, chunkX, chunkZ);
		// other levels
		} else {
			// pre-generate
			final PregenLevel0 pregen = new PregenLevel0();
			this.gen_000.pregenerate(pregen.lobby,    chunkX, chunkZ); // lobby
			this.gen_001.pregenerate(pregen.basement, chunkX, chunkZ); // basement
			this.gen_005.pregenerate(pregen.hotel,    chunkX, chunkZ); // hotel
			this.gen_037.pregenerate(pregen.pools,    chunkX, chunkZ); // pools
			// generate
			this.gen_001.generate(pregen, chunk, plots, chunkX, chunkZ); // basement
			this.gen_023.generate(pregen, chunk, plots, chunkX, chunkZ); // overgrowth
			this.gen_000.generate(pregen, chunk, plots, chunkX, chunkZ); // lobby
			this.gen_006.generate(pregen, chunk, plots, chunkX, chunkZ); // lights out
			this.gen_037.generate(pregen, chunk, plots, chunkX, chunkZ); // pools
			this.gen_005.generate(pregen, chunk, plots, chunkX, chunkZ); // hotel
			this.gen_019.generate(pregen, chunk, plots, chunkX, chunkZ); // attic
		}
		this.gen_309.generate(null, chunk, null, chunkX, chunkZ); // radio station
	}



}
