package com.poixson.backrooms.gens;

import static com.poixson.backrooms.worlds.Level_011.ENABLE_GEN_308;

import java.util.LinkedList;

import org.bukkit.generator.LimitedRegion;

import com.poixson.backrooms.BackroomsPop;
import com.poixson.backrooms.worlds.Level_011;
import com.poixson.tools.plotter.BlockPlotter;


// 308 | Ikea
public class Pop_308 implements BackroomsPop {



	public Pop_308(final Level_011 level11) {
		super();
	}



	@Override
	public void populate(final int chunkX, final int chunkZ,
	final LimitedRegion region, final LinkedList<BlockPlotter> plots) {
		if (!ENABLE_GEN_308) return;
	}



}
