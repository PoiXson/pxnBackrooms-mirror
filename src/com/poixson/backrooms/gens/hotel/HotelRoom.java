package com.poixson.backrooms.gens.hotel;

import java.util.LinkedList;

import org.bukkit.block.BlockFace;
import org.bukkit.generator.LimitedRegion;

import com.poixson.tools.dao.Iabcd;
import com.poixson.tools.plotter.BlockPlotter;


public interface HotelRoom {


	public void build(final Iabcd area, final int y, final BlockFace direction,
			final LimitedRegion region, final LinkedList<BlockPlotter> plots);


}
