package com.poixson.backrooms.levels;

import static com.poixson.utils.NumberUtils.Rnd10K;

import org.bukkit.Location;
import org.bukkit.Material;

import com.poixson.backrooms.BackroomsPlugin;


// 866 | Dirtfield
public class Level_866 extends LevelBackrooms {

	public static final int LEVEL_Y = 0;
	public static final int SUBFLOOR = 3;

	public static final Material GROUND      = Material.RED_SANDSTONE;
	public static final Material GROUND_SLAB = Material.RED_SANDSTONE_SLAB;

	// generators
	public final Gen_866 gen;



	public Level_866(final BackroomsPlugin plugin) {
		super(plugin);
		// generators
		this.gen = this.register(new Gen_866(plugin, LEVEL_Y, 0, SUBFLOOR));
	}



	@Override
	public Location getSpawn(final int level) {
		if (level != 866) throw new RuntimeException("Invalid backrooms level: "+Integer.toString(level));
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
		return 866;
	}
	@Override
	public int getY(final int level) {
		return LEVEL_Y;
	}
	@Override
	public int getMaxY(final int level) {
		return LEVEL_Y + 20;
	}



	@Override
	protected void generate(final ChunkData chunk, final int chunkX, final int chunkZ) {
		this.gen.generate(null, chunk, chunkX, chunkZ);
	}



}
