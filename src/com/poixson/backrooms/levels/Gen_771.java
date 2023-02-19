package com.poixson.backrooms.levels;

import static com.poixson.commonmc.utils.LocationUtils.AxToFace;
import static com.poixson.commonmc.utils.LocationUtils.FaceToAx;
import static com.poixson.commonmc.utils.LocationUtils.Rotate;
import static com.poixson.commonmc.utils.LocationUtils.ValueToFaceQuarter;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.commonmc.tools.BlockPlotter;
import com.poixson.utils.FastNoiseLiteD;
import com.poixson.utils.StringUtils;


// 771 | Crossroads
public class Gen_771 extends GenBackrooms {

	public static final boolean ENABLE_GENERATE = true;

	public static final Material ROAD_SURFACE    = Material.POLISHED_BLACKSTONE;
	public static final Material ROAD_WALL       = Material.POLISHED_BLACKSTONE_BRICK_WALL;
	public static final Material ROAD_WALL_DECOR = Material.CHISELED_POLISHED_BLACKSTONE;
	public static final Material CENTER_PILLAR   = Material.POLISHED_BLACKSTONE_WALL;
	public static final Material CENTER_ARCH     = Material.POLISHED_BLACKSTONE_STAIRS;

	// noise
	protected final FastNoiseLiteD noiseRoadLights;



	public Gen_771(final BackroomsPlugin plugin,
			final int level_y, final int level_h) {
		super(plugin, level_y, level_h);
		// road lanterns
		this.noiseRoadLights = this.register(new FastNoiseLiteD());
		this.noiseRoadLights.setFrequency(0.07);
		this.noiseRoadLights.setFractalOctaves(1);
	}



	@Override
	public void generate(final PreGenData pregen,
			final ChunkData chunk, final int chunkX, final int chunkZ) {
		if (!ENABLE_GENERATE) return;
		final boolean centerX = (chunkX == 0 || chunkX == -1);
		final boolean centerZ = (chunkZ == 0 || chunkZ == -1);
		// world center
		if (centerX && centerZ) {
			final BlockFace quarter = ValueToFaceQuarter(chunkX, chunkZ);
			this.generateCenterRoad(chunk, quarter);
			this.generateCenterArch(chunk, quarter);
			this.generateCenterLamp(chunk, quarter);
		} else
		// road
		if (centerX || centerZ) {
			this.generateRoad(chunk, chunkX, chunkZ);
		}
	}



	// -------------------------------------------------------------------------------
	// world center



	protected void generateCenterArch(final ChunkData chunk, final BlockFace quarter) {
		final String axis = FaceToAx(quarter);
		switch (quarter) {
		case NORTH_EAST:
			this.generateCenterArch(chunk, axis.charAt(0), axis.charAt(1), 15, 15);
			this.generateCenterArch(chunk, axis.charAt(1), axis.charAt(0),  0,  0);
			break;
		case NORTH_WEST:
			this.generateCenterArch(chunk, axis.charAt(0), axis.charAt(1),  0, 15);
			this.generateCenterArch(chunk, axis.charAt(1), axis.charAt(0), 15,  0);
			break;
		case SOUTH_EAST:
			this.generateCenterArch(chunk, axis.charAt(0), axis.charAt(1), 15,  0);
			this.generateCenterArch(chunk, axis.charAt(1), axis.charAt(0),  0, 15);
			break;
		case SOUTH_WEST:
			this.generateCenterArch(chunk, axis.charAt(0), axis.charAt(1),  0,  0);
			this.generateCenterArch(chunk, axis.charAt(1), axis.charAt(0), 15, 15);
			break;
		default: throw new RuntimeException("Unknown quarter: " + quarter.toString());
		}
	}
	protected void generateCenterArch(final ChunkData chunk,
			final char axisA, final char axisB, final int x, final int z) {
		final BlockPlotter plotter = new BlockPlotter(chunk, x, this.level_y+this.level_h+1, z);
		plotter.type('@', ROAD_WALL_DECOR);
		plotter.type('|', CENTER_PILLAR);
		plotter.type('-', Material.POLISHED_BLACKSTONE_SLAB);
		plotter.type('=', Material.POLISHED_BLACKSTONE_SLAB, "top");
		plotter.type('#', Material.POLISHED_BLACKSTONE);
		// small arch
		{
			final BlockFace direction = AxToFace(axisA);
			plotter.type('L', CENTER_ARCH, direction.getOppositeFace().toString().toLowerCase());
			plotter.type('^', CENTER_ARCH, direction.toString().toLowerCase()+",top");
			final StringBuilder[] matrix = plotter.getEmptyMatrix2D(5);
			matrix[4].append("#L"  );
			matrix[3].append(" ^L" );
			matrix[2].append("  ^L");
			matrix[1].append("   |");
			matrix[0].append("   @");
			plotter.place2D("u"+axisA, matrix);
		}
		// large arch
		{
			plotter.setY(plotter.getY() + 5);
			final StringBuilder[] matrix = plotter.getEmptyMatrix2D(8);
			for (int i=0; i<8; i++) {
				matrix[i]
					.append(StringUtils.Repeat(i*2, ' '))
					.append("-#=");
			}
			matrix[7].setLength( matrix[7].length()-1 );
			plotter.place2D("u"+Rotate(axisB, 0.5), matrix);
		}
	}



	protected void generateCenterRoad(final ChunkData chunk, final BlockFace quarter) {
		final BlockPlotter plotter = new BlockPlotter(chunk, this.level_y+this.level_h);
		final StringBuilder[][] matrix = plotter.getEmptyMatrix3D(5, 16);
		plotter.type('#', ROAD_SURFACE);
		plotter.type('+', ROAD_WALL, "autoface");
		switch (quarter) {
		case NORTH_EAST: plotter.setX( 0); plotter.setZ(15); break;
		case NORTH_WEST: plotter.setX(15); plotter.setZ(15); break;
		case SOUTH_EAST: plotter.setX( 0); plotter.setZ( 0); break;
		case SOUTH_WEST: plotter.setX(15); plotter.setZ( 0); break;
		default: throw new RuntimeException("Unknown quarter: " + quarter.toString());
		}
		matrix[0][ 0].append("################"); matrix[1][ 0].append("                ");
		matrix[0][ 1].append("################"); matrix[1][ 1].append("                ");
		matrix[0][ 2].append("################"); matrix[1][ 2].append("                ");
		matrix[0][ 3].append("############### "); matrix[1][ 3].append("                ");
		matrix[0][ 4].append("############### "); matrix[1][ 4].append("               +");
		matrix[0][ 5].append("##############  "); matrix[1][ 5].append("              ++");
		matrix[0][ 6].append("##############  "); matrix[1][ 6].append("              + ");
		matrix[0][ 7].append("#############   "); matrix[1][ 7].append("             ++ ");
		matrix[0][ 8].append("#############   "); matrix[1][ 8].append("             +  ");
		matrix[0][ 9].append("############    "); matrix[1][ 9].append("            ++  ");
		matrix[0][10].append("###########     "); matrix[1][10].append("           ++   ");
		matrix[0][11].append("##########      "); matrix[1][11].append("          ++    ");
		matrix[0][12].append("#########       "); matrix[1][12].append("         ++     ");
		matrix[0][13].append("#######         "); matrix[1][13].append("       +++      ");
		matrix[0][14].append("#####           "); matrix[1][14].append("     +++        ");
		matrix[0][15].append("###             "); matrix[1][15].append("    ++          ");
		// place blocks
		final String axis = "u" + FaceToAx(quarter);
		plotter.place3D(axis, matrix);
	}

	protected void generateCenterLamp(final ChunkData chunk, final BlockFace quarter) {
		final BlockPlotter plotter = new BlockPlotter(chunk, this.level_y+this.level_h+14);
		final StringBuilder[][] matrix = plotter.getEmptyMatrix3D(9, 3);
		plotter.type('|', Material.CHAIN);
		plotter.type('L', Material.SHROOMLIGHT);
		plotter.type('R', Material.SHROOMLIGHT);
		plotter.type('i', Material.LIGHTNING_ROD);
		plotter.type('v', Material.SCULK_VEIN, "autoface");
		switch (quarter) {
		case NORTH_EAST: plotter.setX( 0); plotter.setZ(15); break;
		case NORTH_WEST: plotter.setX(15); plotter.setZ(15); break;
		case SOUTH_EAST: plotter.setX( 0); plotter.setZ( 0); break;
		case SOUTH_WEST: plotter.setX(15); plotter.setZ( 0); break;
		default: throw new RuntimeException("Unknown quarter: " + quarter.toString());
		}
		matrix[0][0].append("i");
		matrix[2][0].append("|");
		matrix[3][0].append("|");
		matrix[4][0].append("|");
		matrix[5][0].append("|v" ); matrix[5][1].append("v");
		matrix[6][0].append("RLv"); matrix[6][1].append("Lv"); matrix[6][2].append("v");
		matrix[7][0].append("Lv "); matrix[7][1].append("v");
		matrix[8][0].append("v  ");
		// place blocks
		final String axis = "d" + FaceToAx(quarter);
		plotter.place3D(axis, matrix);
	}



	// -------------------------------------------------------------------------------
	// axis roads



	protected void generateRoad(final ChunkData chunk, final int chunkX, final int chunkZ) {
		final BlockFace direction, side;
		int x = 0;
		int z = 0;
		if (chunkZ <-1) { direction = BlockFace.NORTH; z = 15; } else
		if (chunkZ > 0) { direction = BlockFace.SOUTH; z =  0; } else
		if (chunkX > 0) { direction = BlockFace.EAST;  x =  0; } else
		if (chunkX <-1) { direction = BlockFace.WEST;  x = 15; } else
			throw new RuntimeException("Unknown direction");
		switch (direction) {
		case NORTH:
		case SOUTH:
			if (chunkX == 0) { side = BlockFace.EAST; x =  0; } else
			if (chunkX ==-1) { side = BlockFace.WEST; x = 15; } else
				throw new RuntimeException("Unknown side for chunk x: " + Integer.toString(chunkX));
			break;
		case EAST:
		case WEST:
			if (chunkZ ==-1) { side = BlockFace.NORTH; z = 15; } else
			if (chunkZ == 0) { side = BlockFace.SOUTH; z =  0; } else
				throw new RuntimeException("Unknown side for chunk z: " + Integer.toString(chunkZ));
			break;
		default: throw new RuntimeException("Unknown direction: " + direction.toString());
		}
		// road
		this.generateRoad(chunk, direction, side, x, z);
		// pillar
		{
			final BlockFace mirrored = direction.getOppositeFace();
			final int pillar;
			switch (direction) {
			case NORTH: case SOUTH: pillar = Math.abs(chunkZ) % 4; break;
			case EAST:  case WEST:  pillar = Math.abs(chunkX) % 4; break;
			default: throw new RuntimeException("Unknown direction: " + direction.toString());
			}
			switch (direction) {
			case NORTH:
				if (pillar == 0) this.generatePillar(chunk, mirrored,  side, x, 15-z); else
				if (pillar == 1) this.generatePillar(chunk, direction, side, x,    z); break;
			case SOUTH:
				if (pillar == 3) this.generatePillar(chunk, mirrored,  side, x, 15-z); else
				if (pillar == 0) this.generatePillar(chunk, direction, side, x,    z); break;
			case EAST:
				if (pillar == 3) this.generatePillar(chunk, mirrored,  side, 15-x, z); else
				if (pillar == 0) this.generatePillar(chunk, direction, side,    x, z); break;
			case WEST:
				if (pillar == 0) this.generatePillar(chunk, mirrored,  side, 15-x, z); else
				if (pillar == 1) this.generatePillar(chunk, direction, side,    x, z); break;
			default: throw new RuntimeException("Unknown direction: " + direction.toString());
			}
		}
	}

	protected void generateRoad(final ChunkData chunk,
			final BlockFace direction, final BlockFace side,
			final int x, final int z) {
		final BlockPlotter plotter = new BlockPlotter(chunk, x, this.level_y+this.level_h, z);
		final StringBuilder[][] matrix = plotter.getEmptyMatrix3D(2, 16);
		plotter.type('#', ROAD_SURFACE);
		plotter.type('+', ROAD_WALL, "autoface");
		for (int i=0; i<16; i++) {
			matrix[0][i].append("### ");
			matrix[1][i].append("   +");
		}
		// place blocks
		final String axis = "u" + FaceToAx(direction) + FaceToAx(side);
		plotter.place3D(axis, matrix);
	}

	protected void generatePillar(final ChunkData chunk,
			final BlockFace direction, final BlockFace side,
			final int x, final int z) {
		final BlockPlotter plotter = new BlockPlotter(chunk, x, this.level_y, z);
		final StringBuilder[][] matrix = plotter.getEmptyMatrix3D(this.level_h, 2);
		plotter.type('#', Material.POLISHED_BLACKSTONE_BRICKS);
		plotter.type('%', Material.POLISHED_BLACKSTONE_BRICK_STAIRS, "top,"+direction.getOppositeFace().toString().toLowerCase());
		int h = this.level_h;
		matrix[--h][0].append("###"); matrix[h][1].append("##");
		matrix[--h][0].append("###"); matrix[h][1].append("##");
		matrix[--h][0].append("###"); matrix[h][1].append("#%");
		matrix[--h][0].append("##%"); matrix[h][1].append("#" );
		matrix[--h][0].append("##" ); matrix[h][1].append("#" );
		matrix[--h][0].append("##" ); matrix[h][1].append("%" );
		matrix[--h][0].append("#%" );
		for (int iy=0; iy<h; iy++)
			matrix[iy][0].append("#");
		final String axis = "u" + FaceToAx(direction) + FaceToAx(side);
		plotter.place3D(axis, matrix);
	}



}
