package com.poixson.backrooms.generators;

import org.bukkit.generator.ChunkGenerator;

import com.poixson.backrooms.BackroomsPlugin;


public abstract class BackroomsGenerator extends ChunkGenerator {

	protected final BackroomsPlugin plugin;



	public BackroomsGenerator(final BackroomsPlugin plugin) {
		this.plugin = plugin;
	}



}
