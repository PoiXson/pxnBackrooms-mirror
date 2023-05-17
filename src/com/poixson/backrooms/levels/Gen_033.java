package com.poixson.backrooms.levels;

import static com.poixson.backrooms.levels.Level_033.ENABLE_GEN_033;

import java.util.LinkedList;

import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.commonmc.tools.plotter.BlockPlotter;


// 33 | Run For Your Life!
public class Gen_033 extends GenBackrooms {



	public Gen_033(final LevelBackrooms backlevel,
			final int level_y, final int level_h) {
		super(backlevel, level_y, level_h);
	}



	@Override
	public void generate(final PreGenData pregen, final ChunkData chunk,
			final LinkedList<BlockPlotter> plots, final int chunkX, final int chunkZ) {
		if (!ENABLE_GEN_033) return;
		for (int z=0; z<16; z++) {
			for (int x=0; x<16; x++) {
//				final int xx = (chunkX * 16) + x;
//				final int zz = (chunkZ * 16) + z;
				switch (x) {
				case 0:
				case 15:
					for (int y=0; y<this.level_h; y++) {
						chunk.setBlock(x, y+this.level_y+2, z, Material.BEDROCK);
					}
					break;
				case 1:
				case 14:
					for (int y=0; y<this.level_h; y++) {
						chunk.setBlock(x, y+this.level_y+2, z, Material.BLACKSTONE);
					}
					break;
				default: break;
				}
			} // end x
		} // end z
	}



}
