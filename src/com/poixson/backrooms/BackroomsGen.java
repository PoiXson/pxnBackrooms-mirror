package com.poixson.backrooms;

import java.util.LinkedList;
import java.util.concurrent.CopyOnWriteArraySet;

import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.tools.plotter.BlockPlotter;
import com.poixson.utils.FastNoiseLiteD;


public abstract class BackroomsGen {

	protected final BackroomsPlugin plugin;
	protected final BackroomsLevel backlevel;

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



	public int getSeed() {
		return this.seed;
	}



	public void initNoise() {}

	protected abstract void loadConfig();



	public abstract void generate(final PreGenData pregen, final ChunkData chunk,
			final LinkedList<BlockPlotter> plots, final int chunkX, final int chunkZ);



}
