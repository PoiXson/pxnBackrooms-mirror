package com.poixson.backrooms.tasks;

import static com.poixson.utils.Utils.GetMS;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.backrooms.BackroomsWorld;
import com.poixson.tools.xTime;
import com.poixson.tools.abstractions.xStartStop;


public class TaskReconvergence extends BukkitRunnable implements xStartStop {

	public static final String DEFAULT_UPDATE_PERIOD = "3h";
	public static final String DEFAULT_UPDATE_GRACE  = "3m";
	public static final String DEFAULT_MAX_GRACE     = "30m";

	protected final BackroomsPlugin plugin;

	protected final long updateTicks = xTime.Parse("1m").ticks(50L);
	protected final long updatePeriod;
	protected final long updateGrace;
	protected final long maxGrace;

	protected long lastUpdated = 0L;
	protected long lastUsed    = 0L;



	public TaskReconvergence(final BackroomsPlugin plugin, final ConfigurationSection config) {
		this(plugin,
			config.getString("Update Period"),
			config.getString("Update Grace" ),
			config.getString("Max Grace"    )
		);
	}
	public TaskReconvergence(final BackroomsPlugin plugin,
			final String updatePeriod, final String updateGrace, final String maxGrace) {
		this(plugin,
			xTime.ParseToLong(updatePeriod),
			xTime.ParseToLong(updateGrace ),
			xTime.ParseToLong(maxGrace    )
		);
	}
	public TaskReconvergence(final BackroomsPlugin plugin,
			final long updatePeriod, final long updateGrace, final long maxGrace) {
		this.plugin       = plugin;
		this.updatePeriod = updatePeriod;
		this.updateGrace  = updateGrace;
		this.maxGrace     = maxGrace;
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
				// run update
				this.lastUpdated = time;
				this.update();
			}
		}
	}
	public void update() {
		this.plugin.log().info("Reconvergence..");
		this.plugin.flushSpawns();
		// update spawn locations per world
		for (final int level : this.plugin.getLevels()) {
			try {
				final BackroomsWorld backlevel = this.plugin.getBackroomsWorld(level);
				final Location loc = backlevel.getSpawnArea(level);
				final World world = backlevel.getWorld();
				loc.setY(backlevel.getOpenY(level));
				world.setSpawnLocation(loc);
			} catch (UnsupportedOperationException ignore) {}
		}
		// refill chests
//TODO
	}



	public void markUsed() {
		this.markUsed(GetMS());
	}
	public void markUsed(final long time) {
		this.lastUsed = time;
	}



	public static void ConfigDefaults(final FileConfiguration config) {
		config.addDefault("Reconvergence.Update Period", DEFAULT_UPDATE_PERIOD);
		config.addDefault("Reconvergence.Update Grace",  DEFAULT_UPDATE_GRACE );
		config.addDefault("Reconvergence.Max Grace",     DEFAULT_MAX_GRACE    );
	}



}
