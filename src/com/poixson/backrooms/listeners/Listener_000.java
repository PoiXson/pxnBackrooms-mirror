package com.poixson.backrooms.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.commonmc.events.OutsideOfWorldEvent;
import com.poixson.commonmc.tools.plugin.xListener;


// level 1   - basement void
// level 309 - radio station sky
public class Listener_000  extends xListener<BackroomsPlugin> {



	public Listener_000(final BackroomsPlugin plugin) {
		super(plugin);
	}



	// -------------------------------------------------------------------------------
	// void/sky teleport



//TODO: teleport relative to exit
	@EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
	public void onOutsideOfWorld(final OutsideOfWorldEvent event) {
		if (event.getOutsideDistance() > 20) {
			final Player player = event.getPlayer();
			switch (event.getOutsideWhere()) {
			case SKY:  this.plugin.noclip(player, 771); break; // to crossroads
//TODO: teleport to 78
			case VOID: this.plugin.noclip(player, 771); break; // to space
			default: throw new RuntimeException("Unknown OutsideOfWorld event type");
			}
		}
	}



}
