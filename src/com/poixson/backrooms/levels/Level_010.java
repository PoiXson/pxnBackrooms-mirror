package com.poixson.backrooms.levels;

import static com.poixson.utils.NumberUtils.Rnd10K;

import org.bukkit.Location;

import com.poixson.backrooms.BackroomsPlugin;


// 10 | Field of Wheat
public class Level_010 extends LevelBackrooms {

	public static final int LEVEL_Y = 0;

	// generators
	public final Gen_010 gen;



	public Level_010(final BackroomsPlugin plugin) {
		super(plugin, 10);
		// generators
		this.gen = this.register(new Gen_010(plugin, LEVEL_Y, 0));
	}



	@Override
	public Location getSpawn(final int level) {
		if (level != 10) throw new RuntimeException("Invalid backrooms level: "+Integer.toString(level));
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
		return 10;
	}
	@Override
	public int getY(final int level) {
		return LEVEL_Y;
	}
	@Override
	public int getMaxY(final int level) {
		return 255;
	}



	@Override
	protected void generate(final ChunkData chunk, final int chunkX, final int chunkZ) {
		this.gen.generate(null, chunk, chunkX, chunkZ);
	}



}
