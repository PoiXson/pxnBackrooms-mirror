package com.poixson.backrooms;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

import com.poixson.backrooms.levels.BackroomsLevel;
import com.poixson.backrooms.levels.Level_000;
import com.poixson.backrooms.levels.Level_009;
import com.poixson.backrooms.levels.Level_011;
import com.poixson.backrooms.levels.Level_771;
import com.poixson.utils.NumberUtils;


public class BackroomsPlugin extends JavaPlugin {
	public static final String LOG_PREFIX  = "[Backrooms] ";
	public static final String CHAT_PREFIX = ChatColor.AQUA + "[Backrooms] " + ChatColor.WHITE;
	public static final Logger log = Logger.getLogger("Minecraft");

	public static final String GENERATOR_NAME = "Backrooms";

	protected static final AtomicReference<BackroomsPlugin> instance = new AtomicReference<BackroomsPlugin>(null);
	protected final AtomicBoolean enableScripts = new AtomicBoolean(false);

	// world generators
	protected final HashMap<Integer, BackroomsLevel> backlevels = new HashMap<Integer, BackroomsLevel>();

//	// listeners
//	protected final AtomicReference<BackroomsCommands>   commandListener = new AtomicReference<BackroomsCommands>(null);



	@Override
	public void onEnable() {
		if (!instance.compareAndSet(null, this))
			throw new RuntimeException("Plugin instance already enabled?");
		{
			final File path = new File(this.getDataFolder(), "scripts");
			this.enableScripts.set(
				path.isDirectory()
			);
		}
/*
		// commands listener
		{
			final BackroomsCommands listener = new BackroomsCommands(this);
			final BackroomsCommands previous = this.commandListener.getAndSet(listener);
			if (previous != null)
				previous.unregister();
			listener.register();
		}
		// player damage listener
		{
			final PlayerDamageListener listener = new PlayerDamageListener(this);
			final PlayerDamageListener previous = this.damageListener.getAndSet(listener);
			final PluginManager pm = Bukkit.getPluginManager();
			if (previous != null)
				HandlerList.unregisterAll(previous);
			pm.registerEvents(listener, this);
		}
*/
	}

	@Override
	public void onDisable() {
		// unload generators
		for (final BackroomsLevel lvl : this.backlevels.values()) {
			lvl.unload();
		}
		this.backlevels.clear();
/*
		// commands listener
		{
			final BackroomsCommands listener = this.commandListener.getAndSet(null);
			if (listener != null)
				listener.unregister();
		}
*/
		// stop schedulers
		try {
			Bukkit.getScheduler()
				.cancelTasks(this);
		} catch (Exception ignore) {}
		// stop listeners
		HandlerList.unregisterAll(this);
		if (!instance.compareAndSet(this, null))
			throw new RuntimeException("Disable wrong instance of plugin?");
	}



	public int getLevel(final Player player) {
		if (player == null)
			return Integer.MIN_VALUE;
		final World world = player.getWorld();
		return this.getLevel(world);
	}
	public int getLevel(final World world) {
		if (world == null)
			return Integer.MIN_VALUE;
		return this.getLevel(world.getName());
	}
	public int getLevel(final String worldName) {
		if (worldName == null || worldName.isEmpty())
			return Integer.MIN_VALUE;
		if (!worldName.startsWith("level"))
			return Integer.MIN_VALUE;
		return Integer.parseInt(worldName.substring(5));
	}



	public int noclip(final int from) {
		final HashMap<Integer, Integer> chance = new HashMap<Integer, Integer>();
		switch (from) {
		case 0: // lobby
			chance.put(Integer.valueOf(   1 ), Integer.valueOf( 25 )); // basement
			chance.put(Integer.valueOf(   5 ), Integer.valueOf( 15 )); // hotel
			chance.put(Integer.valueOf(   9 ), Integer.valueOf( 10 )); // suburbs
			chance.put(Integer.valueOf(  11 ), Integer.valueOf( 10 )); // city
			chance.put(Integer.valueOf( 309 ), Integer.valueOf( 10 )); // path
			chance.put(Integer.valueOf( 771 ), Integer.valueOf( 10 )); // crossroads
			break;
		case -1: // basement
			chance.put(Integer.valueOf(   0 ), Integer.valueOf( 20 )); // lobby
			chance.put(Integer.valueOf(   5 ), Integer.valueOf( 10 )); // hotel
			chance.put(Integer.valueOf(   9 ), Integer.valueOf( 10 )); // suburbs
			chance.put(Integer.valueOf(  11 ), Integer.valueOf( 10 )); // city
			chance.put(Integer.valueOf( 771 ), Integer.valueOf(  2 )); // crossroads
			break;
		case 5: // hotel
			chance.put(Integer.valueOf(   0 ), Integer.valueOf( 20 )); // lobby
			chance.put(Integer.valueOf(   1 ), Integer.valueOf( 15 )); // basement
			chance.put(Integer.valueOf(  11 ), Integer.valueOf( 10 )); // city
			chance.put(Integer.valueOf( 309 ), Integer.valueOf( 10 )); // path
			chance.put(Integer.valueOf( 771 ), Integer.valueOf(  5 )); // crossroads
			break;
		case 9: // suburbs
			chance.put(Integer.valueOf(   0 ), Integer.valueOf(  5 )); // lobby
			chance.put(Integer.valueOf(  11 ), Integer.valueOf( 10 )); // city
			chance.put(Integer.valueOf( 309 ), Integer.valueOf(  5 )); // path
			chance.put(Integer.valueOf( 771 ), Integer.valueOf(  5 )); // crossroads
			break;
		case 11: // city
			chance.put(Integer.valueOf(   0 ), Integer.valueOf(  5 )); // lobby
			chance.put(Integer.valueOf(   1 ), Integer.valueOf( 10 )); // basement
			chance.put(Integer.valueOf(   5 ), Integer.valueOf( 10 )); // hotel
			chance.put(Integer.valueOf(   9 ), Integer.valueOf(  5 )); // suburbs
			chance.put(Integer.valueOf( 309 ), Integer.valueOf( 10 )); // path
			chance.put(Integer.valueOf( 771 ), Integer.valueOf(  5 )); // crossroads
			break;
		case 309: // path
			chance.put(Integer.valueOf(   0 ), Integer.valueOf( 10 )); // lobby
			chance.put(Integer.valueOf(   9 ), Integer.valueOf( 15 )); // suburbs
			chance.put(Integer.valueOf(  11 ), Integer.valueOf( 10 )); // city
			chance.put(Integer.valueOf( 771 ), Integer.valueOf( 20 )); // crossroads
			break;
		default:
			chance.put(Integer.valueOf(   0 ), Integer.valueOf( 20 )); // lobby
			chance.put(Integer.valueOf(   1 ), Integer.valueOf(  1 )); // basement
			chance.put(Integer.valueOf(   5 ), Integer.valueOf(  1 )); // hotel
			chance.put(Integer.valueOf(   9 ), Integer.valueOf(  1 )); // suburbs
			chance.put(Integer.valueOf(  11 ), Integer.valueOf(  1 )); // city
			chance.put(Integer.valueOf( 309 ), Integer.valueOf(  1 )); // path
			chance.put(Integer.valueOf( 771 ), Integer.valueOf(  1 )); // crossroads
			break;
		}
		if (chance.isEmpty())
			return 0;
		int total = 0;
		for (final Integer i : chance.keySet()) {
			total += i;
		}
		final int rnd = NumberUtils.GetRandom(0, total);
		final Iterator<Entry<Integer, Integer>> it = chance.entrySet().iterator();
		while (it.hasNext()) {
			final Entry<Integer, Integer> entry = it.next();
			final Integer level  = entry.getKey();
			final Integer weight = entry.getValue();
			if (total - weight <= rnd)
				return level.intValue();
			total -= weight;
		}
		return 0;
	}



	// -------------------------------------------------------------------------------



	@Override
	public ChunkGenerator getDefaultWorldGenerator(final String worldName, final String argsStr) {
		if (!worldName.startsWith("level"))
			throw new RuntimeException("Invalid world name, must be level# found: "+worldName);
		log.info(String.format("%s%s world: %s", LOG_PREFIX, GENERATOR_NAME, worldName));
		final int level = this.getLevel(worldName);
		// existing generator
		{
			final BackroomsLevel lvl = this.backlevels.get(Integer.valueOf(level));
			if (lvl != null)
				return lvl;
		}
		// new generator instance
		{
			final BackroomsLevel lvl;
			switch (level) {
			case   0: lvl = new Level_000(this); break;
			case   9: lvl = new Level_009(this); break;
			case  11: lvl = new Level_011(this); break;
			case 771: lvl = new Level_771(this); break;
			default: throw new RuntimeException("Invalid backrooms level: "+Integer.toString(level));
			}
			final BackroomsLevel existing = this.backlevels.putIfAbsent(Integer.valueOf(level), lvl);
			if (existing != null)
				return existing;
			return lvl;
		}
	}



}
