package com.poixson.backrooms;

import java.util.LinkedList;

import org.bukkit.generator.LimitedRegion;

import com.poixson.tools.abstractions.Tuple;
import com.poixson.tools.plotter.BlockPlotter;


public interface BackroomsPop {


	public void populate(final LinkedList<Tuple<BlockPlotter, StringBuilder[][]>> plots,
			final LimitedRegion region, final int chunkX, final int chunkZ);


}
