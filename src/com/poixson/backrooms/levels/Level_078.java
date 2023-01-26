package com.poixson.backrooms.levels;

import org.bukkit.Location;

import com.poixson.backrooms.BackroomsPlugin;


// 78 | Space
public class Level_078 extends LevelBackrooms {

	// generators
	public final Gen_078 gen;



	public Level_078(final BackroomsPlugin plugin) {
		super(plugin, 78);
		// generators
		this.gen = this.register(new Gen_078(plugin, 0, 0));
	}



//TODO
	@Override
	public Location getSpawn(final int level) {
		if (level != 78) throw new RuntimeException("Invalid backrooms level: "+Integer.toString(level));
		return this.getSpawn(level, 0, 0);
	}
	@Override
	public Location getSpawn(final int level, final int x, final int z) {
		return this.getSpawn(level, 255, x, 0, z);
	}

	@Override
	public int getY(final int level) {
		return 255;
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
