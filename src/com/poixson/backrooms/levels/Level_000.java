package com.poixson.backrooms.levels;

import static com.poixson.utils.NumberUtils.Rnd10K;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.backrooms.levels.Gen_000.LobbyData;
import com.poixson.backrooms.levels.Gen_001.BasementData;
import com.poixson.backrooms.levels.Gen_005.HotelData;
import com.poixson.backrooms.levels.Gen_037.PoolData;
import com.poixson.tools.dao.Ixy;


// 309 | Radio Station
//  19 | Attic
//   5 | Hotel
//  37 | Poolrooms
//   6 | lights out
//   0 | Lobby
//   1 | Basement
public class Level_000 extends LevelBackrooms {

	public static final int SUBFLOOR   = 3;
	public static final int SUBCEILING = 3;

	// basement
	public static final int Y_001 = 0;
	public static final int H_001 = 30;
	// lobby
	public static final int Y_000 = Y_001 + H_001 + SUBFLOOR + SUBCEILING + 1;
	public static final int H_000 = 4;
	// lights out
	public static final int Y_006 = Y_000 + H_000 + SUBFLOOR + SUBCEILING + 2;
	public static final int H_006 = 5;
	// pools
	public static final int Y_037 = Y_006 + H_006 + 1;
	public static final int H_037 = 10;
	// hotel
	public static final int Y_005 = Y_037 + H_037 + SUBFLOOR + SUBCEILING + 3;
	public static final int H_005 = 6;
	// attic
	public static final int Y_019 = Y_005 + H_005 + SUBFLOOR + SUBCEILING + 1;
	public static final int H_019 = 10;
	// radio station
	public static final int Y_309 = Y_019 + H_019 + SUBFLOOR + SUBCEILING + 1;

	// generators
	public final Gen_001 gen_001;
	public final Gen_000 gen_000;
	public final Gen_006 gen_006;
	public final Gen_037 gen_037;
	public final Gen_005 gen_005;
	public final Gen_019 gen_019;
	public final Gen_309 gen_309;



	public Level_000(final BackroomsPlugin plugin) {
		super(plugin, 0);
		// generators
		this.gen_001 = this.register(new Gen_001(plugin, Y_001, H_001, SUBFLOOR, SUBCEILING)); // basement
		this.gen_000 = this.register(new Gen_000(plugin, Y_000, H_000, SUBFLOOR, SUBCEILING)); // lobby
		this.gen_006 = this.register(new Gen_006(plugin, Y_006, H_006                      )); // lights out
		this.gen_037 = this.register(new Gen_037(plugin, Y_037, H_037, SUBFLOOR, SUBCEILING)); // pools
		this.gen_005 = this.register(new Gen_005(plugin, Y_005, H_005, SUBFLOOR, SUBCEILING)); // hotel
		this.gen_019 = this.register(new Gen_019(plugin, Y_019, H_019, SUBFLOOR, SUBCEILING)); // attic
		this.gen_309 = this.register(new Gen_309(plugin, Y_309,     0, SUBFLOOR            )); // radio station
	}



	@Override
	public Location getSpawn(final int level) {
		final int x, z;
		switch (level) {
		case 1:  // basement
		case 0:  // lobby
		case 6:  // lights out
		case 37: // pools
		case 5:  // hotel
		case 19: // attic
			x = (Rnd10K() * 2) - 10000;
			z = (Rnd10K() * 2) - 10000;
			break;
		case 309: // radio station
//TODO: improve this
			x = (Rnd10K() / 5) - 1000;
			z = Rnd10K();
			break;
		default: throw new RuntimeException("Invalid backrooms level: "+Integer.toString(level));
		}
		return this.getSpawn(level, x, z);
	}
	@Override
	public Location getSpawn(final int level, final int x, final int z) {
		final int y, h;
		switch (level) {
		case   1: y = Y_001; h = H_001; break; // basement
		case   0: y = Y_000; h = H_000; break; // lobby
		case   6: y = Y_006; h = H_006; break; // lights out
		case  37: y = Y_037; h = H_037; break; // pools
		case   5: y = Y_005; h = H_005; break; // hotel
		case  19: y = Y_019; h = H_019; break; // attic
		case 309: y = Y_309; h = 10;    break; // radio station
		default: throw new RuntimeException("Invalid backrooms level: "+Integer.toString(level));
		}
		return this.getSpawn(level, h, x, y, z);
	}

	@Override
	public int getLevelFromY(final int y) {
		if (y < Y_000) return 1;  // basement
		if (y < Y_006) return 0;  // lobby
		if (y < Y_037) return 6;  // lights out
		if (y < Y_005) return 37; // pools
		if (y < Y_019) return 5;  // hotel
		if (y < Y_309) return 19; // attic
		return 309;               // radio station
	}
	@Override
	public int getY(final int level) {
		switch (level) {
		case 1:   return Y_001; // basement
		case 0:   return Y_000; // lobby
		case 6:   return Y_006; // lights out
		case 37:  return Y_037; // pools
		case 5:   return Y_005; // hotel
		case 19:  return Y_019; // attic
		case 309: return Y_309; // radio station
		default: break;
		}
		throw new RuntimeException("Invalid backrooms level: "+Integer.toString(level));
	}
	@Override
	public int getMaxY(final int level) {
		switch (level) {
		case 1:   return Y_000 - 1; // basement
		case 0:   return Y_006 - 1; // lobby
		case 6:   return Y_037 - 1; // lights out
		case 37:  return Y_005 - 1; // pools
		case 5:   return Y_019 - 1; // hotel
		case 19:  return Y_309 - 1; // attic
		case 309: return 320;       // radio station
		default: break;
		}
		throw new RuntimeException("Invalid backrooms level: "+Integer.toString(level));
	}



	public class PregenLevel0 implements PreGenData {
		public final HashMap<Ixy, LobbyData>    lobby    = new HashMap<Ixy, LobbyData>();
		public final HashMap<Ixy, BasementData> basement = new HashMap<Ixy, BasementData>();
		public final HashMap<Ixy, HotelData>    hotel    = new HashMap<Ixy, HotelData>();
		public final HashMap<Ixy, PoolData>     pools    = new HashMap<Ixy, PoolData>();
		public PregenLevel0() {
		}
	}



	@Override
	protected void generate(final ChunkData chunk, final int chunkX, final int chunkZ) {
		// pre-generate
		final PregenLevel0 pregen = new PregenLevel0();
		this.gen_000.pregenerate(pregen.lobby,    chunkX, chunkZ); // lobby
		this.gen_001.pregenerate(pregen.basement, chunkX, chunkZ); // basement
		this.gen_005.pregenerate(pregen.hotel,    chunkX, chunkZ); // hotel
		this.gen_037.pregenerate(pregen.pools,    chunkX, chunkZ); // pools
		// generate
		this.gen_001.generate(pregen, chunk, chunkX, chunkZ); // basement
		this.gen_000.generate(pregen, chunk, chunkX, chunkZ); // lobby
		this.gen_006.generate(pregen, chunk, chunkX, chunkZ); // lights out
		this.gen_037.generate(pregen, chunk, chunkX, chunkZ); // pools
		this.gen_005.generate(pregen, chunk, chunkX, chunkZ); // hotel
		this.gen_019.generate(pregen, chunk, chunkX, chunkZ); // attic
		this.gen_309.generate(pregen, chunk, chunkX, chunkZ); // radio station
	}



	@Override
	public List<BlockPopulator> getDefaultPopulators(final World world) {
		final LinkedList<BlockPopulator> list = new LinkedList<BlockPopulator>();
		if (Gen_309.ENABLE_ROOF)
			list.add(this.gen_309.treePop);
		list.add(this.gen_309.popRadio);
		list.add(this.gen_005.popRooms);
		list.add(this.gen_019.popAttic);
		list.add(this.gen_037.popPools);
		return list;
	}



}
