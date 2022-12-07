package com.poixson.backrooms.generators;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.generator.ChunkGenerator;

import com.poixson.backrooms.BackroomsPlugin;


public abstract class BackroomsGenerator extends ChunkGenerator implements Listener {

	protected final BackroomsPlugin plugin;



	public BackroomsGenerator(final BackroomsPlugin plugin) {
		this.plugin = plugin;
		Bukkit.getPluginManager()
			.registerEvents(this, plugin);
	}

	public void unload() {
	}



}
