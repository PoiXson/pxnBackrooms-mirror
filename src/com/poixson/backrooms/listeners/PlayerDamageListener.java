package com.poixson.backrooms.listeners;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.poixson.backrooms.BackroomsPlugin;


public class PlayerDamageListener implements Listener {

	protected final BackroomsPlugin plugin;

	protected final ConcurrentHashMap<Player, Long> lastDamaged = new ConcurrentHashMap<Player, Long>();

	protected final AtomicInteger cleanup = new AtomicInteger(0);



	public PlayerDamageListener(final BackroomsPlugin plugin) {
		this.plugin = plugin;
	}



/*
	@EventHandler(priority=EventPriority.NORMAL)
	public void onPlayerDamage(final EntityDamageEvent event) {
		final Entity entity = event.getEntity();
		if (entity instanceof Player) {
			final Player player = (Player) entity;
			final DamageCause cause = event.getCause();
			if (cause.compareTo(DamageCause.DROWNING)    == 0
			||  cause.compareTo(DamageCause.SUFFOCATION) == 0) {
				final long current = Utils.GetMS();
				// cleanup
				if (this.cleanup.incrementAndGet() > 20) {
					this.cleanup.set(0);
					final Iterator<Entry<Player, Long>> it = this.lastDamaged.entrySet().iterator();
					final HashSet<Player> remove = new HashSet<Player>();
					while (it.hasNext()) {
						final Entry<Player, Long> entry = it.next();
						if (current - entry.getValue() > 300000L)
							remove.add(entry.getKey());
					}
					for (final Player p : remove) {
						this.lastDamaged.remove(p);
					}
				}
				// no-clip player to the backrooms
				boolean tp = false;
				final double damage = event.getFinalDamage();
				final double health = player.getHealth();
				// prevent death
				if (health - damage <= 2.0) {
					event.setCancelled(true);
					tp = true;
				}
				if (!tp) {
					final Long lastDam = this.lastDamaged.get(player);
					if (lastDam != null) {
						final long last = lastDam.longValue();
						// damaged within last 5 seconds
						if (current - last < 5000L) {
							// random chance
							double chance = 8.0;
							if (cause.compareTo(DamageCause.SUFFOCATION) == 0)
								chance = 2.5;
							int rnd = 0;
							for (int i=0; i<5; i++) {
								rnd = NumberUtils.GetNewRandom(0, 9000, rnd);
							}
							final double mod = ((double)rnd) % chance;
							if (mod < 1.0)
								tp = true;
						}
					} // end lastDam
				} // end !tp
				if (tp) {
					event.setCancelled(true);
					this.tp(player);
				}
			} // end damage cause
		} // end player instance
	}



	protected void tp(final Player player) {
		final String worldName = this.plugin.getBackroomsWorldName();
		final World world = Bukkit.getWorld(worldName);
		if (world == null) {
			player.sendMessage(BackroomsPlugin.CHAT_PREFIX + "The Backrooms could not be found.");
			return;
		}
//TODO: remove this
for (int i=0; i<10; i++) {
	final Location sp = world.getSpawnLocation();
	System.out.println();
	System.out.println();
	System.out.println("SPAWN x: " + sp.getX() + " y: " + sp.getY() + " z: " + sp.getZ());
	System.out.println();
	System.out.println();
}
		final Location spawn = world.getSpawnLocation();
		player.teleport(spawn);
	}
*/



}
