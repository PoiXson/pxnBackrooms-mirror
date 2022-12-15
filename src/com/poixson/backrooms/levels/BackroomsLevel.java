package com.poixson.backrooms.levels;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.generator.ChunkGenerator;

import com.poixson.backrooms.BackroomsPlugin;


public abstract class BackroomsLevel extends ChunkGenerator implements Listener {

	protected final BackroomsPlugin plugin;



	public BackroomsLevel(final BackroomsPlugin plugin) {
		this.plugin = plugin;
		Bukkit.getPluginManager()
			.registerEvents(this, plugin);
	}

	public abstract void unload();



}
