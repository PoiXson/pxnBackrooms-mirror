package com.poixson.backrooms.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.tools.events.OutsideOfWorldEvent;
import com.poixson.tools.events.xListener;


// sky  309 >> 771 | Radio Station     (sky)  to Crossroads
// void   1 >>  94 | Basement          (void) to motion
// sky  771 >> 309 | Crossroads        (sky)  to Radio Station
// void 771 >>   1 | Crossroads        (void) to Basement
// void 111 >>   6 | Run For Your Life (void) to Lights Out
// void  94 >> 771 | Motion            (void) to Crossroads
public class Listener_OutOfWorld implements xListener {

	protected final BackroomsPlugin plugin;



	public Listener_OutOfWorld(final BackroomsPlugin plugin) {
		this.plugin = plugin;
	}



	public void register() {
		xListener.super.register(this.plugin);
	}



	// -------------------------------------------------------------------------------
	// void/sky teleport



	@EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
	public void onOutsideOfWorld(final OutsideOfWorldEvent event) {
		final int level = this.plugin.getLevel(event.getTo());
		LEVEL_SWITCH:
		switch (level) {
		// basement
		case 1: {
			switch (event.getOutsideWhere()) {
			// to 94 motion
			case VOID: this.plugin.noclip(event.getPlayer(), 94); break;
			default: break;
			}
			break LEVEL_SWITCH;
		}
		// motion
		case 94: {
			switch (event.getOutsideWhere()) {
			// to 771 crossroads
			case VOID: this.plugin.noclip(event.getPlayer(), 771); break;
			default: break;
			}
			break LEVEL_SWITCH;
		}
		// run for your life
		case 111: {
			// to 6 lights out
			this.plugin.noclip(event.getPlayer(), 6);
			break LEVEL_SWITCH;
		}
		// radio station
		case 309: {
			switch (event.getOutsideWhere()) {
			// to 771 crossroads
			case SKY: this.plugin.noclip(event.getPlayer(), 771); break;
			default: break;
			}
			break LEVEL_SWITCH;
		}
		// crossroads
		case 771: {
			final Player player = event.getPlayer();
			switch (event.getOutsideWhere()) {
			case VOID: {
				final Location player_loc = player.getLocation();
				// to 94 motion
				if (Math.abs(player_loc.getBlockX()) < 4
				||  Math.abs(player_loc.getBlockZ()) < 4) {
					this.plugin.noclip(event.getPlayer(), 94);
					break;
				}
				// teleport to sky
				player.teleport(player.getLocation().add(0.0, 600.0, 0.0));
				break;
			}
			default: break;
			}
			break LEVEL_SWITCH;
		}
		default: break LEVEL_SWITCH;
		}
	}



}
