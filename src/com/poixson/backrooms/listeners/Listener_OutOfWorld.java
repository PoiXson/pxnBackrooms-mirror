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
// void  33 >>   6 | Run For Your Life (void) to Lights Out
// void  94 >> 771 | Motion            (void) to Crossroads
public class Listener_OutOfWorld  extends xListener<BackroomsPlugin> {



	public Listener_OutOfWorld(final BackroomsPlugin plugin) {
		super(plugin);
	}



	// -------------------------------------------------------------------------------
	// void/sky teleport



	@EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
	public void onOutsideOfWorld(final OutsideOfWorldEvent event) {
		final int level = this.plugin.getLevel(event.getTo());
		WHERE_SWITCH:
		switch (event.getOutsideWhere()) {
		case SKY: {
			LEVEL_SWITCH:
			switch (level) {
			case 309: this.plugin.noclip(event.getPlayer(), 771); break LEVEL_SWITCH; // 309 radio station to 771 crossroads
			default: break LEVEL_SWITCH;
			}
			break WHERE_SWITCH;
		}
		case VOID: {
			LEVEL_SWITCH:
			switch (level) {
			case   1: this.plugin.noclip(event.getPlayer(),  94); break LEVEL_SWITCH; //   1 basement          to 94 motion
			case  33: this.plugin.noclip(event.getPlayer(),   6); break LEVEL_SWITCH; //  33 run for your life to 6 lights out
			case  94: this.plugin.noclip(event.getPlayer(), 771); break LEVEL_SWITCH; //  94 motion            to 771 crossroads
			case 771: { // 771 crossroads
				final Player player = event.getPlayer();
				final Location player_loc = player.getLocation();
				// 771 crossroads to 94 motion
				if (Math.abs(player_loc.getBlockX()) < 4
				||  Math.abs(player_loc.getBlockZ()) < 4) {
					this.plugin.noclip(event.getPlayer(),  94);
					break LEVEL_SWITCH;
				}
				// teleport to sky
				player.teleport(player.getLocation().add(0.0, 600.0, 0.0));
				break LEVEL_SWITCH;
			}
			default: break LEVEL_SWITCH;
			}
			break WHERE_SWITCH;
		}
		default: throw new RuntimeException("Unknown OutsideOfWorld event type");
		}
	}



}
