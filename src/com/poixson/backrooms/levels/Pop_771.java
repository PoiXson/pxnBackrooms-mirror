package com.poixson.backrooms.generators;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.type.Wall;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

import com.poixson.commonbukkit.tools.BlockPlotter;
import com.poixson.utils.FastNoiseLiteD;


public class Level_771_Populator extends BlockPopulator {

	public static final int ROAD_Y = BackGen_771.ROAD_Y;

	protected final FastNoiseLiteD noiseRoadLights;



	public Level_771_Populator(final FastNoiseLiteD noiseRoadLights) {
		super();
		this.noiseRoadLights = noiseRoadLights;
	}



	@Override
	public void populate(final WorldInfo world, final Random rnd,
	final int chunkX, final int chunkZ, final LimitedRegion region) {
		// connect walls/fences
		{
			double value, valueN, valueS, valueE, valueW;
			Wall wall;
			int xx, zz;
			final int y = ROAD_Y;
			for (int z=0; z<16; z++) {
				zz = (chunkZ * 16) + z;
				for (int x=0; x<16; x++) {
					xx = (chunkX * 16) + x;
					if (this.isBlockWall(xx, y+1, zz, region)) {
						wall = (Wall) region.getBlockData(xx, y+1, zz);
						valueN = 0.5;
						valueS = 0.5;
						valueE = 0.5;
						valueW = 0.5;
						// north
						if (this.isBlockSolid(xx, y+1, zz-1, region)) {
							wall.setHeight(BlockFace.NORTH, Wall.Height.LOW);
							valueN = this.noiseRoadLights.getNoise(xx, zz-1);
						}
						// south
						if (this.isBlockSolid(xx, y+1, zz+1, region)) {
							wall.setHeight(BlockFace.SOUTH, Wall.Height.LOW);
							valueS = this.noiseRoadLights.getNoise(xx, zz+1);
						}
						// east
						if (this.isBlockSolid(xx+1, y+1, zz, region)) {
							wall.setHeight(BlockFace.EAST, Wall.Height.LOW);
							valueE = this.noiseRoadLights.getNoise(xx+1, zz);
						}
						// west
						if (this.isBlockSolid(xx-1, y+1, zz, region)) {
							wall.setHeight(BlockFace.WEST, Wall.Height.LOW);
							valueW = this.noiseRoadLights.getNoise(xx-1, zz);
						}
						// lanterns
						if (Material.AIR.equals(region.getType(xx, y+2, zz))) {
							value = this.noiseRoadLights.getNoise(xx, zz);
							if (value > valueN
							&&  value > valueS
							&&  value > valueE
							&&  value > valueW) {
								wall.setUp(true);
								region.setType(xx, y+2, zz, Material.LANTERN);
							}
							region.setBlockData(xx, y+1, zz, wall);
						}
					}
				}
			}
		}
		// center lamp
		if (chunkX == 0 && chunkZ == 0) {
			final BlockPlotter plotter = new BlockPlotter(region, -3, ROAD_Y+5, -3);
			final StringBuilder[][] matrix = plotter.getEmptyMatrix3D(10, 6);
			plotter.type('|', Material.CHAIN);
			plotter.type('L', Material.REDSTONE_LAMP, "on");
			plotter.type('R', Material.REDSTONE_BLOCK);
			plotter.type('v', Material.SCULK_VEIN);
			plotter.type('!', Material.LIGHTNING_ROD);
			matrix[9][2].append("  !!  ");
			matrix[9][3].append("  !!  ");
			for (int i=3; i<8; i++) {
				matrix[i][2].append("  ||  "); matrix[i][3].append("  ||  ");
			}
			matrix[2][0].append("  vv  "); matrix[2][1].append(" vLLv "); matrix[2][2].append("vLRRLv"); matrix[2][3].append("vLRRLv"); matrix[2][4].append(" vLLv "); matrix[2][5].append("  vv  ");
			matrix[1][0].append(""      ); matrix[1][1].append("  vv  "); matrix[1][2].append(" vLLv "); matrix[1][3].append(" vLLv "); matrix[1][4].append("  vv  "); matrix[1][5].append(""      );
			matrix[0][0].append(""      ); matrix[0][1].append(""      ); matrix[0][2].append("  vv  "); matrix[0][3].append("  vv  "); matrix[0][4].append(""      ); matrix[0][5].append(""      );
			plotter.place3D("YZX", matrix);
			// fix vines at center
		}
		if (chunkX == 0 && chunkZ == 0) {
			int yy;
			MultipleFacing data;
			boolean changed;
			for (int z=-3; z<3; z++) {
				for (int x=-3; x<3; x++) {
					for (int y=5; y<8; y++) {
						yy = ROAD_Y + y;
						if (Material.SCULK_VEIN.equals(region.getType(x, yy, z))) {
							changed = false;
							data = (MultipleFacing) region.getBlockData(x, yy, z);
							// above
							if (this.isBlockSolid(x, yy+1, z, region)) {
								changed = true; data.setFace(BlockFace.UP, true);
							}
							// below
							if (this.isBlockSolid(x, yy-1, z, region)) {
								changed = true; data.setFace(BlockFace.DOWN, true);
							}
							// north
							if (this.isBlockSolid(x, yy, z-1, region)) {
								changed = true; data.setFace(BlockFace.NORTH, true);
							}
							// south
							if (this.isBlockSolid(x, yy, z+1, region)) {
								changed = true; data.setFace(BlockFace.SOUTH, true);
							}
							// east
							if (this.isBlockSolid(x+1, yy, z, region)) {
								changed = true; data.setFace(BlockFace.EAST, true);
							}
							// west
							if (this.isBlockSolid(x-1, yy, z, region)) {
								changed = true; data.setFace(BlockFace.WEST, true);
							}
							if (changed)
								region.setBlockData(x,  yy, z, data);
						}
					}
				}
			}
		}
	}

	public boolean isBlockSolid(final int x, final int y, final int z, final LimitedRegion region) {
		final Material type = region.getType(x, y, z);
		switch (type) {
		case SCULK_VEIN: return false;
		default: break;
		}
		return type.isSolid();
	}
	public boolean isBlockWall(final int x, final int y, final int z, final LimitedRegion region) {
		final Material type = region.getType(x, y, z);
		switch (type) {
		case COBBLESTONE_WALL:
		case STONE_BRICK_WALL:
		case MOSSY_COBBLESTONE_WALL:
		case MOSSY_STONE_BRICK_WALL:
		case SANDSTONE_WALL:
		case RED_SANDSTONE_WALL:
		case COBBLED_DEEPSLATE_WALL:
		case DEEPSLATE_BRICK_WALL:
		case DEEPSLATE_TILE_WALL:
		case POLISHED_DEEPSLATE_WALL:
		case BLACKSTONE_WALL:
		case POLISHED_BLACKSTONE_WALL:
		case POLISHED_BLACKSTONE_BRICK_WALL:
		case BRICK_WALL:
		case MUD_BRICK_WALL:
		case ANDESITE_WALL:
		case DIORITE_WALL:
		case GRANITE_WALL:
		case NETHER_BRICK_WALL:
		case RED_NETHER_BRICK_WALL:
		case END_STONE_BRICK_WALL:
		case PRISMARINE_WALL:
			return true;
		default: break;
		}
		return false;
	}



}
