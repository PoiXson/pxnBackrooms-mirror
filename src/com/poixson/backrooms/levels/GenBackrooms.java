package com.poixson.backrooms.levels;

import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.tools.dao.Dxy;
import com.poixson.utils.FastNoiseLiteD;


public abstract class GenBackrooms {

	protected final BackroomsPlugin plugin;

	protected final CopyOnWriteArraySet<FastNoiseLiteD> noises = new CopyOnWriteArraySet<FastNoiseLiteD>();

	public final int level_y;
	public final int level_h;



	public GenBackrooms(final BackroomsPlugin plugin, final int level_y, final int level_h) {
		this.plugin = plugin;
		this.level_y = level_y;
		this.level_h = level_h;
	}



	public void unload() {
	}



	protected FastNoiseLiteD register(final FastNoiseLiteD noise) {
		this.noises.add(noise);
		return noise;
	}



	public void setSeed(final int seed) {
		for (final FastNoiseLiteD noise : this.noises) {
			noise.setSeed(seed);
		}
	}



	public abstract void generate(final Map<Dxy, ? extends PreGenData> datamap,
			final ChunkData chunk, final int chunkX, final int chunkZ);



}
