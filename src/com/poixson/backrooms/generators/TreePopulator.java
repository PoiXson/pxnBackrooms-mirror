package com.poixson.backrooms.generators;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

import com.poixson.utils.FastNoiseLiteD;


public class TreePopulator extends BlockPopulator {

	//TODO
	public static final Material TRUNK  = Material.BIRCH_LOG;
	public static final Material LEAVES = Material.BIRCH_LEAVES;

	protected final FastNoiseLiteD noise;

	protected final int chunkY;



	public TreePopulator(final FastNoiseLiteD noise, final int chunkY) {
		this.noise  = noise;
		this.chunkY  = chunkY;
	}



	@Override
	public void populate(final WorldInfo world, final Random rnd,
	final int chunkX, final int chunkZ, final LimitedRegion region) {
		int x, z;
		for (int iz=0; iz<16; iz++) {
			for (int ix=0; ix<16; ix++) {
				x = ix + (chunkX * 16);
				z = iz + (chunkZ * 16);
				if (this.isTree(x, z))
					this.build(x, z, region);
			}
		}
	}



	public boolean isTree(final int x, final int z) {
		final double current = this.noise.getNoise(x, z);
		for (int xx=-1; xx<2; xx++) {
			for (int zz=-1; zz<2; zz++) {
				if (xx == 0 && zz == 0) continue;
				if (this.noise.getNoise(x+xx, z+zz) > current)
					return false;
			}
		}
		return true;
	}



	public int getTreeSize(final int x, final int z) {
		return (int)Math.abs(Math.round(this.noise.getNoise(x*555.0, z*555.0) * 12.0));
	}



	public void build(final int x, final int z, final LimitedRegion region) {
		// tree size
		final int size = this.getTreeSize(x, z);
		final int size_half = (size / 2) + 3;
		final int size_tree = size + 6;
		// find surface
		int y = 0;
		{
			for (int yy=0; yy<10; yy++) {
				if (Material.AIR.equals(region.getType(x, this.chunkY+yy, z))) {
					y = this.chunkY + yy;
					break;
				}
			}
			if (y == 0)
				return;
		}
		// leaves
		{
			int xx, yy, zz;
			double ax, ay, az;
			double nx, ny, nz;
			double value;
			for (int iz=0; iz<size_tree; iz++) {
				for (int iy=0; iy<size_tree; iy++) {
					for (int ix=0; ix<size_tree; ix++) {
						ax = 1.0 - (Math.abs(ix - size_half) / (double)size_half);
						ay = 1.0 - (Math.abs(iy - size_half) / (double)size_half);
						az = 1.0 - (Math.abs(iz - size_half) / (double)size_half);
						nx = ix + (x * 55);
						nz = iz + (z * 55);
						ny = iy * 211;
						value = ((ax + ay + az) / 2.5) -
								(double)Math.pow( this.noise.getNoise(nx, ny, nz), 2.0 );
						if (value > 0.5) {
							xx = (x + ix) - size_half;
							zz = (z + iz) - size_half;
							yy = y + iy + 3;
							if (Material.AIR.equals(region.getType(xx, yy, zz))) {
								region.setType(xx, yy, zz, LEAVES);
								final BlockData block = region.getBlockData(xx, yy, zz);
								final Leaves leaves = (Leaves) block;
								leaves.setPersistent(true);
								region.setBlockData(xx, yy, zz, block);
							}
						}
					}
				}
			}
		}
		// trunk
		for (int iy=0; iy<size_tree; iy++) {
			region.setType(x, y+iy, z, TRUNK);
		}
	}



}
