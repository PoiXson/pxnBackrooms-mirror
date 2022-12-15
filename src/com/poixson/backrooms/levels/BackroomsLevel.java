package com.poixson.backrooms.levels;

import org.bukkit.generator.ChunkGenerator;

import com.poixson.backrooms.BackroomsPlugin;


public abstract class BackroomsLevel extends ChunkGenerator {

	protected final BackroomsPlugin plugin;



	public BackroomsLevel(final BackroomsPlugin plugin) {
		this.plugin = plugin;
	}

	public abstract void unload();



}
