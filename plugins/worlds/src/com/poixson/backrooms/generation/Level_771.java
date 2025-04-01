package com.poixson.backrooms.gens;

import static com.poixson.utils.BlockUtils.StringToBlockDataDef;
import static com.poixson.utils.LocationUtils.AxToFace;
import static com.poixson.utils.LocationUtils.FaceToAxChar;
import static com.poixson.utils.LocationUtils.FaceToAxString;
import static com.poixson.utils.LocationUtils.FaceToAxisString;
import static com.poixson.utils.LocationUtils.FaceToIxz;
import static com.poixson.utils.LocationUtils.ValueToFaceQuarter;
import static com.poixson.utils.StringUtils.ReplaceInString;

import java.util.LinkedList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.ChunkGenerator.ChunkData;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.poixson.backrooms.BackroomsGen;
import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.backrooms.BackroomsWorld;
import com.poixson.backrooms.PreGenData;
import com.poixson.backrooms.worlds.BackWorld_771;
import com.poixson.tools.DelayedChestFiller;
import com.poixson.tools.xJavaPlugin;
import com.poixson.tools.abstractions.Tuple;
import com.poixson.tools.dao.Iab;
import com.poixson.tools.noise.FastNoiseLiteD;
import com.poixson.tools.plotter.BlockPlotter;


// 771 | Crossroads
public class Gen_771 extends BackroomsGen {

	// default params
	public static final int    DEFAULT_LEVEL_Y          = -61;
	public static final int    DEFAULT_LEVEL_H          = 360;
	public static final double DEFAULT_NOISE_LAMPS_FREQ = 0.3;
	public static final double DEFAULT_NOISE_EXITS_FREQ = 0.08;
	public static final double DEFAULT_NOISE_LOOT_FREQ  = 0.08;
	public static final double DEFAULT_THRESH_LAMPS     = 0.42; // lanterns
	public static final double DEFAULT_THRESH_VOID      = 0.9;  // void shaft
	public static final double DEFAULT_THRESH_LADDER    = 0.79; // ladder shaft
	public static final double DEFAULT_THRESH_LOOT      = 0.64; // loot chest

	// default blocks
	public static final String DEFAULT_BLOCK_NEXUS_ARCH              = "minecraft:polished_blackstone_bricks";
	public static final String DEFAULT_BLOCK_NEXUS_ARCH_SLAB_UPPER   = "minecraft:polished_blackstone_brick_slab[type=top]";
	public static final String DEFAULT_BLOCK_NEXUS_ARCH_SLAB_LOWER   = "minecraft:polished_blackstone_brick_slab[type=bottom]";
	public static final String DEFAULT_BLOCK_NEXUS_ARCH_STAIRS_UPPER = "minecraft:polished_blackstone_brick_stairs[facing=<FACING>]";
	public static final String DEFAULT_BLOCK_NEXUS_ARCH_STAIRS_LOWER = "minecraft:polished_blackstone_brick_stairs[facing=<FACING>,half=top]";
	public static final String DEFAULT_BLOCK_NEXUS_ARCH_PILLAR       = "minecraft:polished_blackstone_brick_wall";
	public static final String DEFAULT_BLOCK_NEXUS_ARCH_BASE         = "minecraft:chiseled_polished_blackstone";
	public static final String DEFAULT_BLOCK_NEXUS_FLOOR_UPPER       = "minecraft:polished_blackstone";
	public static final String DEFAULT_BLOCK_NEXUS_FLOOR_LOWER       = "minecraft:polished_blackstone";
	public static final String DEFAULT_BLOCK_NEXUS_FLOOR_LOWER_SLAB  = "minecraft:polished_blackstone_slab[type=top]";
	public static final String DEFAULT_BLOCK_NEXUS_HATCH             = "minecraft:chiseled_polished_blackstone";
	public static final String DEFAULT_BLOCK_NEXUS_FLAIR_HOT         = "minecraft:gilded_blackstone";
	public static final String DEFAULT_BLOCK_NEXUS_FLAIR_COOL        = "minecraft:blackstone";
	public static final String DEFAULT_BLOCK_NEXUS_WALL              = "minecraft:polished_blackstone_brick_wall[up=true,north=tall,south=tall,east=tall,west=tall]";
	public static final String DEFAULT_BLOCK_NEXUS_WALL_TOP          = "minecraft:polished_blackstone_brick_slab[type=bottom]";
	public static final String DEFAULT_BLOCK_NEXUS_LIGHT_UPPER       = "minecraft:light[level=15]";
	public static final String DEFAULT_BLOCK_NEXUS_LIGHT_LOWER       = "minecraft:light[level=9]";
	public static final String DEFAULT_BLOCK_ROAD_UPPER              = "minecraft:polished_blackstone";
	public static final String DEFAULT_BLOCK_ROAD_LOWER              = "minecraft:polished_blackstone";
	public static final String DEFAULT_BLOCK_ROAD_UPPER_CENTER       = "minecraft:blackstone";
	public static final String DEFAULT_BLOCK_ROAD_LOWER_CENTER       = "minecraft:blackstone";
	public static final String DEFAULT_BLOCK_ROAD_UPPER_WALL         = "minecraft:polished_blackstone_brick_wall[up=false,<DIRS>]";
	public static final String DEFAULT_BLOCK_ROAD_LOWER_WALL         = "minecraft:polished_blackstone_brick_wall[up=false,<DIRS>]";
	public static final String DEFAULT_BLOCK_ROAD_UPPER_LAMP         = "minecraft:lantern";
	public static final String DEFAULT_BLOCK_ROAD_LOWER_LAMP         = "minecraft:soul_lantern";
	public static final String DEFAULT_BLOCK_ROAD_UPPER_LIGHT        = "minecraft:light[level=15]";
	public static final String DEFAULT_BLOCK_ROAD_LOWER_LIGHT        = "minecraft:light[level=15]";
	public static final String DEFAULT_BLOCK_PILLAR                  = "minecraft:deepslate_bricks";
	public static final String DEFAULT_BLOCK_PILLAR_STAIRS_UPPER     = "minecraft:deepslate_brick_stairs[half=top,facing=<FACING>]";
	public static final String DEFAULT_BLOCK_PILLAR_STAIRS_LOWER     = "minecraft:deepslate_brick_stairs[half=bottom,facing=<FACING>]";
	public static final String DEFAULT_BLOCK_PILLAR_STAIRS_UNDER     = "minecraft:deepslate_brick_stairs[half=top,facing=<FACING>]";
	public static final String DEFAULT_BLOCK_PILLAR_STAIRS_BOTTOM    = "minecraft:deepslate_brick_stairs[half=top,facing=<FACING>]";
	public static final String DEFAULT_BLOCK_SHAFT_FLOOR             = "minecraft:dark_oak_planks";

	// params
	public final boolean enable_gen;
	public final int     level_y;
	public final int     level_h;
	public final double  thresh_lamps;
	public final double  thresh_void;
	public final double  thresh_ladder;
	public final double  thresh_loot;

	// blocks
	public final String block_nexus_arch;
	public final String block_nexus_arch_slab_upper;
	public final String block_nexus_arch_slab_lower;
	public final String block_nexus_arch_stairs_upper;
	public final String block_nexus_arch_stairs_lower;
	public final String block_nexus_arch_pillar;
	public final String block_nexus_arch_base;
	public final String block_nexus_floor_upper;
	public final String block_nexus_floor_lower;
	public final String block_nexus_floor_lower_slab;
	public final String block_nexus_hatch;
	public final String block_nexus_flair_hot;
	public final String block_nexus_flair_cool;
	public final String block_nexus_wall;
	public final String block_nexus_wall_top;
	public final String block_nexus_light_upper;
	public final String block_nexus_light_lower;
	public final String block_road_upper;
	public final String block_road_lower;
	public final String block_road_upper_center;
	public final String block_road_lower_center;
	public final String block_road_upper_wall;
	public final String block_road_lower_wall;
	public final String block_road_upper_lamp;
	public final String block_road_lower_lamp;
	public final String block_road_upper_light;
	public final String block_road_lower_light;
	public final String block_pillar;
	public final String block_pillar_stairs_upper;
	public final String block_pillar_stairs_lower;
	public final String block_pillar_stairs_under;
	public final String block_pillar_stairs_bottom;
	public final String block_shaft_floor;

	// noise
	public final FastNoiseLiteD noiseRoadLights;
	public final FastNoiseLiteD noiseExits;
	public final FastNoiseLiteD noiseLoot;

	public final BackWorld_771 world_771;



	public enum PillarType {
		PILLAR_NORM,
		PILLAR_LADDER,
		PILLAR_LOOT_UPPER,
		PILLAR_LOOT_LOWER,
		PILLAR_DROP,
		PILLAR_VOID,
	}



	public Gen_771(final BackroomsWorld backworld, final int seed) {
		super(backworld, null, seed);
		final int level_number = this.getLevelNumber();
		this.world_771 = (BackWorld_771) backworld;
		final ConfigurationSection cfgParams = this.plugin.getConfigLevelParams(level_number);
		final ConfigurationSection cfgBlocks = this.plugin.getConfigLevelBlocks(level_number);
		// params
		this.enable_gen    = cfgParams.getBoolean("Enable-Gen"   );
		this.level_y       = cfgParams.getInt(    "Level-Y"      );
		this.level_h       = cfgParams.getInt(    "Level-Height" );
		this.thresh_lamps  = cfgParams.getDouble( "Thresh-Lamps" );
		this.thresh_void   = cfgParams.getDouble( "Thresh-Void"  );
		this.thresh_ladder = cfgParams.getDouble( "Thresh-Ladder");
		this.thresh_loot   = cfgParams.getDouble( "Thresh-Loot"  );
		// block types
		this.block_nexus_arch              = cfgBlocks.getString("Nexus-Arch"             );
		this.block_nexus_arch_slab_upper   = cfgBlocks.getString("Nexus-Arch-Slab-Upper"  );
		this.block_nexus_arch_slab_lower   = cfgBlocks.getString("Nexus-Arch-Slab-Lower"  );
		this.block_nexus_arch_stairs_upper = cfgBlocks.getString("Nexus-Arch-Stairs-Upper");
		this.block_nexus_arch_stairs_lower = cfgBlocks.getString("Nexus-Arch-Stairs-Lower");
		this.block_nexus_arch_pillar       = cfgBlocks.getString("Nexus-Arch-Pillar"      );
		this.block_nexus_arch_base         = cfgBlocks.getString("Nexus-Arch-Base"        );
		this.block_nexus_floor_upper       = cfgBlocks.getString("Nexus-Floor-Upper"      );
		this.block_nexus_floor_lower       = cfgBlocks.getString("Nexus-Floor-Lower"      );
		this.block_nexus_floor_lower_slab  = cfgBlocks.getString("Nexus-Floor-Lower-Slab" );
		this.block_nexus_hatch             = cfgBlocks.getString("Nexus-Hatch"            );
		this.block_nexus_flair_hot         = cfgBlocks.getString("Nexus-Flair-Hot"        );
		this.block_nexus_flair_cool        = cfgBlocks.getString("Nexus-Flair-Cool"       );
		this.block_nexus_wall              = cfgBlocks.getString("Nexus-Wall"             );
		this.block_nexus_wall_top          = cfgBlocks.getString("Nexus-Wall-Top"         );
		this.block_nexus_light_upper       = cfgBlocks.getString("Nexus-Light-Upper"      );
		this.block_nexus_light_lower       = cfgBlocks.getString("Nexus-Light-Lower"      );
		this.block_road_upper              = cfgBlocks.getString("Road-Upper"             );
		this.block_road_lower              = cfgBlocks.getString("Road-Lower"             );
		this.block_road_upper_center       = cfgBlocks.getString("Road-Upper-Center"      );
		this.block_road_lower_center       = cfgBlocks.getString("Road-Lower-Center"      );
		this.block_road_upper_wall         = cfgBlocks.getString("Road-Upper-Wall"        );
		this.block_road_lower_wall         = cfgBlocks.getString("Road-Lower-Wall"        );
		this.block_road_upper_lamp         = cfgBlocks.getString("Road-Upper-Lamp"        );
		this.block_road_lower_lamp         = cfgBlocks.getString("Road-Lower-Lamp"        );
		this.block_road_upper_light        = cfgBlocks.getString("Road-Upper-Light"       );
		this.block_road_lower_light        = cfgBlocks.getString("Road-Lower-Light"       );
		this.block_pillar                  = cfgBlocks.getString("Pillar"                 );
		this.block_pillar_stairs_upper     = cfgBlocks.getString("Pillar-Stairs-Upper"    );
		this.block_pillar_stairs_lower     = cfgBlocks.getString("Pillar-Stairs-Lower"    );
		this.block_pillar_stairs_under     = cfgBlocks.getString("Pillar-Stairs-Under"    );
		this.block_pillar_stairs_bottom    = cfgBlocks.getString("Pillar-Stairs-Bottom"   );
		this.block_shaft_floor             = cfgBlocks.getString("Shaft-Floor"            );
		// noise
		this.noiseRoadLights = this.register(new FastNoiseLiteD());
		this.noiseExits      = this.register(new FastNoiseLiteD());
		this.noiseLoot       = this.register(new FastNoiseLiteD());
	}



	@Override
	public int getLevelNumber() {
		return 771;
	}



	@Override
	public int getLevelY() {
		return this.level_y;
	}
	@Override
	public int getOpenY() {
		return this.getMinY() + 3;
	}



	// -------------------------------------------------------------------------------
	// generate



	@Override
	public void generate(final PreGenData pregen,
			final LinkedList<Tuple<BlockPlotter, StringBuilder[][]>> plots,
			final ChunkData chunk, final int chunkX, final int chunkZ) {
		if (!this.enable_gen) return;
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
		final BlockFace facing = AxToFace(axis.charAt(2));
		final BlockData block_nexus_arch              = StringToBlockDataDef(this.block_nexus_arch,              DEFAULT_BLOCK_NEXUS_ARCH             );
		final BlockData block_nexus_arch_slab_upper   = StringToBlockDataDef(this.block_nexus_arch_slab_upper,   DEFAULT_BLOCK_NEXUS_ARCH_SLAB_UPPER  );
		final BlockData block_nexus_arch_slab_lower   = StringToBlockDataDef(this.block_nexus_arch_slab_lower,   DEFAULT_BLOCK_NEXUS_ARCH_SLAB_LOWER  );
		final BlockData block_nexus_arch_stairs_upper = StringToBlockDataDef(this.block_nexus_arch_stairs_upper, DEFAULT_BLOCK_NEXUS_ARCH_STAIRS_UPPER, "<FACING>", FaceToAxisString(facing.getOppositeFace()) );
		final BlockData block_nexus_arch_stairs_lower = StringToBlockDataDef(this.block_nexus_arch_stairs_lower, DEFAULT_BLOCK_NEXUS_ARCH_STAIRS_LOWER, "<FACING>", FaceToAxisString(facing)                   );
		final BlockData block_nexus_arch_pillar       = StringToBlockDataDef(this.block_nexus_arch_pillar,       DEFAULT_BLOCK_NEXUS_ARCH_PILLAR      );
		final BlockData block_nexus_arch_base         = StringToBlockDataDef(this.block_nexus_arch_base,         DEFAULT_BLOCK_NEXUS_ARCH_BASE        );
		if (block_nexus_arch              == null) throw new RuntimeException("Invalid block type for level 771 Nexus-Arch"             );
		if (block_nexus_arch_slab_upper   == null) throw new RuntimeException("Invalid block type for level 771 Nexus-Arch-Slab-Upper"  );
		if (block_nexus_arch_slab_lower   == null) throw new RuntimeException("Invalid block type for level 771 Nexus-Arch-Slab-Lower"  );
		if (block_nexus_arch_stairs_upper == null) throw new RuntimeException("Invalid block type for level 771 Nexus-Arch-Stairs-Upper");
		if (block_nexus_arch_stairs_lower == null) throw new RuntimeException("Invalid block type for level 771 Nexus-Arch-Stairs-Lower");
		if (block_nexus_arch_pillar       == null) throw new RuntimeException("Invalid block type for level 771 Nexus-Arch-Pillar"      );
		if (block_nexus_arch_base         == null) throw new RuntimeException("Invalid block type for level 771 Nexus-Arch-Base"        );
		final BlockPlotter plot =
			(new BlockPlotter())
			.axis(axis)
			.xz((0-chunkX)*15, (0-chunkZ)*15)
			.y(this.level_y + this.level_h + 1)
			.whd(16, 15, 16);
		plot.type('#', block_nexus_arch             );
		plot.type('-', block_nexus_arch_slab_upper  );
		plot.type('_', block_nexus_arch_slab_lower  );
		plot.type('L', block_nexus_arch_stairs_upper);
		plot.type('^', block_nexus_arch_stairs_lower);
		plot.type('|', block_nexus_arch_pillar      );
		plot.type('@', block_nexus_arch_base        );
		plot.type('!', Material.LIGHTNING_ROD );
		plot.type('8', Material.CHAIN         );
		plot.type('G', Material.SHROOMLIGHT   );
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
		for (int iy=8; iy<13; iy++)
			matrix[iy][0].append("8");
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
		final BlockData block_nexus_floor_upper      = StringToBlockDataDef(this.block_nexus_floor_upper,      DEFAULT_BLOCK_NEXUS_FLOOR_UPPER     );
		final BlockData block_nexus_floor_lower      = StringToBlockDataDef(this.block_nexus_floor_lower,      DEFAULT_BLOCK_NEXUS_FLOOR_LOWER     );
		final BlockData block_nexus_floor_lower_slab = StringToBlockDataDef(this.block_nexus_floor_lower_slab, DEFAULT_BLOCK_NEXUS_FLOOR_LOWER_SLAB);
		final BlockData block_nexus_hatch            = StringToBlockDataDef(this.block_nexus_hatch,            DEFAULT_BLOCK_NEXUS_HATCH           );
		final BlockData block_nexus_flair_hot        = StringToBlockDataDef(this.block_nexus_flair_hot,        DEFAULT_BLOCK_NEXUS_FLAIR_HOT       );
		final BlockData block_nexus_flair_cool       = StringToBlockDataDef(this.block_nexus_flair_cool,       DEFAULT_BLOCK_NEXUS_FLAIR_COOL      );
		final BlockData block_nexus_wall             = StringToBlockDataDef(this.block_nexus_wall,             DEFAULT_BLOCK_NEXUS_WALL            );
		final BlockData block_nexus_wall_top         = StringToBlockDataDef(this.block_nexus_wall_top,         DEFAULT_BLOCK_NEXUS_WALL_TOP        );
		final BlockData block_nexus_light_upper      = StringToBlockDataDef(this.block_nexus_light_upper,      DEFAULT_BLOCK_NEXUS_LIGHT_UPPER     );
		final BlockData block_nexus_light_lower      = StringToBlockDataDef(this.block_nexus_light_lower,      DEFAULT_BLOCK_NEXUS_LIGHT_LOWER     );
		if (block_nexus_floor_upper      == null) throw new RuntimeException("Invalid block type for level 771 Nexus-Floor-Upper"     );
		if (block_nexus_floor_lower      == null) throw new RuntimeException("Invalid block type for level 771 Nexus-Floor-Lower"     );
		if (block_nexus_floor_lower_slab == null) throw new RuntimeException("Invalid block type for level 771 Nexus-Floor-Lower-Slab");
		if (block_nexus_hatch            == null) throw new RuntimeException("Invalid block type for level 771 Nexus-Hatch"           );
		if (block_nexus_flair_hot        == null) throw new RuntimeException("Invalid block type for level 771 Nexus-Flair-Hot"       );
		if (block_nexus_flair_cool       == null) throw new RuntimeException("Invalid block type for level 771 Nexus-Flair-Cool"      );
		if (block_nexus_wall             == null) throw new RuntimeException("Invalid block type for level 771 Nexus-Wall"            );
		if (block_nexus_wall_top         == null) throw new RuntimeException("Invalid block type for level 771 Nexus-Wall-Top"        );
		if (block_nexus_light_upper      == null) throw new RuntimeException("Invalid block type for level 771 Nexus-Light-Upper"     );
		if (block_nexus_light_lower      == null) throw new RuntimeException("Invalid block type for level 771 Nexus-Light-Lower"     );
		final BlockPlotter plot =
			(new BlockPlotter())
			.axis(axis)
			.xz((0-chunkX)*15, (0-chunkZ)*15)
			.y((this.level_y + this.level_h) - 3)
			.whd(16, 6, 16);
		plot.type('#', block_nexus_floor_upper     );
		plot.type('%', block_nexus_floor_lower     );
		plot.type('-', block_nexus_floor_lower_slab);
		plot.type('/', block_nexus_hatch           );
		plot.type('X', block_nexus_flair_hot       );
		plot.type('*', block_nexus_flair_cool      );
		plot.type('+', block_nexus_wall            );
		plot.type('_', block_nexus_wall_top        );
		plot.type('.', block_nexus_light_upper     );
		plot.type(',', block_nexus_light_lower     );
		final StringBuilder[][] matrix = plot.getMatrix3D();
		matrix[0][ 0].append("%%%%%%%%%%%---"  ); matrix[1][ 0].append(" , , , , , ,  #" ); matrix[2][ 0].append("              ##");
		matrix[0][ 1].append("%%%%%%%%%%%---"  ); matrix[1][ 1].append("              #" ); matrix[2][ 1].append("              ##");
		matrix[0][ 2].append("%%%%%%%%%%---"   ); matrix[1][ 2].append(" , , , , , , ##" ); matrix[2][ 2].append("             ###");
		matrix[0][ 3].append("%%%%%%%%%%---"   ); matrix[1][ 3].append("             #"  ); matrix[2][ 3].append("             ##" );
		matrix[0][ 4].append("%%%%%%%%%---"    ); matrix[1][ 4].append(" , , , , ,  ##"  ); matrix[2][ 4].append("            ###" );
		matrix[0][ 5].append("%%%%%%%%----"    ); matrix[1][ 5].append("            #"   ); matrix[2][ 5].append("            ##"  );
		matrix[0][ 6].append("%%%%%%%----"     ); matrix[1][ 6].append(" , , , , , ##"   ); matrix[2][ 6].append("           ###"  );
		matrix[0][ 7].append("%%%%%%-----"     ); matrix[1][ 7].append("           #"    ); matrix[2][ 7].append("           ##"   );
		matrix[0][ 8].append("%%%%%-----"      ); matrix[1][ 8].append(" , , , ,  ##"    ); matrix[2][ 8].append("          ###"   );
		matrix[0][ 9].append("%%%%-----"       ); matrix[1][ 9].append("         ##"     ); matrix[2][ 9].append("         ###"    );
		matrix[0][10].append("%%------"        ); matrix[1][10].append(" , , ,  ##"      ); matrix[2][10].append("        ###"     );
		matrix[0][11].append("-------"         ); matrix[1][11].append("       ##"       ); matrix[2][11].append("       ###"      );
		matrix[0][12].append("-----"           ); matrix[1][12].append(" ,   ###"        ); matrix[2][12].append("     ####"       );
		matrix[0][13].append("--"              ); matrix[1][13].append("  ####"          ); matrix[2][13].append("  #####"         );
		matrix[0][14].append(""                ); matrix[1][14].append("###"             ); matrix[2][14].append("#####"           );
		matrix[0][15].append(""                ); matrix[1][15].append(""                ); matrix[2][15].append("###"             );
		matrix[3][ 0].append("/***************"); matrix[4][ 0].append("                "); matrix[5][ 0].append("                ");
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
		final String wall_dirs;
		switch (direction) {
		case NORTH:
		case SOUTH: wall_dirs = "north=low,south=low"; break;
		default:    wall_dirs = "east=low,west=low";   break;
		}
		final BlockData block_road_upper        = StringToBlockDataDef(this.block_road_upper,        DEFAULT_BLOCK_ROAD_UPPER       );
		final BlockData block_road_upper_center = StringToBlockDataDef(this.block_road_upper_center, DEFAULT_BLOCK_ROAD_UPPER_CENTER);
		final BlockData block_road_upper_wall   = StringToBlockDataDef(this.block_road_upper_wall,   DEFAULT_BLOCK_ROAD_UPPER_WALL, "<DIRS>", wall_dirs);
		final BlockData block_road_upper_lamp   = StringToBlockDataDef(this.block_road_upper_lamp,   DEFAULT_BLOCK_ROAD_UPPER_LAMP  );
		final BlockData block_road_upper_light  = StringToBlockDataDef(this.block_road_upper_light,  DEFAULT_BLOCK_ROAD_UPPER_LIGHT );
		if (block_road_upper        == null) throw new RuntimeException("Invalid block type for level 771 Road-Upper"       );
		if (block_road_upper_center == null) throw new RuntimeException("Invalid block type for level 771 Road-Upper-Center");
		if (block_road_upper_wall   == null) throw new RuntimeException("Invalid block type for level 771 Road-Upper-Wall"  );
		if (block_road_upper_lamp   == null) throw new RuntimeException("Invalid block type for level 771 Road-Upper-Lamp"  );
		if (block_road_upper_light  == null) throw new RuntimeException("Invalid block type for level 771 Road-Upper-Light" );
		final BlockPlotter plot =
			(new BlockPlotter())
			.axis("u"+FaceToAxChar(direction)+FaceToAxChar(side))
			.xz(x, z)
			.y(this.level_y+this.level_h)
			.whd(16, 3, 16);
		plot.type('#', block_road_upper       );
		plot.type('*', block_road_upper_center);
		plot.type('+', block_road_upper_wall  );
		plot.type('i', block_road_upper_lamp  );
		plot.type('L', block_road_upper_light );
		final StringBuilder[][] matrix = plot.getMatrix3D();
		double value_light;
		final Iab dir = FaceToIxz(direction);
		final int cx = chunkX * 16;
		final int cz = chunkZ * 16;
		for (int i=0; i<16; i++) {
			matrix[1][i].append("   +");
			matrix[0][i].append("*##" );
			value_light = this.noiseRoadLights.getNoise(cx+(dir.a*i), cz+(dir.b*i)) % 0.5;
			if (value_light > this.thresh_lamps) {
				matrix[2][i].append("   i");
				ReplaceInString(matrix[1][i], "L", 2);
			}
		}
		plot.run(chunk, matrix);
	}
	protected void generateRoadBottom(final ChunkData chunk,
			final BlockFace direction, final BlockFace side,
			final int chunkX, final int chunkZ, final int x, final int z) {
		final String wall_dirs;
		switch (direction) {
		case NORTH:
		case SOUTH: wall_dirs = "north=low,south=low"; break;
		default:    wall_dirs = "east=low,west=low";   break;
		}
		final BlockData block_road_lower        = StringToBlockDataDef(this.block_road_lower,        DEFAULT_BLOCK_ROAD_LOWER       );
		final BlockData block_road_lower_center = StringToBlockDataDef(this.block_road_lower_center, DEFAULT_BLOCK_ROAD_LOWER_CENTER);
		final BlockData block_road_lower_wall   = StringToBlockDataDef(this.block_road_lower_wall,   DEFAULT_BLOCK_ROAD_LOWER_WALL, "<DIRS>", wall_dirs);
		final BlockData block_road_lower_lamp   = StringToBlockDataDef(this.block_road_lower_lamp,   DEFAULT_BLOCK_ROAD_LOWER_LAMP  );
		final BlockData block_road_lower_light  = StringToBlockDataDef(this.block_road_lower_light,  DEFAULT_BLOCK_ROAD_LOWER_LIGHT );
		if (block_road_lower        == null) throw new RuntimeException("Invalid block type for level 771 Road-Lower"       );
		if (block_road_lower_center == null) throw new RuntimeException("Invalid block type for level 771 Road-Lower-Center");
		if (block_road_lower_wall   == null) throw new RuntimeException("Invalid block type for level 771 Road-Lower-Wall"  );
		if (block_road_lower_lamp   == null) throw new RuntimeException("Invalid block type for level 771 Road-Lower-Lamp"  );
		if (block_road_lower_light  == null) throw new RuntimeException("Invalid block type for level 771 Road-Lower-Light" );
		final BlockPlotter plot =
			(new BlockPlotter())
			.axis("u"+FaceToAxChar(direction)+FaceToAxChar(side))
			.xz(x, z)
			.y(this.level_y-3)
			.whd(16, 6, 16);
		plot.type('#', block_road_lower       );
		plot.type('*', block_road_lower_center);
		plot.type('+', block_road_lower_wall  );
		plot.type('i', block_road_lower_lamp  );
		plot.type('L', block_road_lower_light );
		plot.type('x', Material.BARRIER       );
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
			if (value_light > this.thresh_lamps) {
				matrix[5][i].append("   i");
				ReplaceInString(matrix[4][i], "L", 2);
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
		// round to nearest chunk group and convert to block location
		final int px = Math.floorDiv(chunkX+1, 4) * 64;
		final int pz = Math.floorDiv(chunkZ+1, 4) * 64;
		final double val = this.noiseExits.getNoise(px, pz);
		if (val > this.thresh_void)
			return PillarType.PILLAR_VOID; // void shaft
		if (val > this.thresh_ladder) {
			return (
				((int)Math.floor(val*10000.0)) % 5 < 2
				? PillarType.PILLAR_DROP   // drop shaft
				: PillarType.PILLAR_LADDER // ladder shaft
			);
		}
		if (val > this.thresh_loot) {
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
		final BlockData block_pillar               = StringToBlockDataDef(this.block_pillar,               DEFAULT_BLOCK_PILLAR);
		final BlockData block_pillar_stairs_upper  = StringToBlockDataDef(this.block_pillar_stairs_upper,  DEFAULT_BLOCK_PILLAR_STAIRS_UPPER,  "<FACING>", FaceToAxisString(facing.getOppositeFace()) );
		final BlockData block_pillar_stairs_lower  = StringToBlockDataDef(this.block_pillar_stairs_lower,  DEFAULT_BLOCK_PILLAR_STAIRS_LOWER,  "<FACING>", FaceToAxisString(side  .getOppositeFace()) );
		final BlockData block_pillar_stairs_under  = StringToBlockDataDef(this.block_pillar_stairs_under,  DEFAULT_BLOCK_PILLAR_STAIRS_UNDER,  "<FACING>", FaceToAxisString(side                    ) );
		final BlockData block_pillar_stairs_bottom = StringToBlockDataDef(this.block_pillar_stairs_bottom, DEFAULT_BLOCK_PILLAR_STAIRS_BOTTOM, "<FACING>", FaceToAxisString(side  .getOppositeFace()) );
		final BlockData block_shaft_floor          = StringToBlockDataDef(this.block_shaft_floor,          DEFAULT_BLOCK_SHAFT_FLOOR);
		if (block_pillar               == null) throw new RuntimeException("Invalid block type for level 771 Pillar"              );
		if (block_pillar_stairs_upper  == null) throw new RuntimeException("Invalid block type for level 771 Pillar-Stairs-Upper" );
		if (block_pillar_stairs_lower  == null) throw new RuntimeException("Invalid block type for level 771 Pillar-Stairs-Lower" );
		if (block_pillar_stairs_under  == null) throw new RuntimeException("Invalid block type for level 771 Pillar-Stairs-Under" );
		if (block_pillar_stairs_bottom == null) throw new RuntimeException("Invalid block type for level 771 Pillar-Stairs-Bottom");
		if (block_shaft_floor          == null) throw new RuntimeException("Invalid block type for level 771 Shaft-Floor"         );
		final BlockPlotter plot =
			(new BlockPlotter())
			.axis("u"+FaceToAxChar(facing)+FaceToAxChar(side))
			.xz(x, z)
			.y(this.level_y)
			.whd(2, this.level_h+2, 5);
		plot.type('#', block_pillar              );
		plot.type('w', block_shaft_floor         );
		plot.type('%', block_pillar_stairs_upper );
		plot.type('&', block_pillar_stairs_lower );
		plot.type('<', block_pillar_stairs_under );
		plot.type('$', block_pillar_stairs_bottom);
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
						this.world_771.loot_chests_upper.addLocation((chunkX*16)+xx, (chunkZ*16)+zz);
						yy = this.level_y + this.level_h + 1;
					} else {
						this.world_771.loot_chests_lower.addLocation((chunkX*16)+xx, (chunkZ*16)+zz);
						yy = this.level_y + 1;
					}
					final BlockData barrel = Bukkit.createBlockData("barrel[facing=up]");
					chunk.setBlock(xx, yy, zz, barrel);
					(new ChestFiller_771(this.plugin, "level_771", xx, yy, zz))
						.start();
				}
			}
			break;
		}
		// ladder shaft
		case PILLAR_LADDER: {
			this.world_771.portal_ladder.addLocation((chunkX*16)+x, (chunkZ*16)+z);
			matrix[h][0].append("   $");
			// trapdoor
			if (x == 0 && z == 0) {
				matrix[h+1][0].append("_ _");
				matrix[h+1][1].append(" _" );
				ReplaceInString(matrix[h][0], "/", 1);
			}
			if (x == -1 && z == -1)
				matrix[h+1][1].append(" _");
			matrix[--h][0].append("  #"); matrix[h][1].append("##"); if (x == 0 && z == 0) ReplaceInString(matrix[h][0], "H", 1);
			matrix[--h][0].append("  #"); matrix[h][1].append("##"); if (x == 0 && z == 0) ReplaceInString(matrix[h][0], "H", 1);
			matrix[--h][0].append("  #"); matrix[h][1].append("##"); if (x == 0 && z == 0) ReplaceInString(matrix[h][0], "H", 1);
			matrix[--h][0].append(" ##"); matrix[h][1].append("#%"); if (x == 0 && z == 0) ReplaceInString(matrix[h][0], "H", 0);
			matrix[--h][0].append(" #%"); matrix[h][1].append("#" ); if (x == 0 && z == 0) ReplaceInString(matrix[h][0], "H", 0);
			for (int iy=0; iy<h; iy++) {
				matrix[iy][0].append(" #");
				matrix[iy][1].append("#" );
				// ladder and lights
				if (x == 0 && z == 0)  ReplaceInString(matrix[iy][0], "H", 0);
				else if (iy % 10 == 0) ReplaceInString(matrix[iy][0], ",", 0);
			}
			// door
			if (x != 0 && z != 0) {
				ReplaceInString(matrix[2][1], "d", 0);
				ReplaceInString(matrix[1][1], "D", 0);
				ReplaceInString(matrix[1][0], "-", 0);
				ReplaceInString(matrix[0][1], " ", 0);
			}
			// floor inside shaft
			ReplaceInString(matrix[0][0], "w", 0);
			break;
		}
		// drop shaft to lower road
		case PILLAR_DROP: {
			this.world_771.portal_drop.addLocation((chunkX*16)+x, (chunkZ*16)+z);
			matrix[h+1][1].append("_"   );
			matrix[  h][0].append("~  $");
			for (int iy=0; iy<h; iy++) {
				matrix[iy][0].append("  #");
				matrix[iy][1].append("##" );
			}
			// doorway
			ReplaceInString(matrix[4][0], "WW", 0);
			ReplaceInString(matrix[3][0], "SS", 0);
			ReplaceInString(matrix[2][1], ".+", 0);
			ReplaceInString(matrix[1][1], ".+", 0);
			ReplaceInString(matrix[0][0], "ww", 0);
			break;
		}
		// void shaft
		case PILLAR_VOID: {
			this.world_771.portal_void.addLocation((chunkX*16)+x, (chunkZ*16)+z);
			matrix[h+1][1].append("_"   );
			matrix[  h][0].append("~  $");
			for (int iy=0; iy<h; iy++) {
				matrix[iy][0].append(" #");
				matrix[iy][1].append("#" );
			}
			// no floor inside shaft
			ReplaceInString(matrix[0][0], ".", 0);
			break;
		}
		default: break;
		}
		plot.run(chunk, matrix);
	}



	public class ChestFiller_771 extends DelayedChestFiller {

		public ChestFiller_771(final xJavaPlugin<BackroomsPlugin> plugin,
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
			for (int i=0; i<27; i++) {
				final int x = xx + (i % 9);
				final int y = zz + Math.floorDiv(i, 9);
				final double value = Gen_771.this.noiseLoot.getNoise(x, y);
				if (value > Gen_771.this.thresh_loot)
					chest.setItem(i, item);
			}
		}

	}



	// -------------------------------------------------------------------------------
	// configs



	@Override
	public void initNoise() {
		final ConfigurationSection cfgParams = this.plugin.getConfigLevelParams(this.getLevelNumber());
		// road lanterns
		this.noiseRoadLights.setFrequency(cfgParams.getDouble("Noise-Lamps-Freq"));
		// special exits
		this.noiseExits     .setFrequency(cfgParams.getDouble("Noise-Exits-Freq"));
		// loot noise
		this.noiseLoot      .setFrequency(cfgParams.getDouble("Noise-Loot-Freq" ));
	}



	@Override
	protected void configDefaults(final ConfigurationSection cfgParams, final ConfigurationSection cfgBlocks) {
		// params
		cfgParams.addDefault("Enable-Gen",       Boolean.TRUE            );
		cfgParams.addDefault("Level-Y",          DEFAULT_LEVEL_Y         );
		cfgParams.addDefault("Level-Height",     DEFAULT_LEVEL_H         );
		cfgParams.addDefault("Noise-Lamps-Freq", DEFAULT_NOISE_LAMPS_FREQ);
		cfgParams.addDefault("Noise-Exits-Freq", DEFAULT_NOISE_EXITS_FREQ);
		cfgParams.addDefault("Noise-Loot-Freq",  DEFAULT_NOISE_LOOT_FREQ );
		cfgParams.addDefault("Thresh-Lamps",     DEFAULT_THRESH_LAMPS    );
		cfgParams.addDefault("Thresh-Void",      DEFAULT_THRESH_VOID     );
		cfgParams.addDefault("Thresh-Ladder",    DEFAULT_THRESH_LADDER   );
		cfgParams.addDefault("Thresh-Loot",      DEFAULT_THRESH_LOOT     );
		// block types
		cfgBlocks.addDefault("Nexus-Arch",              DEFAULT_BLOCK_NEXUS_ARCH             );
		cfgBlocks.addDefault("Nexus-Arch-Slab-Upper",   DEFAULT_BLOCK_NEXUS_ARCH_SLAB_UPPER  );
		cfgBlocks.addDefault("Nexus-Arch-Slab-Lower",   DEFAULT_BLOCK_NEXUS_ARCH_SLAB_LOWER  );
		cfgBlocks.addDefault("Nexus-Arch-Stairs-Upper", DEFAULT_BLOCK_NEXUS_ARCH_STAIRS_UPPER);
		cfgBlocks.addDefault("Nexus-Arch-Stairs-Lower", DEFAULT_BLOCK_NEXUS_ARCH_STAIRS_LOWER);
		cfgBlocks.addDefault("Nexus-Arch-Pillar",       DEFAULT_BLOCK_NEXUS_ARCH_PILLAR      );
		cfgBlocks.addDefault("Nexus-Arch-Base",         DEFAULT_BLOCK_NEXUS_ARCH_BASE        );
		cfgBlocks.addDefault("Nexus-Floor-Upper",       DEFAULT_BLOCK_NEXUS_FLOOR_UPPER      );
		cfgBlocks.addDefault("Nexus-Floor-Lower",       DEFAULT_BLOCK_NEXUS_FLOOR_LOWER      );
		cfgBlocks.addDefault("Nexus-Floor-Lower-Slab",  DEFAULT_BLOCK_NEXUS_FLOOR_LOWER_SLAB );
		cfgBlocks.addDefault("Nexus-Hatch",             DEFAULT_BLOCK_NEXUS_HATCH            );
		cfgBlocks.addDefault("Nexus-Flair-Hot",         DEFAULT_BLOCK_NEXUS_FLAIR_HOT        );
		cfgBlocks.addDefault("Nexus-Flair-Cool",        DEFAULT_BLOCK_NEXUS_FLAIR_COOL       );
		cfgBlocks.addDefault("Nexus-Wall",              DEFAULT_BLOCK_NEXUS_WALL             );
		cfgBlocks.addDefault("Nexus-Wall-Top",          DEFAULT_BLOCK_NEXUS_WALL_TOP         );
		cfgBlocks.addDefault("Nexus-Light-Upper",       DEFAULT_BLOCK_NEXUS_LIGHT_UPPER      );
		cfgBlocks.addDefault("Nexus-Light-Lower",       DEFAULT_BLOCK_NEXUS_LIGHT_LOWER      );
		cfgBlocks.addDefault("Road-Upper",              DEFAULT_BLOCK_ROAD_UPPER             );
		cfgBlocks.addDefault("Road-Lower",              DEFAULT_BLOCK_ROAD_LOWER             );
		cfgBlocks.addDefault("Road-Upper-Center",       DEFAULT_BLOCK_ROAD_UPPER_CENTER      );
		cfgBlocks.addDefault("Road-Lower-Center",       DEFAULT_BLOCK_ROAD_LOWER_CENTER      );
		cfgBlocks.addDefault("Road-Upper-Wall",         DEFAULT_BLOCK_ROAD_UPPER_WALL        );
		cfgBlocks.addDefault("Road-Lower-Wall",         DEFAULT_BLOCK_ROAD_LOWER_WALL        );
		cfgBlocks.addDefault("Road-Upper-Lamp",         DEFAULT_BLOCK_ROAD_UPPER_LAMP        );
		cfgBlocks.addDefault("Road-Lower-Lamp",         DEFAULT_BLOCK_ROAD_LOWER_LAMP        );
		cfgBlocks.addDefault("Road-Upper-Light",        DEFAULT_BLOCK_ROAD_UPPER_LIGHT       );
		cfgBlocks.addDefault("Road-Lower-Light",        DEFAULT_BLOCK_ROAD_LOWER_LIGHT       );
		cfgBlocks.addDefault("Pillar",                  DEFAULT_BLOCK_PILLAR                 );
		cfgBlocks.addDefault("Pillar-Stairs-Upper",     DEFAULT_BLOCK_PILLAR_STAIRS_UPPER    );
		cfgBlocks.addDefault("Pillar-Stairs-Lower",     DEFAULT_BLOCK_PILLAR_STAIRS_LOWER    );
		cfgBlocks.addDefault("Pillar-Stairs-Under",     DEFAULT_BLOCK_PILLAR_STAIRS_UNDER    );
		cfgBlocks.addDefault("Pillar-Stairs-Bottom",    DEFAULT_BLOCK_PILLAR_STAIRS_BOTTOM   );
		cfgBlocks.addDefault("Shaft-Floor",             DEFAULT_BLOCK_SHAFT_FLOOR            );
	}



}
