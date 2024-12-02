package com.poixson.backrooms.gens;

import static com.poixson.utils.MathUtils.MinMax;
import static com.poixson.utils.StringUtils.ReplaceInString;

import java.util.LinkedList;

import org.bukkit.Material;
import org.bukkit.generator.LimitedRegion;

import com.poixson.backrooms.BackroomsPop;
import com.poixson.backrooms.worlds.BackWorld_000;
import com.poixson.tools.abstractions.Tuple;
import com.poixson.tools.plotter.BlockPlotter;


// 1 | Basement
public class Pop_001 implements BackroomsPop {

	protected final BackWorld_000 world_000;
	protected final Gen_001   gen_001;



	public Pop_001(final BackWorld_000 world_000) {
		this.world_000 = world_000;
		this.gen_001   = world_000.gen_001;
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
		final int barrier     = this.gen_001.bedrock_barrier;
		final int subfloor    = this.gen_001.subfloor;
		final int well_size   = MinMax(this.gen_001.well_size, 3, 10);
		final int well_height = this.gen_001.well_height;
		// check for walls
		{
			final int y = level_y + barrier + subfloor + 2;
			for (int iz=0; iz<well_size; iz++) {
				for (int ix=0; ix<well_size; ix++) {
					if (!Material.AIR.equals( region.getType(x+ix, y+2, z+iz) ))
						return;
				}
			}
		}
		this.world_000.portal_001_well.addLocation(x, z);
		// build well
		{
			final BlockPlotter plot =
				(new BlockPlotter())
				.axis("se")
				.xyz(x, 0, z)
				.wd(well_size, well_size);
			plot.type('#', Material.BEDROCK);
			plot.type('.', Material.AIR  );
			final StringBuilder[] matrix = plot.getMatrix2D();
			switch (well_size) {
			case 3:
				matrix[0].append(" #" );
				matrix[1].append("#.#");
				matrix[2].append(" #" );
				break;
			case 4:
				matrix[0].append(" ##" );
				matrix[1].append("#..#");
				matrix[2].append("#..#");
				matrix[3].append(" ##" );
				break;
			case 5:
				matrix[0].append(" ###" );
				matrix[1].append("#...#");
				matrix[2].append("#...#");
				matrix[3].append("#...#");
				matrix[4].append(" ###" );
				break;
			case 6:
				matrix[0].append("  ##"  );
				matrix[1].append(" #..#" );
				matrix[2].append("#....#");
				matrix[3].append("#....#");
				matrix[4].append(" #..#" );
				matrix[5].append("  ##"  );
				break;
			case 7:
				matrix[0].append("  ###"  );
				matrix[1].append(" #...#" );
				matrix[2].append("#.....#");
				matrix[3].append("#.....#");
				matrix[4].append("#.....#");
				matrix[5].append(" #...#" );
				matrix[6].append("  ###"  );
				break;
			case 8:
				matrix[0].append("  ####"  );
				matrix[1].append(" #....#" );
				matrix[2].append("#......#");
				matrix[3].append("#......#");
				matrix[4].append("#......#");
				matrix[5].append("#......#");
				matrix[6].append(" #....#" );
				matrix[7].append("  ####"  );
				break;
			case 9:
				matrix[0].append("  #####"  );
				matrix[1].append(" #.....#" );
				matrix[2].append("#.......#");
				matrix[3].append("#.......#");
				matrix[4].append("#.......#");
				matrix[5].append("#.......#");
				matrix[6].append("#.......#");
				matrix[7].append(" #.....#" );
				matrix[8].append("  #####"  );
				break;
			case 10:
				matrix[0].append("   ####"   );
				matrix[1].append("  #....#"  );
				matrix[2].append(" #......#" );
				matrix[3].append("#........#");
				matrix[4].append("#........#");
				matrix[5].append("#........#");
				matrix[6].append("#........#");
				matrix[7].append(" #......# ");
				matrix[8].append("  #....#"  );
				matrix[9].append("   ####"   );
				break;
			default: throw new RuntimeException("Invalid well size: "+Integer.toString(well_size));
			}
			// well below ground
			final int h = barrier + subfloor + 1;
			for (int i=0; i<h; i++) {
				plot.y(level_y+i);
				plot.run(region, matrix);
			}
			plot.type('#', Material.MOSSY_STONE_BRICKS);
			plot.type('H', Material.AIR               );
			switch (well_size) {
			case 3:  ReplaceInString(matrix[2], "H", 0); break;
			case 4:  ReplaceInString(matrix[3], "H", 0); break;
			case 5:  ReplaceInString(matrix[4], "H", 0); break;
			case 6:  ReplaceInString(matrix[4], "H", 0); break;
			case 7:  ReplaceInString(matrix[5], "H", 0); break;
			case 8:  ReplaceInString(matrix[6], "H", 0); break;
			case 9:  ReplaceInString(matrix[7], "H", 0); break;
			case 10: ReplaceInString(matrix[7], "H", 0); break;
			default: throw new RuntimeException("Invalid well size: "+Integer.toString(well_size));
			}
			// well above ground
			for (int i=0; i<well_height; i++) {
				if (i == 1)
					plot.type('H', "minecraft:ladder[facing=south]");
				plot.y(level_y+h+i);
				plot.run(region, matrix);
			}
		}
	}



}
