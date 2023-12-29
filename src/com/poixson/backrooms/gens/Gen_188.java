package com.poixson.backrooms.gens;

import static com.poixson.backrooms.gens.Gen_005.DEFAULT_BLOCK_HALL_WALL_BOTTOM_X;
import static com.poixson.backrooms.gens.Gen_005.DEFAULT_BLOCK_HALL_WALL_BOTTOM_Z;
import static com.poixson.backrooms.gens.Gen_005.DEFAULT_BLOCK_HALL_WALL_CENTER;
import static com.poixson.backrooms.gens.Gen_005.DEFAULT_BLOCK_HALL_WALL_TOP_X;
import static com.poixson.backrooms.gens.Gen_005.DEFAULT_BLOCK_HALL_WALL_TOP_Z;
import static com.poixson.backrooms.worlds.Level_000.H_019;
import static com.poixson.backrooms.worlds.Level_000.H_188;
import static com.poixson.backrooms.worlds.Level_000.Y_000;
import static com.poixson.backrooms.worlds.Level_000.Y_001;
import static com.poixson.backrooms.worlds.Level_000.Y_005;
import static com.poixson.backrooms.worlds.Level_000.Y_006;
import static com.poixson.backrooms.worlds.Level_000.Y_019;
import static com.poixson.backrooms.worlds.Level_000.Y_023;
import static com.poixson.backrooms.worlds.Level_000.Y_037;
import static com.poixson.backrooms.worlds.Level_000.Y_188;
import static com.poixson.utils.BlockUtils.StringToBlockData;

import java.util.LinkedList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.backrooms.BackroomsGen;
import com.poixson.backrooms.BackroomsLevel;
import com.poixson.backrooms.PreGenData;
import com.poixson.backrooms.worlds.Level_000;
import com.poixson.tools.plotter.BlockPlotter;


// 188 | The Windows
public class Gen_188 extends BackroomsGen {



	public Gen_188(final BackroomsLevel backlevel, final int level_y, final int level_h) {
		super(backlevel, level_y, level_h);
	}



	@Override
	public void generate(final PreGenData pregen, final ChunkData chunk,
			final LinkedList<BlockPlotter> plots, final int chunkX, final int chunkZ) {
		final Level_000 level0 = (Level_000) this.backlevel;
		final BlockData block_hotel_wall_top_x    = StringToBlockData(level0.gen_005.block_hall_wall_top_x,    DEFAULT_BLOCK_HALL_WALL_TOP_X   );
		final BlockData block_hotel_wall_top_z    = StringToBlockData(level0.gen_005.block_hall_wall_top_z,    DEFAULT_BLOCK_HALL_WALL_TOP_Z   );
		final BlockData block_hotel_wall_center   = StringToBlockData(level0.gen_005.block_hall_wall_center,   DEFAULT_BLOCK_HALL_WALL_CENTER  );
		final BlockData block_hotel_wall_bottom_x = StringToBlockData(level0.gen_005.block_hall_wall_bottom_x, DEFAULT_BLOCK_HALL_WALL_BOTTOM_X);
		final BlockData block_hotel_wall_bottom_z = StringToBlockData(level0.gen_005.block_hall_wall_bottom_z, DEFAULT_BLOCK_HALL_WALL_BOTTOM_Z);
		final BlockData block_attic_wall          = StringToBlockData(level0.gen_019.block_wall,       Gen_019.DEFAULT_BLOCK_WALL              );
		final BlockData block_pool_wall           = StringToBlockData(level0.gen_037.block_wall_a,     Gen_037.DEFAULT_BLOCK_WALL_A            );
		final BlockData block_lightsout_wall      = StringToBlockData(level0.gen_006.block_wall,       Gen_006.DEFAULT_BLOCK_WALL              );
		final BlockData block_lobby_wall          = StringToBlockData(level0.gen_000.block_wall,       Gen_000.DEFAULT_BLOCK_WALL              );
		final BlockData block_overgrowth_wall     = StringToBlockData(level0.gen_023.block_wall,       Gen_023.DEFAULT_BLOCK_WALL              );
		if (block_hotel_wall_top_x    == null) throw new RuntimeException("Invalid block type for level 5 Hall-Wall-Top-X"   );
		if (block_hotel_wall_top_z    == null) throw new RuntimeException("Invalid block type for level 5 Hall-Wall-Top-Z"   );
		if (block_hotel_wall_center   == null) throw new RuntimeException("Invalid block type for level 5 Hall-Wall-Center"  );
		if (block_hotel_wall_bottom_x == null) throw new RuntimeException("Invalid block type for level 5 Hall-Wall-Bottom-X");
		if (block_hotel_wall_bottom_z == null) throw new RuntimeException("Invalid block type for level 5 Hall-Wall-Bottom-Z");
		if (block_attic_wall          == null) throw new RuntimeException("Invalid block type for level 5 Attic-Wall"        );
		if (block_pool_wall           == null) throw new RuntimeException("Invalid block type for level 5 Pool-Wall"         );
		if (block_lightsout_wall      == null) throw new RuntimeException("Invalid block type for level 5 LightsOut-Wall"    );
		if (block_lobby_wall          == null) throw new RuntimeException("Invalid block type for level 5 Lobby-Wall"        );
		if (block_overgrowth_wall     == null) throw new RuntimeException("Invalid block type for level 5 Overgrowth-Wall"   );
		final BlockData light = Bukkit.createBlockData("light[level=15]");
		final int y  = Y_188;
		final int cy =(Y_188 + H_188) - 1;
		final int h  = H_188;
		for (int iz=0; iz<16; iz++) {
			final int zz = (chunkZ * 16) + iz;
			for (int ix=0; ix<16; ix++) {
				final int xx = (chunkX * 16) + ix;
				int outer = 0;
				int inner = 0;
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
				// floor
				chunk.setBlock(ix, y, iz, Material.BEDROCK);
				if (outer == 0 && inner == 0) {
					chunk.setBlock(ix, y+1, iz, Material.DIRT);
					chunk.setBlock(ix, y+2, iz, Material.DIRT);
					// border path
					if (xx ==-46 || xx ==-44 || xx == 61 || xx == 60 || (xx ==-43 && zz ==-43) || (xx ==-43 && zz == 59)
					||  zz ==-46 || zz ==-44 || zz == 61 || zz == 60 || (xx == 59 && zz == 59) || (xx == 59 && zz ==-43)) {
						chunk.setBlock(ix, y+3, iz, Material.POLISHED_ANDESITE);
					// grass/path
					} else {
						// grass circle on the side
						final double circle_side = Math.sqrt(Math.pow(xx-69.0, 2.0) + Math.pow(zz-69.0, 2.0));
						if (circle_side < 38.0) {
							chunk.setBlock(ix, y+3, iz, Material.GRASS_BLOCK);
						} else
						// path around side circle grass
						if (circle_side < 42.0) {
							chunk.setBlock(ix, y+3, iz, Material.POLISHED_ANDESITE);
						} else {
							// cut side path
							final double path_cut = ((double)xx) + (((double)zz) * 0.65);
							if (path_cut > 42.0) {
								chunk.setBlock(ix, y+3, iz, Material.POLISHED_GRANITE);
							} else {
								// center path circle
								final double circle_center = Math.sqrt(Math.pow(xx+20.0, 2.0) + Math.pow(zz+5.0, 2.0));
								if (circle_center < 9.0) {
									chunk.setBlock(ix, y+3, iz, Material.POLISHED_GRANITE);
								} else {
									// cross path
									final double path_xz = ((double)xx) +  ((double)zz) + 25.0;
									final double path_zx = ((double)xx) - (((double)zz) *  2.8);
									if ((path_xz < 5.0 && path_xz >-5.0)
									||  (path_zx < 9.0 && path_zx >-9.0)) chunk.setBlock(ix, y+3, iz, Material.POLISHED_ANDESITE);
									else                                  chunk.setBlock(ix, y+3, iz, Material.GRASS_BLOCK      );
								}
							}
						}
					}
					if ((xx+zz+500) % 3 == 0)
						chunk.setBlock(ix, y+7, iz, light);
					// ceiling
					chunk.setBlock(ix, y+cy, iz, Material.WHITE_WOOL);
				}
				// walls
				BlockData block_hotel_wall_top    = null;
				BlockData block_hotel_wall_bottom = null;
				if (outer > 0) {
					block_hotel_wall_top    = (xx==-48 || xx==63 ? block_hotel_wall_top_x    : block_hotel_wall_top_z   );
					block_hotel_wall_bottom = (xx==-48 || xx==63 ? block_hotel_wall_bottom_x : block_hotel_wall_bottom_z);
				}
				Y_LOOP:
				for (int iy=1; iy<h; iy++) {
					final int yy = y + iy;
					boolean isWindow = false;
					if (outer == 1 || inner == 1) {
						final int mod9;
						if      (xx == -48 || xx == 63 ||  xx == -47 || xx == 62) mod9 = (zz+206) % 9;
						else if (zz == -48 || zz == 63 ||  zz == -47 || zz == 62) mod9 = (xx+206) % 9;
						else break Y_LOOP;
						if (mod9 < 6) {
							if      (yy > Y_019+7  && yy < Y_019+11) isWindow = true; // attic 2
							else if (yy > Y_019+2  && yy < Y_019+6 ) isWindow = true; // attic 1
							else if (yy > Y_005+8  && yy < Y_005+12) isWindow = true; // hotel 2
							else if (yy > Y_005+3  && yy < Y_005+7 ) isWindow = true; // hotel 1
							else if (yy > Y_037+17 && yy < Y_037+21) isWindow = true; // poolrooms 4
							else if (yy > Y_037+12 && yy < Y_037+16) isWindow = true; // poolrooms 3
							else if (yy > Y_037+7  && yy < Y_037+11) isWindow = true; // poolrooms 2
							else if (yy > Y_037+2  && yy < Y_037+6 ) isWindow = true; // poolrooms 1
							else if (yy > Y_006+3  && yy < Y_006+7 ) isWindow = true; // lights out 2
							else if (yy > Y_006-1  && yy < Y_006+3 ) isWindow = true; // lights out 1
							else if (yy > Y_000+8  && yy < Y_000+12) isWindow = true; // lobby 2
							else if (yy > Y_000+3  && yy < Y_000+7 ) isWindow = true; // lobby 1
							else if (yy > Y_023+12 && yy < Y_023+16) isWindow = true; // overgrowth 3
							else if (yy > Y_023+7  && yy < Y_023+11) isWindow = true; // overgrowth 2
							else if (yy > Y_023+3  && yy < Y_023+7 ) isWindow = true; // overgrowth 1
							else if (yy > Y_001+34 && yy < Y_001+38) isWindow = true; // basement 7
							else if (yy > Y_001+29 && yy < Y_001+33) isWindow = true; // basement 6
							else if (yy > Y_001+24 && yy < Y_001+28) isWindow = true; // basement 5
							else if (yy > Y_001+19 && yy < Y_001+23) isWindow = true; // basement 4
							else if (yy > Y_001+14 && yy < Y_001+18) isWindow = true; // basement 3
							else if (yy > Y_001+9  && yy < Y_001+13) isWindow = true; // basement 2
							else if (yy > Y_001+4  && yy < Y_001+8 ) isWindow = true; // basement 1
						}
					}
					// light inside wall
					if (inside == 1) {
						if (iy > 3 && iy < cy-1)
							chunk.setBlock(ix, yy, iz, light);
					} else
					// inner wall
					if (inner > 0) {
						if (isWindow) chunk.setBlock(ix, y+iy, iz, Material.BLACK_STAINED_GLASS);
						else          chunk.setBlock(ix, y+iy, iz, Material.QUARTZ_BLOCK       );
					} else
					// outer walls
					if (outer > 0) {
						// between levels
						if (yy >  Y_019+H_019
						||  yy == Y_019
						||  yy == Y_005-1
						||  yy == Y_005
						||  yy == Y_005+1
						||  yy == Y_037
						||  yy == Y_006
						||  yy == Y_000-1
						||  yy == Y_000
						||  yy == Y_000+1
						||  yy == Y_023
						||  yy == Y_023+1
						||  yy == Y_023+2) {
							chunk.setBlock(ix, yy, iz, Material.BEDROCK);
						} else {
							// window
							if (isWindow) {
								chunk.setBlock(ix, yy, iz, Material.BLACK_STAINED_GLASS);
							// wall
							} else {
								if      (yy > Y_019  ) chunk.setBlock(ix, yy, iz, block_attic_wall       ); // attic
								else if (yy ==Y_005+5) chunk.setBlock(ix, yy, iz, block_hotel_wall_top   ); // hotel top
								else if (yy > Y_005+1) chunk.setBlock(ix, yy, iz, block_hotel_wall_center); // hotel center
								else if (yy ==Y_005  ) chunk.setBlock(ix, yy, iz, block_hotel_wall_bottom); // hotel bottom
								else if (yy > Y_037  ) chunk.setBlock(ix, yy, iz, block_pool_wall        ); // poolrooms
								else if (yy > Y_006  ) chunk.setBlock(ix, yy, iz, block_lightsout_wall   ); // lights out
								else if (yy > Y_000  ) chunk.setBlock(ix, yy, iz, block_lobby_wall       ); // lobby
								else if (yy > Y_023  ) chunk.setBlock(ix, yy, iz, block_overgrowth_wall  ); // overgrowth
								else                           chunk.setBlock(ix, yy, iz, Material.BEDROCK     ); // basement
							}
						}
					}
				} // end iy
			} // end ix
		} // end iz
	}



	// -------------------------------------------------------------------------------
	// configs



	@Override
	protected void loadConfig() {
	}
	public static void ConfigDefaults(final FileConfiguration cfg) {
	}



}
