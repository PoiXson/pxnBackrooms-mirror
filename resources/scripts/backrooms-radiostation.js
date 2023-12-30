
importClass(Packages.org.bukkit.Material);

importClass(Packages.com.poixson.tools.plotter.PlotterFactory);
importClass(Packages.com.poixson.utils.StringUtils);



function radio_lot_ground() {
	for (let iz=-16; iz<32; iz++) {
		for (let ix=-16; ix<32; ix++) {
			// gravel surface
			for (let iy=surface_y-5; iy<surface_y; iy++) {
				if (iy == surface_y-1
				|| Material.AIR.equals(region.getType(ix, iy, iz)))
					region.setType(ix, iy, iz, Material.GRAVEL);
			}
			// air above ground
			for (let iy=surface_y; iy<surface_y+5; iy++)
				region.setType(ix, iy, iz, Material.AIR);
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
	let plot = (new PlotterFactory())
		.placer(region)
		.xyz(x, y, z)
		.build();
	plot.type('#', "minecraft:iron_block");
	plot.type('x', "minecraft:iron_bars[east=true,west=true]"  );
	plot.type('z', "minecraft:iron_bars[north=true,south=true]");
	plot.type('N', "minecraft:iron_bars[south=true,west=true]" );
	plot.type('n', "minecraft:iron_bars[south=true,east=true]" );
	plot.type('S', "minecraft:iron_bars[north=true,west=true]" );
	plot.type('s', "minecraft:iron_bars[north=true,east=true]" );
	plot.type('H', "minecraft:ladder[facing=north]"            );
	let size_half = Math.floor(size * 0.5);
	let iy          = 0;
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
					plot.setBlock(size_half, iy+i, size_half, '#');
				break;
			}
			// fence corners
			plot.setBlock((size-inset_level)+2, iy+1,       inset_level -2, 'N'); // north-east
			plot.setBlock(      inset_level -2, iy+1,       inset_level -2, 'n'); // north-west
			plot.setBlock((size-inset_level)+2, iy+1, (size-inset_level)+2, 'S'); // south-east
			plot.setBlock(      inset_level -2, iy+1, (size-inset_level)+2, 's'); // south-west
			// flat square
			let fence_width = (size - (inset_level*2)) + 3;
			for (let i=0; i<fence_width; i++) {
				// fences
				plot.setBlock((   i+inset_level)-1, iy+1,       inset_level -2, 'x'); // x north
				plot.setBlock((   i+inset_level)-1, iy+1, (size-inset_level)+2, 'x'); // x south
				plot.setBlock((size-inset_level)+2, iy+1,    (i+inset_level)-1, 'z'); // z east
				plot.setBlock(      inset_level -2, iy+1,    (i+inset_level)-1, 'z'); // z west
				// beams
				if (i < fence_width-1) {
					plot.setBlock((   i+inset_level)-1, iy,       inset_level -1, '#'); // x north
					plot.setBlock(    i+inset_level,    iy, (size-inset_level)+1, '#'); // x south
					plot.setBlock((size-inset_level)+1, iy, (   i+inset_level)-1, '#'); // z east
					if (i != fence_width-3)
					plot.setBlock(      inset_level -1, iy,     i+inset_level,    '#'); // z west
				}
			}
			// inside cross
			let w = (size - inset_level) + 1;
			for (let i=inset_level; i<w; i++) {
				plot.setBlock(i, iy, size_half, '#');
				plot.setBlock(size_half, iy, i, '#');
			}
		}
		if (inset_micro >= 3) {
			inset_micro = 0;
			inset++;
		}
		// braces
		if (inset_level < size_half-1) {
			plot.setBlock(       inset, iy,      inset, '#');
			plot.setBlock((size)-inset, iy,      inset, '#');
			plot.setBlock(       inset, iy, size-inset, '#');
			plot.setBlock((size)-inset, iy, size-inset, '#');
		}
		// legs
		plot.setBlock(size-inset_level, iy,      inset_level, '#'); // north-east
		plot.setBlock(     inset_level, iy,      inset_level, '#'); // north-west
		plot.setBlock(size-inset_level, iy, size-inset_level, '#'); // south-east
		plot.setBlock(     inset_level, iy, size-inset_level, '#'); // south-west
		// ladder
		if (iy > 0)
			plot.setBlock(inset_level, iy+1, (size-inset_level)-1, 'H');
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
	plot.type('@', Material.POLISHED_DIORITE      ); // wall fill
	plot.type('#', Material.POLISHED_BASALT       ); // wall corner
	plot.type('=', Material.POLISHED_ANDESITE     ); // wall stripe
	plot.type('_', Material.POLISHED_ANDESITE_SLAB); // wall top
	if (enable_ceiling) {
		plot.type('~', "minecraft:stone_slab[type=bottom]"    ); // roof
		plot.type('-', "minecraft:smooth_stone_slab[type=top]"); // ceiling
	} else {
		plot.type('~', Material.AIR); // roof
		plot.type('-', Material.AIR); // ceiling
	}
	let matrix = plot.getMatrix3D();
	let wall, fill;
	for (let iy=0; iy<h-1; iy++) {
		wall = (iy==7 ? '=' : '@');
		if (iy == h-2) fill = '~'; else
		if (iy == h-3) fill = '-'; else
			fill = ' ';
		// north/south walls
		matrix[iy][  0].append('#').append(wall.repeat(w-2)).append('#');
		matrix[iy][d-1].append('#').append(wall.repeat(w-2)).append('#');
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
	plot.type('@', Material.POLISHED_DIORITE      ); // wall fill
	plot.type('#', Material.POLISHED_BASALT       ); // wall corner
	plot.type('_', Material.POLISHED_ANDESITE_SLAB); // wall top
	plot.type('.', Material.AIR                   ); // inside wall
	if (enable_ceiling) {
		plot.type('~', "minecraft:stone_slab[type=bottom]"    ); // roof
		plot.type('-', "minecraft:smooth_stone_slab[type=top]"); // ceiling
	} else {
		plot.type('~', Material.AIR); // roof
		plot.type('-', Material.AIR); // ceiling
	}
	let matrix = plot.getMatrix3D();
	let fill;
	for (let iy=0; iy<h-1; iy++) {
		if (iy == h-2) fill = '~'; else
		if (iy == h-3) fill = '-'; else
			fill = ' ';
		// north/south walls
		if (iy < h-2)
		matrix[iy][  0].append('#').append('.'.repeat(w-2)).append('#');
		matrix[iy][d-1].append('#').append('@'.repeat(w-2)).append('#');
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
