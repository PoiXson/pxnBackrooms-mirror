package com.poixson.backrooms.gens;

import static com.poixson.backrooms.gens.Gen_309.PATH_START_X;
import static com.poixson.backrooms.gens.Gen_309.PATH_START_Z;
import static com.poixson.backrooms.worlds.Level_000.ENABLE_GEN_309;
import static com.poixson.backrooms.worlds.Level_000.ENABLE_TOP_309;
import static com.poixson.backrooms.worlds.Level_000.H_019;
import static com.poixson.backrooms.worlds.Level_000.SUBFLOOR;
import static com.poixson.backrooms.worlds.Level_000.Y_019;
import static com.poixson.backrooms.worlds.Level_000.Y_309;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;
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
import com.poixson.tools.plotter.PlotterFactory;


// 309 | Radio Station
public class Pop_309 implements BackroomsPop {

	public static final double FENCE_NOISE_STRENGTH =  2.0;
	public static final double FENCE_RADIUS         = 65.0;
	public static final double FENCE_THICKNESS      =  1.3;

	protected final BackroomsPlugin plugin;
	protected final Gen_309 gen;

	protected final Pop_309_Trees treePop;

	protected final AtomicInteger special_index = new AtomicInteger(0);



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
		final int count_trees;
		if (ENABLE_TOP_309) count_trees = this.treePop.populate(chunkX, chunkZ, region);
		else                count_trees = 0;
		// radio station
		if (chunkX == 0 && chunkZ == 0) {
			this.populate0x0(region);
		} else
		if (ENABLE_TOP_309) {
			// fence around clearing
			if (Math.abs(chunkX) < 8
			&&  Math.abs(chunkZ) < 8) {
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
			} else {
				// prairie
				if (count_trees == 0
				&&  ENABLE_TOP_309) {
					final int path_clearing = this.gen.path_clearing.get() * 3;
					final int xx = chunkX * 16;
					final int zz = chunkZ * 16;
					if (!this.gen.pathTrace.isPath(xx, zz, path_clearing)) {
						// find surface
						int surface_y = 0;
						SURFACE_LOOP:
						for (int iy=0; iy<12; iy++) {
							if (Material.AIR.equals(region.getType(xx+2, Y_309+iy, zz+2))) {
								surface_y = Y_309 + iy;
								break SURFACE_LOOP;
							}
						}
						if (surface_y != 0) {
							final int special = this.special_index.incrementAndGet();
							final int special_mod11 = special % 11;
							final int special_mod7  = special % 7;
							SWITCH_MOD11:
							switch (special_mod11) {
							case 1: this.populate_stairs(xx, surface_y, zz, region); break SWITCH_MOD11; // stairs
							case 5: this.populate_door(  xx, surface_y, zz, region); break SWITCH_MOD11; // door
							default:
								SWITCH_MOD7:
								switch (special_mod7) {
								case 1: this.populate_hatch(xx, surface_y, zz, region); break SWITCH_MOD7; // hatch
								default: break SWITCH_MOD7;
								}
								break SWITCH_MOD11;
							}
						}
					}
				}
			}
		}
	}



	// forest stairs
	public void populate_stairs(final int x, final int y, final int z, final LimitedRegion region) {
		((Level_000)this.gen.backlevel).portal_309_stairs.add(x, z);
		final BlockPlotter plot =
			(new PlotterFactory())
			.placer(region)
			.axis("use")
			.xyz(x+6, y, z+6)
			.whd(2, 11, 11)
			.build();
		plot.type('<', "minecraft:stone_brick_stairs[facing=south]");
		plot.type('>', "minecraft:stone_brick_stairs[facing=north,half=top]");
		plot.type('L', "minecraft:light[level=15]"                 );
		final StringBuilder[][] matrix = plot.getMatrix3D();
		for (int i=0; i<10; i++) {
			matrix[i][i  ].append("<<");
			matrix[i][i+1].append(">>");
		}
		plot.run();
	}



	// forest door
	public void populate_door(final int x, final int y, final int z, final LimitedRegion region) {
		((Level_000)this.gen.backlevel).portal_309_doors.add(x, z);
		final double value = this.gen.noisePrairie.getNoise(x, z);
		final Material block_door = this.getDoorStyle(value);
		final Material block_wall = this.getDoorWallStyle(value);
		final BlockPlotter plot =
			(new PlotterFactory())
			.placer(region)
			.axis("use")
			.xyz(x+6, y-1, z+6)
			.whd(3, 4, 3)
			.build();
		plot.type('#', block_wall                             );
		plot.type('d', block_door, "[facing=north,half=upper,hinge=right]");
		plot.type('D', block_door, "[facing=north,half=lower,hinge=right]");
		plot.type('b', block_door, "[facing=south,half=upper,hinge=right]");
		plot.type('B', block_door, "[facing=south,half=lower,hinge=right]");
		plot.type('_', Material.WARPED_PRESSURE_PLATE         );
		plot.type('L', "minecraft:light[level=15]"            );
		final StringBuilder[][] matrix = plot.getMatrix3D();
		matrix[3][1].append("###");
		matrix[2][1].append("# #"); matrix[2][0].append(" d" ); matrix[2][2].append(" b" );
		matrix[1][1].append("#_#"); matrix[1][0].append("LDL"); matrix[1][2].append("LBL");
		matrix[0][1].append(" #" );
		plot.run();
	}
	public Material getDoorStyle(final double value) {
		final int style = ((int)Math.floor(value * 1000.0)) % 11;
		switch (style) {
		case 0:  return Material.OAK_DOOR;
		case 1:  return Material.DARK_OAK_DOOR;
		case 2:  return Material.SPRUCE_DOOR;
		case 3:  return Material.BIRCH_DOOR;
		case 4:  return Material.JUNGLE_DOOR;
		case 5:  return Material.ACACIA_DOOR;
		case 6:  return Material.MANGROVE_DOOR;
		case 7:  return Material.CHERRY_DOOR;
		case 8:  return Material.BAMBOO_DOOR;
		case 9:  return Material.CRIMSON_DOOR;
		case 10: return Material.WARPED_DOOR;
		default: throw new RuntimeException("Unknown forest door style: "+Integer.toString(style));
		}
	}
	public Material getDoorWallStyle(final double value) {
		final int style = ((int)Math.floor(value * 100000.0)) % 31;
		switch (style) {
		case 0:  return Material.OAK_PLANKS;
		case 1:  return Material.DARK_OAK_PLANKS;
		case 2:  return Material.SPRUCE_PLANKS;
		case 3:  return Material.BIRCH_PLANKS;
		case 4:  return Material.JUNGLE_PLANKS;
		case 5:  return Material.ACACIA_PLANKS;
		case 6:  return Material.MANGROVE_PLANKS;
		case 7:  return Material.CRIMSON_PLANKS;
		case 8:  return Material.WARPED_PLANKS;
		case 9:  return Material.OAK_WOOD;
		case 10: return Material.DARK_OAK_WOOD;
		case 11: return Material.SPRUCE_WOOD;
		case 12: return Material.BIRCH_WOOD;
		case 13: return Material.JUNGLE_WOOD;
		case 14: return Material.ACACIA_WOOD;
		case 15: return Material.MANGROVE_WOOD;
		case 16: return Material.CRIMSON_HYPHAE;
		case 17: return Material.WARPED_HYPHAE;
		case 18: return Material.STRIPPED_OAK_WOOD;
		case 19: return Material.STRIPPED_DARK_OAK_WOOD;
		case 20: return Material.STRIPPED_SPRUCE_WOOD;
		case 21: return Material.STRIPPED_BIRCH_WOOD;
		case 22: return Material.STRIPPED_JUNGLE_WOOD;
		case 23: return Material.STRIPPED_ACACIA_WOOD;
		case 24: return Material.STRIPPED_MANGROVE_WOOD;
		case 25: return Material.STRIPPED_CRIMSON_HYPHAE;
		case 26: return Material.STRIPPED_WARPED_HYPHAE;
		case 27: return Material.BRICKS;
		case 28: return Material.STONE_BRICKS;
		case 29: return Material.DEEPSLATE_BRICKS;
		case 30: return Material.RED_NETHER_BRICKS;
		default: throw new RuntimeException("Unknown forest door wall style: "+Integer.toString(style));
		}
	}



	// forest hatch
	public void populate_hatch(final int x, final int y, final int z, final LimitedRegion region) {
		// top half
		{
			final BlockPlotter plot =
				(new PlotterFactory())
				.placer(region)
				.axis("dse")
				.xyz(x+6, y+1, z+6)
				.whd(5, 12, 5)
				.build();
			plot.type('#', Material.STONE_BRICKS                    );
			plot.type('-', Material.STONE_BRICK_SLAB                );
			plot.type('/', "minecraft:spruce_trapdoor[facing=south]");
			plot.type('_', Material.STONE_PRESSURE_PLATE            );
			plot.type('H', "minecraft:ladder[facing=north]"         );
			plot.type('L', "minecraft:light[level=15]"              );
			final StringBuilder[][] matrix = plot.getMatrix3D();
			matrix[0][0].append("  L"  ); matrix[1][0].append("-###-");
			matrix[0][1].append("  _"  ); matrix[1][1].append("#####");
			matrix[0][2].append("L_/_L"); matrix[1][2].append("##H##");
			matrix[0][3].append("  _"  ); matrix[1][3].append("#####");
			matrix[0][4].append("  L"  ); matrix[1][4].append("-###-");
			for (int iy=2; iy<4; iy++) {
				matrix[iy][0].append("#####");
				matrix[iy][1].append("#####");
				matrix[iy][2].append("##H##");
				matrix[iy][3].append("#####");
				matrix[iy][4].append("#####");
			}
			for (int iy=4; iy<12; iy++) {
				matrix[iy][1].append("  #" );
				matrix[iy][2].append(" #H#");
				matrix[iy][3].append("  #" );
			}
			plot.run();
		}
		// bottom half
		{
			final BlockPlotter plot =
				(new PlotterFactory())
				.placer(region)
				.axis("use")
				.xyz(x+7, Y_019+SUBFLOOR, z+7)
				.whd(3, 15, 3)
				.build();
			plot.type('#', Material.STONE_BRICKS      );
			plot.type('$', Material.MOSSY_STONE_BRICKS);
			plot.type('H', Material.LADDER            );
			plot.type('.', Material.AIR               );
			final StringBuilder[][] matrix = plot.getMatrix3D();
			for (int iy=4; iy<H_019+2; iy++) {
				matrix[iy][0].append("###");
				matrix[iy][1].append("#H#");
				matrix[iy][2].append("###");
			}
			for (int iy=1; iy<4; iy++) {
				matrix[iy][0].append("#.#");
				matrix[iy][1].append("#H#");
				matrix[iy][2].append("###");
			}
			matrix[0][0].append("$$$");
			matrix[0][1].append("$$$");
			matrix[0][2].append("$$$");
			plot.run();
		}
		((Level_000)this.gen.backlevel).portal_19_to_309.add(x, z);
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
