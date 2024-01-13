package com.poixson.backrooms.gens;

import static com.poixson.backrooms.worlds.Level_771.ENABLE_GEN_771;
import static com.poixson.utils.LocationUtils.AxToFace;
import static com.poixson.utils.LocationUtils.FaceToAxString;
import static com.poixson.utils.LocationUtils.FaceToAxisString;
import static com.poixson.utils.LocationUtils.FaceToIxz;
import static com.poixson.utils.LocationUtils.ValueToFaceQuarter;

import java.util.LinkedList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.generator.ChunkGenerator.ChunkData;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.poixson.backrooms.BackroomsGen;
import com.poixson.backrooms.BackroomsLevel;
import com.poixson.backrooms.PreGenData;
import com.poixson.backrooms.worlds.Level_771;
import com.poixson.tools.DelayedChestFiller;
import com.poixson.tools.abstractions.AtomicDouble;
import com.poixson.tools.abstractions.Tuple;
import com.poixson.tools.dao.Iab;
import com.poixson.tools.plotter.BlockPlotter;
import com.poixson.utils.FastNoiseLiteD;
import com.poixson.utils.StringUtils;


// 771 | Crossroads
public class Gen_771 extends BackroomsGen {

	// default params
	public static final double DEFAULT_NOISE_LAMPS_FREQ = 0.3;
	public static final double DEFAULT_NOISE_EXITS_FREQ = 0.08;
	public static final double DEFAULT_NOISE_LOOT_FREQ  = 0.08;
	public static final double DEFAULT_THRESH_LAMPS     = 0.42; // lanterns
	public static final double DEFAULT_THRESH_VOID      = 0.9;  // void shaft
	public static final double DEFAULT_THRESH_LADDER    = 0.79; // ladder shaft
	public static final double DEFAULT_THRESH_LOOT      = 0.64; // loot chest

	// default blocks
//TODO

	// noise
	public final FastNoiseLiteD noiseRoadLights;
	public final FastNoiseLiteD noiseExits;
	public final FastNoiseLiteD noiseLoot;

	// params
	public final AtomicDouble noise_lamps_freq = new AtomicDouble(DEFAULT_NOISE_LAMPS_FREQ);
	public final AtomicDouble noise_exits_freq = new AtomicDouble(DEFAULT_NOISE_EXITS_FREQ);
	public final AtomicDouble noise_loot_freq  = new AtomicDouble(DEFAULT_NOISE_LOOT_FREQ );
	public final AtomicDouble thresh_lamps     = new AtomicDouble(DEFAULT_THRESH_LAMPS    );
	public final AtomicDouble thresh_void      = new AtomicDouble(DEFAULT_THRESH_VOID     );
	public final AtomicDouble thresh_ladder    = new AtomicDouble(DEFAULT_THRESH_LADDER   );
	public final AtomicDouble thresh_loot      = new AtomicDouble(DEFAULT_THRESH_LOOT     );

	// blocks
//TODO



	public enum PillarType {
		PILLAR_NORM,
		PILLAR_LADDER,
		PILLAR_LOOT_UPPER,
		PILLAR_LOOT_LOWER,
		PILLAR_DROP,
		PILLAR_VOID,
	}



	public Gen_771(final BackroomsLevel backlevel, final int seed,
			final int level_y, final int level_h) {
		super(backlevel, seed, level_y, level_h);
		// noise
		this.noiseRoadLights = this.register(new FastNoiseLiteD());
		this.noiseExits      = this.register(new FastNoiseLiteD());
		this.noiseLoot       = this.register(new FastNoiseLiteD());
	}



	@Override
	public void initNoise() {
		super.initNoise();
		this.noiseRoadLights.setFrequency(this.noise_lamps_freq.get()); // road lanterns
		this.noiseExits     .setFrequency(this.noise_exits_freq.get()); // special exits
		this.noiseLoot      .setFrequency(this.noise_loot_freq .get()); // chest loot
	}



	@Override
	public void generate(final PreGenData pregen,
			final LinkedList<Tuple<BlockPlotter, StringBuilder[][]>> plots,
			final ChunkData chunk, final int chunkX, final int chunkZ) {
		if (!ENABLE_GEN_771) return;
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
			(new BlockPlotter())
			.axis(axis)
			.xz((0-chunkX)*15, (0-chunkZ)*15)
			.y(this.level_y + this.level_h + 1)
			.whd(16, 15, 16);
		final BlockFace facing = AxToFace(axis.charAt(2));
		plot.type('#', Material.POLISHED_BLACKSTONE_BRICKS                       );
		plot.type('-', Material.POLISHED_BLACKSTONE_BRICK_SLAB,   "[type=top]"   );
		plot.type('_', Material.POLISHED_BLACKSTONE_BRICK_SLAB,   "[type=bottom]");
		plot.type('L', Material.POLISHED_BLACKSTONE_BRICK_STAIRS, "[facing="+FaceToAxisString(facing.getOppositeFace())+"]");
		plot.type('^', Material.POLISHED_BLACKSTONE_BRICK_STAIRS, "[facing="+FaceToAxisString(facing)+",half=top]"         );
		plot.type('|', Material.POLISHED_BLACKSTONE_BRICK_WALL);
		plot.type('@', Material.CHISELED_POLISHED_BLACKSTONE  );
		plot.type('!', Material.LIGHTNING_ROD                 );
		plot.type('8', Material.CHAIN                         );
		plot.type('G', Material.SHROOMLIGHT                   );
		plot.type('b', Material.ACACIA_BUTTON, "[face=ceiling]");
		final StringBuilder[][] matrix = plot.getMatrix3D();
		matrix[14][ 0].append("!");
		// big arch
		matrix[13][ 0].append("#");                             matrix[13][ 1].append("_");
		matrix[12][ 2].append("#"); matrix[12][ 1].append("-"); matrix[12][ 3].append("_");
		matrix[11][ 4].append("#"); matrix[11][ 3].append("-"); matrix[11][ 5].append("_");
		matrix[10][ 6].append("#"); matrix[10][ 5].append("-"); matrix[10][ 7].append("_");
		matrix[ 9][ 8].append("#"); matrix[ 9][ 7].append("-"); matrix[ 9][ 9].append("_");
		matrix[ 8][10].append("#"); matrix[ 8][ 9].append("-"); matrix[ 8][11].append("_");
		matrix[ 7][12].append("#"); matrix[ 7][11].append("-"); matrix[ 7][13].append("_");
		matrix[ 6][14].append("#"); matrix[ 6][13].append("-");
		// center lamp
		for (int i=8; i<13; i++)
			matrix[i][0].append("8");
		matrix[7][0].append("G");
		matrix[6][0].append("b");
		// gateway arch
		matrix[6][15].append("_"   );
		matrix[5][15].append("#L"  );
		matrix[4][15].append(" ^L" );
		matrix[3][15].append("  ^L");
		matrix[2][15].append("   |");
		matrix[1][15].append("   @");
		matrix[0][15].append("   #");
		plot.run(chunk, matrix);
	}
	protected void generateCenterFloor(final ChunkData chunk,
			final int chunkX, final int chunkZ, final String axis) {
		final BlockPlotter plot =
			(new BlockPlotter())
			.axis(axis)
			.xz((0-chunkX)*15, (0-chunkZ)*15)
			.y((this.level_y + this.level_h) - 3)
			.whd(16, 6, 16);
		plot.type('#', Material.POLISHED_BLACKSTONE         );
		plot.type('x', Material.CHISELED_POLISHED_BLACKSTONE);
		plot.type('X', Material.GILDED_BLACKSTONE           );
		plot.type('*', Material.BLACKSTONE                  );
		plot.type('+', Material.POLISHED_BLACKSTONE_BRICK_WALL, "[up=true,north=tall,south=tall,east=tall,west=tall]");
		plot.type('-', Material.POLISHED_BLACKSTONE_SLAB,       "[type=top]"   );
		plot.type('_', Material.POLISHED_BLACKSTONE_BRICK_SLAB, "[type=bottom]");
		plot.type('.', Material.LIGHT, "[level=15]");
		plot.type(',', Material.LIGHT, "[level=9]" );
		final StringBuilder[][] matrix = plot.getMatrix3D();
		matrix[0][ 0].append("###########---"  ); matrix[1][ 0].append(" , , , , , ,  #" ); matrix[2][ 0].append("              ##");
		matrix[0][ 1].append("###########---"  ); matrix[1][ 1].append("              #" ); matrix[2][ 1].append("              ##");
		matrix[0][ 2].append("##########---"   ); matrix[1][ 2].append(" , , , , , , ##" ); matrix[2][ 2].append("             ###");
		matrix[0][ 3].append("##########---"   ); matrix[1][ 3].append("             #"  ); matrix[2][ 3].append("             ##" );
		matrix[0][ 4].append("#########---"    ); matrix[1][ 4].append(" , , , , ,  ##"  ); matrix[2][ 4].append("            ###" );
		matrix[0][ 5].append("########----"    ); matrix[1][ 5].append("            #"   ); matrix[2][ 5].append("            ##"  );
		matrix[0][ 6].append("#######----"     ); matrix[1][ 6].append(" , , , , , ##"   ); matrix[2][ 6].append("           ###"  );
		matrix[0][ 7].append("######-----"     ); matrix[1][ 7].append("           #"    ); matrix[2][ 7].append("           ##"   );
		matrix[0][ 8].append("#####-----"      ); matrix[1][ 8].append(" , , , ,  ##"    ); matrix[2][ 8].append("          ###"   );
		matrix[0][ 9].append("####-----"       ); matrix[1][ 9].append("         ##"     ); matrix[2][ 9].append("         ###"    );
		matrix[0][10].append("##------"        ); matrix[1][10].append(" , , ,  ##"      ); matrix[2][10].append("        ###"     );
		matrix[0][11].append("-------"         ); matrix[1][11].append("       ##"       ); matrix[2][11].append("       ###"      );
		matrix[0][12].append("-----"           ); matrix[1][12].append(" ,   ###"        ); matrix[2][12].append("     ####"       );
		matrix[0][13].append("--"              ); matrix[1][13].append("  ####"          ); matrix[2][13].append("  #####"         );
		matrix[0][14].append(""                ); matrix[1][14].append("###"             ); matrix[2][14].append("#####"           );
		matrix[0][15].append(""                ); matrix[1][15].append(""                ); matrix[2][15].append("###"             );
		matrix[3][ 0].append("x***************"); matrix[4][ 0].append("                "); matrix[5][ 0].append("                ");
		matrix[3][ 1].append("***#############"); matrix[4][ 1].append("    .   .   .   "); matrix[5][ 1].append("                ");
		matrix[3][ 2].append("**X##X##########"); matrix[4][ 2].append("                "); matrix[5][ 2].append("                ");
		matrix[3][ 3].append("*##*###*#######" ); matrix[4][ 3].append("  .   .   .   . "); matrix[5][ 3].append("                ");
		matrix[3][ 4].append("*###X####X#####" ); matrix[4][ 4].append("               +"); matrix[5][ 4].append("               _");
		matrix[3][ 5].append("*#X##*#####*##"  ); matrix[4][ 5].append("              ++"); matrix[5][ 5].append("              __");
		matrix[3][ 6].append("*#####*#######"  ); matrix[4][ 6].append("              +" ); matrix[5][ 6].append("              _" );
		matrix[3][ 7].append("*##*###X#####"   ); matrix[4][ 7].append("  .   .   .  ++" ); matrix[5][ 7].append("             __" );
		matrix[3][ 8].append("*#######*####"   ); matrix[4][ 8].append("             +"  ); matrix[5][ 8].append("             _"  );
		matrix[3][ 9].append("*###X####*##"    ); matrix[4][ 9].append("            ++"  ); matrix[5][ 9].append("            __"  );
		matrix[3][10].append("*##########"     ); matrix[4][10].append("           ++"   ); matrix[5][10].append("           __"   );
		matrix[3][11].append("*####*####"      ); matrix[4][11].append("  .   .   ++"    ); matrix[5][11].append("          __"    );
		matrix[3][12].append("*########"       ); matrix[4][12].append("         ++"     ); matrix[5][12].append("         __"     );
		matrix[3][13].append("*######"         ); matrix[4][13].append("       +++"      ); matrix[5][13].append("       ___"      );
		matrix[3][14].append("*####"           ); matrix[4][14].append("     +++"        ); matrix[5][14].append("     ___"        );
		matrix[3][15].append("*##"             ); matrix[4][15].append("  . ++"          ); matrix[5][15].append("    __"          );
		plot.run(chunk, matrix);
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
		final double thresh_lamps = this.thresh_lamps.get();
		final BlockPlotter plot =
			(new BlockPlotter())
			.axis("u"+FaceToAxString(direction)+FaceToAxString(side))
			.xz(x, z)
			.y(this.level_y+this.level_h)
			.whd(16, 3, 16);
		final String wall_dirs;
		switch (direction) {
		case NORTH:
		case SOUTH: wall_dirs = "north=low,south=low"; break;
		default:    wall_dirs = "east=low,west=low";   break;
		}
		plot.type('#', Material.POLISHED_BLACKSTONE);
		plot.type('*', Material.BLACKSTONE         );
		plot.type('+', Material.POLISHED_BLACKSTONE_BRICK_WALL, "[up=false,"+wall_dirs+"]");
		plot.type('i', Material.LANTERN            );
		plot.type('L', Material.LIGHT, "[level=15]");
		final StringBuilder[][] matrix = plot.getMatrix3D();
		double value_light;
		final Iab dir = FaceToIxz(direction);
		final int cx = chunkX * 16;
		final int cz = chunkZ * 16;
		for (int i=0; i<16; i++) {
			matrix[1][i].append("   +");
			matrix[0][i].append("*##" );
			value_light = this.noiseRoadLights.getNoise(cx+(dir.a*i), cz+(dir.b*i)) % 0.5;
			if (value_light > thresh_lamps) {
				matrix[2][i].append("   i");
				StringUtils.ReplaceInString(matrix[1][i], "L", 2);
			}
		}
		plot.run(chunk, matrix);
	}
	protected void generateRoadBottom(final ChunkData chunk,
			final BlockFace direction, final BlockFace side,
			final int chunkX, final int chunkZ, final int x, final int z) {
		final double thresh_light = this.thresh_lamps.get();
		final BlockPlotter plot =
				(new BlockPlotter())
				.axis("u"+FaceToAxString(direction)+FaceToAxString(side))
				.xz(x, z)
				.y(this.level_y-3)
				.whd(16, 6, 16);
		final String wall_dirs;
		switch (direction) {
		case NORTH:
		case SOUTH: wall_dirs = "north=low,south=low"; break;
		default:    wall_dirs = "east=low,west=low";   break;
		}
		plot.type('#', Material.POLISHED_BLACKSTONE);
		plot.type('*', Material.BLACKSTONE         );
		plot.type('+', Material.POLISHED_BLACKSTONE_BRICK_WALL, "[up=false,"+wall_dirs+"]");
		plot.type('i', Material.SOUL_LANTERN       );
		plot.type('x', Material.BARRIER            );
		plot.type('L', Material.LIGHT, "[level=15]");
		final StringBuilder[][] matrix = plot.getMatrix3D();
		double value_light;
		final Iab dir = FaceToIxz(direction);
		final int cx = chunkX * 16;
		final int cz = chunkZ * 16;
		for (int i=0; i<16; i++) {
			matrix[4][i].append("   +");
			matrix[3][i].append("*##x");
			matrix[2][i].append("   x");
			matrix[1][i].append("   x");
			matrix[0][i].append("   x");
			value_light = this.noiseRoadLights.getNoise(cx+(dir.a*i), cz+(dir.b*i)) % 0.5;
			if (value_light > thresh_light) {
				matrix[5][i].append("   i");
				StringUtils.ReplaceInString(matrix[4][i], "L", 2);
			}
		}
		plot.run(chunk, matrix);
	}



	// -------------------------------------------------------------------------------
	// pillars



	protected PillarType findPillarType(final int chunkX, final int chunkZ, final int x, final int z) {
		// near world center
		if (Math.abs(chunkX) < 30
		&&  Math.abs(chunkZ) < 30)
			return PillarType.PILLAR_NORM;
		final double thresh_void   = this.thresh_void.get();
		final double thresh_ladder = this.thresh_ladder.get();
		final double thresh_loot   = this.thresh_loot.get();
		// round to nearest chunk group and convert to block location
		final int px = Math.floorDiv(chunkX+1, 4) * 64;
		final int pz = Math.floorDiv(chunkZ+1, 4) * 64;
		final double val = this.noiseExits.getNoise(px, pz);
		if (val > thresh_void)
			return PillarType.PILLAR_VOID; // void shaft
		if (val > thresh_ladder) {
			return (
				((int)Math.floor(val*10000.0)) % 5 < 2
				? PillarType.PILLAR_DROP   // drop shaft
				: PillarType.PILLAR_LADDER // ladder shaft
			);
		}
		if (val > thresh_loot) {
			return (
				((int)Math.floor(val*10000.0)) % 2 == 0
				? PillarType.PILLAR_LOOT_UPPER
				: PillarType.PILLAR_LOOT_LOWER
			);
		}
		// normal pillar
		return PillarType.PILLAR_NORM;
	}



	protected void generatePillars(final ChunkData chunk,
			final BlockFace facing, final BlockFace side,
			final int chunkX, final int chunkZ, final int x, final int z) {
		final PillarType pillar_type = this.findPillarType(chunkX, chunkZ, x, z);
		final int mod_pillar;
		switch (facing) {
		case NORTH: case SOUTH: mod_pillar = Math.abs(chunkZ) % 4; break;
		case EAST:  case WEST:  mod_pillar = Math.abs(chunkX) % 4; break;
		default: throw new RuntimeException("Unknown direction: " + facing.toString());
		}
		switch (facing) {
		case NORTH:
			if (mod_pillar == 0) this.generatePillar(pillar_type, chunk, facing.getOppositeFace(), side, chunkX, chunkZ, x, 15-z); else
			if (mod_pillar == 1) this.generatePillar(pillar_type, chunk, facing,                   side, chunkX, chunkZ, x,    z); break;
		case SOUTH:
			if (mod_pillar == 3) this.generatePillar(pillar_type, chunk, facing.getOppositeFace(), side, chunkX, chunkZ, x, 15-z); else
			if (mod_pillar == 0) this.generatePillar(pillar_type, chunk, facing,                   side, chunkX, chunkZ, x,    z); break;
		case EAST:
			if (mod_pillar == 3) this.generatePillar(pillar_type, chunk, facing.getOppositeFace(), side, chunkX, chunkZ, 15-x, z); else
			if (mod_pillar == 0) this.generatePillar(pillar_type, chunk, facing,                   side, chunkX, chunkZ,    x, z); break;
		case WEST:
			if (mod_pillar == 0) this.generatePillar(pillar_type, chunk, facing.getOppositeFace(), side, chunkX, chunkZ, 15-x, z); else
			if (mod_pillar == 1) this.generatePillar(pillar_type, chunk, facing,                   side, chunkX, chunkZ,    x, z); break;
		default: throw new RuntimeException("Unknown direction: " + facing.toString());
		}
	}
	protected void generatePillar(final PillarType type, final ChunkData chunk,
			final BlockFace facing, final BlockFace side,
			final int chunkX, final int chunkZ, final int x, final int z) {
		final BlockPlotter plot =
				(new BlockPlotter())
				.axis("u"+FaceToAxString(facing)+FaceToAxString(side))
				.xz(x, z)
				.y(this.level_y)
				.whd(2, this.level_h+2, 5);
		plot.type('#', Material.DEEPSLATE_BRICKS);
		plot.type('w', Material.DARK_OAK_PLANKS);
		plot.type('%', Material.DEEPSLATE_BRICK_STAIRS, "[half=top,facing="   +FaceToAxisString(facing.getOppositeFace())+"]");
		plot.type('<', Material.DEEPSLATE_BRICK_STAIRS, "[half=top,facing="   +FaceToAxisString(side                  )+"]");
		plot.type('$', Material.DEEPSLATE_BRICK_STAIRS, "[half=top,facing="   +FaceToAxisString(side.getOppositeFace())+"]");
		plot.type('&', Material.DEEPSLATE_BRICK_STAIRS, "[half=bottom,facing="+FaceToAxisString(side.getOppositeFace())+"]");
		plot.type('H', Material.LADDER, "[facing="+FaceToAxisString(side.getOppositeFace())+"]");
		plot.type('/', Material.SPRUCE_TRAPDOOR,  "[half=top,facing="+FaceToAxisString(side)+"]");
		plot.type('~', Material.CRIMSON_TRAPDOOR, "[half=top,facing="+FaceToAxisString(side.getOppositeFace())+"]");
		plot.type('d', Material.SPRUCE_DOOR,      "[half=upper,facing="+FaceToAxisString(facing)+"]");
		plot.type('D', Material.SPRUCE_DOOR,      "[half=lower,facing="+FaceToAxisString(facing)+"]");
		plot.type('_', Material.POLISHED_BLACKSTONE_PRESSURE_PLATE);
		plot.type('-', Material.DARK_OAK_PRESSURE_PLATE);
		plot.type('+', Material.DEEPSLATE_TILE_WALL);
		plot.type('S', Material.DARK_OAK_WALL_SIGN, "[facing="+FaceToAxisString(facing.getOppositeFace())+"]");
		plot.type(',', Material.LIGHT, "[level=15]");
		plot.type('W', Material.WATER);
		plot.type('.', Material.AIR);
		int h = this.level_h;
		final StringBuilder[][] matrix = plot.getMatrix3D();
		switch (type) {
		// loot chest
		case PILLAR_LOOT_UPPER:
		case PILLAR_LOOT_LOWER:
		// normal pillar
		case PILLAR_NORM: {
			// top of pillar
			matrix[  h][0].append("   $");
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
				if (PillarType.PILLAR_LOOT_UPPER.equals(type)
				||  PillarType.PILLAR_LOOT_LOWER.equals(type)) {
					final Iab dir = FaceToIxz(side);
					final int xx = (dir.a * 2) + x;
					final int zz = (dir.b * 2) + z;
					final int yy;
					if (PillarType.PILLAR_LOOT_UPPER.equals(type)) {
						((Level_771)this.backlevel).loot_chests_upper.add((chunkX*16)+xx, (chunkZ*16)+zz);
						yy = this.level_y + this.level_h + 1;
					} else {
						((Level_771)this.backlevel).loot_chests_lower.add((chunkX*16)+xx, (chunkZ*16)+zz);
						yy = this.level_y + 1;
					}
					final BlockData barrel = Bukkit.createBlockData("barrel[facing=up]");
					chunk.setBlock(xx, yy, zz, barrel);
					(new ChestFiller_771(this.plugin, "level771", xx, yy, zz))
						.start();
				}
			}
			break;
		}
		// ladder shaft
		case PILLAR_LADDER: {
			((Level_771)this.backlevel).portal_ladder.add((chunkX*16)+x, (chunkZ*16)+z);
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
			((Level_771)this.backlevel).portal_drop.add((chunkX*16)+x, (chunkZ*16)+z);
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
			((Level_771)this.backlevel).portal_void.add((chunkX*16)+x, (chunkZ*16)+z);
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
		plot.run(chunk, matrix);
	}



	public class ChestFiller_771 extends DelayedChestFiller {

		public ChestFiller_771(final JavaPlugin plugin,
				final String worldName, final int x, final int y, final int z) {
			super(plugin, worldName, x, y, z);
		}

		@Override
		public void fill(final Inventory chest) {
			final double thresh_loot = Gen_771.this.thresh_loot.get();
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
				if (value > thresh_loot)
					chest.setItem(i, item);
			}
		}

	}



	// -------------------------------------------------------------------------------
	// configs



	@Override
	protected void loadConfig() {
		// params
		{
			final ConfigurationSection cfg = this.plugin.getLevelParams(771);
			this.noise_lamps_freq.set(cfg.getDouble("Noise-Lamps-Freq"));
			this.noise_exits_freq.set(cfg.getDouble("Noise-Exits-Freq"));
			this.noise_loot_freq .set(cfg.getDouble("Noise-Loot-Freq"));
			this.thresh_lamps    .set(cfg.getDouble("Thresh-Lamps"   ));
			this.thresh_void     .set(cfg.getDouble("Thresh-Void"    ));
			this.thresh_ladder   .set(cfg.getDouble("Thresh-Ladder"  ));
			this.thresh_loot     .set(cfg.getDouble("Thresh-Loot"    ));
		}
		// block types
		{
//TODO
		}
	}
	public static void ConfigDefaults(final FileConfiguration cfg) {
		// params
		cfg.addDefault("Level771.Params.Noise-Lamps-Freq", DEFAULT_NOISE_LAMPS_FREQ);
		cfg.addDefault("Level771.Params.Noise-Exits-Freq", DEFAULT_NOISE_EXITS_FREQ);
		cfg.addDefault("Level771.Params.Noise-Loot-Freq",  DEFAULT_NOISE_LOOT_FREQ );
		cfg.addDefault("Level771.Params.Thresh-Lamps",     DEFAULT_THRESH_LAMPS    );
		cfg.addDefault("Level771.Params.Thresh-Void",      DEFAULT_THRESH_VOID     );
		cfg.addDefault("Level771.Params.Thresh-Ladder",    DEFAULT_THRESH_LADDER   );
		cfg.addDefault("Level771.Params.Thresh-Loot",      DEFAULT_THRESH_LOOT     );
		// block types
//TODO
	}



}
