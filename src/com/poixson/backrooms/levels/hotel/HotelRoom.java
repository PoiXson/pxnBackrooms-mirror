package com.poixson.backrooms.levels.hotel;

import java.util.LinkedList;

import org.bukkit.block.BlockFace;
import org.bukkit.generator.LimitedRegion;

import com.poixson.commonmc.tools.plotter.BlockPlotter;
import com.poixson.tools.dao.Iabcd;


public interface HotelRoom {


	public void build(final Iabcd area, final int y, final BlockFace direction,
			final LimitedRegion region, final LinkedList<BlockPlotter> plots);


}
