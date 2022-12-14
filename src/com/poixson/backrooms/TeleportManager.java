package com.poixson.backrooms;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.bukkit.Location;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.poixson.backrooms.levels.BackroomsLevel;
import com.poixson.tools.xTime;
import com.poixson.utils.NumberUtils;
import com.poixson.utils.Utils;


public class TeleportManager {
	public static final String LOG_PREFIX = BackroomsPlugin.LOG_PREFIX;
	public static final Logger log        = BackroomsPlugin.log;

	protected final BackroomsPlugin plugin;

	protected final HashMap<Integer, HashMap<Integer, Integer>> weights;
	protected int rndLast = 0;

	protected final long updatePeriod = xTime.ParseToLong("1h");
	protected final long updateGrace  = xTime.ParseToLong("30s");
	protected long timeUpdated = 0L;
	protected long timeLast    = 0L;
	protected final HashMap<Integer, Integer> levelsFromTo = new HashMap<Integer, Integer>();
	protected final HashMap<Integer, Location> levelSpawns = new HashMap<Integer, Location>();



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



	public int getDestinationLevel(final int level_from) {
		final long current = Utils.GetMS();
		// since last update
		final long sinceUpdate = current - this.timeUpdated;
		if (sinceUpdate > this.updatePeriod) {
			// since last tp
			final long sinceLast = current - this.timeLast;
			if (sinceLast > this.updateGrace) {
				// reset from/to levels
				log.info(LOG_PREFIX+"Rolling the teleport dice..");
				this.timeUpdated = current;
				this.levelsFromTo.clear();
				this.levelSpawns.clear();
			}
		}
		this.timeLast = current;
		// cached from/to level
		{
			final Integer level = this.levelsFromTo.get(Integer.valueOf(level_from));
			if (level != null)
				return level.intValue();
		}
		// find from/to level
		{
			final int level = this._getDestinationLevel(level_from);
			this.levelsFromTo.put(Integer.valueOf(level_from), Integer.valueOf(level));
			return level;
		}
	}
	protected int _getDestinationLevel(final int level_from) {
		final HashMap<Integer, Integer> weights = this.weights.get(Integer.valueOf(level_from));
		if (weights == null)           throw new RuntimeException("Unknown backrooms level: "+Integer.toString(level_from));
		if (weights.isEmpty()) throw new RuntimeException("Backrooms level has no weights set: "+Integer.toString(level_from));
		int total = 0;
		for (final Integer i : weights.values()) {
			total += i.intValue();
		}
		final int rnd = NumberUtils.GetNewRandom(0, total, this.rndLast);
		this.rndLast = rnd;
		Entry<Integer, Integer> entry;
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
		log.warning(LOG_PREFIX+"Failed to find random level");
		return 0;
	}



//TODO: random locations near cached location
	public Location getSpawnLocation(final int level) {
		// cached spawn location
		{
			final Location loc = this.levelSpawns.get(Integer.valueOf(level));
			if (loc != null)
				return loc;
		}
		// find spawn location
		{
			final BackroomsLevel backrooms = this.plugin.getBackroomsLevel(level);
			Location loc = null;
			for (int i=0; i<5; i++) {
				loc = backrooms.getSpawn(level);
				if (loc != null)
					break;
			}
			if (loc == null) throw new RuntimeException("Failed to find spawn location for level: "+Integer.toString(level));
			this.levelSpawns.put(Integer.valueOf(level), loc);
			return loc;
		}
	}



}
