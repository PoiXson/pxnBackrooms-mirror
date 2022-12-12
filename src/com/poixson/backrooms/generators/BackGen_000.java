package com.poixson.backrooms.generators;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.WorldInfo;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.backrooms.generators.Level_005.HotelDAO;
import com.poixson.tools.dao.Dxy;


// 309 | Path
//   5 | Hotel
//   0 | Lobby
//   1 | Basement
public class BackGen_000 extends BackroomsGenerator {

	public static final boolean BUILD_ROOF = true;

	public static final int SUBFLOOR = 3;

	public final Level_000 level_000;
	public final Level_001 level_001;
	public final Level_005 level_005;
	public final Level_309 level_309;



	public BackGen_000(final BackroomsPlugin plugin) {
		super(plugin);
		this.level_000 = new Level_000(plugin);
		this.level_001 = new Level_001(plugin);
		this.level_005 = new Level_005(plugin);
		this.level_309 = new Level_309(plugin);
	}



	@Override
	public void generateSurface(
			final WorldInfo worldInfo, final Random random,
			final int chunkX, final int chunkZ, final ChunkData chunk) {
if (chunkX == -1 && chunkZ == 1) return;
//if (chunkZ % 10 == 0) return;
		final int seed = Long.valueOf(worldInfo.getSeed()).intValue();
		this.level_000.setSeed(seed);
		this.level_001.setSeed(seed);
		this.level_005.setSeed(seed);
		this.level_309.setSeed(seed);
		// pre-generate
		int xx, zz;
		final HashMap<Dxy, HotelDAO> prehotel = this.level_005.pregenerateHotel(chunkX, chunkZ);
		// generate
		for (int z=0; z<16; z++) {
			for (int x=0; x<16; x++) {
				xx = x + (chunkX * 16);
				zz = z + (chunkZ * 16);
				// basement
				this.level_001.generateBasement(chunkX, chunkZ, chunk, x, z, xx, zz);
				// 0 main lobby
				this.level_000.generateLobby(chunkX, chunkZ, chunk, x, z, xx, zz);
				// hotel
				this.level_005.generateHotel(prehotel, chunkX, chunkZ, chunk, x, z, xx, zz);
				// 309 woods path
				this.level_309.generateWoodsPath(chunkX, chunkZ, chunk, x, z, xx, zz);
			}
		}
	}



	@Override
	public List<BlockPopulator> getDefaultPopulators(final World world) {
		return Arrays.asList(
			this.level_309.treePop,
			this.level_005.roomPop
		);
	}



}
