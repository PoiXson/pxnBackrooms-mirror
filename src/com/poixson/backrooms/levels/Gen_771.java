package com.poixson.backrooms.levels;

import static com.poixson.backrooms.levels.Level_771.ENABLE_GEN_771;
import static com.poixson.commonmc.utils.LocationUtils.FaceToAxString;
import static com.poixson.commonmc.utils.LocationUtils.FaceToIxz;
import static com.poixson.commonmc.utils.LocationUtils.Rotate;
import static com.poixson.commonmc.utils.LocationUtils.ValueToFaceQuarter;

import java.util.LinkedList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Barrel;
import org.bukkit.generator.ChunkGenerator.ChunkData;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.poixson.commonmc.tools.DelayedChestFiller;
import com.poixson.commonmc.tools.plotter.BlockPlotter;
import com.poixson.commonmc.tools.plotter.PlotterFactory;
import com.poixson.tools.dao.Iab;
import com.poixson.utils.FastNoiseLiteD;
import com.poixson.utils.StringUtils;


// 771 | Crossroads
public class Gen_771 extends GenBackrooms {

	public static final double THRESH_LIGHT  = 0.42; // lanterns
	public static final double THRESH_LADDER = 0.81; // ladder shaft
	public static final double THRESH_LOOT   = 0.78; // loot chest
	public static final double THRESH_VOID   = 0.85; // void shaft
	public static final double THRESH_LOOT_A = 0.65; // loot type
	public static final double THRESH_LOOT_B = 0.75; // loot type
	public static final int PILLAR_B_OFFSET = 10;

	// noise
	public final FastNoiseLiteD noiseRoadLights;
	public final FastNoiseLiteD noiseSpecial;
	public final FastNoiseLiteD noiseLoot;

	public enum PillarType {
		PILLAR_NORM,
		PILLAR_LADDER,
		PILLAR_LOOT,
		PILLAR_DROP,
		PILLAR_VOID,
	}



	public Gen_771(final LevelBackrooms backlevel,
			final int level_y, final int level_h) {
		super(backlevel, level_y, level_h);
		// road lanterns
		this.noiseRoadLights = this.register(new FastNoiseLiteD());
		this.noiseRoadLights.setFrequency(0.3);
		// special exits
		this.noiseSpecial = this.register(new FastNoiseLiteD());
		this.noiseSpecial.setFrequency(0.5);
		// chest loot
		this.noiseLoot = this.register(new FastNoiseLiteD());
		this.noiseLoot.setFrequency(0.1);
	}



	@Override
	public void generate(final PreGenData pregen, final ChunkData chunk,
			final LinkedList<BlockPlotter> plots, final int chunkX, final int chunkZ) {
		if (!ENABLE_GEN_771) return;
		for (int iz=0; iz<16; iz++) {
			for (int ix=0; ix<16; ix++)
				chunk.setBlock(ix, this.level_y, iz, Material.BARRIER);
		}
		final boolean centerX = (chunkX == 0 || chunkX == -1);
		final boolean centerZ = (chunkZ == 0 || chunkZ == -1);
		// world center
		if (centerX && centerZ) {
			this.generateWorldCenter(chunk, chunkX, chunkZ);
		} else
		// road
		if (centerX || centerZ) {
			this.generateRoad(chunk, chunkX, chunkZ);
		}
	}



	// -------------------------------------------------------------------------------
	// world center



	protected void generateWorldCenter(final ChunkData chunk, final int chunkX, final int chunkZ) {
		final BlockFace quarter = ValueToFaceQuarter(chunkX, chunkZ);
		final String axis = FaceToAxString(quarter);
		this.generateCenterArches(chunk, chunkX, chunkZ, "u"+axis);
		this.generateCenterArches(chunk, chunkX, chunkZ, "u"+axis.charAt(1)+axis.charAt(0));
		this.generateCenterFloor( chunk, chunkX, chunkZ, "u"+axis);
	}
	protected void generateCenterArches(final ChunkData chunk,
			final int chunkX, final int chunkZ, final String axis) {
		final BlockPlotter plot =
			(new PlotterFactory())
			.placer(chunk)
			.axis(axis)
			.xz((0-chunkX)*15, (0-chunkZ)*15)
			.y(this.level_y + this.level_h + 2)
			.whd(16, 14, 4)
			.build();
		plot.type('#', Material.POLISHED_BLACKSTONE_BRICKS);
		plot.type('-', Material.POLISHED_BLACKSTONE_BRICK_SLAB, "top"   );
		plot.type('_', Material.POLISHED_BLACKSTONE_BRICK_SLAB, "bottom");
		plot.type('L', Material.POLISHED_BLACKSTONE_BRICK_STAIRS, Character.toString( Rotate(axis.charAt(2), 0.5))       );
		plot.type('^', Material.POLISHED_BLACKSTONE_BRICK_STAIRS, Character.toString(        axis.charAt(2)      ), "top");
		plot.type('|', Material.POLISHED_BLACKSTONE_BRICK_WALL);
		plot.type('@', Material.CHISELED_POLISHED_BLACKSTONE);
		plot.type('!', Material.LIGHTNING_ROD);
		plot.type('8', Material.CHAIN);
		plot.type('G', Material.SHROOMLIGHT);
		final StringBuilder[][] matrix = plot.getMatrix3D();
		matrix[13][ 0].append("!");
		// big arch
		matrix[12][ 0].append("#");
		matrix[11][ 2].append("#"); matrix[11][ 1].append("-"); matrix[12][ 1].append("_");
		matrix[10][ 4].append("#"); matrix[10][ 3].append("-"); matrix[11][ 3].append("_");
		matrix[ 9][ 6].append("#"); matrix[ 9][ 5].append("-"); matrix[10][ 5].append("_");
		matrix[ 8][ 8].append("#"); matrix[ 8][ 7].append("-"); matrix[ 9][ 7].append("_");
		matrix[ 7][10].append("#"); matrix[ 7][ 9].append("-"); matrix[ 8][ 9].append("_");
		matrix[ 6][12].append("#"); matrix[ 6][11].append("-"); matrix[ 7][11].append("_");
		matrix[ 5][14].append("#"); matrix[ 5][13].append("-"); matrix[ 6][13].append("_");
		// center lamp
		for (int i=7; i<12; i++)
			matrix[i][0].append("8");
		matrix[6][0].append("G");
		// gateway arch
		matrix[5][15].append("_"   );
		matrix[4][15].append("#L"  );
		matrix[3][15].append(" ^L" );
		matrix[2][15].append("  ^L");
		matrix[1][15].append("   |");
		matrix[0][15].append("   @");
		plot.run();
	}
	protected void generateCenterFloor(final ChunkData chunk,
			final int chunkX, final int chunkZ, final String axis) {
		final BlockPlotter plot =
			(new PlotterFactory())
			.placer(chunk)
			.axis(axis)
			.xz((0-chunkX)*15, (0-chunkZ)*15)
			.y((this.level_y + this.level_h) - 3)
			.whd(16, 5, 16)
			.build();
		plot.type('#', Material.POLISHED_BLACKSTONE);
		plot.type('x', Material.CHISELED_POLISHED_BLACKSTONE);
		plot.type('X', Material.GILDED_BLACKSTONE);
		plot.type('*', Material.BLACKSTONE);
		plot.type('+', Material.POLISHED_BLACKSTONE_BRICK_WALL, "autoface");
		plot.type('-', Material.POLISHED_BLACKSTONE_SLAB, "top");
		plot.type('.', Material.LIGHT, "15");
		plot.type(',', Material.LIGHT,  "9");
		final StringBuilder[][] matrix = plot.getMatrix3D();
		matrix[0][ 0].append("###########---"); matrix[1][ 0].append(" , , , , , ,  #"); matrix[2][ 0].append("              ##"); matrix[3][ 0].append("x***************"); matrix[4][ 0].append("                ");
		matrix[0][ 1].append("###########---"); matrix[1][ 1].append("              #"); matrix[2][ 1].append("              ##"); matrix[3][ 1].append("***#############"); matrix[4][ 1].append("    .   .   .   ");
		matrix[0][ 2].append("##########---" ); matrix[1][ 2].append(" , , , , , , ##"); matrix[2][ 2].append("             ###"); matrix[3][ 2].append("**X##X##########"); matrix[4][ 2].append("                ");
		matrix[0][ 3].append("##########---" ); matrix[1][ 3].append("             #" ); matrix[2][ 3].append("             ##" ); matrix[3][ 3].append("*##*###*#######" ); matrix[4][ 3].append("  .   .   .   . ");
		matrix[0][ 4].append("#########---"  ); matrix[1][ 4].append(" , , , , ,  ##" ); matrix[2][ 4].append("            ###" ); matrix[3][ 4].append("*###X####X#####" ); matrix[4][ 4].append("               +");
		matrix[0][ 5].append("########----"  ); matrix[1][ 5].append("            #"  ); matrix[2][ 5].append("            ##"  ); matrix[3][ 5].append("*#X##*#####*##"  ); matrix[4][ 5].append("              ++");
		matrix[0][ 6].append("#######----"   ); matrix[1][ 6].append(" , , , , , ##"  ); matrix[2][ 6].append("           ###"  ); matrix[3][ 6].append("*#####*#######"  ); matrix[4][ 6].append("              +" );
		matrix[0][ 7].append("######-----"   ); matrix[1][ 7].append("           #"   ); matrix[2][ 7].append("           ##"   ); matrix[3][ 7].append("*##*###X#####"   ); matrix[4][ 7].append("  .   .   .  ++" );
		matrix[0][ 8].append("#####-----"    ); matrix[1][ 8].append(" , , , ,  ##"   ); matrix[2][ 8].append("          ###"   ); matrix[3][ 8].append("*#######*####"   ); matrix[4][ 8].append("             +"  );
		matrix[0][ 9].append("####-----"     ); matrix[1][ 9].append("         ##"    ); matrix[2][ 9].append("         ###"    ); matrix[3][ 9].append("*###X####*##"    ); matrix[4][ 9].append("            ++"  );
		matrix[0][10].append("##------"      ); matrix[1][10].append(" , , ,  ##"     ); matrix[2][10].append("        ###"     ); matrix[3][10].append("*##########"     ); matrix[4][10].append("           ++"   );
		matrix[0][11].append("-------"       ); matrix[1][11].append("       ##"      ); matrix[2][11].append("       ###"      ); matrix[3][11].append("*####*####"      ); matrix[4][11].append("  .   .   ++"    );
		matrix[0][12].append("-----"         ); matrix[1][12].append(" ,   ###"       ); matrix[2][12].append("     ####"       ); matrix[3][12].append("*########"       ); matrix[4][12].append("         ++"     );
		matrix[0][13].append("--"            ); matrix[1][13].append("  ####"         ); matrix[2][13].append("  #####"         ); matrix[3][13].append("*######"         ); matrix[4][13].append("       +++"      );
		matrix[0][14].append(""              ); matrix[1][14].append("###"            ); matrix[2][14].append("#####"           ); matrix[3][14].append("*####"           ); matrix[4][14].append("     +++"        );
		matrix[0][15].append(""              ); matrix[1][15].append(""               ); matrix[2][15].append("###"             ); matrix[3][15].append("*##"             ); matrix[4][15].append("  . ++"          );
		plot.run();
	}



	// -------------------------------------------------------------------------------
	// axis roads



	protected void generateRoad(final ChunkData chunk, final int chunkX, final int chunkZ) {
		int x = 0;
		int z = 0;
		final BlockFace direction, side;
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
		// axis roads
		this.generateRoadTop(   chunk, direction, side, chunkX, chunkZ, x, z);
		this.generateRoadBottom(chunk, direction, side, chunkX, chunkZ, x, z);
		this.generatePillars(   chunk, direction, side, chunkX, chunkZ, x, z);
	}



	protected void generateRoadTop(final ChunkData chunk,
			final BlockFace direction, final BlockFace side,
			final int chunkX, final int chunkZ, final int x, final int z) {
		final BlockPlotter plot =
			(new PlotterFactory())
			.placer(chunk)
			.axis("u"+FaceToAxString(direction)+FaceToAxString(side))
			.xz(x, z)
			.y(this.level_y+this.level_h)
			.whd(16, 3, 16)
			.build();
		plot.type('#', Material.POLISHED_BLACKSTONE);
		plot.type('*', Material.BLACKSTONE);
		plot.type('+', Material.POLISHED_BLACKSTONE_BRICK_WALL, "autoface");
		plot.type('i', Material.SOUL_LANTERN);
		plot.type('L', Material.LIGHT, "15");
		final StringBuilder[][] matrix = plot.getMatrix3D();
		double value_light;
		final Iab dir = FaceToIxz(direction);
		final int cx = chunkX * 16;
		final int cz = chunkZ * 16;
		for (int i=0; i<16; i++) {
			matrix[1][i].append("   +");
			matrix[0][i].append("*##" );
			value_light = this.noiseRoadLights.getNoise(cx+(dir.a*i), cz+(dir.b*i)) % 0.5;
			if (value_light > THRESH_LIGHT) {
				matrix[2][i].append("   i");
				StringUtils.ReplaceInString(matrix[1][i], "L", 2);
			}
		}
		plot.run();
	}
	protected void generateRoadBottom(final ChunkData chunk,
			final BlockFace direction, final BlockFace side,
			final int chunkX, final int chunkZ, final int x, final int z) {
		final BlockPlotter plot =
				(new PlotterFactory())
				.placer(chunk)
				.axis("u"+FaceToAxString(direction)+FaceToAxString(side))
				.xz(x, z)
				.y(this.level_y)
				.whd(16, 3, 16)
				.build();
		plot.type('#', Material.POLISHED_BLACKSTONE);
		plot.type('*', Material.BLACKSTONE);
		plot.type('+', Material.POLISHED_BLACKSTONE_BRICK_WALL, "autoface");
		plot.type('i', Material.LANTERN);
		plot.type('L', Material.LIGHT, "15");
		final StringBuilder[][] matrix = plot.getMatrix3D();
		double value_light;
		final Iab dir = FaceToIxz(direction);
		final int cx = chunkX * 16;
		final int cz = chunkZ * 16;
		for (int i=0; i<16; i++) {
			matrix[1][i].append("   +");
			matrix[0][i].append("*##" );
			value_light = this.noiseRoadLights.getNoise(cx+(dir.a*i), cz+(dir.b*i)) % 0.5;
			if (value_light > THRESH_LIGHT) {
				matrix[2][i].append("   i");
				StringUtils.ReplaceInString(matrix[1][i], "L", 2);
			}
		}
		plot.run();
	}



	// -------------------------------------------------------------------------------
	// pillars



	protected void generatePillars(final ChunkData chunk,
			final BlockFace direction, final BlockFace side,
			final int chunkX, final int chunkZ, final int x, final int z) {
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
	protected void generatePillar(final PillarType type, final ChunkData chunk,
			final BlockFace direction, final BlockFace side,
			final int chunkX, final int chunkZ, final int x, final int z) {
		final BlockPlotter plot =
				(new PlotterFactory())
				.placer(chunk)
				.axis("u"+FaceToAxString(direction)+FaceToAxString(side))
				.xz(x, z)
				.y(this.level_y)
				.whd(2, this.level_h+2, 5)
				.build();
		plot.type('#', Material.DEEPSLATE_BRICKS);
		plot.type('%', Material.DEEPSLATE_BRICK_STAIRS, "top",    direction.getOppositeFace().toString().toLowerCase());
		plot.type('<', Material.DEEPSLATE_BRICK_STAIRS, "top",    side.toString().toLowerCase());
		plot.type('$', Material.DEEPSLATE_BRICK_STAIRS, "top",    side.getOppositeFace().toString().toLowerCase());
		plot.type('&', Material.DEEPSLATE_BRICK_STAIRS, "bottom", side.getOppositeFace().toString().toLowerCase());
		plot.type('w', Material.DARK_OAK_PLANKS);
		plot.type('H', Material.LADDER, side.getOppositeFace().toString().toLowerCase());
		plot.type('/', Material.SPRUCE_TRAPDOOR,  "top", side.toString().toLowerCase());
		plot.type('~', Material.CRIMSON_TRAPDOOR, "top", side.getOppositeFace().toString().toLowerCase());
		plot.type('d', Material.SPRUCE_DOOR,      "top", direction.toString().toLowerCase());
		plot.type('D', Material.SPRUCE_DOOR,   "bottom", direction.toString().toLowerCase());
		plot.type('_', Material.POLISHED_BLACKSTONE_PRESSURE_PLATE);
		plot.type('-', Material.DARK_OAK_PRESSURE_PLATE);
		plot.type('+', Material.DEEPSLATE_TILE_WALL, "autoface");
		plot.type('S', Material.DARK_OAK_WALL_SIGN, direction.getOppositeFace().toString().toLowerCase());
		plot.type(',', Material.LIGHT,  "15");
		plot.type('W', Material.WATER);
		plot.type('.', Material.AIR);
		int h = this.level_h;
		final StringBuilder[][] matrix = plot.getMatrix3D();
		switch (type) {
		// loot chest
		case PILLAR_LOOT:
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
			if (x == 0 && z == 0) {
				if (PillarType.PILLAR_LOOT.equals(type)) {
					final Iab dir = FaceToIxz(side);
					final int xx = (dir.a * 2) + x;
					final int zz = (dir.b * 2) + z;
					final int yy = this.level_y + this.level_h + 1;
					chunk.setBlock(xx, yy, zz, Material.BARREL);
					final BlockData data = chunk.getBlockData(xx, yy, zz);
					((Barrel)data).setFacing(BlockFace.UP);
					chunk.setBlock(xx, yy, zz, data);
					(new ChestFiller_771(this.plugin, "level771", xx, yy, zz))
						.start();
				}
			}
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
				StringUtils.ReplaceInString(matrix[0][1], " ", 0);
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
		plot.run();
	}



	public class ChestFiller_771 extends DelayedChestFiller {

		public ChestFiller_771(final JavaPlugin plugin,
				final String worldName, final int x, final int y, final int z) {
			super(plugin, worldName, x, y, z);
		}

		@Override
		public void fill(final Inventory chest) {
//TODO
			final ItemStack item = new ItemStack(Material.BREAD);
			final Location loc = chest.getLocation();
			final int xx = loc.getBlockX();
			final int zz = loc.getBlockZ();
			int x, y;
			double value;
			for (int i=0; i<27; i++) {
				x = xx + (i % 9);
				y = zz + Math.floorDiv(i, 9);
				value = Gen_771.this.noiseLoot.getNoise(x, y);
				if (value > THRESH_LOOT)
					chest.setItem(i, item);
			}
		}

	}



}
