package com.poixson.backrooms;

import java.util.LinkedList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicReference;

import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.commonmc.tools.plotter.BlockPlotter;
import com.poixson.utils.FastNoiseLiteD;
import com.poixson.utils.Utils;


public abstract class BackroomsGen {

	protected final BackroomsPlugin plugin;
	protected final BackroomsLevel backlevel;

	protected final CopyOnWriteArraySet<FastNoiseLiteD> noises = new CopyOnWriteArraySet<FastNoiseLiteD>();

	public final int level_y;
	public final int level_h;



	public BackroomsGen(final BackroomsLevel backlevel,
			final int level_y, final int level_h) {
		this.plugin    = backlevel.plugin;
		this.backlevel = backlevel;
		this.level_y   = level_y;
		this.level_h   = level_h;
	}



	public void register() {}
	public void unregister() {}



	protected FastNoiseLiteD register(final FastNoiseLiteD noise) {
		this.noises.add(noise);
		return noise;
	}

	public void setSeed(final int seed) {
		for (final FastNoiseLiteD noise : this.noises) {
			noise.setSeed(seed);
		}
	}
	public void initNoise() {}



	public abstract void generate(final PreGenData pregen, final ChunkData chunk,
			final LinkedList<BlockPlotter> plots, final int chunkX, final int chunkZ);



	protected abstract void loadConfig();



	public static BlockData StringToBlockData(final AtomicReference<String> atomic, final String def) {
		final String blockStr = atomic.get();
		if (Utils.notEmpty(blockStr))
			return StringToBlockData(blockStr);
		return StringToBlockData(def);
	}
	public static BlockData StringToBlockData(final String blockStr) {
		return Bukkit.createBlockData(blockStr);
	}



}
