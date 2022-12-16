package com.poixson.backrooms;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.poixson.backrooms.levels.BackroomsLevel;
import com.poixson.backrooms.levels.Level_000;
import com.poixson.backrooms.levels.Level_009;
import com.poixson.backrooms.levels.Level_011;
import com.poixson.backrooms.levels.Level_771;
import com.poixson.backrooms.levels.Level_866;
import com.poixson.backrooms.listeners.BackroomsCommands;
import com.poixson.backrooms.listeners.PlayerDamageListener;
import com.poixson.utils.NumberUtils;
import com.poixson.utils.Utils;


public class BackroomsPlugin extends JavaPlugin {
	public static final String LOG_PREFIX  = "[Backrooms] ";
	public static final String CHAT_PREFIX = ChatColor.AQUA + "[Backrooms] " + ChatColor.WHITE;
	public static final Logger log = Logger.getLogger("Minecraft");

	public static final String GENERATOR_NAME = "Backrooms";

	protected static final AtomicReference<BackroomsPlugin> instance = new AtomicReference<BackroomsPlugin>(null);
	protected final AtomicBoolean enableScripts = new AtomicBoolean(false);

	// world generators
	protected final HashMap<Integer, BackroomsLevel> backlevels = new HashMap<Integer, BackroomsLevel>();

	// listeners
	protected final AtomicReference<BackroomsCommands>    commandListener      = new AtomicReference<BackroomsCommands>(null);
	protected final AtomicReference<PlayerDamageListener> playerDamageListener = new AtomicReference<PlayerDamageListener>(null);



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
			final PlayerDamageListener previous = this.playerDamageListener.getAndSet(listener);
			final PluginManager pm = Bukkit.getPluginManager();
			if (previous != null)
				HandlerList.unregisterAll(previous);
			pm.registerEvents(listener, this);
		}
	}

	@Override
	public void onDisable() {
		// unload generators
		for (final BackroomsLevel lvl : this.backlevels.values()) {
			lvl.unload();
		}
		this.backlevels.clear();
		// commands listener
		{
			final BackroomsCommands listener = this.commandListener.getAndSet(null);
			if (listener != null)
				listener.unregister();
		}
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
		final int level = Integer.parseInt(worldName.substring(5));
		if (!isValidLevel(level))
			return Integer.MIN_VALUE;
		return level;
	}

	public void assertValidLevel(final int level) {
		if (!this.isValidLevel(level))
			throw new RuntimeException("Invalid backrooms level: "+Integer.toString(level));
	}
	public boolean isValidLevel(final int level) {
		switch (level) {
		case 0:
		case 1:
		case 5:
		case 9:
		case 11:
		case 19:
		case 37:
		case 309:
		case 771:
		case 866:
			return true;
		default: break;
		}
		return false;
	}



	public World getLevelWorld(final int level) {
		final String worldName = this.getLevelWorldName(level);
		if (Utils.isEmpty(worldName))
			return null;
		return Bukkit.getWorld(worldName);
	}
	public String getLevelWorldName(final int level) {
		switch (level) {
		case 0:
		case 1:
		case 5:
		case 19:
		case 37:
		case 309: return "level0";
		case 9:   return "level9";
		case 11:  return "level11";
		case 771: return "level771";
		case 866: return "level866";
		default: break;
		}
		return null;
	}
	public BackroomsLevel getBackroomsLevel(final int level) {
		switch (level) {
		case 0:
		case 1:
		case 5:
		case 19:
		case 37:
		case 309: return this.backlevels.get(Integer.valueOf(0));
		case 9:   return this.backlevels.get(Integer.valueOf(9));
		case 11:  return this.backlevels.get(Integer.valueOf(11));
		case 771: return this.backlevels.get(Integer.valueOf(771));
		case 866: return this.backlevels.get(Integer.valueOf(866));
		default: break;
		}
		return null;
	}



	public void noclip(final Player player, final int level) {
		if (level == Integer.MIN_VALUE) {
			this.noclip(player);
			return;
		}
		final World world = this.getLevelWorld(level);
		if (world == null) throw new RuntimeException("Failed to find world for backrooms level: "+Integer.toString(level));
		final BackroomsLevel lvl = this.getBackroomsLevel(level);
		Location loc = lvl.getSpawn(level);
		if (loc == null)
			loc = world.getSpawnLocation();
		log.info(LOG_PREFIX+"No-clip player: "+player.getName()+" to level: "+Integer.toString(level));
		player.teleport(loc);
	}
	public int noclip(final Player player) {
		final int levelFrom = this.getLevel(player);
		final int levelTo = this.noclip(levelFrom);
		this.noclip(player, levelTo);
		return levelTo;
	}
	public int noclip(final int from) {
		final HashMap<Integer, Integer> chance = new HashMap<Integer, Integer>();
		switch (from) {
		case 0: // lobby
			chance.put(Integer.valueOf(   1 ), Integer.valueOf( 25 )); // basement
			chance.put(Integer.valueOf(   5 ), Integer.valueOf( 15 )); // hotel
			chance.put(Integer.valueOf(   9 ), Integer.valueOf( 10 )); // suburbs
			chance.put(Integer.valueOf(  11 ), Integer.valueOf( 10 )); // city
			chance.put(Integer.valueOf(  19 ), Integer.valueOf( 10 )); // attic
			chance.put(Integer.valueOf(  37 ), Integer.valueOf( 10 )); // pools
			chance.put(Integer.valueOf( 309 ), Integer.valueOf( 10 )); // path
			chance.put(Integer.valueOf( 771 ), Integer.valueOf( 10 )); // crossroads
			chance.put(Integer.valueOf( 866 ), Integer.valueOf( 10 )); // dirtfield
			break;
		case -1: // basement
			chance.put(Integer.valueOf(   0 ), Integer.valueOf( 20 )); // lobby
			chance.put(Integer.valueOf(   5 ), Integer.valueOf( 10 )); // hotel
			chance.put(Integer.valueOf(   9 ), Integer.valueOf(  8 )); // suburbs
			chance.put(Integer.valueOf(  11 ), Integer.valueOf(  8 )); // city
			chance.put(Integer.valueOf(  19 ), Integer.valueOf( 20 )); // attic
			chance.put(Integer.valueOf(  37 ), Integer.valueOf( 10 )); // pools
			chance.put(Integer.valueOf( 309 ), Integer.valueOf(  2 )); // path
			chance.put(Integer.valueOf( 771 ), Integer.valueOf(  2 )); // crossroads
			chance.put(Integer.valueOf( 866 ), Integer.valueOf(  5 )); // dirtfield
			break;
		case 5: // hotel
			chance.put(Integer.valueOf(   0 ), Integer.valueOf( 20 )); // lobby
			chance.put(Integer.valueOf(   1 ), Integer.valueOf( 10 )); // basement
			chance.put(Integer.valueOf(  11 ), Integer.valueOf( 10 )); // city
			chance.put(Integer.valueOf(  19 ), Integer.valueOf( 15 )); // attic
			chance.put(Integer.valueOf(  37 ), Integer.valueOf( 20 )); // pools
			chance.put(Integer.valueOf( 866 ), Integer.valueOf(  5 )); // dirtfield
			break;
		case 9: // suburbs
			chance.put(Integer.valueOf(   0 ), Integer.valueOf(  5 )); // lobby
			chance.put(Integer.valueOf(  11 ), Integer.valueOf( 10 )); // city
			chance.put(Integer.valueOf(  37 ), Integer.valueOf( 10 )); // pools
			chance.put(Integer.valueOf( 309 ), Integer.valueOf(  5 )); // path
			chance.put(Integer.valueOf( 771 ), Integer.valueOf(  5 )); // crossroads
			chance.put(Integer.valueOf( 866 ), Integer.valueOf(  5 )); // dirtfield
			break;
		case 11: // city
			chance.put(Integer.valueOf(   0 ), Integer.valueOf(  5 )); // lobby
			chance.put(Integer.valueOf(   1 ), Integer.valueOf(  5 )); // basement
			chance.put(Integer.valueOf(   5 ), Integer.valueOf( 10 )); // hotel
			chance.put(Integer.valueOf(   9 ), Integer.valueOf( 10 )); // suburbs
			chance.put(Integer.valueOf(  37 ), Integer.valueOf(  5 )); // pools
			break;
		case 309: // path
			chance.put(Integer.valueOf(   0 ), Integer.valueOf( 10 )); // lobby
			chance.put(Integer.valueOf(   9 ), Integer.valueOf( 15 )); // suburbs
			chance.put(Integer.valueOf(  11 ), Integer.valueOf( 10 )); // city
			chance.put(Integer.valueOf( 771 ), Integer.valueOf( 20 )); // crossroads
			chance.put(Integer.valueOf( 866 ), Integer.valueOf( 10 )); // dirtfield
			break;
		default:
			chance.put(Integer.valueOf(   0 ), Integer.valueOf( 10 )); // lobby
			chance.put(Integer.valueOf(   1 ), Integer.valueOf(  1 )); // basement
			chance.put(Integer.valueOf(   5 ), Integer.valueOf(  1 )); // hotel
			chance.put(Integer.valueOf(   9 ), Integer.valueOf(  1 )); // suburbs
			chance.put(Integer.valueOf(  11 ), Integer.valueOf(  1 )); // city
			chance.put(Integer.valueOf(  19 ), Integer.valueOf(  1 )); // attic
			chance.put(Integer.valueOf(  37 ), Integer.valueOf(  1 )); // pools
			chance.put(Integer.valueOf( 309 ), Integer.valueOf(  1 )); // path
			chance.put(Integer.valueOf( 771 ), Integer.valueOf(  1 )); // crossroads
			chance.put(Integer.valueOf( 866 ), Integer.valueOf(  1 )); // dirtfield
			break;
		}
		if (chance.isEmpty())
			return 0;
		int total = 0;
		for (final Integer i : chance.values()) {
			total += i;
		}
		final int total2 = total * total;
		final int rnd = NumberUtils.GetNewRandom(0, total2, NumberUtils.GetRandom(0, total2)) % total;
		final Iterator<Entry<Integer, Integer>> it = chance.entrySet().iterator();
		while (it.hasNext()) {
			final Entry<Integer, Integer> entry = it.next();
			final Integer level  = entry.getKey();
			final Integer weight = entry.getValue();
			if (total - weight <= rnd)
				return level.intValue();
			total -= weight;
		}
		log.warning(LOG_PREFIX+"Failed to find random level");
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
			case 866: lvl = new Level_866(this); break;
			default: throw new RuntimeException("Invalid backrooms level: "+Integer.toString(level));
			}
			final BackroomsLevel existing = this.backlevels.putIfAbsent(Integer.valueOf(level), lvl);
			if (existing != null)
				return existing;
			return lvl;
		}
	}



	protected static final AtomicInteger Last10K = new AtomicInteger(0);

	public static int Rnd10K() {
		final int rnd = NumberUtils.GetNewRandom(0, 9999, Last10K.get());
		Last10K.set(rnd);
		return rnd;
	}



}
