package com.poixson.backrooms.tasks;

import static com.poixson.utils.Utils.GetMS;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.tools.xTime;
import com.poixson.tools.abstractions.xStartStop;


public class TaskReconvergence extends BukkitRunnable implements xStartStop {

	protected final BackroomsPlugin plugin;

	protected final long updateTicks = (new xTime("1m")).ticks(50L);
	protected final long updatePeriod = xTime.ParseToLong("3h");
	protected final long updateGrace  = xTime.ParseToLong("3m");
	protected final long maxGrace     = xTime.ParseToLong("30m");

	protected long lastUpdated = 0L;
	protected long lastUsed    = 0L;



	public TaskReconvergence(final BackroomsPlugin plugin) {
		this.plugin = plugin;
	}



	@Override
	public void start() {
		this.runTaskTimer(this.plugin, this.updateTicks*2L, this.updateTicks);
		final long time = GetMS();
		this.lastUpdated = time;
		this.lastUsed    = time;
	}
	@Override
	public void stop() {
		try {
			this.cancel();
		} catch (IllegalStateException ignore) {}
	}



	@Override
	public void run() {
		if (this.lastUsed <= 0) return;
		if (Bukkit.getOnlinePlayers().size() == 0) return;
		{
			final long time = GetMS();
			final long sinceUpdated = time - this.lastUpdated;
			// update period
			if (sinceUpdated >= this.updatePeriod) {
				final long sinceLastUsed = time - this.lastUsed;
				// within grace period
				if (sinceLastUsed <= this.updateGrace) {
					if (sinceUpdated <= this.updatePeriod+this.maxGrace)
						return;
				}
				// run hourly
				this.lastUpdated = time;
				this.update();
			}
		}
	}
	protected void update() {
		// flush teleport manager
		this.plugin.getTeleportManager()
			.flush();
		// announce quote
		this.plugin.getQuoteAnnouncer()
			.announce();
		// refill chests
//TODO
	}



	public void markUsed() {
		this.markUsed(GetMS());
	}
	public void markUsed(final long time) {
		this.lastUsed = time;
	}



}
