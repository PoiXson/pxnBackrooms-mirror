package com.poixson.backrooms.tasks;

import java.util.concurrent.CopyOnWriteArraySet;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.tools.xTime;
import com.poixson.tools.abstractions.xStartStop;


public class TaskInvisiblePlayers extends BukkitRunnable implements xStartStop {

	public static final long DEFAULT_INTERVAL = xTime.Parse("5s").ticks(50L);

	protected final BackroomsPlugin plugin;

	protected final long interval;

	protected final CopyOnWriteArraySet<Player> invisible_players = new CopyOnWriteArraySet<Player>();



	public TaskInvisiblePlayers(final BackroomsPlugin plugin) {
		this.plugin   = plugin;
		this.interval = DEFAULT_INTERVAL;
	}



	@Override
	public void start() {
		if (this.plugin.enableInvisiblePlayers())
			this.runTaskTimer(this.plugin, this.interval, this.interval);
	}
	@Override
	public void stop() {
		try {
			this.cancel();
		} catch (Exception ignore) {}
	}



	@Override
	public void run() {
		for (final Player player : Bukkit.getOnlinePlayers())
			this.update(player);
	}

	public void update(final Player player) {
		if (this.plugin.enableInvisiblePlayers()) {
			final int level = this.plugin.getLevel(player);
			switch (level) {
			case 6: // lights out
				this.invisible_players.add(player);
				player.removePotionEffect(PotionEffectType.INVISIBILITY);
				player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, (int)(this.interval+40L), 1));
				break;
			default:
				if (this.invisible_players.remove(player))
					player.removePotionEffect(PotionEffectType.INVISIBILITY);
				break;
			}
		}
	}



}
