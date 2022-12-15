package com.poixson.backrooms.generators;

import static com.poixson.commonbukkit.utils.LocationUtils.RotateAround00;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.WorldInfo;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.commonbukkit.tools.BlockPlotter;
import com.poixson.utils.FastNoiseLiteD;
import com.poixson.utils.FastNoiseLiteD.NoiseType;
import com.poixson.utils.StringUtils;


// 771 | Crossroads
public class BackGen_771 extends BackroomsGenerator {

	public static final int ROAD_Y = 200;

	public static final Material ROAD_SURFACE    = Material.POLISHED_BLACKSTONE;
	public static final Material ROAD_WALL       = Material.POLISHED_BLACKSTONE_BRICK_WALL;
	public static final Material ROAD_WALL_DECOR = Material.CHISELED_POLISHED_BLACKSTONE;
	public static final Material CENTER_PILLAR   = Material.POLISHED_BLACKSTONE_WALL;
	public static final Material CENTER_ARCH     = Material.POLISHED_BLACKSTONE_STAIRS;

	protected final FastNoiseLiteD noiseRoadLights;

	public final Level_771_Populator crossPop;



	public BackGen_771(final BackroomsPlugin plugin) {
		super(plugin);
		// road lanterns
		this.noiseRoadLights = new FastNoiseLiteD();
		this.noiseRoadLights.setNoiseType(NoiseType.OpenSimplex2);
		this.noiseRoadLights.setFrequency(0.07);
		this.noiseRoadLights.setFractalOctaves(1);
		// populators
		this.crossPop = new Level_771_Populator(this.noiseRoadLights);
	}



	@Override
	public void generateSurface(
			final WorldInfo worldInfo, final Random random,
			final int chunkX, final int chunkZ, final ChunkData chunk) {
		final int seed = Long.valueOf(worldInfo.getSeed()).intValue();
		this.noiseRoadLights.setSeed(seed);
		final boolean centerX = (chunkX == 0 || chunkX == -1);
		final boolean centerZ = (chunkZ == 0 || chunkZ == -1);
		// world center
		if (centerX && centerZ) {
			this.generateRoadCenter(chunk, chunkX, chunkZ);
			this.generateCenterArch(chunk, chunkX, chunkZ, true);
			this.generateCenterArch(chunk, chunkX, chunkZ, false);
		} else
		// road
		if (centerX || centerZ) {
			this.generateRoad(chunk, chunkX, chunkZ);
		}
	}



	public void generateRoadCenter(final ChunkData chunk, final int chunkX, final int chunkZ) {
		final BlockPlotter plotter = new BlockPlotter(chunk);
		plotter.absY = ROAD_Y;
		plotter.type('x', ROAD_SURFACE);
		plotter.type('+', ROAD_WALL);
		final StringBuilder[][] matrix = plotter.getNewMatrix3D(5, 16, 16);
		int xx, zz;
		double v;
		// floor
		for (int z=0; z<16; z++) {
			zz = Math.abs((chunkZ * 16) + z) + chunkZ;
			for (int x=0; x<16; x++) {
				xx = Math.abs((chunkX * 16) + x) + chunkX;
				v = Math.sqrt( Math.pow((double)xx, 2.0) + Math.pow((double)zz, 2.0) );
				if (v < 15.5)
					matrix[0][z].setCharAt(x, 'x');
			}
		}
		// wall/fence
		for (int z=0; z<16; z++) {
			for (int x=0; x<16; x++) {
				if (matrix[0][z].charAt(x) != ' ')
					continue;
				if ( (z > 0  && matrix[0][z-1].charAt(x) != ' ') // north
				||   (z < 15 && matrix[0][z+1].charAt(x) != ' ') // south
				||   (x < 15 && matrix[0][z].charAt(x+1) != ' ') // east
				||   (x > 0  && matrix[0][z].charAt(x-1) != ' ') // west
				||   (z>0  && x<15 && matrix[0][z-1].charAt(x+1) != ' ') // north-east
				||   (z>0  && x>0  && matrix[0][z-1].charAt(x-1) != ' ') // north-west
				||   (z<15 && x>0  && matrix[0][z+1].charAt(x-1) != ' ') // south-west
				||   (z<15 && x<15 && matrix[0][z+1].charAt(x+1) != ' ') // south-east
				) {
					matrix[1][z].setCharAt(x, '+');
				}
			}
		}
		// place blocks
		final String axis = RotateAround00(chunkX, chunkZ);
		plotter.place3D(axis, matrix);
	}

	protected void generateCenterArch(final ChunkData chunk,
			final int chunkX, final int chunkZ, final boolean ab) {
		final int x, z;
		final String axis;
		final BlockFace dir;
		if (chunkZ == 0) {
			if (chunkX == 0) {
				if (ab) { x =  3; z = 15; axis = "Yxz"; dir = BlockFace.EAST;
				} else  { x = 15; z =  3; axis = "Yzx"; dir = BlockFace.SOUTH; }
			} else {
				if (ab) { x = 12; z = 15; axis = "YXz"; dir = BlockFace.WEST;
				} else  { x =  0; z =  3; axis = "Yzx"; dir = BlockFace.SOUTH; }
			}
		} else {
			if (chunkX == 0) {
				if (ab) { x =  3; z =  0; axis = "Yxz"; dir = BlockFace.EAST;
				} else  { x = 15; z = 12; axis = "YZx"; dir = BlockFace.NORTH; }
			} else {
				if (ab) { x = 12; z =  0; axis = "YXz"; dir = BlockFace.WEST;
				} else  { x =  0; z = 12; axis = "YZX"; dir = BlockFace.NORTH; }
			}
		}
		final BlockPlotter plotter = new BlockPlotter(chunk, x, ROAD_Y+1, z);
		plotter.type('@', ROAD_WALL_DECOR);
		plotter.type('|', CENTER_PILLAR);
		plotter.type('-', Material.POLISHED_BLACKSTONE_SLAB);
		plotter.type('=', Material.POLISHED_BLACKSTONE_SLAB, "top");
		plotter.type('#', Material.POLISHED_BLACKSTONE);
		// small arch
		{
			plotter.type('L', CENTER_ARCH, dir.getOppositeFace().toString());
			plotter.type('^', CENTER_ARCH, dir.toString()+",top");
			final StringBuilder[] matrix = plotter.getEmptyMatrix2D(5);
			matrix[4].append("  L#");
			matrix[3].append(" L^");
			matrix[2].append("L^");
			matrix[1].append("|");
			matrix[0].append("@");
			plotter.place2D("Y"+axis.charAt(1), matrix);
		}
		// large arch
		{
			plotter.absY += 5;
			final StringBuilder[][] matrix = plotter.getEmptyMatrix3D(8, 4);
			for (int i=0; i<8; i++) {
				matrix[i][3]
					.append(StringUtils.Repeat(i*2, ' '))
					.append("-#=");
			}
			matrix[7][3].setLength( matrix[7][3].length()-1 );
			plotter.place3D(axis, matrix);
		}
	}



	public void generateRoad(final ChunkData chunk, final int chunkX, final int chunkZ) {
		int x, z;
		final String axis;
		// road
		{
			if (       chunkX ==  0) { axis = "usw"; x =  3; z =  0;
			} else if (chunkX == -1) { axis = "use"; x = 12; z =  0;
			} else if (chunkZ ==  0) { axis = "uen"; x =  0; z =  3;
			} else if (chunkZ == -1) { axis = "ues"; x =  0; z = 12;
			} else return;
			final BlockPlotter plotter = new BlockPlotter(chunk, x, ROAD_Y, z);
			final StringBuilder[][] matrix = plotter.getEmptyMatrix3D(2, 16);
			plotter.type('#', ROAD_SURFACE);
			plotter.type('|', ROAD_WALL);
			for (int iz=0; iz<16; iz++) {
				matrix[1][iz].append("|");
				matrix[0][iz].append(" ###");
			}
			plotter.place3D(axis, matrix);
		}
		// pillars
		{
			if (       chunkX >  0) { if (chunkX % 2 == 1) return;
			} else if (chunkX < -1) { if (chunkX % 2 == 0) return;
			} else if (chunkZ >  0) { if (chunkZ % 2 == 1) return;
			} else if (chunkZ < -1) { if (chunkZ % 2 == 0) return; }
			final String axis2, dir;
			if (       chunkX ==  0) { axis2 = "unw"; x =  3; z =  6; dir = "west";
			} else if (chunkX == -1) { axis2 = "une"; x = 12; z =  6; dir = "east";
			} else if (chunkZ ==  0) { axis2 = "uwn"; x =  6; z =  3; dir = "north";
			} else if (chunkZ == -1) { axis2 = "uws"; x =  6; z = 12; dir = "south";
			} else return;
			int y = ROAD_Y;
			final BlockPlotter plotter = new BlockPlotter(chunk, x, 0, z);
			final StringBuilder[][] matrix = plotter.getEmptyMatrix3D(y, 2);
			plotter.type('x', Material.POLISHED_BLACKSTONE_BRICKS);
			plotter.type('L', Material.POLISHED_BLACKSTONE_BRICK_STAIRS, "top,"+dir);
			matrix[--y][1].append(" xxx"); matrix[y][0].append("  xx");
			matrix[--y][1].append(" xxx"); matrix[y][0].append("  xx");
			matrix[--y][1].append(" xxx"); matrix[y][0].append("  Lx");
			matrix[--y][1].append(" Lxx"); matrix[y][0].append("   x");
			matrix[--y][1].append("  xx"); matrix[y][0].append("   x");
			matrix[--y][1].append("  xx"); matrix[y][0].append("   L");
			matrix[--y][1].append("  Lx");
			for (int iy=0; iy<y; iy++)
				matrix[iy][1].append("   x");
			plotter.place3D(axis, matrix);
			if (chunkX == 0 || chunkX == -1) plotter.absZ += 3;
			if (chunkZ == 0 || chunkZ == -1) plotter.absX += 3;
			plotter.place3D(axis2, matrix);
		}
	}



	@Override
	public List<BlockPopulator> getDefaultPopulators(final World world) {
		return Arrays.asList(
			this.crossPop
		);
	}



}
