package com.poixson.backrooms;

import java.util.LinkedList;
import java.util.concurrent.CopyOnWriteArraySet;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.tools.abstractions.Tuple;
import com.poixson.tools.plotter.BlockPlotter;
import com.poixson.utils.FastNoiseLiteD;
import com.poixson.utils.NumberUtils;


public abstract class BackroomsGen {

	public final BackroomsPlugin plugin;
	public final BackroomsLevel backlevel;

	protected final CopyOnWriteArraySet<FastNoiseLiteD> noises = new CopyOnWriteArraySet<FastNoiseLiteD>();
	protected final int seed;

	public final int level_y;
	public final int level_h;



	public BackroomsGen(final BackroomsLevel backlevel, final int seed,
			final int level_y, final int level_h) {
		this.plugin    = backlevel.plugin;
		this.backlevel = backlevel;
		this.seed      = seed;
		this.level_y   = level_y;
		this.level_h   = level_h;
	}



	public void register() {}
	public void unregister() {}



	protected FastNoiseLiteD register(final FastNoiseLiteD noise) {
		noise.setSeed(this.seed);
		this.noises.add(noise);
		return noise;
	}



	public abstract int getLevelNumber();

	public int getSeed() {
		return this.seed;
	}



	protected void initNoise(final ConfigurationSection cfgParams) {}

	protected abstract void loadConfig(final ConfigurationSection cfgParams, final ConfigurationSection cfgBlocks);
	protected abstract void configDefaults(final ConfigurationSection cfgParams, final ConfigurationSection cfgBlocks);



	public abstract void generate(final PreGenData pregen,
			final LinkedList<Tuple<BlockPlotter, StringBuilder[][]>> plots,
			final ChunkData chunk, final int chunkX, final int chunkZ);



}
