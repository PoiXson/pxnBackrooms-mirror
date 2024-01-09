
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
	plot.type('=', Material.COPPER_BLOCK                       );
	plot.type('_', Material.CUT_COPPER_SLAB                    );
	plot.type('x', "minecraft:iron_bars[north=true,south=true]");
	plot.type('X', "minecraft:iron_bars[east=true,west=true]"  );
	plot.type('I', Material.MOSSY_STONE_BRICK_WALL             );
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

function radio_path(x, z, w, d) {
	let block_floor = Bukkit.createBlockData("minecraft:cobblestone_slab[type=bottom]"   );
	let block_fence = Bukkit.createBlockData("minecraft:iron_bars[north=true,south=true]");
	// path across yard
	for (let iz=0; iz<d; iz++) {
		for (let ix=0; ix<w; ix++)
			region.setBlockData(x+ix, surface_y, z-iz, block_floor);
		region.setBlockData(x-1, surface_y, z-iz, block_fence); // west fence
		region.setBlockData(x+w, surface_y, z-iz, block_fence); // east fence
	}
	// porch at door
	for (let iz=0; iz<3; iz++) {
		for (let ix=-1; ix<=w; ix++)
			region.setType(x+ix, surface_y, (z-iz)-d, Material.COBBLESTONE);
	}
	// pillars
	for (let iy=0; iy<4; iy++) {
		region.setType(x-1, iy+surface_y+1, z-d, Material.POLISHED_DEEPSLATE_WALL);
		region.setType(x+w, iy+surface_y+1, z-d, Material.POLISHED_DEEPSLATE_WALL);
	}
	// porch roof
	{
		let plot = (new PlotterFactory())
			.placer(region)
			.axis("une")
			.xyz(x-2, surface_y+4, (z-d)+1)
			.whd(9, 4, 4)
			.build();
		plot.type('-', "minecraft:polished_andesite_slab[type=top]"                      );
		plot.type('_', "minecraft:polished_andesite_slab[type=bottom]"                   );
		plot.type('#', "minecraft:polished_granite"                                      );
		plot.type('@', "minecraft:polished_andesite"                                     );
		plot.type('s', "minecraft:cobbled_deepslate_slab[type=top]"                      );
		plot.type('*', "minecraft:lodestone"                                             );
		plot.type('+', "minecraft:red_stained_glass_pane[north=true,east=true,west=true]");
		plot.type('x', "minecraft:red_stained_glass_pane[north=true,south=true]"         );
		plot.type('L', "minecraft:light[level=15]");
		let matrix = plot.getMatrix3D();
		matrix[3][0].append("_________"); matrix[3][1].append("_#######_"); matrix[3][2].append("_#_____#_"); matrix[3][3].append("_#_____#_");
		matrix[2][0].append("+++++++++"); matrix[2][1].append("x#*#*#*#x"); matrix[2][2].append("x*#####*x"); matrix[2][3].append("x#######x");
		matrix[1][0].append("---------"); matrix[1][1].append("-@sssss@-"); matrix[1][2].append("-sssssss-"); matrix[1][3].append("-sssssss-");
		matrix[0][1].append("  L L L  ");
		plot.run();
	}
	// front door
	{
		let plot = (new PlotterFactory())
			.placer(region)
			.axis("une")
			.xyz(x, surface_y, (z-d)-2)
			.whd(10, 10, 10)
			.build();
		plot.type('=', "minecraft:polished_deepslate_wall[east=tall,west=tall]" );
		plot.type('x', "minecraft:tinted_glass"                                 );
		plot.type('d', "minecraft:iron_door[half=upper,facing=north,hinge=left]");
		plot.type('D', "minecraft:iron_door[half=lower,facing=north,hinge=left]");
		plot.type('_', "minecraft:heavy_weighted_pressure_plate"                );
		let matrix = plot.getMatrix3D();
		matrix[4][1]                              .append("====="); matrix[4][2].append("xxxxx");
		matrix[3][1]                              .append("=xxx="); matrix[3][2].append("x   x");
		matrix[2][1]                              .append("=xdx="); matrix[2][2].append("x   x");
		matrix[1][0].append("  _  "); matrix[1][1].append("=xDx="); matrix[1][2].append("x _ x");
		plot.run();
	}
}



function radio_antenna(x, y, z, size) {
	if (!enable_ceiling) return;
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
	plot.type('#', Material.POLISHED_ANDESITE             ); // wall fill
	plot.type('|', Material.POLISHED_BASALT               ); // wall corner
	plot.type('T', Material.TUFF                          ); // wall top accent
	plot.type('=', Material.POLISHED_GRANITE              ); // wall stripe
	plot.type('_', Material.POLISHED_GRANITE_SLAB         ); // wall top
	plot.type('F', Material.POLISHED_DIORITE              ); // 1st floor
	plot.type('f', Material.POLISHED_ANDESITE             ); // 2nd floor
	plot.type('~', "minecraft:stone_slab[type=bottom]"    ); // roof
	plot.type('-', "minecraft:smooth_stone_slab[type=top]"); // ceiling
	plot.type('.', Material.AIR);
	if (!enable_ceiling) {
		plot.type('~', Material.AIR); // roof
		plot.type('-', Material.AIR); // ceiling
	}
	let matrix = plot.getMatrix3D();
	let fill, wall, accent;
	for (let iy=0; iy<h-1; iy++) {
		wall   = (iy==7   ? '=' : '#');
		accent = (iy==h-2 ? 'T' : ' ');
		if (iy == 0  ) fill = 'F'; else // floor 1
		if (iy == 6  ) fill = '-'; else // ceiling 1
		if (enable_ceiling
		&&  iy == 7  ) fill = 'f'; else // floor 2
		if (iy == h-3) fill = '-'; else // ceiling 2
		if (iy == h-2) fill = '~'; else // roof
			fill = '.';
		// north/south walls
		matrix[iy][d-1].append('|').append(accent.repeat(w-2)).append('|'); // front accent
		matrix[iy][  0].append('|').append(accent.repeat(w-2)).append('|'); // back accent
		if (iy < h-2) {
			matrix[iy][  1].append(" |").append(wall.repeat(w-4)).append("| "); // back wall
			matrix[iy][d-2].append(" |").append(wall.repeat(w-4)).append("| "); // front wall
			// east/west walls
			for (let iz=2; iz<d-2; iz++)
				matrix[iy][iz].append(' ').append(wall).append(fill.repeat(w-4)).append(wall).append(' '); // side walls
		}
	}
	// wall top
	matrix[h-1][d-1].append('_'.repeat(w)); // front wall top
	matrix[h-1][  0].append('_'.repeat(w)); // back wall top
	for (let iz=1; iz<d-1; iz++) {
		matrix[h-1][iz].append('_').append(' '.repeat(w-2)).append('_'); // wall top
		matrix[h-2][iz].append('T').append('~'.repeat(w-2)).append('T'); // top accent
	}
	plot.run();
}

function radio_building_front(x, z, w, h, d) {
	let plot = (new PlotterFactory())
		.placer(region)
		.axis("use")
		.xyz(x, surface_y, z)
		.whd(w, h, d)
		.build();
	plot.type('#', Material.POLISHED_ANDESITE             ); // wall fill
	plot.type('|', Material.POLISHED_BASALT               ); // wall corner
	plot.type('T', Material.TUFF                          ); // wall top accent
	plot.type('_', Material.POLISHED_GRANITE_SLAB         ); // wall top
	plot.type('F', Material.POLISHED_DIORITE              ); // floor
	plot.type('~', "minecraft:stone_slab[type=bottom]"    ); // roof
	plot.type('-', "minecraft:smooth_stone_slab[type=top]"); // ceiling
	plot.type('.', Material.AIR);
	if (!enable_ceiling) {
		plot.type('~', Material.AIR); // roof
		plot.type('-', Material.AIR); // ceiling
	}
	let matrix = plot.getMatrix3D();
	let fill, accent;
	for (let iy=0; iy<h-1; iy++) {
		accent = (iy==h-2 ? 'T' : ' ');
		if (iy == 0  ) fill = 'F'; else // floor
		if (iy == h-2) fill = '~'; else // roof
		if (iy == h-3) fill = '-'; else // ceiling
			fill = '.';
		// north/south walls
		matrix[iy][d-1].append('|').append(accent.repeat(w-2)).append('|'); // front accent
		if (iy < h-2) {
			matrix[iy][  0].append(" |").append(fill.repeat(w-4)).append("| "); // back wall
			matrix[iy][d-2].append(" |").append( '#'.repeat(w-4)).append("| "); // front wall
			// east/west walls
			for (let iz=1; iz<d-2; iz++)
				matrix[iy][iz].append(" #").append(fill.repeat(w-4)).append("# "); // side walls
		}
	}
	// wall top
	matrix[h-1][d-1].append('_'.repeat(w)); // front wall top
	matrix[h-1][  1]            .append(' '.repeat(w-1)).append('_'); // next to building wall top
	matrix[h-2][  1].append(' ').append('~'.repeat(w-2)).append('T'); // next to building accent
	for (let iz=2; iz<d-1; iz++) {
		matrix[h-1][iz].append('_').append(' '.repeat(w-2)).append('_'); // wall top
		matrix[h-2][iz].append('T').append('~'.repeat(w-2)).append('T'); // top accent
	}
	plot.run();
}



function radio_building_inside_walls_1st(x, z, w, d, h) {
	let plot = (new PlotterFactory())
		.placer(region)
		.axis("use")
		.xyz(x, surface_y, z)
		.whd(w, h, d)
		.build();
	plot.type('#', Material.LIGHT_GRAY_CONCRETE               );
	plot.type('P', Material.LIGHT_GRAY_CONCRETE_POWDER        );
	plot.type('=', Material.STRIPPED_OAK_WOOD                 );
	plot.type('-', "minecraft:oak_slab[type=top]"             );
	plot.type('c', "minecraft:purpur_stairs[facing=west]"     ); // chair
	plot.type('C', "minecraft:purpur_stairs[facing=east]"     ); // chair
	plot.type('s', "minecraft:crimson_wall_sign[facing=north]"); // chair
	plot.type('S', "minecraft:crimson_wall_sign[facing=south]"); // chair
	plot.type('~', Material.HEAVY_WEIGHTED_PRESSURE_PLATE     );
	plot.type('.', Material.AIR                               );
	let matrix = plot.getMatrix3D();
	// 1st floor
	matrix[1][ 0].append("########################################");
	matrix[1][ 1].append("#            #       #         #       #");
	matrix[1][ 2].append("#            #       #         #       #");
	matrix[1][ 3].append("#            #       #         #       #");
	matrix[1][ 4].append("#            #       #         #       #");
	matrix[1][ 5].append("#            #       #    ######       #");
	matrix[1][ 6].append("######################    #    #       #");
	matrix[1][ 7].append("#                         #    #       #");
	matrix[1][ 8].append("#                         #    #       #");
	matrix[1][ 9].append("#                         #    #       #");
	matrix[1][10].append("#                         #    #       #");
	matrix[1][11].append("#    ################PPPPPPPPPPP########");
	matrix[1][12].append("#    #  #           #P         P        ");
	matrix[1][13].append("#    #  #           #P         P        ");
	matrix[1][14].append("#    #  #           #P         P        ");
	matrix[1][15].append("#    #  ####        #P         P        ");
	matrix[1][16].append("#    #  #  #        #P         P        ");
	matrix[1][17].append("#    #  #  #        #P         P        ");
	matrix[1][18].append("#    ################PPPPPPPPPPP        ");
	matrix[1][19].append("#    #      #"                           );
	matrix[1][20].append("#    #      #"                           );
	matrix[1][21].append("#    #      #"                           );
	matrix[1][22].append("#    #      #"                           );
	matrix[1][23].append("#    #      #"                           );
	matrix[1][24].append("#    #      #"                           );
	matrix[1][25].append("#    #      #"                           );
	matrix[1][26].append("#    #      #"                           );
	matrix[1][27].append("#############"                           );
	matrix[1][28].append("#           #"                           );
	matrix[1][29].append("#           #"                           );
	matrix[1][30].append("#           #"                           );
	matrix[1][31].append("#           #"                           );
	matrix[1][32].append("#           #"                           );
	matrix[1][33].append("#           #"                           );
	matrix[1][34].append("#           #"                           );
	matrix[1][35].append("#           #"                           );
	matrix[1][36].append("#           #"                           );
	matrix[1][37].append("#############"                           );
	// duplicate up y
	for (let iz=0; iz<38; iz++) {
		for (let iy=2; iy<h; iy++)
			matrix[iy][iz].append(matrix[1][iz].toString());
	}
	// front door
	StringUtils.ReplaceInString(matrix[4][18], "     ", 24);
	for (let iy=1; iy<4; iy++)
		StringUtils.ReplaceInString(matrix[iy][18], "     ", 24);
	// reception desk
	StringUtils.ReplaceInString(matrix[3][11], "---", 27);
	StringUtils.ReplaceInString(matrix[2][11], "...", 27);
	StringUtils.ReplaceInString(matrix[1][11], "===", 27);
	// reception benches
	StringUtils.ReplaceInString(matrix[1][13], "s", 22); StringUtils.ReplaceInString(matrix[1][12], "s", 30);
	StringUtils.ReplaceInString(matrix[1][17], "S", 22); StringUtils.ReplaceInString(matrix[1][16], "S", 30);
	for (let iz=0; iz<3; iz++) {
		StringUtils.ReplaceInString(matrix[1][14+iz], "c", 22);
		StringUtils.ReplaceInString(matrix[1][13+iz], "C", 30);
	}
	plot.run();
	// doors - reception to hall
	plot.type('d', "minecraft:iron_door[half=upper,facing=north,hinge=left]" ); plot.setBlock(23, 2, 11, 'd');
	plot.type('D', "minecraft:iron_door[half=lower,facing=north,hinge=left]" ); plot.setBlock(23, 1, 11, 'D');
	plot.type('d', "minecraft:iron_door[half=upper,facing=north,hinge=right]"); plot.setBlock(24, 2, 11, 'd');
	plot.type('D', "minecraft:iron_door[half=lower,facing=north,hinge=right]"); plot.setBlock(24, 1, 11, 'D');
	plot.setBlock(23, 1, 10, '~'); plot.setBlock(24, 1, 10, '~');
	plot.setBlock(23, 1, 12, '~'); plot.setBlock(24, 1, 12, '~');
	// reception back door
	plot.type('d', "minecraft:iron_door[half=upper,facing=south]"); plot.setBlock(29, 2, 5, 'd');
	plot.type('D', "minecraft:iron_door[half=lower,facing=south]"); plot.setBlock(29, 1, 5, 'D');
	plot.setBlock(29, 1,  4, '~'); plot.setBlock(29, 1,  6, '~');
	// doors - east room
	plot.type('d', "minecraft:iron_door[half=upper,facing=east,hinge=left]" ); plot.setBlock(31, 2, 2, 'd');
	plot.type('D', "minecraft:iron_door[half=lower,facing=east,hinge=left]" ); plot.setBlock(31, 1, 2, 'D');
	plot.type('d', "minecraft:iron_door[half=upper,facing=east,hinge=right]"); plot.setBlock(31, 2, 3, 'd');
	plot.type('D', "minecraft:iron_door[half=lower,facing=east,hinge=right]"); plot.setBlock(31, 1, 3, 'D');
	plot.setBlock(30, 1, 2, '~'); plot.setBlock(32, 1, 2, '~');
	plot.setBlock(30, 1, 3, '~'); plot.setBlock(32, 1, 3, '~');
	// doors - far south room
	plot.type('d', "minecraft:iron_door[half=upper,facing=south,hinge=right]"); plot.setBlock(2, 2, 27, 'd');
	plot.type('D', "minecraft:iron_door[half=lower,facing=south,hinge=right]"); plot.setBlock(2, 1, 27, 'D');
	plot.type('d', "minecraft:iron_door[half=upper,facing=south,hinge=left]" ); plot.setBlock(3, 2, 27, 'd');
	plot.type('D', "minecraft:iron_door[half=lower,facing=south,hinge=left]" ); plot.setBlock(3, 1, 27, 'D');
	plot.setBlock(2, 1, 26, '~'); plot.setBlock(2, 1, 28, '~');
	plot.setBlock(3, 1, 26, '~'); plot.setBlock(3, 1, 28, '~');
	// doors - adjacent waiting room
	plot.type('d', "minecraft:iron_door[half=upper,facing=south,hinge=right]"); plot.setBlock(15, 2, 11, 'd');
	plot.type('D', "minecraft:iron_door[half=lower,facing=south,hinge=right]"); plot.setBlock(15, 1, 11, 'D');
	plot.type('d', "minecraft:iron_door[half=upper,facing=south,hinge=left]" ); plot.setBlock(16, 2, 11, 'd');
	plot.type('D', "minecraft:iron_door[half=lower,facing=south,hinge=left]" ); plot.setBlock(16, 1, 11, 'D');
	plot.setBlock(15, 1, 10, '~'); plot.setBlock(15, 1, 12, '~');
	plot.setBlock(16, 1, 10, '~'); plot.setBlock(16, 1, 12, '~');
	// doors - east rooms, north to south
	plot.type('d', "minecraft:spruce_door[half=upper,facing=south,hinge=right]"); plot.setBlock(36, 2, 11, 'd');
	plot.type('D', "minecraft:spruce_door[half=lower,facing=south,hinge=right]"); plot.setBlock(36, 1, 11, 'D');
	plot.type('d', "minecraft:spruce_door[half=upper,facing=south,hinge=left]" ); plot.setBlock(37, 2, 11, 'd');
	plot.type('D', "minecraft:spruce_door[half=lower,facing=south,hinge=left]" ); plot.setBlock(37, 1, 11, 'D');
	plot.setBlock(36, 1, 10, '~'); plot.setBlock(36, 1, 12, '~');
	plot.setBlock(37, 1, 10, '~'); plot.setBlock(37, 1, 12, '~');
	// doors - north center room
	plot.type('d', "minecraft:iron_door[half=upper,facing=north,hinge=left]" ); plot.setBlock(17, 2, 6, 'd');
	plot.type('D', "minecraft:iron_door[half=lower,facing=north,hinge=left]" ); plot.setBlock(17, 1, 6, 'D');
	plot.type('d', "minecraft:iron_door[half=upper,facing=north,hinge=right]"); plot.setBlock(18, 2, 6, 'd');
	plot.type('D', "minecraft:iron_door[half=lower,facing=north,hinge=right]"); plot.setBlock(18, 1, 6, 'D');
	plot.setBlock(17, 1, 5, '~'); plot.setBlock(18, 1, 5, '~');
	plot.setBlock(17, 1, 7, '~'); plot.setBlock(18, 1, 7, '~');
	// doors - north west room
	plot.type('d', "minecraft:iron_door[half=upper,facing=north,hinge=left]" ); plot.setBlock( 9, 2, 6, 'd');
	plot.type('D', "minecraft:iron_door[half=lower,facing=north,hinge=left]" ); plot.setBlock( 9, 1, 6, 'D');
	plot.type('d', "minecraft:iron_door[half=upper,facing=north,hinge=right]"); plot.setBlock(10, 2, 6, 'd');
	plot.type('D', "minecraft:iron_door[half=lower,facing=north,hinge=right]"); plot.setBlock(10, 1, 6, 'D');
	plot.setBlock(9, 1, 5, '~'); plot.setBlock(10, 1, 5, '~');
	plot.setBlock(9, 1, 7, '~'); plot.setBlock(10, 1, 7, '~');
	plot.type('d', "minecraft:iron_door[half=upper,facing=north,hinge=left]"); plot.setBlock(3, 2, 6, 'd');
	plot.type('D', "minecraft:iron_door[half=lower,facing=north,hinge=left]"); plot.setBlock(3, 1, 6, 'D');
	plot.setBlock(3, 1, 5, '~'); plot.setBlock(3, 1, 7, '~');
	// doors to stairs
	plot.type('d', "minecraft:iron_door[half=upper,facing=south,hinge=right]"); plot.setBlock(6, 2, 11, 'd');
	plot.type('D', "minecraft:iron_door[half=lower,facing=south,hinge=right]"); plot.setBlock(6, 1, 11, 'D');
	plot.type('d', "minecraft:iron_door[half=upper,facing=south,hinge=left]" ); plot.setBlock(7, 2, 11, 'd');
	plot.type('D', "minecraft:iron_door[half=lower,facing=south,hinge=left]" ); plot.setBlock(7, 1, 11, 'D');
	plot.setBlock(6, 1, 10, '~'); plot.setBlock(6, 1, 12, '~');
	plot.setBlock(7, 1, 10, '~'); plot.setBlock(7, 1, 12, '~');
	// recording studio window (1st floor)
	plot.type('x', "minecraft:black_stained_glass_pane[east=true,west=true]");
	plot.type('<', "minecraft:oak_stairs[facing=north,half=top]"            );
	plot.type('>', "minecraft:oak_stairs[facing=south,half=top]"            );
	for (let ix=0; ix<5; ix++) {
		plot.setBlock(ix+6, 3, 27, 'x'); // glass
		plot.setBlock(ix+6, 2, 27, 'x'); // glass
		plot.setBlock(ix+6, 1, 27, '='); // wall
		plot.setBlock(ix+6, 1, 26, '>'); // inside desk
		plot.setBlock(ix+6, 1, 28, '<'); // outside desk

	}
	// doors - 1st floor recording studio
	plot.type('d', "minecraft:iron_door[half=upper,facing=east,hinge=left]" ); plot.setBlock(5, 2, 23, 'd');
	plot.type('D', "minecraft:iron_door[half=lower,facing=east,hinge=left]" ); plot.setBlock(5, 1, 23, 'D');
	plot.type('d', "minecraft:iron_door[half=upper,facing=east,hinge=right]"); plot.setBlock(5, 2, 24, 'd');
	plot.type('D', "minecraft:iron_door[half=lower,facing=east,hinge=right]"); plot.setBlock(5, 1, 24, 'D');
	plot.setBlock(4, 1, 23, '~'); plot.setBlock(6, 1, 23, '~');
	plot.setBlock(4, 1, 24, '~'); plot.setBlock(6, 1, 24, '~');
	// clear area for stairs
	for (let ix=0; ix<5; ix++) {
		for (let iy=0; iy<3; iy++) {
			plot.setBlock(6+ix, 5+iy, 16, '.');
			plot.setBlock(6+ix, 5+iy, 17, '.');
		}
	}
	// stairs (lower)
	plot.type('<', "minecraft:oak_stairs[facing=south]");
	for (let i=0; i<3; i++) {
		plot.setBlock(6, 1+i, 13+i, '<');
		plot.setBlock(7, 1+i, 13+i, '<');
	}
	// landing
	plot.type('F', "minecraft:polished_diorite_slab[type=top]");
	for (let iz=0; iz<2; iz++) {
		for (let ix=0; ix<2; ix++)
			plot.setBlock(6+ix, 3, 16+iz, 'F')
	}
	// stairs (upper)
	plot.type('<', "minecraft:oak_stairs[facing=east]");
	for (let i=0; i<4; i++) {
		plot.setBlock(8+i, 4+i, 16, '<');
		plot.setBlock(8+i, 4+i, 17, '<');
	}
	// storage under stairs (hall)
	plot.type('d', "minecraft:spruce_door[half=upper,facing=east]"); plot.setBlock(5, 2, 17, 'd');
	plot.type('D', "minecraft:spruce_door[half=lower,facing=east]"); plot.setBlock(5, 1, 17, 'D');
	plot.setBlock(4, 1, 17, '~');
	// storage under stairs (room)
	plot.type('d', "minecraft:spruce_door[half=upper,facing=south]"); plot.setBlock(10, 2, 15, 'd');
	plot.type('D', "minecraft:spruce_door[half=lower,facing=south]"); plot.setBlock(10, 1, 15, 'D');
	plot.setBlock(10, 1, 14, '~');
}



radio_lot_ground();
radio_lot_fence();
radio_building_back( -14, -14, 44, 16, 23);
radio_building_front(-14,   7, 17, 9, 21);
radio_path(12, 31, 5, 21);
radio_antenna(-11, 9, -11, 16);
radio_building_inside_walls_1st(-12, -12, 44, 38, 7);
