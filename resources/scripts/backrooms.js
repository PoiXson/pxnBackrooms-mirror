//# include = inc.js
//# include = backrooms-level0.js
//# include = backrooms-level5.js
//# include = backrooms-level11.js
//# include = backrooms-level22.js
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
// backrooms.js

importClass(Packages.com.poixson.commonbukkit.utils.BukkitUtils);
importClass(Packages.me.auburn.FastNoiseLiteF);



var options = {
	seed: 0,
	difficulty: 1,
	expose:   false,
	allWeird: false,
	level0: {
		y: 0,
		h: 6,
		wall_thresh: 0.4,
		wall_median: 0.15,
		door_thresh: 0.85,
		door_median: 0.05,
	},
	level5: {
		y: 9,
		h: 6
	},
	level11: {
		y: 40,
	},
	level22: {
		y: -55,
		h: 10
	},
};
options.weird = {
	thresh_active: (options.difficulty * 0.38) + 0.3,
	thresh_style:  (options.difficulty * 0.3 ) + 0.8,
};
options.seed = Math.abs(options.seed);



function generate(chunk) {
	let buffer = [ ];
	generate_level0(chunk, buffer);
	generate_level5(chunk, buffer);
	generate_level11(chunk, buffer);
	generate_level22(chunk, buffer);
	chunk.setBlocksJS(buffer);
}



function can_spawn(x, z) {
	return true;
}
function get_spawn(world) {
	let x = 0;
	let y = options.level0.y + 1;
	let z = 0;
	return new Packages.org.bukkit.Location(world, x, y, z);
}



// -------------------------------------------------------------------------------
// noise



var noise = { };



// weird zones
noise.weird_active = new FastNoiseLiteF(options.seed);
noise.weird_active.setNoiseType(FastNoiseLiteF.NoiseType.OpenSimplex2S);
noise.weird_active.setFrequency( 0.005 / (options.difficulty + 1) );
noise.weird_active.setFractalOctaves(1);
noise.weird_active.setFractalType(FastNoiseLiteF.FractalType.None);

// weird style
noise.weird_style = new FastNoiseLiteF(options.seed);
noise.weird_style.setNoiseType(FastNoiseLiteF.NoiseType.OpenSimplex2S);
noise.weird_style.setFrequency( 0.0005 / (options.difficulty * 5) );
noise.weird_style.setFractalOctaves(3);
noise.weird_style.setFractalType(FastNoiseLiteF.FractalType.PingPong);



function getWeirdness(x, z) {
	let wrd = { };
	wrd.value = noise.weird_active.getNoise(x, z);
	wrd.style = wrd.value + (noise.weird_style.getNoise(x, z) * 0.2);
	wrd.extra = Math.floor(wrd.style * 1000) % 1000;
	wrd.active = (wrd.value > options.weird.thresh_active);
	wrd.point = true;
	// highest weirdness in the area
	LOOP_Z:
	for (let zz=-1; zz<2; zz++) {
		LOOP_X:
		for (let xx=-1; xx<2; xx++) {
			if (xx == 0 && zz == 0)
				continue LOOP_X;
			if (noise.weird_active.getNoise(x+xx, z+zz) > wrd.value) {
				wrd.point = false;
				break LOOP_Z;
			}
		}
	}
	return wrd;
}
