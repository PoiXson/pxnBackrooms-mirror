package com.poixson.backrooms.levels;

import static com.poixson.utils.NumberUtils.Rnd10K;

import org.bukkit.Location;

import com.poixson.backrooms.BackroomsPlugin;


// 771 | Crossroads
public class Level_771 extends LevelBackrooms {

	public static final int LEVEL_Y = 0;
	public static final int LEVEL_H = 200;

	// generators
	public final Gen_771 gen;



	public Level_771(final BackroomsPlugin plugin) {
		super(plugin, 771);
		// generators
		this.gen = this.register(new Gen_771(plugin, LEVEL_Y, 0));
	}



	@Override
	public Location getSpawn(final int level) {
		int x = (Rnd10K() * 2) - 10000;
		int z = (Rnd10K() * 2) - 10000;
		if (Math.abs(x) > Math.abs(z)) {
			z = 0;
		} else {
			x = 0;
		}
		return this.getSpawn(level, x, z);
	}
	@Override
	public Location getSpawn(final int level, final int x, final int z) {
		return this.getSpawn(level, 10, x, LEVEL_Y+LEVEL_H, z);
	}

	@Override
	public int getY(final int level) {
		return LEVEL_Y + LEVEL_H;
	}
	@Override
	public int getMaxY(final int level) {
		return LEVEL_Y + LEVEL_H + 20;
	}



	@Override
	protected void generate(final ChunkData chunk, final int chunkX, final int chunkZ) {
		this.gen.generate(null, chunk, chunkX, chunkZ);
	}



/*
	@Override
	public List<BlockPopulator> getDefaultPopulators(final World world) {
		return Arrays.asList(
			this.gen.crossPop
		);
	}
*/



}
