package com.poixson.backrooms.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.tools.events.OutsideOfWorldEvent;
import com.poixson.tools.events.xListener;


//   1 | Basement
// 309 | Radio Station
public class Listener_000  extends xListener<BackroomsPlugin> {



	public Listener_000(final BackroomsPlugin plugin) {
		super(plugin);
	}



	// -------------------------------------------------------------------------------
	// void/sky teleport



	@EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
	public void onOutsideOfWorld(final OutsideOfWorldEvent event) {
		if (event.getOutsideDistance() > 20) {
			final Player player = event.getPlayer();
			final int level = this.plugin.getLevel(event.getTo());
			switch (level) {
			case 309: this.plugin.noclip(player, 771); break; // to 771 crossroads
			case   1: this.plugin.noclip(player,  94); break; // to  94 motion
			default: break;
			}
		}
	}



}
