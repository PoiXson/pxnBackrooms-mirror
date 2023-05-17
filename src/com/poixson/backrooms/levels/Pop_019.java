package com.poixson.backrooms.levels;

import static com.poixson.backrooms.levels.Level_000.ENABLE_GEN_019;

import java.util.LinkedList;

import org.bukkit.generator.LimitedRegion;

import com.poixson.commonmc.tools.plotter.BlockPlotter;


// 19 | Attic
public class Pop_019 implements PopBackrooms {



	public Pop_019(final Level_000 level0) {
	}



	@Override
	public void populate(final int chunkX, final int chunkZ,
	final LimitedRegion region, final LinkedList<BlockPlotter> plots) {
		if (!ENABLE_GEN_019) return;
//TODO
	}



}
