package com.poixson.backrooms.levels;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.generator.WorldInfo;

import com.poixson.backrooms.BackroomsPlugin;


// 151 | Dollhouse
public class Level_151 extends BackroomsLevel {

	public static final boolean BUILD_ROOF = true;

	public static final int SUBFLOOR   = 3;
	public static final int SUBCEILING = 3;

	public static final int HOUSE_Y = 100;
	public static final int HOUSE_H = 100;

	// generators
	public final Gen_151 gen_151;



	public Level_151(final BackroomsPlugin plugin) {
		super(plugin);
		// generators
		this.gen_151 = new Gen_151();
	}
	@Override
	public void unload() {
		this.gen_151.unload();
	}



//TODO
	@Override
	public Location getSpawn(final int level) {
		if (level != 151) throw new RuntimeException("Invalid backrooms level: "+Integer.toString(level));
		return this.getSpawn(level, 0, 0);
	}
	@Override
	public Location getSpawn(final int level, final int x, final int z) {
		return this.getSpawn(level, 255, x, 0, z);
	}

	@Override
	public int getLevelFromY(final int y) {
		return 151;
	}
	public int getYFromLevel(final int level) {
		return HOUSE_Y;
	}
	public int getMaxYFromLevel(final int level) {
		return 319;
	}



	@Override
	public void generateSurface(
			final WorldInfo worldInfo, final Random random,
			final int chunkX, final int chunkZ, final ChunkData chunk) {
	}



}
