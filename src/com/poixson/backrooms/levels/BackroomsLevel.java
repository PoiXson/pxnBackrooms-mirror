package com.poixson.backrooms.levels;

import java.util.Random;

import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;

import com.poixson.backrooms.BackroomsPlugin;


public abstract class BackroomsLevel extends ChunkGenerator {

	protected final BackroomsPlugin plugin;



	public BackroomsLevel(final BackroomsPlugin plugin) {
		this.plugin = plugin;
	}

	public abstract void unload();



	@Override
	public abstract void generateSurface(final WorldInfo worldInfo, final Random random,
			final int chunkX, final int chunkZ, final ChunkData chunk);



}
