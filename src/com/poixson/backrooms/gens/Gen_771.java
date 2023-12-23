package com.poixson.backrooms.gens;

import static com.poixson.backrooms.worlds.Level_771.ENABLE_GEN_771;
import static com.poixson.utils.LocationUtils.FaceToAxString;
import static com.poixson.utils.LocationUtils.FaceToIxz;
import static com.poixson.utils.LocationUtils.ValueToFaceQuarter;

import java.util.LinkedList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Barrel;
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
import com.poixson.tools.dao.Iab;
import com.poixson.tools.plotter.BlockPlotter;
import com.poixson.tools.plotter.PlotterFactory;
import com.poixson.utils.FastNoiseLiteD;
import com.poixson.utils.StringUtils;


// 771 | Crossroads
public class Gen_771 extends BackroomsGen {

	// default params
	public static final double DEFAULT_NOISE_LAMPS_FREQ = 0.3;
	public static final double DEFAULT_NOISE_EXITS_FREQ = 0.5;
	public static final double DEFAULT_NOISE_LOOT_FREQ  = 0.1;
	public static final double DEFAULT_THRESH_LAMPS     = 0.42; // lanterns
	public static final double DEFAULT_THRESH_LADDER    = 0.81; // ladder shaft
	public static final double DEFAULT_THRESH_VOID      = 0.85; // void shaft
	public static final double DEFAULT_THRESH_LOOT      = 0.7;  // loot chest
	public static final int PILLAR_B_OFFSET = 10;

	// default blocks
//TODO

	// noise
	public final FastNoiseLiteD noiseRoadLights;
	public final FastNoiseLiteD noiseSpecial;
	public final FastNoiseLiteD noiseLoot;

	// params
	public final AtomicDouble noise_lamps_freq = new AtomicDouble(DEFAULT_NOISE_LAMPS_FREQ);
	public final AtomicDouble noise_exits_freq = new AtomicDouble(DEFAULT_NOISE_EXITS_FREQ);
	public final AtomicDouble noise_loot_freq  = new AtomicDouble(DEFAULT_NOISE_LOOT_FREQ );
	public final AtomicDouble thresh_lamps     = new AtomicDouble(DEFAULT_THRESH_LAMPS    );
	public final AtomicDouble thresh_ladder    = new AtomicDouble(DEFAULT_THRESH_LADDER   );
	public final AtomicDouble thresh_void      = new AtomicDouble(DEFAULT_THRESH_VOID     );
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



	public Gen_771(final BackroomsLevel backlevel,
			final int level_y, final int level_h) {
		super(backlevel, level_y, level_h);
		// noise
		this.noiseRoadLights = this.register(new FastNoiseLiteD());
		this.noiseSpecial = this.register(new FastNoiseLiteD());
		this.noiseLoot = this.register(new FastNoiseLiteD());
	}



	@Override
	public void initNoise() {
		// road lanterns
		this.noiseRoadLights.setFrequency(this.noise_lamps_freq.get());
		// special exits
		this.noiseSpecial.setFrequency(this.noise_exits_freq.get());
		// chest loot
		this.noiseLoot.setFrequency(this.noise_loot_freq.get());
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
			.whd(16, 14, 16)
			.build();
		plot.type('#', "minecraft:polished_blackstone_bricks"                 );
		plot.type('-', "minecraft:polished_blackstone_brick_slab[type=top]"   );
		plot.type('_', "minecraft:polished_blackstone_brick_slab[type=bottom]");
//TODO
plot.type('L', "minecraft:polished_blackstone_brick_stairs");
plot.type('^', "minecraft:polished_blackstone_brick_stairs");
//		plot.type('L', "minecraft:polished_blackstone_brick_stairs", Character.toString( Rotate(axis.charAt(2), 0.5))       );
//		plot.type('^', "minecraft:polished_blackstone_brick_stairs", Character.toString(        axis.charAt(2)      ), "top");
		plot.type('|', "minecraft:polished_blackstone_brick_wall"             );
		plot.type('@', "minecraft:chiseled_polished_blackstone"               );
		plot.type('!', Material.LIGHTNING_ROD                                 );
		plot.type('8', Material.CHAIN                                         );
		plot.type('G', Material.SHROOMLIGHT                                   );
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
		plot.type('#', Material.POLISHED_BLACKSTONE         );
		plot.type('x', Material.CHISELED_POLISHED_BLACKSTONE);
		plot.type('X', Material.GILDED_BLACKSTONE           );
		plot.type('*', Material.BLACKSTONE                  );
plot.type('+', Material.POLISHED_BLACKSTONE_BRICK_WALL);
plot.type('-', Material.POLISHED_BLACKSTONE_SLAB);
//		plot.type('+', Material.POLISHED_BLACKSTONE_BRICK_WALL, "autoface");
//		plot.type('-', Material.POLISHED_BLACKSTONE_SLAB, "top");
		plot.type('.', "minecraft:light[level=15]");
		plot.type(',', "minecraft:light[level=9]" );
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
		final double thresh_lamps = this.thresh_lamps.get();
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
//TODO
plot.type('+', Material.POLISHED_BLACKSTONE_BRICK_WALL);
//		plot.type('+', Material.POLISHED_BLACKSTONE_BRICK_WALL, "autoface");
		plot.type('i', Material.LANTERN);
		plot.type('L', "minecraft:light[level=15]");
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
		plot.run();
	}
	protected void generateRoadBottom(final ChunkData chunk,
			final BlockFace direction, final BlockFace side,
			final int chunkX, final int chunkZ, final int x, final int z) {
		final double thresh_light = this.thresh_lamps.get();
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
//TODO
plot.type('+', Material.POLISHED_BLACKSTONE_BRICK_WALL);
//		plot.type('+', Material.POLISHED_BLACKSTONE_BRICK_WALL, "autoface");
		plot.type('i', Material.SOUL_LANTERN);
		plot.type('L', "minecraft:light[level=15]");
		final StringBuilder[][] matrix = plot.getMatrix3D();
		double value_light;
		final Iab dir = FaceToIxz(direction);
		final int cx = chunkX * 16;
		final int cz = chunkZ * 16;
		for (int i=0; i<16; i++) {
			matrix[1][i].append("   +");
			matrix[0][i].append("*##" );
			value_light = this.noiseRoadLights.getNoise(cx+(dir.a*i), cz+(dir.b*i)) % 0.5;
			if (value_light > thresh_light) {
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
		final double thresh_ladder = this.thresh_ladder.get();
		final double thresh_void   = this.thresh_void.get();
		final double thresh_loot   = this.thresh_loot.get();
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
		if (valB > thresh_ladder) {
			if (valC > thresh_loot) {
			if (valC > thresh_void) pillar_type = PillarType.PILLAR_VOID;   // void shaft
			else                    pillar_type = PillarType.PILLAR_DROP;   // drop shaft
			} else                  pillar_type = PillarType.PILLAR_LADDER; // ladder shaft
		} else {
			if (valC < thresh_loot) pillar_type = PillarType.PILLAR_NORM;   // normal pillar
			else                                                            // loot chest
			if (((int)Math.floor(valC*10000.0)) % 2 == 0) pillar_type = PillarType.PILLAR_LOOT_UPPER;
			else                                          pillar_type = PillarType.PILLAR_LOOT_LOWER;
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
		plot.type('#', Material.DEEPSLATE_BRICKS                  );
		plot.type('w', Material.DARK_OAK_PLANKS                   );
//TODO
plot.type('%', Material.DEEPSLATE_BRICK_STAIRS);
plot.type('<', Material.DEEPSLATE_BRICK_STAIRS);
plot.type('$', Material.DEEPSLATE_BRICK_STAIRS);
plot.type('&', Material.DEEPSLATE_BRICK_STAIRS);
plot.type('H', Material.LADDER);
plot.type('/', Material.SPRUCE_TRAPDOOR);
plot.type('~', Material.CRIMSON_TRAPDOOR);
plot.type('d', Material.SPRUCE_DOOR);
plot.type('D', Material.SPRUCE_DOOR);
//		plot.type('%', Material.DEEPSLATE_BRICK_STAIRS, "top",    direction.getOppositeFace().toString().toLowerCase());
//		plot.type('<', Material.DEEPSLATE_BRICK_STAIRS, "top",    side.toString().toLowerCase());
//		plot.type('$', Material.DEEPSLATE_BRICK_STAIRS, "top",    side.getOppositeFace().toString().toLowerCase());
//		plot.type('&', Material.DEEPSLATE_BRICK_STAIRS, "bottom", side.getOppositeFace().toString().toLowerCase());
//		plot.type('H', Material.LADDER, side.getOppositeFace().toString().toLowerCase());
//		plot.type('/', Material.SPRUCE_TRAPDOOR,  "top", side.toString().toLowerCase());
//		plot.type('~', Material.CRIMSON_TRAPDOOR, "top", side.getOppositeFace().toString().toLowerCase());
//		plot.type('d', Material.SPRUCE_DOOR,      "top", direction.toString().toLowerCase());
//		plot.type('D', Material.SPRUCE_DOOR,   "bottom", direction.toString().toLowerCase());
		plot.type('_', Material.POLISHED_BLACKSTONE_PRESSURE_PLATE);
		plot.type('-', Material.DARK_OAK_PRESSURE_PLATE           );
//TODO
plot.type('+', Material.DEEPSLATE_TILE_WALL);
plot.type('S', Material.DARK_OAK_WALL_SIGN);
plot.type(',', "minecraft:light[level=15]");
//		plot.type('+', Material.DEEPSLATE_TILE_WALL, "autoface"   );
//		plot.type('S', Material.DARK_OAK_WALL_SIGN, direction.getOppositeFace().toString().toLowerCase());
//		plot.type(',', Material.LIGHT,  "15"                      );
		plot.type('W', Material.WATER                             );
		plot.type('.', Material.AIR                               );
		int h = this.level_h;
		final StringBuilder[][] matrix = plot.getMatrix3D();
		switch (type) {
		// loot chest
		case PILLAR_LOOT_UPPER:
		case PILLAR_LOOT_LOWER:
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
				if (PillarType.PILLAR_LOOT_UPPER.equals(type)
				||  PillarType.PILLAR_LOOT_LOWER.equals(type)) {
					final Iab dir = FaceToIxz(side);
					final int xx = (dir.a * 2) + x;
					final int zz = (dir.b * 2) + z;
					final int yy;
					if (PillarType.PILLAR_LOOT_UPPER.equals(type)) {
//TODO: duplicating 4 times
						((Level_771)this.backlevel).loot_chests_upper.add((chunkX*16)+xx, (chunkZ*16)+zz);
						yy = this.level_y + this.level_h + 1;
					} else {
//TODO: duplicating 4 times
						((Level_771)this.backlevel).loot_chests_lower.add((chunkX*16)+xx, (chunkZ*16)+zz);
						yy = this.level_y + 1;
					}
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
//TODO: duplicating 4 times
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
//TODO: duplicating 4 times
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
//TODO: duplicating 4 times
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
		plot.run();
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
			this.thresh_ladder   .set(cfg.getDouble("Thresh-Ladder"  ));
			this.thresh_void     .set(cfg.getDouble("Thresh-Void"    ));
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
		cfg.addDefault("Level771.Params.Thresh-Ladder",    DEFAULT_THRESH_LADDER   );
		cfg.addDefault("Level771.Params.Thresh-Void",      DEFAULT_THRESH_VOID     );
		cfg.addDefault("Level771.Params.Thresh-Loot",      DEFAULT_THRESH_LOOT     );
		// block types
//TODO
	}



}
