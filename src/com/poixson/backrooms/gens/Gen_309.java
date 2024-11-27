package com.poixson.backrooms.gens;

import static com.poixson.utils.BlockUtils.StringToBlockDataDef;
import static com.poixson.utils.Utils.IsEmpty;

import java.util.LinkedList;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.backrooms.BackroomsGen;
import com.poixson.backrooms.BackroomsWorld;
import com.poixson.backrooms.PreGenData;
import com.poixson.backrooms.worlds.Level_000;
import com.poixson.tools.xRand;
import com.poixson.tools.abstractions.Triple;
import com.poixson.tools.abstractions.Tuple;
import com.poixson.tools.dao.Iab;
import com.poixson.tools.noise.FastNoiseLiteD;
import com.poixson.tools.noise.FastNoiseLiteD.FractalType;
import com.poixson.tools.plotter.BlockPlotter;
import com.poixson.tools.plotter.PlotterCache;
import com.poixson.tools.plotter.generation.TreeStyle;
import com.poixson.tools.worldstore.RandomMaze;
import com.poixson.utils.MathUtils;
import com.poixson.utils.StringUtils;


// 309 | Radio Station
public class Gen_309 extends BackroomsGen {
	public static final boolean DEBUG_GLASS_GRID = true;
	public static final String  DEBUG_STRUCTURE  = null;

	// default params
	public static final int    DEFAULT_LEVEL_H                  = 8;
	public static final int    DEFAULT_SUBFLOOR                 = 3;
	public static final int    DEFAULT_GROUND_THICKNESS_MIN     = 1;
	public static final int    DEFAULT_GROUND_THICKNESS_MAX     = 5;
	// path
	public static final int    DEFAULT_CELL_SIZE                = 256;
	public static final double DEFAULT_PATH_CHANCE              = 1.9;
	public static final double DEFAULT_PATH_WIDTH_MIN           = 3.3;
	public static final double DEFAULT_PATH_WIDTH_MAX           = 7.4;
	public static final double DEFAULT_PATH_WONDER              = 24.5;
	public static final double DEFAULT_PATH_FADE                = 2.9;
	public static final int    DEFAULT_PATH_BERM_MIN            = 6;
	public static final int    DEFAULT_PATH_BERM_MAX            = 16;
	public static final double DEFAULT_PATH_BERM_WEIGHT         = 1.8;
	// radio stations
	public static final double DEFAULT_RADIO_STATION_FADE       = 6.3;
	public static final double DEFAULT_PATH_RADIO_WONDER        = 4.7;
	public static final double DEFAULT_RADIO_STATION_CHANCE     = 1.9;
	public static final int    DEFAULT_RADIO_STATION_DISTANCE   = 5;
	public static final double DEFAULT_RADIO_STATION_MARGIN     = 0.35;
	// ground
	public static final double DEFAULT_NOISE_GROUND_FREQ        = 0.003;
	public static final int    DEFAULT_NOISE_GROUND_OCTAVE      = 3;
	public static final double DEFAULT_NOISE_GROUND_GAIN        = 0.65;
	public static final double DEFAULT_NOISE_GROUND_LACUN       = 2.0;
	// path wonder
	public static final double DEFAULT_NOISE_PATH_WONDER_FREQ   = 0.0057;
	public static final int    DEFAULT_NOISE_PATH_WONDER_OCTAVE = 3;
	public static final double DEFAULT_NOISE_PATH_WONDER_GAIN   = 0.5;
	public static final double DEFAULT_NOISE_PATH_WONDER_LACUN  = 10.0;
	// path width
	public static final double DEFAULT_NOISE_PATH_WIDTH_FREQ    = 0.0032;
	public static final int    DEFAULT_NOISE_PATH_WIDTH_OCTAVE  = 2;
	public static final double DEFAULT_NOISE_PATH_WIDTH_GAIN    = 18.0;
	public static final double DEFAULT_NOISE_PATH_WIDTH_LACUN   = 1.8;
	// path center (radio station)
	public static final double DEFAULT_NOISE_PATH_CENTER_FREQ   = 0.018;
	public static final int    DEFAULT_NOISE_PATH_CENTER_OCTAVE = 2;
	public static final double DEFAULT_NOISE_PATH_CENTER_GAIN   = 3.3;
	public static final double DEFAULT_NOISE_PATH_CENTER_LACUN  = 2.2;
	// tree placement
	public static final double DEFAULT_NOISE_TREES_PLACE_FREQ   = 0.14;
	public static final String DEFAULT_TREE_STYLE = TreeStyle.FAN_OAK.toString();

	// default blocks
	public static final String DEFAULT_BLOCK_SUBFLOOR    = "minecraft:stone";
	public static final String DEFAULT_BLOCK_DIRT        = "minecraft:dirt";
	public static final String DEFAULT_BLOCK_PATH        = "minecraft:dirt_path";
	public static final String DEFAULT_BLOCK_GRASS       = "minecraft:grass_block";
	public static final String DEFAULT_BLOCK_TREE_TRUNK  = "minecraft:birch_log";
	public static final String DEFAULT_BLOCK_TREE_BRANCH = "minecraft:birch_log";
	public static final String DEFAULT_BLOCK_TREE_LEAVES = "minecraft:birch_leaves";

	// params
	public final boolean enable_gen;
	public final boolean enable_top;
	public final int     level_y;
	public final int     subfloor;
	public final int     ground_thickness_min;
	public final int     ground_thickness_max;
	public final int     cell_size;
	public final int     cell_half;
	public final double  path_chance;
	public final double  path_width_min;
	public final double  path_width_max;
	public final double  path_wonder;
	public final double  path_fade;
	public final int     path_berm_min;
	public final int     path_berm_max;
	public final double  path_berm_weight;
	public final double  radio_station_fade;
	public final double  path_radio_wonder;
	public final double  radio_station_chance;
	public final int     radio_station_distance;
	public final double  radio_station_margin;
	// trees
	public final String  tree_style;
	public final double  tree_height_min;
	public final double  tree_height_max;
	public final double  tree_height_weight;
	public final double  tree_trunk_size_min;
	public final double  tree_trunk_size_max;
	public final double  tree_trunk_size_factor;
	public final double  tree_trunk_size_modify_min;
	public final double  tree_trunk_size_modify_max;
	public final double  tree_branches_from_top;
	public final double  tree_branch_zone_percent;
	public final double  tree_branch_length_min;
	public final double  tree_branch_length_max;
	public final double  tree_branch_length_weight;
	public final double  tree_branch_attenuation_min;
	public final double  tree_branch_attenuation_max;
	public final double  tree_branch_tier_len_add_min;
	public final double  tree_branch_tier_len_add_max;
	public final double  tree_branch_tier_space_min;
	public final double  tree_branch_tier_space_max;
	public final double  tree_branch_yaw_add_min;
	public final double  tree_branch_yaw_add_max;
	public final double  tree_branch_pitch_min;
	public final double  tree_branch_pitch_max;
	public final double  tree_branch_pitch_modify_min;
	public final double  tree_branch_pitch_modify_max;
	public final double  tree_branch_split_min_length;
	public final double  tree_branch_num_splits_min;
	public final double  tree_branch_num_splits_max;
	public final double  tree_leaves_thickness;

	// blocks
	public final String block_subfloor;
	public final String block_dirt;
	public final String block_path;
	public final String block_grass;
	public final String block_tree_trunk;
	public final String block_tree_branch;
	public final String block_tree_leaves;

	// noise
	public final FastNoiseLiteD noiseGround;
	public final FastNoiseLiteD noisePathWonder;
	public final FastNoiseLiteD noisePathWidth;
	public final FastNoiseLiteD noisePathCenter;
	public final FastNoiseLiteD noiseTreePlacement;

	public final xRand random_fade = (new xRand()).seed_time();

	public final RandomMaze maze;
	public final PlotterCache structures;

	public final Level_000 level_000;



	public Gen_309(final BackroomsWorld backworld, final int seed, final BackroomsGen gen_below) {
		super(backworld, gen_below, seed);
		final int level_number = this.getLevelNumber();
		this.level_000 = (Level_000) backworld;
		final ConfigurationSection cfgParams = this.plugin.getConfigLevelParams(level_number);
		final ConfigurationSection cfgBlocks = this.plugin.getConfigLevelBlocks(level_number);
		// params
		this.enable_gen                   = cfgParams.getBoolean("Enable-Gen"               );
		this.enable_top                   = cfgParams.getBoolean("Enable-Top"               );
		this.level_y                      = cfgParams.getInt(    "Level-Y"                  );
		this.subfloor                     = cfgParams.getInt(    "SubFloor"                 );
		this.ground_thickness_min         = cfgParams.getInt(    "Ground-Thickness-Min"     );
		this.ground_thickness_max         = cfgParams.getInt(    "Ground-Thickness-Max"     );
		final int cfg_cell_size           = cfgParams.getInt(    "Map-Cell-Size"            );
		this.cell_size                    = MathUtils.MinMax(MathUtils.RoundNormal(cfg_cell_size, 16), 16, 1024*1024);
		this.cell_half                    = Math.floorDiv(this.cell_size, 2);
		this.path_chance                  = cfgParams.getDouble( "Path-Chance"              );
		this.path_width_min               = cfgParams.getDouble( "Path-Width-Min"           );
		this.path_width_max               = cfgParams.getDouble( "Path-Width-Max"           );
		this.path_wonder                  = cfgParams.getDouble( "Path-Wonder-Factor"       );
		this.path_fade                    = cfgParams.getDouble( "Path-Fade-Factor"         );
		this.path_berm_min                = cfgParams.getInt(    "Path-Berm-Min"            );
		this.path_berm_max                = cfgParams.getInt(    "Path-Berm-Max"            );
		this.path_berm_weight             = cfgParams.getDouble( "Path-Berm-Weight"         );
		this.radio_station_fade           = cfgParams.getDouble( "Radio-Station-Fade-Factor");
		this.path_radio_wonder            = cfgParams.getDouble( "Path-Radio-Wonder"        );
		this.radio_station_chance         = MathUtils.MinMax(cfgParams.getDouble("Radio-Station-Chance"), 1.0, 99.0);
		this.radio_station_distance       = cfgParams.getInt(    "Radio-Station-Distance"   );
		this.radio_station_margin         = cfgParams.getDouble( "Radio-Station-Margin"     );
		// trees
		this.tree_style                   = cfgParams.getString( "Tree-Style",                   DEFAULT_TREE_STYLE);
		this.tree_height_min              = cfgParams.getDouble( "Tree-Height-Min",              Double.MIN_VALUE);
		this.tree_height_max              = cfgParams.getDouble( "Tree-Height-Max",              Double.MIN_VALUE);
		this.tree_height_weight           = cfgParams.getDouble( "Tree-Height-Weight",           Double.MIN_VALUE);
		this.tree_trunk_size_min          = cfgParams.getDouble( "Tree-Trunk-Size-Min",          Double.MIN_VALUE);
		this.tree_trunk_size_max          = cfgParams.getDouble( "Tree-Trunk-Size-Max",          Double.MIN_VALUE);
		this.tree_trunk_size_factor       = cfgParams.getDouble( "Tree-Trunk-Size-Factor",       Double.MIN_VALUE);
		this.tree_trunk_size_modify_min   = cfgParams.getDouble( "Tree-Trunk-Size-Modify-Min",   Double.MIN_VALUE);
		this.tree_trunk_size_modify_max   = cfgParams.getDouble( "Tree-Trunk-Size-Modify-Max",   Double.MIN_VALUE);
		this.tree_branches_from_top       = cfgParams.getDouble( "Tree-Branches-From-Top",       Double.MIN_VALUE);
		this.tree_branch_zone_percent     = cfgParams.getDouble( "Tree-Branch-Zone-Percent",     Double.MIN_VALUE);
		this.tree_branch_length_min       = cfgParams.getDouble( "Tree-Branch-Length-Min",       Double.MIN_VALUE);
		this.tree_branch_length_max       = cfgParams.getDouble( "Tree-Branch-Length-Max",       Double.MIN_VALUE);
		this.tree_branch_length_weight    = cfgParams.getDouble( "Tree-Branch-Length-Weight",    Double.MIN_VALUE);
		this.tree_branch_attenuation_min  = cfgParams.getDouble( "Tree-Branch-Attenuation-Min",  Double.MIN_VALUE);
		this.tree_branch_attenuation_max  = cfgParams.getDouble( "Tree-Branch-Attenuation-Max",  Double.MIN_VALUE);
		this.tree_branch_tier_len_add_min = cfgParams.getDouble( "Tree-Branch-Tier-Len-Add-Min", Double.MIN_VALUE);
		this.tree_branch_tier_len_add_max = cfgParams.getDouble( "Tree-Branch-Tier-Len-Add-Max", Double.MIN_VALUE);
		this.tree_branch_tier_space_min   = cfgParams.getDouble( "Tree-Branch-Tier-Space-Min",   Double.MIN_VALUE);
		this.tree_branch_tier_space_max   = cfgParams.getDouble( "Tree-Branch-Tier-Space-Max",   Double.MIN_VALUE);
		this.tree_branch_yaw_add_min      = cfgParams.getDouble( "Tree-Branch-Yaw-Add-Min",      Double.MIN_VALUE);
		this.tree_branch_yaw_add_max      = cfgParams.getDouble( "Tree-Branch-Yaw-Add-Max",      Double.MIN_VALUE);
		this.tree_branch_pitch_min        = cfgParams.getDouble( "Tree-Branch-Pitch-Min",        Double.MIN_VALUE);
		this.tree_branch_pitch_max        = cfgParams.getDouble( "Tree-Branch-Pitch-Max",        Double.MIN_VALUE);
		this.tree_branch_pitch_modify_min = cfgParams.getDouble( "Tree-Branch-Pitch-Modify-Min", Double.MIN_VALUE);
		this.tree_branch_pitch_modify_max = cfgParams.getDouble( "Tree-Branch-Pitch-Modify-Max", Double.MIN_VALUE);
		this.tree_branch_split_min_length = cfgParams.getDouble( "Tree-Branch-Split-Min-Length", Double.MIN_VALUE);
		this.tree_branch_num_splits_min   = cfgParams.getDouble( "Tree-Branch-Num-Splits-Min",   Double.MIN_VALUE);
		this.tree_branch_num_splits_max   = cfgParams.getDouble( "Tree-Branch-Num-Splits-Max",   Double.MIN_VALUE);
		this.tree_leaves_thickness        = cfgParams.getDouble( "Tree-Leaves-Thickness",        Double.MIN_VALUE);
		// block types
		this.block_subfloor    = cfgBlocks.getString("SubFloor"   );
		this.block_dirt        = cfgBlocks.getString("Dirt"       );
		this.block_path        = cfgBlocks.getString("Path"       );
		this.block_grass       = cfgBlocks.getString("Grass"      );
		this.block_tree_trunk  = cfgBlocks.getString("Tree-Trunk" );
		this.block_tree_branch = cfgBlocks.getString("Tree-Branch");
		this.block_tree_leaves = cfgBlocks.getString("Tree-Leaves");
		// noise
		this.noiseGround        = this.register(new FastNoiseLiteD());
		this.noisePathWonder    = this.register(new FastNoiseLiteD());
		this.noisePathWidth     = this.register(new FastNoiseLiteD());
		this.noisePathCenter    = this.register(new FastNoiseLiteD());
		this.noiseTreePlacement = this.register(new FastNoiseLiteD());
		// path maze
		this.maze = new RandomMaze(this.plugin, "level_000", "maze_309", this.path_chance);
		// structures cache
		final String path_local = this.plugin.getDataFolder().getPath() + "/structures/radiostations/";
		final String path_res = "structures/309/";
		this.structures = new PlotterCache(path_local, path_res, this.getClass());
	}



	@Override
	public int getLevelNumber() {
		return 309;
	}



	@Override
	public int getLevelY() {
		return this.level_y;
	}
	@Override
	public int getOpenY() {
		return this.getMinY() + this.subfloor + 1;
	}

	@Override
	public int getMinY() {
		return this.getLevelY() + this.bedrock_barrier;
	}



	public boolean isNear(final Location loc, final int distance) {
		return this.isNear(loc.getBlockX(), loc.getBlockZ(), distance);
	}
	public boolean isNear(final int x, final int z, final int distance) {
		final int maze_x = Math.floorDiv(x, this.cell_size);
		final int maze_z = Math.floorDiv(z, this.cell_size);
		final int dist = MathUtils.MinMax(Math.floorDiv(distance, this.cell_size), 1, 99);
		// nearest radio station
		final Tuple<Iab, Map<String, Object>>[] near =
			this.level_000.radio_stations
				.near(maze_x, maze_z, dist, false, false);
		if (!IsEmpty(near)) {
			for (final Tuple<Iab, Map<String, Object>> entry : near) {
				if (!IsEmpty( (String) entry.val.get("structure") ))
					return true;
			}
		}
		return false;
	}



	@Override
	public void register() {
		super.register();
		this.maze.start();
	}
	@Override
	public void unregister() {
		super.unregister();
		this.maze.stop();
	}



	@Override
	public void generate(final PreGenData pregen,
			final LinkedList<Tuple<BlockPlotter, StringBuilder[][]>> plots,
			final ChunkData chunk, final int chunkX, final int chunkZ) {
		if (!this.enable_gen) return;
		final BlockData block_dirt     = StringToBlockDataDef(this.block_dirt,     DEFAULT_BLOCK_DIRT    );
		final BlockData block_path     = StringToBlockDataDef(this.block_path,     DEFAULT_BLOCK_PATH    );
		final BlockData block_grass    = StringToBlockDataDef(this.block_grass,    DEFAULT_BLOCK_GRASS   );
		final BlockData block_subfloor = StringToBlockDataDef(this.block_subfloor, DEFAULT_BLOCK_SUBFLOOR);
		if (block_dirt     == null) throw new RuntimeException("Invalid block type for level 309 Dirt"    );
		if (block_path     == null) throw new RuntimeException("Invalid block type for level 309 Path"    );
		if (block_grass    == null) throw new RuntimeException("Invalid block type for level 309 Grass"   );
		if (block_subfloor == null) throw new RuntimeException("Invalid block type for level 309 SubFloor");
		final int y_base  = this.level_y + this.bedrock_barrier;
		final int y_floor = y_base + this.subfloor;
		final int cell_center_x = MathUtils.FloorNormal(chunkX*16, this.cell_size) + this.cell_half;
		final int cell_center_z = MathUtils.FloorNormal(chunkZ*16, this.cell_size) + this.cell_half;
		final double noise_path_center = Gen_309.this.noisePathWonder.getNoise(cell_center_x, cell_center_z);
		final int path_center_x = (int)Math.floor( ((double)cell_center_x) + (noise_path_center*this.path_wonder) );
		final int path_center_z = (int)Math.floor( ((double)cell_center_z) + (noise_path_center*this.path_wonder) );
		final int maze_x = Math.floorDiv(chunkX*16, this.cell_size);
		final int maze_z = Math.floorDiv(chunkZ*16, this.cell_size);
		final boolean is_debug_structure = !IsEmpty(DEBUG_STRUCTURE);
		final Map<String, Object> keyval = this.level_000.radio_stations.getKeyValMap(maze_x, maze_z, false, true);
		if (keyval == null) throw new NullPointerException("Failed to get region keyval map");
		final double maze_value = this.maze.getMazeEntry(maze_x, maze_z, false, true);
		final double maze_value_n = this.maze.getMazeEntry(maze_x,   maze_z-1, false, true);
		final double maze_value_s = this.maze.getMazeEntry(maze_x,   maze_z+1, false, true);
		final double maze_value_e = this.maze.getMazeEntry(maze_x+1, maze_z,   false, true);
		final double maze_value_w = this.maze.getMazeEntry(maze_x-1, maze_z,   false, true);
		final boolean has_path   = ((int)Math.ceil(maze_value  ) == 1);
		final boolean has_path_n = ((int)Math.ceil(maze_value_n) == 1);
		final boolean has_path_s = ((int)Math.ceil(maze_value_s) == 1);
		final boolean has_path_e = ((int)Math.ceil(maze_value_e) == 1);
		final boolean has_path_w = ((int)Math.ceil(maze_value_w) == 1);
		final int path_dirs =
			(has_path_n ? 1 : 0) +
			(has_path_s ? 1 : 0) +
			(has_path_e ? 1 : 0) +
			(has_path_w ? 1 : 0);
		boolean has_radio_station = false;
		// cell has path
		if (is_debug_structure
		||  has_path) {
			// existing radio station
			if (keyval.containsKey("structure")) {
				if (!IsEmpty((String)keyval.get("structure")))
					has_radio_station = true;
			// find straight path
			} else
			if (is_debug_structure
			||  path_dirs == 1) {
				int path_dirsB = 0;
				if (has_path_n && (int)Math.ceil(this.maze.getMazeEntry(maze_x,   maze_z-2, false, true)) == 1) path_dirsB++;
				if (has_path_s && (int)Math.ceil(this.maze.getMazeEntry(maze_x,   maze_z+2, false, true)) == 1) path_dirsB++;
				if (has_path_e && (int)Math.ceil(this.maze.getMazeEntry(maze_x+2, maze_z,   false, true)) == 1) path_dirsB++;
				if (has_path_w && (int)Math.ceil(this.maze.getMazeEntry(maze_x-2, maze_z,   false, true)) == 1) path_dirsB++;
				if (is_debug_structure
				||  path_dirsB == 1) {
					// radio station chance
					if (is_debug_structure
					|| this.radio_station_chance == 1.0
					|| (int)Math.floor(this.random.nextDouble(1.0, this.radio_station_chance)) == 1) {
						// check near
						if (is_debug_structure
						|| !this.isNear(path_center_x, path_center_z, this.radio_station_distance)) {
							has_radio_station = true;
							final String structure = (
								is_debug_structure
								? DEBUG_STRUCTURE
								: this.nextRadioStation(maze_x, maze_z)
							);
							keyval.put("structure", structure);
							keyval.put("x", Integer.valueOf(path_center_x));
							keyval.put("z", Integer.valueOf(path_center_z));
//TODO: radio_y detect ground height
keyval.put("y", Integer.valueOf(142));
							final Triple<BlockPlotter, StringBuilder[][], String> tup = this.structures.get(structure);
							final BlockPlotter plot = tup.key;
							// half width plus margin percent
							final int radius = (int) Math.ceil( ((double)Math.max(plot.w, plot.d)) * (0.5+this.radio_station_margin) );
							keyval.put("clearing", Integer.valueOf(radius));
						} // end check near
					} // end radio station chance
				} // end straight path
			} // path end
		} // end cell path
		// place blocks
		for (int iz=0; iz<16; iz++) {
			final int zz = (chunkZ * 16) + iz;
			for (int ix=0; ix<16; ix++) {
				final int xx = (chunkX * 16) + ix;
				// barrier
				for (int iy=0; iy<this.bedrock_barrier; iy++)
					chunk.setBlock(ix, this.level_y+iy, iz, Material.BEDROCK);
				// subfloor
				for (int iy=0; iy<this.subfloor; iy++)
					chunk.setBlock(ix, y_base+iy, iz, block_subfloor);
				// ground
				final int elevation;
				{
					final double g1 = this.noiseGround.getNoise(xx, zz);
					final double g2 = (g1 < 0.0 ? g1 * 0.6 : g1) + 1.0;
					final double ground_h = (double) (this.ground_thickness_max - this.ground_thickness_min);
					elevation = ((int) (g2 * (ground_h / 2.0))) + this.ground_thickness_min;
				}
				for (int iy=0; iy<elevation; iy++)
					chunk.setBlock(ix, y_floor+iy, iz, block_dirt);
				// path
				boolean is_path   = false;
				boolean is_center = false;
				if (is_debug_structure
				||  has_path) {
					// path width
					final double path_width;
					{
						final double noise_width = this.noisePathWidth.getNoise(xx, zz);
						final double path_width_percent = (noise_width+1.0)/2.0;
						path_width = MathUtils.Remap(this.path_width_min, this.path_width_max, path_width_percent);
					}
					// north/south path
					if (is_debug_structure
					|| (has_path_n && zz <  path_center_z)
					|| (has_path_s && zz >= path_center_z)) {
						final double noise_path = this.noisePathWonder.getNoise(cell_center_x, zz);
						final int path_x = (int)Math.floor( ((double)cell_center_x) + (noise_path*this.path_wonder) );
						final double dist_path = (double)Math.abs(xx - path_x);
						if (dist_path < path_width) {
							if (this.path_fade > 0.0) {
								final double fade = this.random_fade.nextDouble(0.0, this.path_fade);
								if (dist_path <= path_width-fade)
									is_path = true;
							} else {
								is_path = true;
							}
						}
					}
					// east/west path
					if (is_debug_structure
					|| (has_path_e && xx >= path_center_x)
					|| (has_path_w && xx <  path_center_x)) {
						final double noise_path = this.noisePathWonder.getNoise(xx, cell_center_z);
						final int path_z = (int)Math.floor( ((double)cell_center_z) + (noise_path*this.path_wonder) );
						final double dist_path = (double)Math.abs(zz - path_z);
						if (dist_path < path_width) {
							if (this.path_fade > 0.0) {
								final double fade = this.random_fade.nextDouble(0.0, this.path_fade);
								if (dist_path <= path_width-fade)
									is_path = true;
							} else {
								is_path = true;
							}
						}
					}
					// center clearing / radio station
					if (is_debug_structure
					|| (has_path && path_dirs > 0)) {
						double center_size = 0.0;
						if (has_radio_station) {
							final int w = MathUtils.CastInt(keyval.get("clearing"), Integer.MIN_VALUE);
							final double wonder_noise = this.noisePathCenter.getNoise(xx, zz);
							final double dist_wonder = this.path_radio_wonder * wonder_noise;
							center_size = (w>0 ? (double)w : path_width) + dist_wonder;
							if (this.radio_station_fade > 0.0) {
								final double fade = this.random_fade.nextDouble(0.0, this.radio_station_fade);
								center_size -= fade;
							}
						} else {
							if (path_dirs >= 3) center_size = path_width * 1.6;
							else                center_size = path_width * 1.2;
							if (this.path_fade > 0.0) {
								final double fade = this.random_fade.nextDouble(0.0, this.path_fade);
								center_size -= fade;
							}
						}
						final double dist = MathUtils.Distance2D(xx, zz, path_center_x, path_center_z);
						if (dist <= center_size)
							is_center = true;
					}
				} // end path
				// surface
				if (is_path || is_center) chunk.setBlock(ix, y_floor+elevation, iz, block_path );
				else                      chunk.setBlock(ix, y_floor+elevation, iz, block_grass);
				// debug grid
				if (DEBUG_GLASS_GRID) {
					if (xx % this.cell_size == 0 || zz % this.cell_size == 0)
						chunk.setBlock(ix, y_floor+elevation+2, iz, Material.GLASS);
					final int group_width = this.cell_size * this.maze.group_size;
					if (xx % group_width == 0
					&&  zz % group_width == 0) {
						for (int iy=0; iy<50; iy++)
							chunk.setBlock(ix, y_floor+elevation+iy, iz, Material.GLASS);
					}
				}
			} // end x
		} // end z
		// structure
		if (keyval != null) {
			final String structure = (String) keyval.get("structure");
			if (!IsEmpty(structure)) {
				final Triple<BlockPlotter, StringBuilder[][], String> tup =
					this.structures.get(structure);
				final BlockPlotter      plot   = tup.key;
				final StringBuilder[][] matrix = tup.val;
				final String            script = tup.ent;
				if (plot   == null) throw new NullPointerException("plot");
				if (matrix == null) throw new NullPointerException("matrix");
				final int x = MathUtils.CastInt(keyval.get("x"), Integer.MIN_VALUE);
				final int y = MathUtils.CastInt(keyval.get("y"), Integer.MIN_VALUE);
				final int z = MathUtils.CastInt(keyval.get("z"), Integer.MIN_VALUE);
				if (x == Integer.MIN_VALUE
				||  y == Integer.MIN_VALUE
				||  z == Integer.MIN_VALUE) {
					final char[] missing = new char[3];
					if (x == Integer.MIN_VALUE) missing[0] = 'x';
					if (y == Integer.MIN_VALUE) missing[1] = 'y';
					if (z == Integer.MIN_VALUE) missing[2] = 'z';
					final String str = StringUtils.MergeStrings(", ", missing);
					throw new RuntimeException("Failed to get structure location: "+str);
				}
				// place structure
				plot.x = x - (chunkX * 16) - Math.floorDiv(plot.w, 2);
				plot.z = z - (chunkZ * 16) - Math.floorDiv(plot.d, 2);
				plot.y = y;
				plot.run(chunk, matrix);
				// run script
				if (!IsEmpty(script)) {
//TODO
				}
			}
		} // end structure
	}



	public String nextRadioStation(final int maze_x, final int maze_z) {
//TODO: check nearby structures
return "radio_station_1";
	}



	// -------------------------------------------------------------------------------
	// configs



	@Override
	protected void initNoise() {
		super.initNoise();
		final ConfigurationSection cfgParams = this.plugin.getConfigLevelParams(this.getLevelNumber());
		// path ground
		this.noiseGround.setFrequency(         cfgParams.getDouble("Noise-Ground-Freq"  ) );
		this.noiseGround.setFractalOctaves(    cfgParams.getInt(   "Noise-Ground-Octave") );
		this.noiseGround.setFractalGain(       cfgParams.getDouble("Noise-Ground-Gain"  ) );
		this.noiseGround.setFractalLacunarity( cfgParams.getDouble("Noise-Ground-Lacun" ) );
		this.noiseGround.setFractalType(       FractalType.Ridged                         );
		// path
		this.noisePathWonder.setFrequency(         cfgParams.getDouble("Noise-Path-Wonder-Freq"  ) );
		this.noisePathWonder.setFractalOctaves(    cfgParams.getInt(   "Noise-Path-Wonder-Octave") );
		this.noisePathWonder.setFractalGain(       cfgParams.getDouble("Noise-Path-Wonder-Gain"  ) );
		this.noisePathWonder.setFractalLacunarity( cfgParams.getDouble("Noise-Path-Wonder-Lacun" ) );
		// path width
		this.noisePathWidth.setFrequency(         cfgParams.getDouble("Noise-Path-Width-Freq"  ) );
		this.noisePathWidth.setFractalOctaves(    cfgParams.getInt(   "Noise-Path-Width-Octave") );
		this.noisePathWidth.setFractalGain(       cfgParams.getDouble("Noise-Path-Width-Gain"  ) );
		this.noisePathWidth.setFractalLacunarity( cfgParams.getDouble("Noise-Path-Width-Lacun" ) );
		// path center (radio station)
		this.noisePathCenter.setFrequency(         cfgParams.getDouble("Noise-Path-Center-Freq"  ) );
		this.noisePathCenter.setFractalOctaves(    cfgParams.getInt(   "Noise-Path-Center-Octave") );
		this.noisePathCenter.setFractalGain(       cfgParams.getDouble("Noise-Path-Center-Gain"  ) );
		this.noisePathCenter.setFractalLacunarity( cfgParams.getDouble("Noise-Path-Center-Lacun" ) );
		// tree placement
		this.noiseTreePlacement.setFrequency( cfgParams.getDouble("Noise-Tree-Placement-Freq") );
	}



	@Override
	protected void configDefaults(final ConfigurationSection cfgParams, final ConfigurationSection cfgBlocks) {
		// params
		cfgParams.addDefault("Enable-Gen",                  Boolean.TRUE                                     );
		cfgParams.addDefault("Enable-Top",                  Boolean.TRUE                                     );
		cfgParams.addDefault("Level-Y",                     Integer.valueOf(this.getDefaultY()              ));
		cfgParams.addDefault("SubFloor",                    Integer.valueOf(DEFAULT_SUBFLOOR                ));
		cfgParams.addDefault("Ground-Thickness-Min",        Integer.valueOf(DEFAULT_GROUND_THICKNESS_MIN    ));
		cfgParams.addDefault("Ground-Thickness-Max",        Integer.valueOf(DEFAULT_GROUND_THICKNESS_MAX    ));
		cfgParams.addDefault("Map-Cell-Size",               Integer.valueOf(DEFAULT_CELL_SIZE               ));
		// path
		cfgParams.addDefault("Path-Chance",                 Double.valueOf( DEFAULT_PATH_CHANCE             ));
		cfgParams.addDefault("Path-Width-Min",              Double.valueOf( DEFAULT_PATH_WIDTH_MIN          ));
		cfgParams.addDefault("Path-Width-Max",              Double.valueOf( DEFAULT_PATH_WIDTH_MAX          ));
		cfgParams.addDefault("Path-Wonder-Factor",          Double.valueOf( DEFAULT_PATH_WONDER             ));
		cfgParams.addDefault("Path-Fade-Factor",            Double.valueOf( DEFAULT_PATH_FADE               ));
		cfgParams.addDefault("Path-Berm-Min",               Integer.valueOf(DEFAULT_PATH_BERM_MIN           ));
		cfgParams.addDefault("Path-Berm-Max",               Integer.valueOf(DEFAULT_PATH_BERM_MAX           ));
		cfgParams.addDefault("Path-Berm-Weight",            Double.valueOf( DEFAULT_PATH_BERM_WEIGHT        ));
		cfgParams.addDefault("Radio-Station-Fade-Factor",   Double.valueOf( DEFAULT_RADIO_STATION_FADE      ));
		cfgParams.addDefault("Path-Radio-Wonder",           Double.valueOf( DEFAULT_PATH_RADIO_WONDER       ));
		cfgParams.addDefault("Radio-Station-Chance",        Double.valueOf( DEFAULT_RADIO_STATION_CHANCE    ));
		cfgParams.addDefault("Radio-Station-Distance",      Integer.valueOf(DEFAULT_RADIO_STATION_DISTANCE  ));
		cfgParams.addDefault("Radio-Station-Margin",        Double.valueOf( DEFAULT_RADIO_STATION_MARGIN    ));
		// ground noise
		cfgParams.addDefault("Noise-Ground-Freq",           Double .valueOf(DEFAULT_NOISE_GROUND_FREQ       ));
		cfgParams.addDefault("Noise-Ground-Octave",         Integer.valueOf(DEFAULT_NOISE_GROUND_OCTAVE     ));
		cfgParams.addDefault("Noise-Ground-Gain",           Double .valueOf(DEFAULT_NOISE_GROUND_GAIN       ));
		cfgParams.addDefault("Noise-Ground-Lacun",          Double .valueOf(DEFAULT_NOISE_GROUND_LACUN      ));
		// path wonder
		cfgParams.addDefault("Noise-Path-Wonder-Freq",      Double .valueOf(DEFAULT_NOISE_PATH_WONDER_FREQ  ));
		cfgParams.addDefault("Noise-Path-Wonder-Octave",    Integer.valueOf(DEFAULT_NOISE_PATH_WONDER_OCTAVE));
		cfgParams.addDefault("Noise-Path-Wonder-Gain",      Double .valueOf(DEFAULT_NOISE_PATH_WONDER_GAIN  ));
		cfgParams.addDefault("Noise-Path-Wonder-Lacun",     Double .valueOf(DEFAULT_NOISE_PATH_WONDER_LACUN ));
		// path width
		cfgParams.addDefault("Noise-Path-Width-Freq",       Double .valueOf(DEFAULT_NOISE_PATH_WIDTH_FREQ   ));
		cfgParams.addDefault("Noise-Path-Width-Octave",     Integer.valueOf(DEFAULT_NOISE_PATH_WIDTH_OCTAVE ));
		cfgParams.addDefault("Noise-Path-Width-Gain",       Double .valueOf(DEFAULT_NOISE_PATH_WIDTH_GAIN   ));
		cfgParams.addDefault("Noise-Path-Width-Lacun",      Double .valueOf(DEFAULT_NOISE_PATH_WIDTH_LACUN  ));
		// radio station clearing
		cfgParams.addDefault("Noise-Path-Center-Freq",      Double .valueOf(DEFAULT_NOISE_PATH_CENTER_FREQ  ));
		cfgParams.addDefault("Noise-Path-Center-Octave",    Integer.valueOf(DEFAULT_NOISE_PATH_CENTER_OCTAVE));
		cfgParams.addDefault("Noise-Path-Center-Gain",      Double .valueOf(DEFAULT_NOISE_PATH_CENTER_GAIN  ));
		cfgParams.addDefault("Noise-Path-Center-Lacun",     Double .valueOf(DEFAULT_NOISE_PATH_CENTER_LACUN ));
		// tree placement
		cfgParams.addDefault("Noise-Tree-Placement-Freq",   Double .valueOf(DEFAULT_NOISE_TREES_PLACE_FREQ  ));
		// block types
		cfgBlocks.addDefault("SubFloor",    DEFAULT_BLOCK_SUBFLOOR   );
		cfgBlocks.addDefault("Dirt",        DEFAULT_BLOCK_DIRT       );
		cfgBlocks.addDefault("Path",        DEFAULT_BLOCK_PATH       );
		cfgBlocks.addDefault("Grass",       DEFAULT_BLOCK_GRASS      );
		cfgBlocks.addDefault("Tree-Trunk",  DEFAULT_BLOCK_TREE_TRUNK );
		cfgBlocks.addDefault("Tree-Branch", DEFAULT_BLOCK_TREE_BRANCH);
		cfgBlocks.addDefault("Tree-Leaves", DEFAULT_BLOCK_TREE_LEAVES);
	}



}
