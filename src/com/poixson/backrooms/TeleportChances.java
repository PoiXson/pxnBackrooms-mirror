package com.poixson.backrooms;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.logging.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.poixson.utils.NumberUtils;


public class TeleportChances {
	public static final String LOG_PREFIX = BackroomsPlugin.LOG_PREFIX;
	public static final Logger log        = BackroomsPlugin.log;

	protected final HashMap<Integer, HashMap<Integer, Integer>> weights;
	protected int rndLast = 0;



	public TeleportChances(final HashMap<Integer, HashMap<Integer, Integer>> weights) {
		this.weights = weights;
	}



	public static TeleportChances Load(final BackroomsPlugin plugin) {
		final HashMap<Integer, HashMap<Integer, Integer>> chances = new HashMap<Integer, HashMap<Integer, Integer>>();
		final InputStream input = plugin.getResource("chances.json");
		if (input == null) throw new RuntimeException("Failed to load chances.json");
		final InputStreamReader reader = new InputStreamReader(input);
		final JsonElement json = JsonParser.parseReader(reader);
		final Iterator<Entry<String, JsonElement>> it = json.getAsJsonObject().entrySet().iterator();
		String key;
		while (it.hasNext()) {
			final Entry<String, JsonElement> entry = it.next();
			key = entry.getKey();
			final int level = (
				NumberUtils.IsNumeric(key)
				? Integer.parseInt(key)
				: Integer.MIN_VALUE
			);
			final HashMap<Integer, Integer> weights = new HashMap<Integer, Integer>();
			final Iterator<Entry<String, JsonElement>> it2 = entry.getValue().getAsJsonObject().entrySet().iterator();
			while (it2.hasNext()) {
				final Entry<String, JsonElement> entry2 = it2.next();
				final int lvl = Integer.parseInt(entry2.getKey());
				final int w   = entry2.getValue().getAsInt();
				weights.put(Integer.valueOf(lvl), Integer.valueOf(w));
			}
			chances.put(Integer.valueOf(level), weights);
		}
		return new TeleportChances(chances);
	}



	public int getDestinationLevel(final int level_from) {
		final HashMap<Integer, Integer> weights = this.weights.get(Integer.valueOf(level_from));
		if (weights == null)           throw new RuntimeException("Unknown backrooms level: "+Integer.toString(level_from));
		if (weights.isEmpty()) throw new RuntimeException("Backrooms level has no weights set: "+Integer.toString(level_from));
		int total = 0;
		for (final Integer i : weights.values()) {
			total += i.intValue();
		}
		final int rnd = NumberUtils.GetNewRandom(0, total, this.rndLast);
		this.rndLast = rnd;
		final Iterator<Entry<Integer, Integer>> it = weights.entrySet().iterator();
		while (it.hasNext()) {
			final Entry<Integer, Integer> entry = it.next();
			final int level  = entry.getKey().intValue();
			final int weight = entry.getValue().intValue();
			total -= weight;
			if (total <= rnd)
				return level;
		}
		log.warning(LOG_PREFIX+"Failed to find random level");
		return 0;
	}



}
