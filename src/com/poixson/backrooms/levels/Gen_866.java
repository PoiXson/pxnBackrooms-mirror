package com.poixson.backrooms.levels;

import java.util.Map;

import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.tools.dao.Dxy;
import com.poixson.utils.FastNoiseLiteD;


// 866 | Dirtfield
public class Gen_866 extends GenBackrooms {

	public final int subfloor;

	// noise
	protected final FastNoiseLiteD noiseField;

	protected int rndLast = 0;



	public Gen_866(final BackroomsPlugin plugin, final int level_y, final int level_h,
			final int subfloor) {
		super(plugin, level_y, level_h);
		this.subfloor   = subfloor;
		// field
		this.noiseField = this.register(new FastNoiseLiteD());
		this.noiseField.setFrequency(0.006);
		this.noiseField.setFractalOctaves(1);
	}



	@Override
	public void generate(final Map<Dxy, ? extends PreGenData> datamap,
			final ChunkData chunk, final int chunkX, final int chunkZ) {
		for (int z=0; z<16; z++) {
			for (int x=0; x<16; x++) {
				final int xx = (chunkX * 16) + x;
				final int zz = (chunkZ * 16) + z;
/*
				// subfloor
				chunk.setBlock(x, 0, z, Material.BEDROCK);
				chunk.setBlock(x, 1, z, Material.ACACIA_PLANKS);
				chunk.setBlock(x, 2, z, Material.SMOOTH_RED_SANDSTONE);
				final double value = this.noiseField.getNoise(xx, zz);
//				final double mx = ((double)xx) + value;
//				final double mz = ((double)zz) + (value * 5.0);
//				final double value2 = this.noiseField.getNoise(mx, z) + 1.0;
				final double h = value * 1.2;
				final int ih = (int) Math.floor(h);
				final double hh = h - ((double)ih);
				final boolean isHalf = (hh > 0.6);
				int y = this.level_y + 3;
				for (int iy=0; iy<ih; iy++) {
					chunk.setBlock(x, y+iy, z, Material.SMOOTH_RED_SANDSTONE);
				}
				y += ih;
				if (isHalf) {
					chunk.setBlock(x, y, z, Material.SMOOTH_RED_SANDSTONE_SLAB);
				} else {
//					final int rnd = NumberUtils.GetNewRandom(0, 999, this.rndLast) % 100;
//					this.rndLast = rnd;
//					if (rnd < 2) {
//						chunk.setBlock(x, y, z, Material.DEAD_BUSH);
//					} else
//					if (rnd < 5) {
//						chunk.setBlock(x, y, z, Material.GRASS);
//					}
				}
*/
			} // end x
		} // end z
	}



}
