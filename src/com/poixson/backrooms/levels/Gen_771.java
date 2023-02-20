package com.poixson.backrooms.levels;

import static com.poixson.commonmc.utils.LocationUtils.AxToFace;
import static com.poixson.commonmc.utils.LocationUtils.FaceToAx;
import static com.poixson.commonmc.utils.LocationUtils.FaceToIxy;
import static com.poixson.commonmc.utils.LocationUtils.Rotate;
import static com.poixson.commonmc.utils.LocationUtils.ValueToFaceQuarter;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.commonmc.tools.BlockPlotter;
import com.poixson.tools.dao.Ixy;
import com.poixson.utils.FastNoiseLiteD;
import com.poixson.utils.FastNoiseLiteD.NoiseType;
import com.poixson.utils.FastNoiseLiteD.RotationType3D;
import com.poixson.utils.StringUtils;


// 771 | Crossroads
public class Gen_771 extends GenBackrooms {

	public static final boolean ENABLE_GENERATE = true;

	public static final double THRESH_LIGHT  = 0.42; // lanterns
	public static final double THRESH_LADDER = 0.82; // ladder shaft
	public static final double THRESH_LOOT   = 0.78; // loot chest
	public static final double THRESH_VOID   = 0.88; // void shaft
	public static final double THRESH_LOOT_A = 0.65; // loot type
	public static final double THRESH_LOOT_B = 0.75; // loot type
	public static final int PILLAR_B_OFFSET = 10;

	// noise
	protected final FastNoiseLiteD noiseRoadLights;
	protected final FastNoiseLiteD noiseSpecial;
	protected final FastNoiseLiteD noiseLoot;

	public enum PillarType {
		PILLAR_NORM,
		PILLAR_LADDER,
		PILLAR_LOOT,
		PILLAR_DROP,
		PILLAR_VOID,
	}



	public Gen_771(final BackroomsPlugin plugin,
			final int level_y, final int level_h) {
		super(plugin, level_y, level_h);
		// road lanterns
		this.noiseRoadLights = this.register(new FastNoiseLiteD());
		this.noiseRoadLights.setFrequency(0.3);
		this.noiseRoadLights.setFractalOctaves(1);
		// special exits
		this.noiseSpecial = this.register(new FastNoiseLiteD());
		this.noiseSpecial.setFrequency(0.5);
		this.noiseSpecial.setFractalOctaves(1);
		// chest loot
		this.noiseLoot = this.register(new FastNoiseLiteD());
		this.noiseLoot.setFrequency(0.1);
		this.noiseLoot.setFractalOctaves(1);
		this.noiseLoot.setNoiseType(NoiseType.OpenSimplex2);
		this.noiseLoot.setRotationType3D(RotationType3D.ImproveXYPlanes);
	}



//TODO: change to use pregenerate function
	@Override
	public void generate(final PreGenData pregen,
			final ChunkData chunk, final int chunkX, final int chunkZ) {
		if (!ENABLE_GENERATE) return;
		for (int iz=0; iz<16; iz++) {
			for (int ix=0; ix<16; ix++)
				chunk.setBlock(ix, this.level_y, iz, Material.BARRIER);
		}
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
		plotter.type('@', Material.CHISELED_POLISHED_BLACKSTONE);
		plotter.type('|', Material.POLISHED_BLACKSTONE_BRICK_WALL);
		plotter.type('-', Material.POLISHED_BLACKSTONE_BRICK_SLAB);
		plotter.type('=', Material.POLISHED_BLACKSTONE_BRICK_SLAB, "top");
		plotter.type('#', Material.POLISHED_BLACKSTONE_BRICKS);
		// small arch
		{
			final BlockFace direction = AxToFace(axisA);
			plotter.type('L', Material.POLISHED_BLACKSTONE_BRICK_STAIRS, direction.getOppositeFace().toString().toLowerCase());
			plotter.type('^', Material.POLISHED_BLACKSTONE_BRICK_STAIRS, direction.toString().toLowerCase()+",top");
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
		final BlockPlotter plotter = new BlockPlotter(chunk, (this.level_y+this.level_h)-3);
		final StringBuilder[][] matrix = plotter.getEmptyMatrix3D(5, 16);
		plotter.type('#', Material.POLISHED_BLACKSTONE);
		plotter.type('X', Material.GILDED_BLACKSTONE);
		plotter.type('x', Material.CHISELED_POLISHED_BLACKSTONE);
		plotter.type('*', Material.BLACKSTONE);
		plotter.type('+', Material.POLISHED_BLACKSTONE_BRICK_WALL, "autoface");
		plotter.type('-', Material.POLISHED_BLACKSTONE_SLAB, "top");
		plotter.type('.', Material.LIGHT, "15");
		plotter.type(',', Material.LIGHT, "9");
		switch (quarter) {
		case NORTH_EAST: plotter.setX( 0); plotter.setZ(15); break;
		case NORTH_WEST: plotter.setX(15); plotter.setZ(15); break;
		case SOUTH_EAST: plotter.setX( 0); plotter.setZ( 0); break;
		case SOUTH_WEST: plotter.setX(15); plotter.setZ( 0); break;
		default: throw new RuntimeException("Unknown quarter: " + quarter.toString());
		}
		matrix[0][ 0].append("###########---"); matrix[1][ 0].append(" , , , , , ,  #"); matrix[2][ 0].append("              ##"); matrix[3][ 0].append("x***************"); matrix[4][ 0].append("                ");
		matrix[0][ 1].append("###########---"); matrix[1][ 1].append("              #"); matrix[2][ 1].append("              ##"); matrix[3][ 1].append("***#############"); matrix[4][ 1].append("    .   .   .   ");
		matrix[0][ 2].append("##########---" ); matrix[1][ 2].append(" , , , , , , ##"); matrix[2][ 2].append("             ###"); matrix[3][ 2].append("**X#############"); matrix[4][ 2].append("                ");
		matrix[0][ 3].append("##########---" ); matrix[1][ 3].append("             #" ); matrix[2][ 3].append("             ##" ); matrix[3][ 3].append("*##*###########" ); matrix[4][ 3].append("  .   .   .   . ");
		matrix[0][ 4].append("#########---"  ); matrix[1][ 4].append(" , , , , ,  ##" ); matrix[2][ 4].append("            ###" ); matrix[3][ 4].append("*###X##########" ); matrix[4][ 4].append("               +");
		matrix[0][ 5].append("########----"  ); matrix[1][ 5].append("            #"  ); matrix[2][ 5].append("            ##"  ); matrix[3][ 5].append("*####*########"  ); matrix[4][ 5].append("              ++");
		matrix[0][ 6].append("#######----"   ); matrix[1][ 6].append(" , , , , , ##"  ); matrix[2][ 6].append("           ###"  ); matrix[3][ 6].append("*#####*#######"  ); matrix[4][ 6].append("              +" );
		matrix[0][ 7].append("######-----"   ); matrix[1][ 7].append("           #"   ); matrix[2][ 7].append("           ##"   ); matrix[3][ 7].append("*######X#####"   ); matrix[4][ 7].append("  .   .   .  ++" );
		matrix[0][ 8].append("#####-----"    ); matrix[1][ 8].append(" , , , ,  ##"   ); matrix[2][ 8].append("          ###"   ); matrix[3][ 8].append("*#######*####"   ); matrix[4][ 8].append("             +"  );
		matrix[0][ 9].append("####-----"     ); matrix[1][ 9].append("         ##"    ); matrix[2][ 9].append("         ###"    ); matrix[3][ 9].append("*###########"    ); matrix[4][ 9].append("            ++"  );
		matrix[0][10].append("##------"      ); matrix[1][10].append(" , , ,  ##"     ); matrix[2][10].append("        ###"     ); matrix[3][10].append("*##########"     ); matrix[4][10].append("           ++"   );
		matrix[0][11].append("-------"       ); matrix[1][11].append("       ##"      ); matrix[2][11].append("       ###"      ); matrix[3][11].append("*#########"      ); matrix[4][11].append("  .   .   ++"    );
		matrix[0][12].append("-----"         ); matrix[1][12].append(" ,   ###"       ); matrix[2][12].append("     ####"       ); matrix[3][12].append("*########"       ); matrix[4][12].append("         ++"     );
		matrix[0][13].append("--"            ); matrix[1][13].append("  ####"         ); matrix[2][13].append("  #####"         ); matrix[3][13].append("*######"         ); matrix[4][13].append("       +++"      );
		matrix[0][14].append(""              ); matrix[1][14].append("###"            ); matrix[2][14].append("#####"           ); matrix[3][14].append("*####"           ); matrix[4][14].append("     +++"        );
		matrix[0][15].append(""              ); matrix[1][15].append(""               ); matrix[2][15].append("###"             ); matrix[3][15].append("*##"             ); matrix[4][15].append("  . ++"          );
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
		this.generateRoadTop(chunk, direction, side, chunkX, chunkZ, x, z);
		this.generateRoadBottom(chunk, direction, side, chunkX, chunkZ, x, z);
		// pillar
		{
			// round to nearest chunk group and convert to block location
			final int px = Math.floorDiv(chunkX+1, 4) * 64;
			final int pz = Math.floorDiv(chunkZ+1, 4) * 64;
			final double valB = this.noiseSpecial.getNoise(px, pz);
			final double valC = this.noiseSpecial.getNoise(px+PILLAR_B_OFFSET, pz+PILLAR_B_OFFSET);
			final PillarType pillar_type;
			if (Math.abs(chunkX) < 30
			&&  Math.abs(chunkZ) < 30) {
				pillar_type = PillarType.PILLAR_NORM;
			} else
			if (valB > THRESH_LADDER) {
				if (valC > THRESH_LOOT) {
				if (valC > THRESH_VOID) pillar_type = PillarType.PILLAR_VOID;   // void shaft
				else                    pillar_type = PillarType.PILLAR_DROP;   // drop shaft
				} else                  pillar_type = PillarType.PILLAR_LADDER; // ladder shaft
			} else {
				if (valC > THRESH_LOOT) pillar_type = PillarType.PILLAR_LOOT; // loot chest
				else                    pillar_type = PillarType.PILLAR_NORM; // normal pillar
			}
			final BlockFace mirrored = direction.getOppositeFace();
			final int mod_pillar;
			switch (direction) {
			case NORTH: case SOUTH: mod_pillar = Math.abs(chunkZ) % 4; break;
			case EAST:  case WEST:  mod_pillar = Math.abs(chunkX) % 4; break;
			default: throw new RuntimeException("Unknown direction: " + direction.toString());
			}
			switch (direction) {
			case NORTH:
				if (mod_pillar == 0) this.generatePillar(pillar_type, chunk, mirrored,  side, chunkX, chunkZ, x, 15-z); else
				if (mod_pillar == 1) this.generatePillar(pillar_type, chunk, direction, side, chunkX, chunkZ, x,    z); break;
			case SOUTH:
				if (mod_pillar == 3) this.generatePillar(pillar_type, chunk, mirrored,  side, chunkX, chunkZ, x, 15-z); else
				if (mod_pillar == 0) this.generatePillar(pillar_type, chunk, direction, side, chunkX, chunkZ, x,    z); break;
			case EAST:
				if (mod_pillar == 3) this.generatePillar(pillar_type, chunk, mirrored,  side, chunkX, chunkZ, 15-x, z); else
				if (mod_pillar == 0) this.generatePillar(pillar_type, chunk, direction, side, chunkX, chunkZ,    x, z); break;
			case WEST:
				if (mod_pillar == 0) this.generatePillar(pillar_type, chunk, mirrored,  side, chunkX, chunkZ, 15-x, z); else
				if (mod_pillar == 1) this.generatePillar(pillar_type, chunk, direction, side, chunkX, chunkZ,    x, z); break;
			default: throw new RuntimeException("Unknown direction: " + direction.toString());
			}
		}
	}

	protected void generateRoadTop(final ChunkData chunk,
			final BlockFace direction, final BlockFace side,
			final int chunkX, final int chunkZ, final int x, final int z) {
		final BlockPlotter plotter = new BlockPlotter(chunk, x, this.level_y+this.level_h, z);
		final StringBuilder[][] matrix = plotter.getEmptyMatrix3D(3, 16);
		plotter.type('#', Material.POLISHED_BLACKSTONE);
		plotter.type('*', Material.BLACKSTONE);
		plotter.type('+', Material.POLISHED_BLACKSTONE_BRICK_WALL, "autoface");
		plotter.type('i', Material.SOUL_LANTERN);
		plotter.type('L', Material.LIGHT, "15");
		double value_light;
		final Ixy dir = FaceToIxy(direction);
		final int cx = chunkX * 16;
		final int cz = chunkZ * 16;
		for (int i=0; i<16; i++) {
			matrix[1][i].append("   +");
			matrix[0][i].append("*##" );
			value_light = this.noiseRoadLights.getNoise(cx+(dir.x*i), cz+(dir.y*i)) % 0.5;
			if (value_light > THRESH_LIGHT) {
				matrix[2][i].append("   i");
				StringUtils.ReplaceInString(matrix[1][i], "L", 2);
			}
		}
		// place blocks
		final String axis = "u" + FaceToAx(direction) + FaceToAx(side);
		plotter.place3D(axis, matrix);
	}
	protected void generateRoadBottom(final ChunkData chunk,
			final BlockFace direction, final BlockFace side,
			final int chunkX, final int chunkZ, final int x, final int z) {
		final BlockPlotter plotter = new BlockPlotter(chunk, x, this.level_y, z);
		final StringBuilder[][] matrix = plotter.getEmptyMatrix3D(3, 16);
		plotter.type('#', Material.POLISHED_BLACKSTONE);
		plotter.type('*', Material.BLACKSTONE);
		plotter.type('+', Material.POLISHED_BLACKSTONE_BRICK_WALL, "autoface");
		plotter.type('i', Material.LANTERN);
		plotter.type('L', Material.LIGHT, "15");
		double value_light;
		final Ixy dir = FaceToIxy(direction);
		final int cx = chunkX * 16;
		final int cz = chunkZ * 16;
		for (int i=0; i<16; i++) {
			matrix[1][i].append("   +");
			matrix[0][i].append("*##" );
			value_light = this.noiseRoadLights.getNoise(cx+(dir.x*i), cz+(dir.y*i)) % 0.5;
			if (value_light > THRESH_LIGHT) {
				matrix[2][i].append("   i");
				StringUtils.ReplaceInString(matrix[1][i], "L", 2);
			}
		}
		// place blocks
		final String axis = "u" + FaceToAx(direction) + FaceToAx(side);
		plotter.place3D(axis, matrix);
	}



	// -------------------------------------------------------------------------------
	// pillars



	protected void generatePillar(final PillarType type, final ChunkData chunk,
			final BlockFace direction, final BlockFace side,
			final int chunkX, final int chunkZ, final int x, final int z) {
		final BlockPlotter plotter = new BlockPlotter(chunk, x, this.level_y, z);
		final StringBuilder[][] matrix = plotter.getEmptyMatrix3D(this.level_h+2, 2);
		plotter.type('#', Material.DEEPSLATE_BRICKS);
		plotter.type('%', Material.DEEPSLATE_BRICK_STAIRS, "top,"   +direction.getOppositeFace().toString().toLowerCase());
		plotter.type('<', Material.DEEPSLATE_BRICK_STAIRS, "top,"   +side.toString().toLowerCase());
		plotter.type('$', Material.DEEPSLATE_BRICK_STAIRS, "top,"   +side.getOppositeFace().toString().toLowerCase());
		plotter.type('&', Material.DEEPSLATE_BRICK_STAIRS, "bottom,"+side.getOppositeFace().toString().toLowerCase());
		plotter.type('w', Material.DARK_OAK_PLANKS);
		plotter.type('H', Material.LADDER, side.getOppositeFace().toString().toLowerCase());
		plotter.type('/', Material.SPRUCE_TRAPDOOR,  "top,"+side.toString().toLowerCase());
		plotter.type('~', Material.CRIMSON_TRAPDOOR, "top,"+side.getOppositeFace().toString().toLowerCase());
		plotter.type('d', Material.SPRUCE_DOOR, "top,"   +direction.toString().toLowerCase());
		plotter.type('D', Material.SPRUCE_DOOR, "bottom,"+direction.toString().toLowerCase());
		plotter.type('_', Material.POLISHED_BLACKSTONE_PRESSURE_PLATE);
		plotter.type('-', Material.DARK_OAK_PRESSURE_PLATE);
		plotter.type('+', Material.DEEPSLATE_TILE_WALL);
		plotter.type('S', Material.DARK_OAK_WALL_SIGN, direction.getOppositeFace().toString().toLowerCase());
		plotter.type('U', Material.BARREL, "up");
		plotter.type(',', Material.LIGHT,  "15");
		plotter.type('W', Material.WATER);
		plotter.type('.', Material.AIR);
		int h = this.level_h;
		switch (type) {
		// loot chest
		case PILLAR_LOOT:
			if (x == 0 && z == 0)
				matrix[h+1][0].append("  U");
		// normal pillar
		case PILLAR_NORM: {
			// top of pillar
			matrix[  h][0].append("   %");
			matrix[--h][0].append("###"); matrix[h][1].append("##");
			matrix[--h][0].append("###"); matrix[h][1].append("##");
			matrix[--h][0].append("###"); matrix[h][1].append("#%");
			matrix[--h][0].append("##%"); matrix[h][1].append("#" );
			matrix[--h][0].append("##" ); matrix[h][1].append("#" );
			matrix[--h][0].append("##" ); matrix[h][1].append("%" );
			matrix[--h][0].append("#%" );
			// vertical pillar
			for (int iy=8; iy<h; iy++)
				matrix[iy][0].append("#");
			// bottom of pillar
			matrix[7][0].append("#&"  );
			matrix[6][0].append("##"  );
			matrix[5][0].append("##&" );
			matrix[4][0].append("  <&");
			matrix[3][0].append("   #");
			matrix[2][0].append("   #");
			matrix[1][0].append("   #");
			matrix[0][0].append("   $");
			// loot chest
			if (PillarType.PILLAR_LOOT.equals(type))
				if (x == 0 && z == 0)
					StringUtils.ReplaceInString(matrix[1][0], "U", 2);
			break;
		}
		// ladder shaft
		case PILLAR_LADDER: {
			matrix[h][0].append("   $");
			// trapdoor
			if (x == 0 && z == 0) {
				matrix[h+1][0].append("_ _");
				matrix[h+1][1].append(" _" );
				StringUtils.ReplaceInString(matrix[h][0], "/", 1);
			}
			if (x == -1 && z == -1)
				matrix[h+1][1].append(" _");
			matrix[--h][0].append("  #"); matrix[h][1].append("##"); if (x == 0 && z == 0) StringUtils.ReplaceInString(matrix[h][0], "H", 1);
			matrix[--h][0].append("  #"); matrix[h][1].append("##"); if (x == 0 && z == 0) StringUtils.ReplaceInString(matrix[h][0], "H", 1);
			matrix[--h][0].append("  #"); matrix[h][1].append("##"); if (x == 0 && z == 0) StringUtils.ReplaceInString(matrix[h][0], "H", 1);
			matrix[--h][0].append(" ##"); matrix[h][1].append("#%"); if (x == 0 && z == 0) StringUtils.ReplaceInString(matrix[h][0], "H", 0);
			matrix[--h][0].append(" #%"); matrix[h][1].append("#" ); if (x == 0 && z == 0) StringUtils.ReplaceInString(matrix[h][0], "H", 0);
			for (int iy=0; iy<h; iy++) {
				matrix[iy][0].append(" #");
				matrix[iy][1].append("#" );
				// ladder and lights
				if (x == 0 && z == 0)  StringUtils.ReplaceInString(matrix[iy][0], "H", 0);
				else if (iy % 10 == 0) StringUtils.ReplaceInString(matrix[iy][0], ",", 0);
			}
			// door
			if (x != 0 && z != 0) {
				StringUtils.ReplaceInString(matrix[2][1], "d", 0);
				StringUtils.ReplaceInString(matrix[1][1], "D", 0);
				StringUtils.ReplaceInString(matrix[1][0], "-", 0);
			}
			// floor inside shaft
			StringUtils.ReplaceInString(matrix[0][0], "w", 0);
			break;
		}
		// drop shaft to lower road
		case PILLAR_DROP: {
			matrix[h+1][1].append("_"   );
			matrix[  h][0].append("~  $");
			for (int iy=0; iy<h; iy++) {
				matrix[iy][0].append("  #");
				matrix[iy][1].append("##" );
			}
			// doorway
			StringUtils.ReplaceInString(matrix[4][0], "WW", 0);
			StringUtils.ReplaceInString(matrix[3][0], "SS", 0);
			StringUtils.ReplaceInString(matrix[2][1], ".+", 0);
			StringUtils.ReplaceInString(matrix[1][1], ".+", 0);
			StringUtils.ReplaceInString(matrix[0][0], "ww", 0);
			break;
		}
		// void shaft
		case PILLAR_VOID: {
			matrix[h+1][1].append("_"   );
			matrix[  h][0].append("~  $");
			for (int iy=0; iy<h; iy++) {
				matrix[iy][0].append(" #");
				matrix[iy][1].append("#" );
			}
			// no floor inside shaft
			StringUtils.ReplaceInString(matrix[0][0], ".", 0);
			break;
		}
		default: break;
		}
		final String axis = "u" + FaceToAx(direction) + FaceToAx(side);
		plotter.place3D(axis, matrix);
	}



}
