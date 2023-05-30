package com.poixson.backrooms.levels;

import java.util.LinkedList;

import org.bukkit.generator.LimitedRegion;

import com.poixson.commonmc.tools.plotter.BlockPlotter;


public interface PopBackrooms {


	public void populate(final int chunkX, final int chunkZ,
			final LimitedRegion region, final LinkedList<BlockPlotter> plots);


}
