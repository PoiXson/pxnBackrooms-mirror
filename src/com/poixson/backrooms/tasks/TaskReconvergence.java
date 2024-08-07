package com.poixson.backrooms.tasks;

import static com.poixson.utils.BukkitUtils.SafeCancel;
import static com.poixson.utils.Utils.GetMS;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
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

	protected final File file;



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
		this.file = new File(this.plugin.getDataFolder(), "reconvergence.json");
	}



	@Override
	public void start() {
		final long time = GetMS();
		// load reconvergence.json
		final FileConfiguration cfg = YamlConfiguration.loadConfiguration(this.file);
		this.lastUpdated = cfg.getLong("Last Updated", time);
		this.lastUsed    = cfg.getLong("Last Used",    time);
		// start task
		this.runTaskTimer(this.plugin, this.updateTicks*2L, this.updateTicks);
	}
	@Override
	public void stop() {
		SafeCancel(this);
		// save reconvergence.json
		final FileConfiguration cfg = YamlConfiguration.loadConfiguration(this.file);
		cfg.set("Last Updated", Long.valueOf(this.lastUpdated));
		cfg.set("Last Used",    Long.valueOf(this.lastUsed   ));
		try {
			cfg.save(this.file);
		} catch (IOException e) {
			e.printStackTrace();
		}
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
				this.update();
			}
		}
	}
	public void update() {
		this.plugin.log().info("Reconvergence..");
		this.lastUpdated = GetMS();
		this.plugin.flushSpawns();
		// update spawn locations per world
		for (final int level : this.plugin.getMainLevels()) {
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
