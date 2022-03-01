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
// backrooms-level5.js
// hotel



// hotel halls/rooms
noise.level5_rooms = new FastNoiseLiteF(options.seed);
noise.level5_rooms.setNoiseType(FastNoiseLiteF.NoiseType.Cellular);
noise.level5_rooms.setFrequency(0.02);
noise.level5_rooms.setFractalOctaves(1);
noise.level5_rooms.setFractalType(FastNoiseLiteF.FractalType.PingPong);
noise.level5_rooms.setCellularJitter(0.3);
noise.level5_rooms.setFractalPingPongStrength(2.0);
noise.level5_rooms.setCellularDistanceFunction(FastNoiseLiteF.CellularDistanceFunction.Manhattan);
noise.level5_rooms.setCellularReturnType(FastNoiseLiteF.CellularReturnType.Distance);



function generate_level5(chunk, buffer) {
	let chunkX = chunk.absX;
	let chunkZ = chunk.absZ;
	let y = options.level5.y;
	let h = options.level5.h;
	let absX, absZ;
	// preload noise
	let area = [];
	let val;
	let search = 8;
	for (let z=0-search; z<16+search; z++) {
		area[z] = [];
		for (let x=0-search; x<16+search; x++) {
			absX = chunkX + x;
			absZ = chunkZ + z;
			val = noise.level5_rooms.getNoise(rotX(absX, absZ), rotZ(absX, absZ));
			area[z][x] = {
				value: val,
				inout: (val < 0.65),
			};
		} // end x
	} // end z
	// find walls
	for (let z=0; z<16; z++) {
		for (let x=0; x<16; x++) {
			// inside/room
			if (area[z][x].inout) {
				// edge/wall
				area[z][x].wall = (
					!area[z][x+1].inout || !area[z][x-1].inout ||
					!area[z+1][x].inout || !area[z-1][x].inout
				);
				if (area[z][x].wall) {
					// thin wall
					area[z][x].thinwall = (
						(!area[z][x+2].inout && !area[z][x-2].inout) ||
						(!area[z+2][x].inout && !area[z-2][x].inout)
					);
				}
			}
		} // end x
	} // end z
	// fixes
	let found;
	for (let z=0; z<16; z++) {
		for (let x=0; x<16; x++) {
			// inside/room
			if (area[z][x].inout) {
				// thin wall corner fill
				if (!area[z][x].wall) {
					if ((area[z][x+1].wall && area[z][x-1].wall)
					||  (area[z+1][x].wall && area[z-1][x].wall))
						area[z][x].wall = true;
				}
			// outside/hall
			} else {
				// find direction
				area[z][x].direction = "";
				found = 0;
				SEARCH_LOOP:
				for (let i=1; i<search; i++) {
					// north/south
					if (area[z][x+i].inout || area[z][x-i].inout) {
						if (area[z][x].direction == "")
							area[z][x].direction = "ns";
						// center of hall
						area[z][x].center = (area[z][x+i].inout && area[z][x-i].inout);
						found++;
					} else
					// east/west
					if (area[z+i][x].inout || area[z-i][x].inout) {
						if (area[z][x].direction == "")
							area[z][x].direction = "ew";
						// center of hall
						area[z][x].center = (area[z+i][x].inout && area[z-i][x].inout);
						found++;
					}
					if (area[z][x].center || found > 1)
						break SEARCH_LOOP;
				}
			}
		} // end x
	} // end z
	// place blocks
	let blocksCommon = {
		bedrock: BukkitUtils.ParseBlockType("bedrock"),
	};
	let blocksNormal = {
		wall:    BukkitUtils.ParseBlockType("stripped_dark_oak_wood"),
		ceiling: BukkitUtils.ParseBlockType("smooth_stone"),
		lamp:    BukkitUtils.ParseBlockType("redstone_lamp[lit=true]"),
	};
	let blocks;
	let block;
	let modX, modZ;
	let isLamp, mod;
	let facing, type;
	for (let z=0; z<16; z++) {
		for (let x=0; x<16; x++) {
			absX = chunkX + x;
			absZ = chunkZ + z;
			blocks = blocksNormal;
			// bedrock
			if (!options.expose) {
				setBlock(buffer, blocksCommon.bedrock, x, y-1, z);
			}
			// inside/room
			if (area[z][x].inout) {
				// wall
				if (area[z][x].wall) {
					// thin wall - opening
					if (area[z][x].thinwall) {
						setBlock(buffer, blocks.wall, x, y,       z);
						setBlock(buffer, blocks.wall, x, (y+h)-2, z);
					// wall
					} else {
						for (let i=0; i<h-1; i++) {
							setBlock(buffer, blocks.wall, x, y+i, z);
						}
					}
				}
			// outside/hall
			} else {
				// floor
				{
					modX = x % 2;
					modZ = z % 2;
					type = "black_glazed_terracotta";
					facing = "n";
					if (area[z][x].direction == "ns") {
						facing = (modX==0 ? "n" : "s");
					} else
					if (area[z][x].direction == "ew") {
						facing = (modZ==0 ? "n" : "s");
					}
					block = BukkitUtils.ParseBlockType(type+"[facing="+facing+"]");
					setBlock(buffer, block, x, y, z);
				}
				if (!options.expose) {
					// lamps
					isLamp = area[z][x].center
					if (isLamp) {
						if (area[z][x].direction == "ns") {
							mod = (absZ<0 ? 1-absZ : absZ) % 7;
						} else {
							mod = (absX<0 ? 1-absX : absX) % 7;
						}
						if (mod > 1)
							isLamp = false;
					}
					// lamp
					if (isLamp) {
						setBlock(buffer, blocks.lamp, x, y+h-1, z);
					// ceiling
					} else {
						setBlock(buffer, blocks.ceiling, x, y+h-1, z);
					}
				}
			}
		} // end x
	} // end z
}
