package com.poixson.backrooms.gens;

import java.util.LinkedList;

import org.bukkit.Material;
import org.bukkit.generator.LimitedRegion;

import com.poixson.backrooms.BackroomsPop;
import com.poixson.backrooms.worlds.Level_000;
import com.poixson.tools.abstractions.Tuple;
import com.poixson.tools.plotter.BlockPlotter;


// 1 | Basement
public class Pop_001 implements BackroomsPop {

	protected final Level_000 level_000;
	protected final Gen_001   gen_001;



	public Pop_001(final Level_000 level_000) {
		this.level_000 = level_000;
		this.gen_001   = level_000.gen_001;
	}



	@Override
	public void populate(final LinkedList<Tuple<BlockPlotter, StringBuilder[][]>> plots,
			final LimitedRegion region, final int chunkX, final int chunkZ) {
		if (!this.gen_001.enable_gen) return;
		int xx, zz;
		double value;
		for (int iz=0; iz<16; iz++) {
			zz = (chunkZ * 16) + iz;
			for (int ix=0; ix<16; ix++) {
				xx = (chunkX * 16) + ix;
				value = this.gen_001.noiseWell.getNoise(xx, zz);
				if (value > this.gen_001.noiseWell.getNoise(xx+1, zz)
				&&  value > this.gen_001.noiseWell.getNoise(xx-1, zz)
				&&  value > this.gen_001.noiseWell.getNoise(xx, zz+1)
				&&  value > this.gen_001.noiseWell.getNoise(xx, zz-1) ) {
					this.generateWell(region, xx, zz);
					return;
				}
			}
		}
	}



	protected void generateWell(final LimitedRegion region,
			final int x, final int z) {
		final int level_y     = this.gen_001.level_y;
		final int subfloor    = this.gen_001.subfloor;
		final int well_size   = this.gen_001.well_size;
		final int well_height = this.gen_001.well_height;
		// check for walls
		{
			final int y = level_y + subfloor + 2;
			final int halfL = (int) Math.floor((double)well_size / 2);
			final int halfH = (int) Math.ceil( (double)well_size / 2);
			for (int iz=0-halfL; iz<halfH; iz++) {
				for (int ix=0-halfL; ix<halfH; ix++) {
					if (!Material.AIR.equals( region.getType(x+ix, y+1, z+iz) ))
						return;
				}
			}
		}
		// build well
		{
			this.level_000.portal_1_well.add(x, z);
			final BlockPlotter plot =
				(new BlockPlotter())
				.axis("use")
				.xyz(x, level_y, z)
				.wd(well_size, well_size)
				.h(subfloor+well_height+5);
			plot.type('#', Material.BEDROCK);
			plot.type('x', Material.MOSSY_STONE_BRICKS);
			plot.type('.', Material.AIR  );
			final StringBuilder[][] matrix = plot.getMatrix3D();
			matrix[0][1].append(" ...");
			matrix[0][2].append(" ...");
			matrix[0][3].append(" ...");
			int iy = 1;
			// well below floor
			for (int i=0; i<subfloor+1; i++) {
				matrix[iy+i][0].append(" ###" );
				matrix[iy+i][1].append("#...#");
				matrix[iy+i][2].append("#...#");
				matrix[iy+i][3].append("#...#");
				matrix[iy+i][4].append(" ###" );
			}
			iy += subfloor + 1;
			// well above floor
			for (int i=0; i<well_height; i++) {
				matrix[iy+i][0].append(" xxx" );
				matrix[iy+i][1].append("x...x");
				matrix[iy+i][2].append("x...x");
				matrix[iy+i][3].append("x...x");
				matrix[iy+i][4].append(" xxx" );
			}
			iy += well_height;
			// clear above well
			for (int i=0; i<3; i++) {
				for (int iz=0; iz<5; iz++)
					matrix[iy+i][iz].append(".....");
			}
			plot.run(region, matrix);
		}
	}



}
