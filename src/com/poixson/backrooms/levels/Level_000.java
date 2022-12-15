package com.poixson.backrooms.levels;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.WorldInfo;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.backrooms.levels.Gen_005.HotelDAO;
import com.poixson.tools.dao.Dxy;


// 309 | Path
//  19 | Attic
//   5 | Hotel
//  37 | Pools
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
	// path
	public static final int Y_309 = SUBFLOOR + Y_019 + H_019 + SUBCEILING + 1;

	// generators
	public final Gen_000 gen_000;
	public final Gen_001 gen_001;
	public final Gen_005 gen_005;
	public final Gen_019 gen_019;
	public final Gen_037 gen_037;
	public final Gen_309 gen_309;



	public Level_000(final BackroomsPlugin plugin) {
		super(plugin);
		this.gen_000 = new Gen_000();
		this.gen_001 = new Gen_001();
		this.gen_005 = new Gen_005();
		this.gen_019 = new Gen_019();
		this.gen_037 = new Gen_037();
		this.gen_309 = new Gen_309();
		Bukkit.getPluginManager()
			.registerEvents(this.gen_001, plugin);
	}

	@Override
	public void unload() {
		this.gen_000.unload();
		this.gen_001.unload();
		this.gen_005.unload();
		this.gen_019.unload();
		this.gen_037.unload();
		this.gen_309.unload();
	}



	@Override
	public void generateSurface(
			final WorldInfo worldInfo, final Random random,
			final int chunkX, final int chunkZ, final ChunkData chunk) {
if (chunkX == -1 && chunkZ == 1) return;
//if (chunkZ % 10 == 0) return;
		final int seed = Long.valueOf(worldInfo.getSeed()).intValue();
		this.gen_000.setSeed(seed);
		this.gen_001.setSeed(seed);
		this.gen_005.setSeed(seed);
		this.gen_019.setSeed(seed);
		this.gen_037.setSeed(seed);
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
				// 309 woods path
				this.gen_309.generateWoodsPath(chunkX, chunkZ, chunk, x, z, xx, zz);
			}
		}
	}



	@Override
	public List<BlockPopulator> getDefaultPopulators(final World world) {
		return Arrays.asList(
			this.gen_309.treePop,
			this.gen_005.roomPop
		);
	}



}
