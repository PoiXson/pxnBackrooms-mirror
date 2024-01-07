
importClass(Packages.org.bukkit.Bukkit);
importClass(Packages.org.bukkit.Material);

importClass(Packages.com.poixson.utils.FastNoiseLiteD);
importClass(Packages.com.poixson.tools.plotter.PlotterFactory);
importClass(Packages.com.poixson.utils.StringUtils);



function radio_lot_ground() {
	let noise_floor = new FastNoiseLiteD();
	noise_floor.setSeed(seed);
	noise_floor.setFrequency(0.1);
	noise_floor.setFractalOctaves(2);
	noise_floor.setFractalType(FastNoiseLiteD.FractalType.FBm);
	let lichen = Bukkit.createBlockData("minecraft:glow_lichen[down=true]");
	// prepare area
	for (let iz=-16; iz<32; iz++) {
		for (let ix=-16; ix<32; ix++) {
			// fill dirt
			for (let iy=surface_y-5; iy<surface_y; iy++) {
				switch (region.getType(ix, iy, iz)) {
					case Material.AIR:
					case Material.GRASS_BLOCK:
						region.setType(ix, iy, iz, Material.DIRT);
						break;
					default: break;
				}
			}
			// air above ground
			for (let iy=surface_y; iy<surface_y+5; iy++)
				region.setType(ix, iy, iz, Material.AIR);
		}
	}
	// lot ground
	for (let iz=-15; iz<31; iz++) {
		for (let ix=-15; ix<31; ix++) {
			let value = noise_floor.getNoise(ix, iz);
			if      (value > 0.5) region.setType(ix, surface_y-1, iz, Material.CLAY                      );
			else if (value > 0.0) region.setType(ix, surface_y-1, iz, Material.LIGHT_GRAY_CONCRETE_POWDER);
			else                  region.setType(ix, surface_y-1, iz, Material.PODZOL                    );
			if ((value > 0.2 && value < 0.45)
			||  (value >-0.4 && value <-0.35))
				region.setBlockData(ix, surface_y, iz, lichen);
		}
	}
}

function radio_lot_fence() {
	let plot = (new PlotterFactory())
		.placer(region)
		.axis("use")
		.xyz(-16, surface_y, -16)
		.whd(48, 5, 48)
		.build();
	plot.type('=', Material.COPPER_BLOCK);
	plot.type('_', Material.CUT_COPPER_SLAB);
	plot.type('x', "minecraft:iron_bars[north=true,south=true]");
	plot.type('X', "minecraft:iron_bars[east=true,west=true]"  );
	plot.type('I', Material.MOSSY_STONE_BRICK_WALL);
	let matrix = plot.getMatrix3D();
	// north fence
	matrix[4][0]            .append('_'.repeat(48));
	matrix[3][0].append('I').append('X'.repeat(46)).append('I');
	matrix[0][0]            .append('='.repeat(48));
	matrix[2][0].append(matrix[3][0].toString());
	matrix[1][0].append(matrix[3][0].toString());
	// south fence
	matrix[4][47]            .append('_'.repeat(48));
	matrix[3][47].append('I').append('X'.repeat(46)).append('I');
	matrix[0][47]            .append('='.repeat(48));
	// front gate
	let gate_pos = (path_start_x + 14) - (path_width * 0.5);
	StringUtils.ReplaceInString(matrix[4][47], ' '.repeat(path_width+6), gate_pos);
	StringUtils.ReplaceInString(matrix[3][47], ' '.repeat(path_width+6), gate_pos);
	StringUtils.ReplaceInString(matrix[0][47], ' '.repeat(path_width+6), gate_pos);
	matrix[2][47].append(matrix[3][47].toString());
	matrix[1][47].append(matrix[3][47].toString());
	// east/west fence
	for (let z=1; z<47; z++) {
		matrix[4][z].append('_').append(' '.repeat(46)).append('_');
		matrix[3][z].append('x').append(' '.repeat(46)).append('x');
		matrix[0][z].append('=').append(' '.repeat(46)).append('=');
		matrix[2][z].append(matrix[3][z].toString());
		matrix[1][z].append(matrix[3][z].toString());
	}
	plot.run();
}



function radio_antenna(x, y, z, size) {
	let block_beam          = Bukkit.createBlockData("minecraft:iron_block"                      );
	let block_iron_bars_xns = Bukkit.createBlockData("minecraft:iron_bars[east=true,west=true]"  );
	let block_iron_bars_zew = Bukkit.createBlockData("minecraft:iron_bars[north=true,south=true]");
	let block_iron_bars_ne  = Bukkit.createBlockData("minecraft:iron_bars[south=true,west=true]" );
	let block_iron_bars_nw  = Bukkit.createBlockData("minecraft:iron_bars[south=true,east=true]" );
	let block_iron_bars_se  = Bukkit.createBlockData("minecraft:iron_bars[north=true,west=true]" );
	let block_iron_bars_sw  = Bukkit.createBlockData("minecraft:iron_bars[north=true,east=true]" );
	let block_ladder        = Bukkit.createBlockData("minecraft:ladder[facing=north]"            );
	let size_half = Math.floor(size * 0.5);
	let iy          = surface_y+5                                                 ;
	let inset       = 0;
	let inset_micro = 1;
	let inset_level = 0;
	while (true) {
		if (inset >= size_half) {
			inset = inset_level + 1;
			inset_micro = 1;
			inset_level++;
			// top of tower
			if (inset_level > size_half) {
				for (let i=0; i<5; i++)
					region.setBlockData(x+size_half, y+iy+i, z+size_half, block_beam);
				break;
			}
			// fence corners
			region.setBlockData( x+(size-inset_level)+2, y+iy+1,(z+      inset_level)-2, block_iron_bars_ne); // north-east
			region.setBlockData((x+     inset_level) -2, y+iy+1,(z+      inset_level)-2, block_iron_bars_nw); // north-west
			region.setBlockData( x+(size-inset_level)+2, y+iy+1, z+(size-inset_level)+2, block_iron_bars_se); // south-east
			region.setBlockData((x+      inset_level)-2, y+iy+1, z+(size-inset_level)+2, block_iron_bars_sw); // south-west
			// flat square
			let fence_width = (size - (inset_level*2)) + 3;
			for (let i=0; i<fence_width; i++) {
				// fences
				region.setBlockData(x+(   i+inset_level)-1, y+iy+1, z+      inset_level -2, block_iron_bars_xns); // x north
				region.setBlockData(x+(   i+inset_level)-1, y+iy+1, z+(size-inset_level)+2, block_iron_bars_xns); // x south
				region.setBlockData(x+(size-inset_level)+2, y+iy+1, z+   (i+inset_level)-1, block_iron_bars_zew); // z east
				region.setBlockData(x+      inset_level -2, y+iy+1, z+   (i+inset_level)-1, block_iron_bars_zew); // z west
				// beams
				if (i < fence_width-1) {
					region.setBlockData(x+(   i+inset_level)-1, y+iy, z+      inset_level -1, block_beam); // x north
					region.setBlockData(x+    i+inset_level,    y+iy, z+(size-inset_level)+1, block_beam); // x south
					region.setBlockData(x+(size-inset_level)+1, y+iy, z+(   i+inset_level)-1, block_beam); // z east
					if (i != fence_width-3)
					region.setBlockData(x+      inset_level -1, y+iy, z+    i+inset_level,    block_beam); // z west
				}
			}
			// inside cross
			let w = (size - inset_level) + 1;
			for (let i=inset_level; i<w; i++) {
				region.setBlockData(x+i,         y+iy, z+size_half, block_beam);
				region.setBlockData(x+size_half, y+iy, z+i,         block_beam);
			}
		}
		if (inset_micro >= 3) {
			inset_micro = 0;
			inset++;
		}
		// braces
		if (inset_level < size_half-1) {
			region.setBlockData(x+       inset, y+iy, z+     inset, block_beam);
			region.setBlockData(x+(size)-inset, y+iy, z+     inset, block_beam);
			region.setBlockData(x+       inset, y+iy, z+size-inset, block_beam);
			region.setBlockData(x+(size)-inset, y+iy, z+size-inset, block_beam);
		}
		// legs
		region.setBlockData(x+size-inset_level, y+iy, z+     inset_level, block_beam); // north-east
		region.setBlockData(x+     inset_level, y+iy, z+     inset_level, block_beam); // north-west
		region.setBlockData(x+size-inset_level, y+iy, z+size-inset_level, block_beam); // south-east
		region.setBlockData(x+     inset_level, y+iy, z+size-inset_level, block_beam); // south-west
		// ladder
		if (iy > 0)
			region.setBlockData(x+inset_level, y+iy+1, z+(size-inset_level)-1, block_ladder);
		iy++;
		inset_micro++;
	}
}



function radio_building_back(x, z, w, h, d) {
	let plot = (new PlotterFactory())
		.placer(region)
		.axis("use")
		.xyz(x, surface_y, z)
		.whd(w, h, d)
		.build();
	plot.type('@', Material.POLISHED_ANDESITE    ); // wall fill
	plot.type('#', Material.POLISHED_BASALT      ); // wall corner
	plot.type('=', Material.POLISHED_GRANITE     ); // wall stripe
	plot.type('_', Material.POLISHED_GRANITE_SLAB); // wall top
	plot.type('F', Material.POLISHED_DIORITE     ); // floor
	plot.type('~', "minecraft:stone_slab[type=bottom]"    ); // roof
	plot.type('-', "minecraft:smooth_stone_slab[type=top]"); // ceiling
	plot.type('.', Material.AIR);
	if (!enable_ceiling) {
		plot.type('~', Material.AIR); // roof
		plot.type('-', Material.AIR); // ceiling
	}
	let matrix = plot.getMatrix3D();
	let wall, fill;
	for (let iy=0; iy<h-1; iy++) {
		wall = (iy==7 ? '=' : '@');
		if (iy == 0  ) fill = ' '; else // subfloor
		if (iy == 1  ) fill = 'F'; else // floor 1
		if (iy == 6  ) fill = '-'; else // ceiling 1
		if (iy == 7  ) fill = 'F'; else // floor 2
		if (iy == h-3) fill = '-'; else // ceiling 2
		if (iy == h-2) fill = '~'; else // roof
			fill = '.';
		// north/south walls
		matrix[iy][  0].append('#').append(wall.repeat(w-2)).append('#'); // back wall
		matrix[iy][d-1].append('#').append(wall.repeat(w-2)).append('#'); // front wall
		// east/west walls
		for (let iz=1; iz<d-1; iz++)
			matrix[iy][iz].append(wall).append(fill.repeat(w-2)).append(wall);
	}
	// wall top
	matrix[h-1][  0].append('_'.repeat(w));
	matrix[h-1][d-1].append('_'.repeat(w));
	for (let iz=1; iz<d-1; iz++)
		matrix[h-1][iz].append('_').append(' '.repeat(w-2)).append('_');
	plot.run();
}

function radio_building_front(x, z, w, h, d) {
	let plot = (new PlotterFactory())
		.placer(region)
		.axis("use")
		.xyz(x, surface_y, z)
		.whd(w, h, d)
		.build();
	plot.type('@', Material.POLISHED_ANDESITE    ); // wall fill
	plot.type('#', Material.POLISHED_BASALT      ); // wall corner
	plot.type('_', Material.POLISHED_GRANITE_SLAB); // wall top
	plot.type('F', Material.POLISHED_DIORITE     ); // floor
	plot.type('~', "minecraft:stone_slab[type=bottom]"    ); // roof
	plot.type('-', "minecraft:smooth_stone_slab[type=top]"); // ceiling
	plot.type('.', Material.AIR);
	if (!enable_ceiling) {
		plot.type('~', Material.AIR); // roof
		plot.type('-', Material.AIR); // ceiling
	}
	let matrix = plot.getMatrix3D();
	let fill;
	for (let iy=0; iy<h-1; iy++) {
		if (iy == 0  ) fill = 'F'; else
		if (iy == h-2) fill = '~'; else
		if (iy == h-3) fill = '-'; else
			fill = '.';
		// north/south walls
		if (iy < h-2)
		matrix[iy][  0].append('#').append(fill.repeat(w-2)).append('#'); // back wall
		matrix[iy][d-1].append('#').append('@'.repeat(w-2)).append('#'); // front wall
		// east/west walls
		for (let iz=1; iz<d-1; iz++)
			matrix[iy][iz].append('@').append(fill.repeat(w-2)).append('@');
	}
	// wall top
	matrix[h-1][d-1].append('_'.repeat(w));
	for (let iz=1; iz<d-1; iz++)
		matrix[h-1][iz].append('_').append(' '.repeat(w-2)).append('_');
	plot.run();
}



radio_lot_ground();
radio_lot_fence();
radio_building_back( -13, -13, 42, 16, 21);
radio_building_front(-13,   7, 15,  9, 21);
radio_antenna(-11, 134, -11, 16);
