package com.poixson.backrooms.gens;

import static com.poixson.backrooms.worlds.Level_000.ENABLE_GEN_001;
import static com.poixson.backrooms.worlds.Level_000.SUBFLOOR;

import java.util.LinkedList;

import org.bukkit.Material;
import org.bukkit.generator.LimitedRegion;

import com.poixson.backrooms.BackroomsPop;
import com.poixson.backrooms.worlds.Level_000;
import com.poixson.tools.plotter.BlockPlotter;
import com.poixson.tools.plotter.PlotterFactory;


public class Pop_001 implements BackroomsPop {

	public static final int WELL_SIZE   = 5;
	public static final int WELL_HEIGHT = 2;

	protected final Level_000 level0;
	protected final Gen_001   gen;



	public Pop_001(final Level_000 level0) {
		this.level0 = level0;
		this.gen    = level0.gen_001;
	}



	@Override
	public void populate(final int chunkX, final int chunkZ,
	final LimitedRegion region, final LinkedList<BlockPlotter> plots) {
		if (!ENABLE_GEN_001) return;
		int xx, zz;
		double value;
		for (int iz=0; iz<16; iz++) {
			for (int ix=0; ix<16; ix++) {
				xx = (chunkX * 16) + ix;
				zz = (chunkZ * 16) + iz;
				value = this.gen.noiseWell.getNoise(xx, zz);
				if (value > this.gen.noiseWell.getNoise(xx+1, zz)
				&&  value > this.gen.noiseWell.getNoise(xx-1, zz)
				&&  value > this.gen.noiseWell.getNoise(xx, zz+1)
				&&  value > this.gen.noiseWell.getNoise(xx, zz-1) ) {
					this.generateWell(region, xx, zz);
					return;
				}
			}
		}
	}



	protected void generateWell(final LimitedRegion region,
			final int x, final int z) {
		// check for walls
		{
			final int y = this.gen.level_y + SUBFLOOR + 2;
			final int halfL = (int) Math.floor((double)WELL_SIZE / 2);
			final int halfH = (int) Math.ceil( (double)WELL_SIZE / 2);
			for (int iz=0-halfL; iz<halfH; iz++) {
				for (int ix=0-halfL; ix<halfH; ix++) {
					if (!Material.AIR.equals( region.getType(x+ix, y+1, z+iz) ))
						return;
				}
			}
		}
		// build well
		{
			this.level0.portal_1_to_771.add(x, z);
			final BlockPlotter plot =
				(new PlotterFactory())
				.placer(region)
				.axis("use")
				.xyz(x, this.gen.level_y, z)
				.wd(WELL_SIZE, WELL_SIZE)
				.h(SUBFLOOR+WELL_HEIGHT+5)
				.build();
			plot.type('#', Material.BEDROCK);
			plot.type('x', Material.MOSSY_STONE_BRICKS);
			plot.type('.', Material.AIR  );
			final StringBuilder[][] matrix = plot.getMatrix3D();
			matrix[0][1].append(" ...");
			matrix[0][2].append(" ...");
			matrix[0][3].append(" ...");
			int iy = 1;
			// well below floor
			for (int i=0; i<SUBFLOOR+1; i++) {
				matrix[iy+i][0].append(" ###" );
				matrix[iy+i][1].append("#...#");
				matrix[iy+i][2].append("#...#");
				matrix[iy+i][3].append("#...#");
				matrix[iy+i][4].append(" ###" );
			}
			iy += SUBFLOOR + 1;
			// well above floor
			for (int i=0; i<WELL_HEIGHT; i++) {
				matrix[iy+i][0].append(" xxx" );
				matrix[iy+i][1].append("x...x");
				matrix[iy+i][2].append("x...x");
				matrix[iy+i][3].append("x...x");
				matrix[iy+i][4].append(" xxx" );
			}
			iy += WELL_HEIGHT;
			// clear above well
			for (int i=0; i<3; i++) {
				for (int iz=0; iz<5; iz++)
					matrix[iy+i][iz].append(".....");
			}
			plot.run();
		}
	}



}
