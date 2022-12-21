package com.poixson.backrooms.levels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.WorldInfo;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.backrooms.levels.Gen_005.HotelDAO;
import com.poixson.tools.dao.Dxy;


// 309 | Radio Station
//  19 | Attic
//   5 | Hotel
//  37 | Poolrooms
//   0 | Lobby
//   1 | Basement
public class Level_000 extends BackroomsLevel {

	public static final boolean BUILD_ROOF = true;

	public static final int SUBFLOOR   = 3;
	public static final int SUBCEILING = 3;

	// basement
	public static final int Y_001 = 0;
	public static final int H_001 = 30;
	// lobby
	public static final int Y_000 = SUBFLOOR + Y_001 + H_001 + 1;
	public static final int H_000 = 7;
	// pools
	public static final int Y_037 = SUBFLOOR + Y_000 + H_000 + SUBCEILING + 1;
	public static final int H_037 = 10;
	// hotel
	public static final int Y_005 = SUBFLOOR + Y_037 + H_037 + SUBCEILING + 1;
	public static final int H_005 = 7;
	// attic
	public static final int Y_019 = SUBFLOOR + Y_005 + H_005 + SUBCEILING + 1;
	public static final int H_019 = 10;
	// radio station
	public static final int Y_309 = SUBFLOOR + Y_019 + H_019 + SUBCEILING + 1;

	// generators
	public final Gen_001 gen_001;
	public final Gen_000 gen_000;
	public final Gen_037 gen_037;
	public final Gen_005 gen_005;
	public final Gen_019 gen_019;
	public final Gen_309 gen_309;



	public Level_000(final BackroomsPlugin plugin) {
		super(plugin);
		// generators
		this.gen_001 = new Gen_001();
		this.gen_000 = new Gen_000();
		this.gen_037 = new Gen_037();
		this.gen_005 = new Gen_005();
		this.gen_019 = new Gen_019();
		this.gen_309 = new Gen_309();
	}

	@Override
	public void unload() {
		this.gen_001.unload();
		this.gen_000.unload();
		this.gen_037.unload();
		this.gen_005.unload();
		this.gen_019.unload();
		this.gen_309.unload();
	}



	@Override
	public Location getSpawn(final int level) {
		final int x, z;
		switch (level) {
		case 1:
		case 0:
		case 37:
		case 5:
		case 19:
			x = (BackroomsPlugin.Rnd10K() * 2) - 10000;
			z = (BackroomsPlugin.Rnd10K() * 2) - 10000;
			break;
		case 309:
			x = (BackroomsPlugin.Rnd10K() / 5) - 1000;
			z = BackroomsPlugin.Rnd10K();
			break;
		default: throw new RuntimeException("Invalid backrooms level: "+Integer.toString(level));
		}
		return this.getSpawn(level, x, z);
	}
	@Override
	public Location getSpawn(final int level, final int x, final int z) {
		final int y, h;
		switch (level) {
		case   1: y = Y_001; h = H_001; break;
		case   0: y = Y_000; h = H_000; break;
		case  37: y = Y_037; h = H_037; break;
		case   5: y = Y_005; h = H_005; break;
		case  19: y = Y_019; h = H_019; break;
		case 309: y = Y_309; h = 10;    break;
		default: throw new RuntimeException("Invalid backrooms level: "+Integer.toString(level));
		}
		return this.getSpawn(level, h, x, y, z);
	}

	@Override
	public int getLevelFromY(final int y) {
		if (y <= Y_001 + H_001) return 1;
		if (y <= Y_000 + H_000) return 0;
		if (y <= Y_037 + H_037) return 37;
		if (y <= Y_005 + H_005) return 5;
		if (y <= Y_019 + H_019) return 19;
		return 309;
	}
	public int getYFromLevel(final int level) {
		switch (level) {
		case 1:   return Y_001;
		case 0:   return Y_000;
		case 37:  return Y_037;
		case 5:   return Y_005;
		case 19:  return Y_019;
		case 309: return Y_309;
		default: break;
		}
		throw new RuntimeException("Invalid backrooms level: "+Integer.toString(level));
	}
	public int getMaxYFromLevel(final int level) {
		switch (level) {
		case 1:   return Y_001 + H_001;
		case 0:   return Y_000 + H_000;
		case 37:  return Y_037 + H_037;
		case 5:   return Y_005 + H_005;
		case 19:  return Y_019 + H_019;
		case 309: return 255;
		default: break;
		}
		throw new RuntimeException("Invalid backrooms level: "+Integer.toString(level));
	}



	@Override
	public void generateSurface(
			final WorldInfo worldInfo, final Random random,
			final int chunkX, final int chunkZ, final ChunkData chunk) {
//if (chunkX == 2 && chunkZ == 2) return;
//if (chunkX % 20 == 0 || chunkZ % 20 == 0) return;
		final int seed = Long.valueOf(worldInfo.getSeed()).intValue();
		this.gen_001.setSeed(seed);
		this.gen_000.setSeed(seed);
		this.gen_037.setSeed(seed);
		this.gen_005.setSeed(seed);
		this.gen_019.setSeed(seed);
		this.gen_309.setSeed(seed);
		// pre-generate
		int xx, zz;
		final HashMap<Dxy, HotelDAO> prehotel = this.gen_005.pregenerateHotel(chunkX, chunkZ);
		// generate
		for (int z=0; z<16; z++) {
			for (int x=0; x<16; x++) {
				xx = x + (chunkX * 16);
				zz = z + (chunkZ * 16);
				// basement
				this.gen_001.generateBasement(chunkX, chunkZ, chunk, x, z, xx, zz);
				// 0 main lobby
				this.gen_000.generateLobby(chunkX, chunkZ, chunk, x, z, xx, zz);
				// pools
				this.gen_037.generatePools(chunkX, chunkZ, chunk, x, z, xx, zz);
				// hotel
				this.gen_005.generateHotel(prehotel, chunkX, chunkZ, chunk, x, z, xx, zz);
				// attic
				this.gen_019.generateAttic(chunkX, chunkZ, chunk, x, z, xx, zz);
				// radio station
				this.gen_309.generateWoodsPath(chunkX, chunkZ, chunk, x, z, xx, zz);
			}
		}
	}



	@Override
	public List<BlockPopulator> getDefaultPopulators(final World world) {
		final List<BlockPopulator> list = new ArrayList<BlockPopulator>();
		if (BUILD_ROOF)
			list.add(this.gen_309.treePop);
		list.add(this.gen_309.radioPop);
		list.add(this.gen_005.roomPop);
		list.add(this.gen_019.atticPop);
		return list;
	}



}
