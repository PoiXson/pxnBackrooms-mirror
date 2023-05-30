package com.poixson.backrooms.levels;

import java.util.LinkedList;
import java.util.concurrent.CopyOnWriteArraySet;

import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.commonmc.tools.plotter.BlockPlotter;
import com.poixson.utils.FastNoiseLiteD;


public abstract class GenBackrooms {

	protected final BackroomsPlugin plugin;
	protected final LevelBackrooms backlevel;

	protected final CopyOnWriteArraySet<FastNoiseLiteD> noises = new CopyOnWriteArraySet<FastNoiseLiteD>();

	public final int level_y;
	public final int level_h;



	public GenBackrooms(final LevelBackrooms backlevel,
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



	public abstract void generate(final PreGenData pregen, final ChunkData chunk,
			final LinkedList<BlockPlotter> plots, final int chunkX, final int chunkZ);



}
