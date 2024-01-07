package com.poixson.backrooms.gens;

import static com.poixson.backrooms.gens.Gen_309.PATH_START_X;
import static com.poixson.backrooms.gens.Gen_309.PATH_START_Z;
import static com.poixson.backrooms.worlds.Level_000.ENABLE_GEN_309;
import static com.poixson.backrooms.worlds.Level_000.ENABLE_TOP_309;

import java.util.LinkedList;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.generator.LimitedRegion;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.backrooms.BackroomsPop;
import com.poixson.backrooms.worlds.Level_000;
import com.poixson.scripting.xScript;
import com.poixson.scripting.xScriptInstance;
import com.poixson.scripting.loader.xScriptLoader;
import com.poixson.scripting.loader.xScriptLoader_File;
import com.poixson.tools.plotter.BlockPlotter;


// 309 | Radio Station
public class Pop_309 implements BackroomsPop {

	public static final double FENCE_NOISE_STRENGTH =  2.0;
	public static final double FENCE_RADIUS         = 65.0;
	public static final double FENCE_THICKNESS      =  1.3;

	protected final BackroomsPlugin plugin;
	protected final Gen_309 gen;

	protected final Pop_309_Trees treePop;



	public Pop_309(final Level_000 level0) {
		this.plugin = level0.getPlugin();
		this.gen    = level0.gen_309;
		this.treePop = new Pop_309_Trees(this.gen);
	}



	@Override
	public void populate(final int chunkX, final int chunkZ,
	final LimitedRegion region, final LinkedList<BlockPlotter> plots) {
		if (!ENABLE_GEN_309) return;
		// trees
		if (ENABLE_TOP_309)
			this.treePop.populate(null, null, chunkX, chunkZ, region);
		// radio station
		if (chunkX == 0 && chunkZ == 0) {
			this.populate0x0(region);
		} else
		// fence around clearing
		if (Math.abs(chunkX) < 8
		&&  Math.abs(chunkZ) < 8) {
			if (ENABLE_TOP_309) {
				double distance;
				int xx, zz;
				for (int iz=0; iz<16; iz++) {
					zz = (chunkZ * 16) + iz;
					LOOP_X:
						for (int ix=0; ix<16; ix++) {
							xx = (chunkX * 16) + ix;
							distance = this.gen.getCenterClearingDistance(xx, zz, FENCE_NOISE_STRENGTH);
							if (distance >= FENCE_RADIUS
							&&  distance <= FENCE_RADIUS + FENCE_THICKNESS) {
								boolean found = false;
								int sy = this.gen.level_y;
								LOOP_SURFACE:
									for (int i=0; i<10; i++) {
										final Material type = region.getType(xx, sy+i, zz);
										if (Material.AIR.equals(type)) {
											found = true;
											sy += i;
											break LOOP_SURFACE;
										}
									}
								if (found) {
									final int path_x = this.gen.getPathX(zz);
									if (zz > 0
									&&  xx < path_x+5
									&&  xx > path_x-5)
										continue LOOP_X;
									final BlockData bars = Bukkit.createBlockData("iron_bars[north=true,south=true,east=true,west=true]");
									for (int iy=0; iy<5; iy++)
										region.setBlockData(xx, sy+iy, zz, bars);
									region.setType(xx, sy+5, zz, Material.CUT_COPPER_SLAB);
								}
							}
						} // end ix
				} // end iz
			} // end ENABLE_TOP_309
		}
	}



	// radio station
	public void populate0x0(final LimitedRegion region) {
		// find surface
		int y = Integer.MIN_VALUE;
		for (int i=0; i<10; i++) {
			if (Material.AIR.equals(region.getType(PATH_START_X, this.gen.level_y+i, PATH_START_Z-1))) {
				y = this.gen.level_y + i;
				break;
			}
		}
		if (y == Integer.MIN_VALUE) {
			this.log().warning("Failed to generate level 309 building; unknown y point.");
			return;
		}
		// radio station script
		xScript script = null;
		try {
			final xScriptLoader loader =
				new xScriptLoader_File(
					this.plugin.getClass(),
					"scripts", // local path
					"scripts", // resource path
					"backrooms-radiostation.js"
				);
			script = new xScriptInstance(loader, false);
			script
				.setVariable("region",         region                   )
				.setVariable("surface_y",      y                        )
				.setVariable("seed",           this.gen.getSeed()       )
				.setVariable("enable_ceiling", ENABLE_TOP_309           )
				.setVariable("path_width",     this.gen.path_width.get())
				.setVariable("path_start_x",   PATH_START_X             )
				.setVariable("path_start_z",   PATH_START_Z             );
			script.start();
		} finally {
			if (script != null)
				script.stop();
		}
	}



	public Logger log() {
		return this.plugin.getLogger();
	}



}
