package com.poixson.backrooms.levels;

import static com.poixson.utils.NumberUtils.Rnd10K;

import org.bukkit.Location;

import com.poixson.backrooms.BackroomsPlugin;


// 33 | Run For Your Life!
public class Level_033 extends LevelBackrooms {

	public static final boolean BUILD_ROOF = true;

	public static final int SUBFLOOR   = 3;
	public static final int SUBCEILING = 3;

	public static final int LEVEL_Y = 0;
	public static final int LEVEL_H = 8;

	// generators
	public final Gen_033 gen;



	public Level_033(final BackroomsPlugin plugin) {
		super(plugin, 33);
		// generators
		this.gen = this.register(new Gen_033(plugin, LEVEL_Y, LEVEL_H, BUILD_ROOF, SUBFLOOR, SUBCEILING));
	}



	@Override
	public Location getSpawn(final int level) {
		if (level != 11) throw new RuntimeException("Invalid backrooms level: "+Integer.toString(level));
		final int x = (Rnd10K() * 2) - 10000;
		final int z = (Rnd10K() * 2) - 10000;
		return this.getSpawn(level, x, z);
	}
	@Override
	public Location getSpawn(final int level, final int x, final int z) {
		return this.getSpawn(level, 10, x, LEVEL_Y, z);
	}

	@Override
	public int getLevelFromY(final int y) {
		return 11;
	}
	@Override
	public int getY(final int level) {
		return LEVEL_Y;
	}
	@Override
	public int getMaxY(final int level) {
		return LEVEL_Y + LEVEL_H;
	}



	@Override
	protected void generate(final ChunkData chunk, final int chunkX, final int chunkZ) {
		this.gen.generate(null, chunk, chunkX, chunkZ);
	}



}
