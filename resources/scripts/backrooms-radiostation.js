
importClass(Packages.org.bukkit.Material);

importClass(Packages.com.poixson.commonmc.tools.plotter.PlotterFactory)



function radio_fence() {
	let plot = (new PlotterFactory())
		.placer(region)
		.axis("use")
		.whd(48, 6, 48)
		.build();
	plot.type('=', Material.COPPER_BLOCK);
	plot.type('-', Material.CUT_COPPER_SLAB);
	plot.type('I', Material.IRON_BARS);
	plot.type('g', Material.GRAVEL);
	let matrix = plot.getMatrix3D();
	// gravel floor
	for (let z=0; z<48; z++)
		matrix[0][z].append('g'.repeat(48));
	// fence
	matrix[5][47].append('-'.repeat(48));
	matrix[4][47].append('I'.repeat(48));
	matrix[3][47].append('I'.repeat(48));
	matrix[2][47].append('I'.repeat(48));
	matrix[1][47].append('='.repeat(48));
	matrix[5][0].append('-'.repeat(21)).append("      ").append('-'.repeat(21));
	matrix[4][0].append('I'.repeat(21)).append("      ").append('I'.repeat(21));
	matrix[3][0].append('I'.repeat(21)).append("      ").append('I'.repeat(21));
	matrix[2][0].append('I'.repeat(21)).append("      ").append('I'.repeat(21));
	matrix[1][0].append('='.repeat(21)).append("      ").append('='.repeat(21));
	for (let z=1; z<47; z++) {
		matrix[5][z].append("-").append(" ".repeat(46)).append("-");
		matrix[4][z].append("I").append(" ".repeat(46)).append("I");
		matrix[3][z].append("I").append(" ".repeat(46)).append("I");
		matrix[2][z].append("I").append(" ".repeat(46)).append("I");
		matrix[1][z].append("=").append(" ".repeat(46)).append("=");
	}
	plot.run();
}



function radio_antenna(x, y, z, size) {
	let block_type = Material.IRON_BLOCK;
	let size_half = Math.floor(size * 0.5);
	let iy = 0;
	let inset       = 0;
	let inset_micro = 1;
	let inset_level = 0;
	while (true) {
		if (inset >= size_half) {
			inset    = inset_level + 1;
			inset_micro = 1;
			inset_level++;
			// top of tower
			if (inset_level > size_half) {
				for (let i=0; i<5; i++)
					region.setType(x+size_half, y+iy+i, z+size_half, block_type);
				break;
			}
			// flat square
			for (let i=0; i<size_half-inset_level; i++) {
				region.setType(x+     inset_level+i, y+iy-1, z+     inset_level-1, block_type);
				region.setType(x+size-inset_level-i, y+iy-1, z+     inset_level-1, block_type);
				region.setType(x+     inset_level+i, y+iy-1, z+size-inset_level+1, block_type);
				region.setType(x+size-inset_level-i, y+iy-1, z+size-inset_level+1, block_type);
				region.setType(x+     inset_level-1, y+iy-1, z+     inset_level+i, block_type);
				region.setType(x+size-inset_level+1, y+iy-1, z+     inset_level+i, block_type);
				region.setType(x+     inset_level-1, y+iy-1, z+size-inset_level-i, block_type);
				region.setType(x+size-inset_level+1, y+iy-1, z+size-inset_level-i, block_type);
			}
			// inside cross
			let w = (size - inset_level) + 2;
			for (let i=inset_level-1; i<w; i++) {
				region.setType(x+        i, y+iy-1, z+size_half, block_type);
				region.setType(x+size_half, y+iy-1, z+        i, block_type);
			}
		}
		if (inset_micro >= 3) {
			inset_micro = 0;
			inset++;
		}
		region.setType(x+     inset,       y+iy, z+     inset,       block_type);
		region.setType(x+size-inset,       y+iy, z+     inset,       block_type);
		region.setType(x+     inset,       y+iy, z+size-inset,       block_type);
		region.setType(x+size-inset,       y+iy, z+size-inset,       block_type);
		region.setType(x+     inset_level, y+iy, z+     inset_level, block_type);
		region.setType(x+size-inset_level, y+iy, z+     inset_level, block_type);
		region.setType(x+     inset_level, y+iy, z+size-inset_level, block_type);
		region.setType(x+size-inset_level, y+iy, z+size-inset_level, block_type);
		iy++;
		inset_micro++;
	}
}



radio_fence();
radio_antenna(0, 122, 0, 16);
