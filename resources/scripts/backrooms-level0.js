/* ===============================================================================
 *  Copyright (c) 2022 lorenzop
 *  <https://poixson.com>
 *  Released under the GPL 3.0
 * 
 *  Description: Script to generate the Backrooms
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ===============================================================================
 */
// backrooms-level0.js
// lobby



// walls
noise.level0_walls = new FastNoiseLiteF(options.seed);
noise.level0_walls.setNoiseType(FastNoiseLiteF.NoiseType.Cellular);
noise.level0_walls.setFrequency(0.029 / (1.0 + (0.1 * options.difficulty)));
noise.level0_walls.setFractalOctaves(1);
noise.level0_walls.setFractalType(FastNoiseLiteF.FractalType.PingPong);
noise.level0_walls.setCellularDistanceFunction(FastNoiseLiteF.CellularDistanceFunction.Manhattan);
noise.level0_walls.setCellularReturnType(FastNoiseLiteF.CellularReturnType.Distance);

// door noise
noise.level0_doors = new FastNoiseLiteF(options.seed);
noise.level0_doors.setNoiseType(FastNoiseLiteF.NoiseType.Cellular);
noise.level0_doors.setFrequency(0.025);
noise.level0_doors.setFractalOctaves(3);
noise.level0_doors.setFractalType(FastNoiseLiteF.FractalType.PingPong);
noise.level0_doors.setRotationType3D(FastNoiseLiteF.RotationType3D.ImproveXYPlanes);
noise.level0_doors.setCellularDistanceFunction(FastNoiseLiteF.CellularDistanceFunction.Manhattan);
noise.level0_doors.setCellularReturnType(FastNoiseLiteF.CellularReturnType.Distance);



function generate_level0(chunk, buffer) {
	let chunkX = chunk.absX;
	let chunkZ = chunk.absZ;
	let y = options.level0.y;
	let h = options.level0.h;
	let absX, absZ;
	let modX, modZ;
	let area = [];
	let val;
	let search = 8;
	for (let z=0-search; z<16+search; z++) {
		area[z] = [ ];
		for (let x=0-search; x<16+search; x++) {
			absX = chunkX + x;
			absZ = chunkZ + z;
			modX = (absX<0 ? 1-absX : absX) % 5;
			modZ = (absZ<0 ? 1-absZ : absZ) % 6;
			val = noise.level0_walls.getNoise(rotX(absX, absZ), rotZ(absX, absZ));
			area[z][x] = {
				value: val,
				weird: getWeirdness(absX, absZ),
				wall:  false,
				wallnear: -1,
				door:  false,
				lamp: (modX == 0 && modZ < 2),
			};
			area[z][x].wall = (
				val > options.level0.wall_thresh &&
				val < options.level0.wall_thresh + options.level0.wall_median
			);
			// doors
			if (area[z][x].wall) {
				val = noise.level0_doors.getNoise(rotX(absX, absZ), rotZ(absX, absZ));
				area[z][x].door = (
					val > options.level0.door_thresh &&
					val < options.level0.door_thresh + options.level0.door_median
				);
			}
		} // end x
	} // end z
	// fix lights
	for (let z=0; z<16; z++) {
		for (let x=0; x<16; x++) {
			SEARCH_LOOP:
			for (let i=1; i<search; i++) {
				if (area[z][x+i].wall || area[z][x-i].wall
				||  area[z+i][x].wall || area[z-i][x].wall) {
					area[z][x].wallnear = i;
					break SEARCH_LOOP;
				}
			} // end SEARCH_LOOP
			// wall is near
			if (area[z][x].lamp) {
				if (area[z][x].wallnear != -1 && area[z][x].wallnear <= 1)
					area[z][x].lamp = false;
			}
//TODO: find door direction
		} // end x
	} // end z
	let blocks;
	let blocksCommon = {
		bedrock:  BukkitUtils.ParseBlockType("bedrock"),
		lamp:     BukkitUtils.ParseBlockType("redstone_lamp[lit=true]"),
		rs_block: BukkitUtils.ParseBlockType("redstone_block"),
	};
	let blocksNormal = {
		wall_top: BukkitUtils.ParseBlockType("yellow_terracotta"),
		wall:     BukkitUtils.ParseBlockType("yellow_terracotta"),
		wall_bot: BukkitUtils.ParseBlockType("yellow_terracotta"),
		floor:    BukkitUtils.ParseBlockType("light_gray_wool"),
		door:     BukkitUtils.ParseBlockType("birch_door"),
		ceiling:  BukkitUtils.ParseBlockType("smooth_stone"),
	};
	let blocksWeirdNorm = {
		wall_top: BukkitUtils.ParseBlockType("light_gray_terracotta"),
		wall:     BukkitUtils.ParseBlockType("brown_mushroom_block"),
		wall_bot: BukkitUtils.ParseBlockType("light_gray_terracotta"),
		floor:    BukkitUtils.ParseBlockType("red_wool"),
		door:     BukkitUtils.ParseBlockType("spruce_door"),
		ceiling:  BukkitUtils.ParseBlockType("warped_planks"),
	};
	let blocksWeirdRare = {
		wall_top: BukkitUtils.ParseBlockType("chiseled_nether_bricks"),
		wall:     BukkitUtils.ParseBlockType("nether_wart_block"),
		wall_bot: BukkitUtils.ParseBlockType("cracked_nether_bricks"),
		floor:    BukkitUtils.ParseBlockType("tinted_glass"),
		door:     BukkitUtils.ParseBlockType("crimson_door"),
		ceiling:  BukkitUtils.ParseBlockType("crimson_planks"),
	};
	// place the blocks
	for (let z=0; z<16; z++) {
		for (let x=0; x<16; x++) {
			// weird area
			if (options.allWeird || area[z][x].active) {
				// rare weird
				if (area[z][x].style > options.weird.thresh_style) {
					blocks = blocksWeirdRare;
				// normal weird
				} else {
					blocks = blocksWeirdNorm;
				}
			// normal area
			} else {
				blocks = blocksNormal;
			}
			// bedrock
			if (!options.expose) {
				setBlock(buffer, blocksCommon.bedrock, x, y-1, z);
			}
			// floor
			setBlock(buffer, blocks.floor, x, y, z);
			if (!options.expose) {
				// lights
				if (area[z][x].lamp) {
					setBlock(buffer, blocksCommon.lamp,     x, y+h,   z);
					setBlock(buffer, blocksCommon.rs_block, x, y+h+1, z);
				// ceiling
				} else {
					setBlock(buffer, blocks.ceiling, x, y+h,   z);
					setBlock(buffer, blocks.ceiling, x, y+h+1, z);
				}
			}
			// wall
			if (area[z][x].wall) {
				for (let i=0; i<h; i++) {
					if (area[z][x].door) {
						if (i == 1) {
//TODO: place a door
						}
						if (i == 1 || i == 2)
							continue;
					}
					if (i > h) {
						setBlock(buffer, blocks.wall_top, x, y+i,   z);
						setBlock(buffer, blocks.wall_top, x, y+i+1, z);
						break;
					}
					if (i <= 1) {
						setBlock(buffer, blocks.wall_bot, x, y+i, z);
					} else {
						setBlock(buffer, blocks.wall,     x, y+i, z);
					}
				}
				// fill crawl space
				if (!options.expose) {
					setBlock(buffer, blocks.ceiling, x, y+h,   z);
					setBlock(buffer, blocks.ceiling, x, y+h+1, z);
				}
			} // end wall
		} // end x
	} // end z
}
