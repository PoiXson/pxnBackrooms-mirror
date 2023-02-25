package com.poixson.backrooms.listeners;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;

import com.poixson.backrooms.BackroomsPlugin;


public class ItemDespawnListener extends xListener<BackroomsPlugin> {



	public ItemDespawnListener(final BackroomsPlugin plugin) {
		super(plugin);
	}



	@EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
	public void onItemDespawn(final ItemDespawnEvent event) {
		if (EntityType.DROPPED_ITEM.equals(event.getEntityType())) {
			final World world = event.getLocation().getWorld();
			if (this.plugin.isValidLevel(world)) {
				event.setCancelled(true);
				event.getEntity().setUnlimitedLifetime(true);
			}
		}
	}



}
