package com.poixson.backrooms.levels;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

import com.poixson.backrooms.BackroomsPlugin;


// 309 | Radio Station
public class Pop_309 extends BlockPopulator {



	public Pop_309() {
	}



	@Override
	public void populate(final WorldInfo world, final Random rnd,
	final int chunkX, final int chunkZ, final LimitedRegion region) {
		if (chunkX == 0 && chunkZ == 0)
			this.populate00(region);
	}



	public void populate00(final LimitedRegion region) {
		int y = Integer.MIN_VALUE;
		for (int i=0; i<10; i++) {
			if (Material.AIR.equals(region.getType(0, i+Gen_309.PATH_Y, 15))) {
				y = i + Gen_309.PATH_Y;
				break;
			}
		}
		if (y == Integer.MIN_VALUE) {
			BackroomsPlugin.log.warning("Failed to generate level 309 building; unknown y point.");
			return;
		}
		// doorstep
		for (int iz=16; iz<18; iz++) {
			for (int ix=0; ix<2; ix++) {
				region.setType(ix, y-1, iz, Material.STONE);
			}
		}
		// radio station building
		for (int iz=0; iz<16; iz++) {
			for (int ix=0; ix<16; ix++) {
				// subfloor
				for (int iy=1; iy<4; iy++) {
					region.setType(ix, y-iy, iz, Material.STONE);
				}
				region.setType(ix, y, iz, Material.AIR);
				region.setType(ix, y+1, iz, Material.AIR);
				// wall
				if (iz == 15) {
					for (int iy=0; iy<8; iy++) {
						if (ix == 0 && (iy == 0 || iy == 1))
							continue;
						region.setType(ix, y+iy, iz, Material.STONE_BRICKS);
					}
				}
				// floor
				
				
				
				
			}
		}
	}



}
