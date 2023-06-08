package com.poixson.backrooms.tasks;

import static com.poixson.backrooms.BackroomsPlugin.LOG_PREFIX;
import static com.poixson.commonmc.tools.plugin.xJavaPlugin.LOG;
import static com.poixson.utils.Utils.SafeClose;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.Location;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.poixson.backrooms.BackroomsLevel;
import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.utils.NumberUtils;
import com.poixson.utils.RandomUtils;


public class TeleportManager {

	protected final BackroomsPlugin plugin;

	protected final HashMap<Integer, HashMap<Integer, Integer>> weights;
	protected final HashMap<Integer, Location> cachedSpawns = new HashMap<Integer, Location>();
	protected final HashMap<Integer, Integer> cachedToLevel = new HashMap<Integer, Integer>();

	protected int rndLast = 0;



	public TeleportManager(final BackroomsPlugin plugin,
	final HashMap<Integer, HashMap<Integer, Integer>> weights) {
		this.plugin  = plugin;
		this.weights = weights;
	}

	public static TeleportManager Load(final BackroomsPlugin plugin) {
		final HashMap<Integer, HashMap<Integer, Integer>> chances = new HashMap<Integer, HashMap<Integer, Integer>>();
		final InputStream input = plugin.getResource("chances.json");
		if (input == null) throw new RuntimeException("Failed to load chances.json");
		final InputStreamReader reader = new InputStreamReader(input);
		final JsonElement json = JsonParser.parseReader(reader);
		SafeClose(reader);
		SafeClose(input);
		final Iterator<Entry<String, JsonElement>> it = json.getAsJsonObject().entrySet().iterator();
		String key;
		int level, lvl, w;
		Entry<String, JsonElement> entry, entry2;
		HashMap<Integer, Integer> weights;
		while (it.hasNext()) {
			entry = it.next();
			key = entry.getKey();
			level = (
				NumberUtils.IsNumeric(key)
				? Integer.parseInt(key)
				: Integer.MIN_VALUE
			);
			weights = new HashMap<Integer, Integer>();
			final Iterator<Entry<String, JsonElement>> it2 = entry.getValue().getAsJsonObject().entrySet().iterator();
			while (it2.hasNext()) {
				entry2 = it2.next();
				lvl = Integer.parseInt(entry2.getKey());
				w   = entry2.getValue().getAsInt();
				weights.put(Integer.valueOf(lvl), Integer.valueOf(w));
			}
			chances.put(Integer.valueOf(level), weights);
		}
		return new TeleportManager(plugin, chances);
	}



	public void markUsed() {
		this.plugin.getHourlyTask()
			.markUsed();
	}

	public void flush() {
		final int count = this.cachedSpawns.size();
		this.cachedSpawns.clear();
		this.cachedToLevel.clear();
		if (count > 0)
			LOG.info(LOG_PREFIX + "Rolling the teleport dice..");
	}



	public int getDestinationLevel(final int level_from) {
		this.markUsed();
		// cached
		{
			final Integer level = this.cachedToLevel.get(Integer.valueOf(level_from));
			if (level != null)
				return level.intValue();
		}
		// find from/to level
		{
			final int level = this.findDestinationLevel(level_from);
			this.cachedToLevel.putIfAbsent(Integer.valueOf(level_from), Integer.valueOf(level));
			return level;
		}
	}
	protected int findDestinationLevel(final int level_from) {
		final HashMap<Integer, Integer> weights = this.weights.get(Integer.valueOf(level_from));
		if (weights == null)   throw new RuntimeException("Unknown backrooms level: " + Integer.toString(level_from));
		if (weights.isEmpty()) throw new RuntimeException("Backrooms level has no weights set: " + Integer.toString(level_from));
		int total = 0;
		for (final Integer i : weights.values()) {
			total += i.intValue();
		}
		if (total < 1)
			return 0;
		Entry<Integer, Integer> entry;
		if (total == 1) {
			final Iterator<Entry<Integer, Integer>> it = weights.entrySet().iterator();
			entry = it.next();
			if (entry.getValue().intValue() == 1)
				return entry.getKey().intValue();
		}
		final int rnd = RandomUtils.GetNewRandom(0, total, this.rndLast);
		this.rndLast = rnd;
		int level, weight;
		final Iterator<Entry<Integer, Integer>> it = weights.entrySet().iterator();
		while (it.hasNext()) {
			entry = it.next();
			level  = entry.getKey().intValue();
			weight = entry.getValue().intValue();
			total -= weight;
			if (total <= rnd)
				return level;
		}
		LOG.warning(LOG_PREFIX + "Failed to find random level");
		return 0;
	}



	public Location getSpawnArea(final int level) {
		// cached
		{
			final Location spawn = this.cachedSpawns.get(Integer.valueOf(level));
			if (spawn != null)
				return spawn;
		}
		// find spawn
		{
			final BackroomsLevel backlevel = this.plugin.getBackroomsLevel(level);
			if (backlevel == null) {
				LOG.warning(LOG_PREFIX + "Unknown backrooms level: " + Integer.toString(level));
				return null;
			}
			final Location spawn = backlevel.getNewSpawnArea(level);
			this.cachedSpawns.put(Integer.valueOf(level), spawn);
			return spawn;
		}
	}

	public Location getSpawnLocation(final int level) {
		final Location spawn = this.getSpawnArea(level);
		return this.getSpawnLocation(spawn, level);
	}
	public Location getSpawnLocation(final Location spawn, final int level) {
		final BackroomsLevel backlevel = this.plugin.getBackroomsLevel(level);
		if (backlevel == null) {
			LOG.warning(LOG_PREFIX + "Unknown backrooms level: " + Integer.toString(level));
			return null;
		}
		return backlevel.getSpawnNear(spawn);
	}



}
