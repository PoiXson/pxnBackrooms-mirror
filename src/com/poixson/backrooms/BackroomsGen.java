package com.poixson.backrooms;

import java.util.LinkedList;
import java.util.concurrent.CopyOnWriteArraySet;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.tools.xRand;
import com.poixson.tools.abstractions.Tuple;
import com.poixson.tools.plotter.BlockPlotter;
import com.poixson.utils.FastNoiseLiteD;


public abstract class BackroomsGen {

	public final BackroomsPlugin plugin;
	public final BackroomsWorld backworld;
	public final BackroomsGen   gen_below;

	protected final CopyOnWriteArraySet<FastNoiseLiteD> noises = new CopyOnWriteArraySet<FastNoiseLiteD>();
	protected final int seed;

	public final int bedrock_barrier;

	protected final xRand random = (new xRand()).seed_time();



	protected BackroomsGen(final BackroomsWorld backworld, final BackroomsGen gen_below, final int seed) {
		this.plugin    = backworld.plugin;
		this.backworld = backworld;
		this.gen_below = gen_below;
		this.seed      = seed;
		this.bedrock_barrier = this.plugin.getBedrockBarrier();
		final int level_number = this.getLevelNumber();
		final ConfigurationSection cfgParams = this.plugin.getConfigLevelParams(level_number);
		final ConfigurationSection cfgBlocks = this.plugin.getConfigLevelBlocks(level_number);
		this.configDefaults(cfgParams, cfgBlocks);
	}



	public void register() {
		this.initNoise();
	}
	public void unregister() {}



	protected FastNoiseLiteD register(final FastNoiseLiteD noise) {
		noise.setSeed(this.seed);
		this.noises.add(noise);
		return noise;
	}



	public abstract int getLevelNumber();



	public abstract int getLevelY();
	public int getOpenY() {
		return this.getMinY() + 1;
	}

	public int getMinY() {
		return -64;
	}
	public int getMaxY() {
		return 320;
	}

	protected int getDefaultY() {
		return (this.gen_below == null ? -64 : this.gen_below.getMaxY()+1);
	}



	public int getSeed() {
		return this.seed;
	}

	protected void initNoise() {}

	protected abstract void configDefaults(final ConfigurationSection cfgParams, final ConfigurationSection cfgBlocks);



	public abstract void generate(final PreGenData pregen,
			final LinkedList<Tuple<BlockPlotter, StringBuilder[][]>> plots,
			final ChunkData chunk, final int chunkX, final int chunkZ);



}
