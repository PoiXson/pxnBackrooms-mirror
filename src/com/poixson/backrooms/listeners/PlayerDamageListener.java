package com.poixson.backrooms.listeners;

import static com.poixson.utils.NumberUtils.Rnd10K;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.tools.xTime;
import com.poixson.utils.Utils;


public class PlayerDamageListener implements Listener {

	public static final long DAMAGE_TIMEOUT = xTime.Parse("5s").ms();
	public static final double MIN_DAMAGE = 3.0;

	protected final BackroomsPlugin plugin;

	protected final AtomicInteger cleanup = new AtomicInteger(0);



	public class PlayerDamageDAO {
		public long last;
		public int count = 1;

		public PlayerDamageDAO() {
			this.last = Utils.GetMS();
		}

		public int increment() {
			this.last = Utils.GetMS();
			return ++this.count;
		}

	}
	protected final HashMap<UUID, PlayerDamageDAO> lastPlayerDamage = new HashMap<UUID, PlayerDamageDAO>();



	public PlayerDamageListener(final BackroomsPlugin plugin) {
		this.plugin = plugin;
	}



	public void register() {
		Bukkit.getPluginManager()
			.registerEvents(this, this.plugin);
	}
	public void unregister() {
		HandlerList.unregisterAll(this);
	}



	@EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
	public void onPlayerDamage(final EntityDamageEvent event) {
		final Entity entity = event.getEntity();
		if (entity instanceof Player) {
			final Player player = (Player) entity;
			final double health = player.getHealth();
			final DamageCause cause = event.getCause();
			switch (cause) {
			case SUFFOCATION:
			case VOID: {
				final int count = this.incrementPlayerDamageCount(player.getUniqueId());
				final int countMin = (health > MIN_DAMAGE ? 4 : 2);
				if (count <= countMin)
					return;
				break;
			}
			default: return;
			}
			final int rnd = Rnd10K();
			if (rnd % 10 < 5)
				return;
			// noclip into the backrooms
			event.setCancelled(true);
			this.plugin.noclip(player);
			this.lastPlayerDamage.remove(player.getUniqueId());
			// cleanup
			if (this.lastPlayerDamage.size() % 5 == 0) {
				final long time = Utils.GetMS();
				final ArrayList<UUID> remove = new ArrayList<UUID>();
				final Iterator<Entry<UUID, PlayerDamageDAO>> it = this.lastPlayerDamage.entrySet().iterator();
				while (it.hasNext()) {
					final Entry<UUID, PlayerDamageDAO> entry = it.next();
					final PlayerDamageDAO dao = entry.getValue();
					if (dao.last + DAMAGE_TIMEOUT > time)
						remove.add(entry.getKey());
				}
				for (final UUID uuid : remove) {
					this.lastPlayerDamage.remove(uuid);
				}
			}
		}
	}

	public int incrementPlayerDamageCount(final UUID uuid) {
		// cached
		{
			final PlayerDamageDAO dao = this.lastPlayerDamage.get(uuid);
			if (dao != null) {
				if (dao.last + DAMAGE_TIMEOUT >= Utils.GetMS())
					return dao.increment();
			}
		}
		// new dao
		{
			final PlayerDamageDAO dao = new PlayerDamageDAO();
			this.lastPlayerDamage.put(uuid, dao);
			return dao.count;
		}
	}



}
