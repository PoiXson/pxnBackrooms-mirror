package com.poixson.backrooms.gens;

import static com.poixson.utils.BlockUtils.StringToBlockDataDef;

import java.util.LinkedList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.backrooms.BackroomsGen;
import com.poixson.backrooms.BackroomsLevel;
import com.poixson.backrooms.PreGenData;
import com.poixson.backrooms.worlds.Level_000;
import com.poixson.tools.abstractions.Tuple;
import com.poixson.tools.plotter.BlockPlotter;


// 188 | The Windows
public class Gen_188 extends BackroomsGen {

	// default params
	public static final boolean DEFAULT_DARK_ROOM = false;

	// default blocks
	public static final String DEFAULT_BLOCK_SUBFLOOR         = "minecraft:dirt";
	public static final String DEFAULT_BLOCK_FLOOR            = "minecraft:green_wool";
	public static final String DEFAULT_BLOCK_FLOOR_PATH_LINES = "minecraft:polished_andesite";
	public static final String DEFAULT_BLOCK_FLOOR_PATH_AREAS = "minecraft:polished_granite";
	public static final String DEFAULT_BLOCK_WALL             = "minecraft:quartz_block";
	public static final String DEFAULT_BLOCK_CEILING          = "minecraft:white_wool";
	public static final String DEFAULT_BLOCK_WINDOW           = "minecraft:black_stained_glass";

	// params
	public final boolean enable_gen;
	public final int     level_y;
	public final int     level_h;
	public final int     subfloor;
	public final boolean dark_room;

	// blocks
	public final String block_subfloor;
	public final String block_floor;
	public final String block_floor_path_lines;
	public final String block_floor_path_areas;
	public final String block_wall;
	public final String block_ceiling;
	public final String block_window;



	public Gen_188(final BackroomsLevel backlevel, final int seed, final int level_y) {
		super(backlevel, null, seed);
		final int level_number = this.getLevelNumber();
		final ConfigurationSection cfgParams = this.plugin.getConfigLevelParams(level_number);
		final ConfigurationSection cfgBlocks = this.plugin.getConfigLevelBlocks(level_number);
		// params
		final Level_000 level_000 = (Level_000) backlevel;
		final Gen_001 gen_001 = level_000.gen_001;
		final Gen_309 gen_309 = level_000.gen_309;
		this.enable_gen = cfgParams.getBoolean("Enable-Gen");
		this.level_y    = level_000.gen_001.level_y;
		this.level_h    = gen_309.level_y - this.level_y - gen_001.bedrock_barrier - gen_001.subfloor;
		this.subfloor  = level_000.gen_001.subfloor;
		this.dark_room = cfgParams.getBoolean("Dark-Room");
		// block types
		this.block_subfloor         = cfgBlocks.getString("SubFloor"        );
		this.block_floor            = cfgBlocks.getString("Floor"           );
		this.block_floor_path_lines = cfgBlocks.getString("Floor-Path-Lines");
		this.block_floor_path_areas = cfgBlocks.getString("Floor-Path-Areas");
		this.block_wall             = cfgBlocks.getString("Wall"            );
		this.block_ceiling          = cfgBlocks.getString("Ceiling"         );
		this.block_window           = cfgBlocks.getString("Window"          );
	}



	@Override
	public int getLevelNumber() {
		return 188;
	}

	@Override
	public int getNextY() {
		return this.level_y + this.level_h;
	}



	@Override
	public void generate(final PreGenData pregen,
			final LinkedList<Tuple<BlockPlotter, StringBuilder[][]>> plots,
			final ChunkData chunk, final int chunkX, final int chunkZ) {
		if (!this.enable_gen) return;
		final Level_000 level_000 = (Level_000) this.backlevel;
		final BlockData block_subfloor         = StringToBlockDataDef(this.block_subfloor,         DEFAULT_BLOCK_SUBFLOOR        );
		final BlockData block_floor            = StringToBlockDataDef(this.block_floor,            DEFAULT_BLOCK_FLOOR           );
		final BlockData block_floor_path_lines = StringToBlockDataDef(this.block_floor_path_lines, DEFAULT_BLOCK_FLOOR_PATH_LINES);
		final BlockData block_floor_path_areas = StringToBlockDataDef(this.block_floor_path_areas, DEFAULT_BLOCK_FLOOR_PATH_AREAS);
		final BlockData block_wall             = StringToBlockDataDef(this.block_wall,             DEFAULT_BLOCK_WALL            );
		final BlockData block_ceiling          = StringToBlockDataDef(this.block_ceiling,          DEFAULT_BLOCK_CEILING         );
		final BlockData block_window           = StringToBlockDataDef(this.block_window,           DEFAULT_BLOCK_WINDOW          );
		if (block_subfloor         == null) throw new RuntimeException("Invalid block type for level 188 SubFloor"        );
		if (block_floor            == null) throw new RuntimeException("Invalid block type for level 188 Floor"           );
		if (block_floor_path_lines == null) throw new RuntimeException("Invalid block type for level 188 Floor-Path-Lines");
		if (block_floor_path_areas == null) throw new RuntimeException("Invalid block type for level 188 Floor-Path-Areas");
		if (block_wall             == null) throw new RuntimeException("Invalid block type for level 188 Wall"            );
		if (block_ceiling          == null) throw new RuntimeException("Invalid block type for level 188 Ceiling"         );
		if (block_window           == null) throw new RuntimeException("Invalid block type for level 188 Window"          );
		final Gen_001 gen_001 = level_000.gen_001;
		final Gen_023 gen_023 = level_000.gen_023;
		final Gen_000 gen_000 = level_000.gen_000;
		final Gen_006 gen_006 = level_000.gen_006;
		final Gen_037 gen_037 = level_000.gen_037;
		final Gen_005 gen_005 = level_000.gen_005;
		final Gen_019 gen_019 = level_000.gen_019;
		final BlockData block_water = Bukkit.createBlockData("minecraft:water[level=0]");
		final BlockData light = Bukkit.createBlockData("light[level=15]");
		final int y_base  = this.level_y + this.bedrock_barrier;
		final int y_floor = y_base + this.subfloor;
		final int y_ceil  = (y_floor + this.level_h) - 1;
		for (int iz=0; iz<16; iz++) {
			final int zz = (chunkZ * 16) + iz;
			for (int ix=0; ix<16; ix++) {
				final int xx = (chunkX * 16) + ix;
				int outer  = 0;
				int inner  = 0;
				int inside = 0;
				if (xx == -48) outer++; if (xx == 63) outer++;
				if (zz == -48) outer++; if (zz == 63) outer++;
				if (outer == 0) {
					if (xx == -47) inner++; if (xx == 62) inner++;
					if (zz == -47) inner++; if (zz == 62) inner++;
					if (inner == 0) {
						if (xx == -46) inside++; if (xx == 61) inside++;
						if (zz == -46) inside++; if (zz == 61) inside++;
					}
				}
				// subfloor
				for (int iy=0; iy<this.subfloor; iy++)
					chunk.setBlock(ix, y_base+iy, iz, block_subfloor);
				// floor
				for (int iy=0; iy<this.bedrock_barrier; iy++)
					chunk.setBlock(ix, this.level_y+iy, iz, Material.BEDROCK);
				if (outer == 0
				&&  inner == 0) {
					// border path
					if (xx < -43 || xx > 58
					||  zz < -43 || zz > 58) {
						chunk.setBlock(ix, y_floor, iz, block_floor_path_lines);
					// grass/path
					} else {
						// grass circle on the side
						final double circle_side = Math.sqrt(Math.pow(xx-69.0, 2.0) + Math.pow(zz-69.0, 2.0));
						if (circle_side < 38.0) {
							chunk.setBlock(ix, y_floor, iz, block_floor);
						} else
						// path around side circle grass
						if (circle_side < 42.0) {
							chunk.setBlock(ix, y_floor, iz, block_floor_path_lines);
						} else {
							// cut side path
							final double path_cut = ((double)xx) + (((double)zz) * 0.65);
							if (path_cut > 42.0) {
								chunk.setBlock(ix, y_floor, iz, block_floor_path_areas);
							} else {
								// center path circle
								final double circle_center = Math.sqrt(Math.pow(xx+20.0, 2.0) + Math.pow(zz+5.0, 2.0));
								if (circle_center < 9.0) {
									chunk.setBlock(ix, y_floor, iz, block_floor_path_areas);
								} else {
									// cross path
									final double path_xz = ((double)xx) +  ((double)zz) + 25.0;
									final double path_zx = ((double)xx) - (((double)zz) *  2.8);
									if ((path_xz < 5.0 && path_xz >-5.0)
									||  (path_zx < 9.0 && path_zx >-9.0)) chunk.setBlock(ix, y_floor, iz, block_floor_path_lines);
									else                                  chunk.setBlock(ix, y_floor, iz, block_floor           );
								}
							}
						}
					}
					if ((xx+zz+500) % 3 == 0)
						chunk.setBlock(ix, y_floor+4, iz, light);
					// ceiling
					chunk.setBlock(ix, y_ceil, iz, block_ceiling);
				}
				Y_LOOP:
				for (int iy=0; iy<this.level_h; iy++) {
					final int yy = y_floor + iy;
					boolean isWindow = false;
					if (outer == 1
					||  inner == 1) {
						final int mod9;
						if      (xx == -48 || xx == 63 ||  xx == -47 || xx == 62) mod9 = (zz+206) % 9;
						else if (zz == -48 || zz == 63 ||  zz == -47 || zz == 62) mod9 = (xx+206) % 9;
						else break Y_LOOP;
						if (mod9 < 6) {
							if (xx > -45 || zz > -45) {
								final int mod_window = (yy + 64) % 4;
								if (mod_window < 3) isWindow = true;
							}
						}
					}
					if (isWindow) {
						// stop below floor
						if (iy == 0) isWindow = false;
						// stop before ceiling
						if (yy > y_ceil-2) isWindow = false;
					}
					// outer walls
					if (outer > 0) {
						if (isWindow) {
							final int lvl = level_000.getLevel(yy);
							int lvl_min_y = level_000.getY(lvl);
							int lvl_max_y = level_000.getMaxY(lvl);
							LEVEL_SWITCH:
							switch(lvl) {
							case  1: lvl_min_y += gen_001.bedrock_barrier + gen_001.subfloor + 1; lvl_max_y -= 3;                      break LEVEL_SWITCH;
							case 23: lvl_min_y += gen_023.bedrock_barrier + gen_023.subfloor + 1; lvl_max_y -= gen_023.subceiling + 2; break LEVEL_SWITCH;
							case  0: lvl_min_y += gen_000.bedrock_barrier + gen_000.subfloor + 1; lvl_max_y -= gen_000.subceiling + 1; break LEVEL_SWITCH;
							case  6: lvl_min_y += gen_006.bedrock_barrier;                                                             break LEVEL_SWITCH;
							case 37: lvl_min_y += gen_037.bedrock_barrier + gen_037.subfloor + 1; lvl_max_y -= gen_037.subceiling + 2; break LEVEL_SWITCH;
							case  5: lvl_min_y += gen_005.bedrock_barrier + gen_005.subfloor + 1; lvl_max_y -= gen_005.subceiling + 1; break LEVEL_SWITCH;
							case 19: lvl_min_y += gen_019.bedrock_barrier + gen_019.subfloor;     lvl_max_y -= 2;                      break LEVEL_SWITCH;
							default: break LEVEL_SWITCH;
							}
							if (yy < lvl_min_y
							||  yy > lvl_max_y)
								isWindow = false;
						}
						if (isWindow) {
							final int poolrooms_y = gen_037.level_y+gen_037.bedrock_barrier+gen_037.subfloor;
							if (yy > poolrooms_y && yy <= poolrooms_y+gen_037.water_depth)
								chunk.setBlock(ix, yy, iz, block_water);
						}
						if (!isWindow)
							chunk.setBlock(ix, yy, iz, Material.BEDROCK);
					} else
					// inner wall
					if (inner > 0) {
						if (isWindow) chunk.setBlock(ix, yy, iz, block_window);
						else          chunk.setBlock(ix, yy, iz, block_wall  );
					} else
					// light inside wall
					if (!this.dark_room) {
						if (inside == 1
						&&  yy > y_floor+4
						&&  yy < y_ceil -1
						&& (xx+yy+zz+500) % 3 == 0)
								chunk.setBlock(ix, yy, iz, light);
					}
				} // end iy
				// ceiling lights
				if (!this.dark_room) {
					if (outer == 0
					&&  inner == 0
					&& (xx+zz+500) % 3 == 0)
						chunk.setBlock(ix, y_ceil-1, iz, light);
				}
			} // end ix
		} // end iz
		// east doors
		if (chunkX == 3 && chunkZ == 0) {
			final BlockPlotter plot =
				(new BlockPlotter())
				.axis("eus")
				.xyz(13, y_floor, 0)
				.whd(3, 5, 8);
			plot.type('.', Material.AIR    );
			plot.type('x', Material.BEDROCK);
			plot.type('#', block_wall      );
			plot.type('0', block_window    );
			plot.type('_', Material.STONE_PRESSURE_PLATE                            );
			plot.type('-', Material.POLISHED_BLACKSTONE_PRESSURE_PLATE              );
			plot.type('d', "minecraft:iron_door[half=upper,hinge=left,facing=east]" );
			plot.type('D', "minecraft:iron_door[half=lower,hinge=left,facing=east]" );
			plot.type('b', "minecraft:iron_door[half=upper,hinge=right,facing=east]");
			plot.type('B', "minecraft:iron_door[half=lower,hinge=right,facing=east]");
			final StringBuilder[][] matrix = plot.getMatrix3D();
			matrix[1][4].append("########"); matrix[2][4].append("........");
			matrix[1][3].append("00#00#00"); matrix[2][3].append("........");
			matrix[1][2].append("db#db#db"); matrix[2][2].append("........");
			matrix[1][1].append("DB#DB#DB"); matrix[2][1].append("--.--.--");
			matrix[1][0].append("xxxxxxxx"); matrix[2][0].append("xxxxxxxx");
			matrix[0][1].append("__ __ __");
			plot.run(chunk, matrix);
		}
		// south doors
		if (chunkX == 1 && chunkZ == 3) {
			final BlockPlotter plot =
				(new BlockPlotter())
				.axis("suw")
				.xyz(10, y_floor, 13)
				.whd(9, 5, 3);
			plot.type('.', Material.AIR    );
			plot.type('x', Material.BEDROCK);
			plot.type('#', block_wall      );
			plot.type('0', block_window    );
			plot.type('_', Material.STONE_PRESSURE_PLATE                             );
			plot.type('-', Material.POLISHED_BLACKSTONE_PRESSURE_PLATE               );
			plot.type('d', "minecraft:iron_door[half=upper,hinge=left,facing=south]" );
			plot.type('D', "minecraft:iron_door[half=lower,hinge=left,facing=south]" );
			plot.type('b', "minecraft:iron_door[half=upper,hinge=right,facing=south]");
			plot.type('B', "minecraft:iron_door[half=lower,hinge=right,facing=south]");
			final StringBuilder[][] matrix = plot.getMatrix3D();
			matrix[1][4].append("########"); matrix[2][4].append("........");
			matrix[1][3].append("00#00#00"); matrix[2][3].append("........");
			matrix[1][2].append("db#db#db"); matrix[2][2].append("........");
			matrix[1][1].append("DB#DB#DB"); matrix[2][1].append("--.--.--");
			matrix[1][0].append("xxxxxxxx"); matrix[2][0].append("xxxxxxxx");
			matrix[0][1].append("__ __ __");
			plot.run(chunk, matrix);
		}
	}



	// -------------------------------------------------------------------------------
	// configs



	@Override
	public void configDefaults(final ConfigurationSection cfgParams, final ConfigurationSection cfgBlocks) {
		// params
		cfgParams.addDefault("Enable-Gen", Boolean.TRUE     );
		cfgParams.addDefault("Dark-Room",  DEFAULT_DARK_ROOM);
		// block types
		cfgBlocks.addDefault("SubFloor",         DEFAULT_BLOCK_SUBFLOOR        );
		cfgBlocks.addDefault("Floor",            DEFAULT_BLOCK_FLOOR           );
		cfgBlocks.addDefault("Floor-Path-Lines", DEFAULT_BLOCK_FLOOR_PATH_LINES);
		cfgBlocks.addDefault("Floor-Path-Areas", DEFAULT_BLOCK_FLOOR_PATH_AREAS);
		cfgBlocks.addDefault("Ceiling",          DEFAULT_BLOCK_CEILING         );
		cfgBlocks.addDefault("Window",           DEFAULT_BLOCK_WINDOW          );
	}



}
