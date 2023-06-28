package com.poixson.backrooms.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityExplodeEvent;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.commonmc.tools.plugin.xListener;


// 33 | Run For Your Life!
public class Listener_033 extends xListener<BackroomsPlugin> {



	public Listener_033(final BackroomsPlugin plugin) {
		super(plugin);
	}



	@EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
	public void onEntityExplode(final EntityExplodeEvent event) {
		final int level = this.plugin.getLevelFromWorld(event.getEntity().getWorld());
		if (level == 33)
			event.setYield(0.0f);
	}



}
