package com.poixson.backrooms.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.backrooms.levels.Listener_006;


public class RedstoneListener implements Listener {

	protected final BackroomsPlugin plugin;

	protected final Listener_006 listener_006;



	public RedstoneListener(final BackroomsPlugin plugin) {
		this.plugin = plugin;
		this.listener_006 = new Listener_006(plugin);
	}



	public void register() {
		Bukkit.getPluginManager()
			.registerEvents(this, this.plugin);
	}
	public void unregister() {
		HandlerList.unregisterAll(this);
	}



	@EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
	public void onBlockRedstone(final BlockRedstoneEvent event) {
		this.listener_006.onBlockRedstone(event);
	}



}
