package com.poixson.backrooms.levels;

import org.bukkit.Location;

import com.poixson.backrooms.BackroomsPlugin;


// 151 | Dollhouse
public class Level_151 extends LevelBackrooms {

	public static final boolean BUILD_ROOF = true;

	public static final int SUBFLOOR   = 3;
	public static final int SUBCEILING = 3;

	public static final int LEVEL_Y = 100;
	public static final int LEVEL_H = 100;

	// generators
	public final Gen_151 gen;



	public Level_151(final BackroomsPlugin plugin) {
		super(plugin);
		// generators
		this.gen = this.register(new Gen_151(plugin, LEVEL_Y, LEVEL_H, BUILD_ROOF, SUBFLOOR, SUBCEILING));
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
	@Override
	public int getY(final int level) {
		return LEVEL_Y;
	}
	@Override
	public int getMaxY(final int level) {
		return 319;
	}



	@Override
	protected void generate(final ChunkData chunk, final int chunkX, final int chunkZ) {
		this.gen.generate(null, chunk, chunkX, chunkZ);
	}



}
