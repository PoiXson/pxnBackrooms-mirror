package com.poixson.backrooms.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.tools.events.xListener;


// 33 | Run For Your Life!
public class Listener_033 extends xListener {

	protected final BackroomsPlugin plugin;



	public Listener_033(final BackroomsPlugin plugin) {
		super(plugin);
		this.plugin = plugin;
	}



	// -------------------------------------------------------------------------------
	// disable explosion damage



	@EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
	public void onEntityDamaged(final EntityDamageByEntityEvent event) {
		final Entity entity = event.getEntity();
		if (entity instanceof Player) {
			final int level = this.plugin.getLevel( (Player)entity );
			if (level == 33) {
				switch (event.getCause()) {
				case BLOCK_EXPLOSION:
				case ENTITY_EXPLOSION:
					event.setCancelled(true);
					break;
				default: break;
				}
			}
		}
	}



}
