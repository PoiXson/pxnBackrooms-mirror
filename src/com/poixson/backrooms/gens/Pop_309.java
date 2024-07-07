package com.poixson.backrooms.gens;

import static com.poixson.backrooms.gens.Gen_309.PATH_START_X;
import static com.poixson.backrooms.gens.Gen_309.PATH_START_Z;
import static com.poixson.utils.NumberUtils.MinMax;

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
import com.poixson.tools.abstractions.Tuple;
import com.poixson.tools.plotter.BlockPlotter;


// 309 | Radio Station
public class Pop_309 implements BackroomsPop {

	protected final BackroomsPlugin plugin;
	protected final Level_000 level_000;
	protected final Gen_309 gen_309;

	protected final Pop_309_Trees treePop;

	protected final AtomicInteger special_index = new AtomicInteger(0);



	public Pop_309(final Level_000 level_000) {
		this.plugin    = level_000.getPlugin();
		this.level_000 = level_000;
		this.gen_309   = level_000.gen_309;
		this.treePop = new Pop_309_Trees(this.gen_309);
	}



	@Override
	public void populate(final LinkedList<Tuple<BlockPlotter, StringBuilder[][]>> plots,
			final LimitedRegion region, final int chunkX, final int chunkZ) {
		if (!this.gen_309.enable_gen) return;
		// trees
		final int count_trees;
		if (this.gen_309.enable_top) count_trees = this.treePop.populate(chunkX, chunkZ, region);
		else                     count_trees = 0;
		// radio station
		if (chunkX == 0 && chunkZ == 0) {
			this.populate0x0(region);
		} else
		if (this.gen_309.enable_top) {
			// fence around clearing
			if (Math.abs(chunkX) < 8
			&&  Math.abs(chunkZ) < 8) {
				final double fence_radius    = this.gen_309.fence_radius;
				final double fence_strength  = this.gen_309.fence_strength;
				final double fence_thickness = this.gen_309.fence_thickness;
				final int y_floor = this.gen_309.level_y + this.gen_309.bedrock_barrier + this.gen_309.subfloor;
				for (int iz=0; iz<16; iz++) {
					final int zz = (chunkZ * 16) + iz;
					LOOP_X:
					for (int ix=0; ix<16; ix++) {
						final int xx = (chunkX * 16) + ix;
						final double distance = this.gen_309.getCenterClearingDistance(xx, zz, fence_strength);
						if (distance >= fence_radius
						&&  distance <= fence_radius + fence_thickness) {
							boolean found = false;
							int sy = y_floor;
							LOOP_SURFACE:
							for (int iy=0; iy<10; iy++) {
								final Material type = region.getType(xx, sy+iy, zz);
								if (Material.AIR.equals(type)) {
									found = true;
									sy += iy;
									break LOOP_SURFACE;
								}
							}
							if (found) {
								final int path_x = this.gen_309.getPathX(zz);
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
				&&  this.gen_309.enable_top) {
					final int path_clearing = this.gen_309.path_clearing * 3;
					final int xx = chunkX * 16;
					final int zz = chunkZ * 16;
					if (!this.gen_309.pathTrace.isPath(xx, zz, path_clearing)) {
						// find surface
						int surface_y = 0;
						LOOP_SURFACE:
						for (int iy=0; iy<12; iy++) {
							if (Material.AIR.equals(region.getType(xx+2, this.gen_309.level_y+iy, zz+2))) {
								surface_y = this.gen_309.level_y + iy;
								break LOOP_SURFACE;
							}
						}
						if (surface_y != 0) {
							final int special = this.special_index.incrementAndGet();
							final int mod_a = special % MinMax(this.gen_309.special_mod_a, 5, 100);
							SWITCH_MOD_A:
							switch (mod_a) {
							case 1: this.populate_stairs(xx, surface_y, zz, region); break SWITCH_MOD_A; // stairs
							case 4: this.populate_door(  xx, surface_y, zz, region); break SWITCH_MOD_A; // door
							default:
								final int mod_b = special % MinMax(this.gen_309.special_mod_b, 5, 100);
								SWITCH_MOD_B:
								switch (mod_b) {
								case 1: this.populate_hatch(xx, surface_y, zz, region); break SWITCH_MOD_B; // hatch
								default: break SWITCH_MOD_B;
								}
								break SWITCH_MOD_A;
							}
						}
					}
				}
			}
		}
	}



	// forest stairs
	public void populate_stairs(final int x, final int y, final int z, final LimitedRegion region) {
		this.level_000.portal_309_stairs.add(x, z);
		final BlockPlotter plot =
			(new BlockPlotter())
			.axis("use")
			.xyz(x+6, y, z+6)
			.whd(2, 11, 11);
		plot.type('<', "minecraft:stone_brick_stairs[facing=south]"         );
		plot.type('>', "minecraft:stone_brick_stairs[facing=north,half=top]");
		plot.type('L', "minecraft:light[level=15]"                          );
		final StringBuilder[][] matrix = plot.getMatrix3D();
		for (int i=0; i<10; i++) {
			matrix[i][i  ].append("<<");
			matrix[i][i+1].append(">>");
		}
		plot.run(region, matrix);
	}



	// forest door
	public void populate_door(final int x, final int y, final int z, final LimitedRegion region) {
		this.level_000.portal_309_doors.add(x, z);
		final double value = this.gen_309.noisePrairie.getNoise(x, z);
		final Material block_door = this.getDoorStyle(value);
		final Material block_wall = this.getDoorWallStyle(value);
		final BlockPlotter plot =
			(new BlockPlotter())
			.axis("use")
			.xyz(x+6, y-1, z+6)
			.whd(3, 4, 3);
		plot.type('#', block_wall                                         );
		plot.type('d', block_door, "[facing=north,half=upper,hinge=right]");
		plot.type('D', block_door, "[facing=north,half=lower,hinge=right]");
		plot.type('b', block_door, "[facing=south,half=upper,hinge=right]");
		plot.type('B', block_door, "[facing=south,half=lower,hinge=right]");
		plot.type('_', Material.WARPED_PRESSURE_PLATE                     );
		plot.type('L', "minecraft:light[level=15]"                        );
		final StringBuilder[][] matrix = plot.getMatrix3D();
		matrix[3][1].append("###");
		matrix[2][1].append("# #"); matrix[2][0].append(" d" ); matrix[2][2].append(" b" );
		matrix[1][1].append("#_#"); matrix[1][0].append("LDL"); matrix[1][2].append("LBL");
		matrix[0][1].append(" #" );
		plot.run(region, matrix);
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
		final int level_019_y = (this.level_000.gen_019.level_y + this.level_000.gen_019.bedrock_barrier) - 1;
		final int level_019_h = this.level_000.gen_019.level_h;
		// top half
		{
			final BlockPlotter plot =
				(new BlockPlotter())
				.axis("dse")
				.xyz(x+6, y+1, z+6)
				.whd(5, 12, 5);
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
			plot.run(region, matrix);
		}
		// bottom half
		{
			final BlockPlotter plot =
				(new BlockPlotter())
				.axis("use")
				.xyz(x+7, level_019_y+this.gen_309.subfloor, z+7)
				.whd(3, 15, 3);
			plot.type('#', Material.STONE_BRICKS      );
			plot.type('$', Material.MOSSY_STONE_BRICKS);
			plot.type('H', Material.LADDER            );
			plot.type('.', Material.AIR               );
			final StringBuilder[][] matrix = plot.getMatrix3D();
			for (int iy=4; iy<level_019_h+2; iy++) {
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
			plot.run(region, matrix);
		}
		this.level_000.portal_019_to_309.add(x, z);
	}



	// radio station
	public void populate0x0(final LimitedRegion region) {
		// find surface
		int y = Integer.MIN_VALUE;
		for (int iy=0; iy<10; iy++) {
			if (Material.AIR.equals(region.getType(PATH_START_X, this.gen_309.level_y+iy, PATH_START_Z-1))) {
				y = this.gen_309.level_y + iy;
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
				.setVariable("region",         region                 )
				.setVariable("surface_y",      y                      )
				.setVariable("seed",           this.gen_309.getSeed() )
				.setVariable("enable_ceiling", this.gen_309.enable_top)
				.setVariable("path_width",     this.gen_309.path_width)
				.setVariable("path_start_x",   PATH_START_X           )
				.setVariable("path_start_z",   PATH_START_Z           );
			script.start();
		} finally {
			if (script != null)
				script.stop();
		}
	}



	public Logger log() {
		return this.plugin.log();
	}



}
