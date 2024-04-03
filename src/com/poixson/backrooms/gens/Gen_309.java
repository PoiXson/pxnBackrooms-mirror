package com.poixson.backrooms.gens;

import static com.poixson.utils.BlockUtils.StringToBlockData;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.backrooms.BackroomsGen;
import com.poixson.backrooms.BackroomsLevel;
import com.poixson.backrooms.PreGenData;
import com.poixson.tools.PathTracer;
import com.poixson.tools.abstractions.Tuple;
import com.poixson.tools.plotter.BlockPlotter;
import com.poixson.utils.FastNoiseLiteD;
import com.poixson.utils.FastNoiseLiteD.FractalType;


// 309 | Radio Station
public class Gen_309 extends BackroomsGen {

	// default params
	public static final int    DEFAULT_LEVEL_H              = 8;
	public static final int    DEFAULT_SUBFLOOR             = 3;
	public static final double DEFAULT_THRESH_PRAIRIE       = 0.35;
	public static final int    DEFAULT_PATH_WIDTH           = 3;
	public static final int    DEFAULT_PATH_CLEARING        = 15;
	public static final double DEFAULT_FENCE_RADIUS         = 65.0;
	public static final double DEFAULT_FENCE_STRENGTH       = 2.0;
	public static final double DEFAULT_FENCE_THICKNESS      = 1.3;
	public static final int    PATH_START_X                 = 14;
	public static final int    PATH_START_Z                 = 32;
	public static final int    DEFAULT_SPECIAL_MOD_A        = 19;
	public static final int    DEFAULT_SPECIAL_MOD_B        = 15;
	public static final double DEFAULT_NOISE_PATH_FREQ      = 0.01;
	public static final double DEFAULT_NOISE_GROUND_FREQ    = 0.002;
	public static final int    DEFAULT_NOISE_GROUND_OCTAVE  = 3;
	public static final double DEFAULT_NOISE_GROUND_GAIN    = 0.5;
	public static final double DEFAULT_NOISE_GROUND_LACUN   = 2.0;
	public static final double DEFAULT_NOISE_TREES_FREQ     = 0.2;
	public static final double DEFAULT_NOISE_PRAIRIE_FREQ   = 0.004;
	public static final int    DEFAULT_NOISE_PRAIRIE_OCTAVE = 2;
	public static final double DEFAULT_NOISE_PRAIRIE_WEIGHT = 2.0;
	public static final double DEFAULT_NOISE_PRAIRIE_LACUN  = 5.0;

	// default blocks
	public static final String DEFAULT_BLOCK_SUBFLOOR    = "minecraft:stone";
	public static final String DEFAULT_BLOCK_DIRT        = "minecraft:dirt";
	public static final String DEFAULT_BLOCK_PATH        = "minecraft:dirt_path";
	public static final String DEFAULT_BLOCK_GRASS       = "minecraft:grass_block";
	public static final String DEFAULT_BLOCK_TREE_TRUNK  = "minecraft:birch_log";
	public static final String DEFAULT_BLOCK_TREE_LEAVES = "minecraft:birch_leaves";

	// params
	public final boolean enable_gen;
	public final boolean enable_top;
	public final int     level_y;
	public final int     level_h;
	public final int     subfloor;
	public final double  thresh_prairie;
	public final int     path_width;
	public final int     path_clearing;
	public final double  fence_radius;
	public final double  fence_strength;
	public final double  fence_thickness;
	public final int     special_mod_a;
	public final int     special_mod_b;

	// blocks
	public final String block_subfloor;
	public final String block_dirt;
	public final String block_path;
	public final String block_grass;
	public final String block_tree_trunk;
	public final String block_tree_leaves;

	// noise
	public final FastNoiseLiteD noisePath;
	public final FastNoiseLiteD noiseGround;
	public final FastNoiseLiteD noiseTrees;
	public final FastNoiseLiteD noisePrairie;

	// path locations
	protected final PathTracer pathTrace;
	protected final AtomicReference<ConcurrentHashMap<Integer, Double>> pathCache =
			new AtomicReference<ConcurrentHashMap<Integer, Double>>(null);



	public Gen_309(final BackroomsLevel backlevel, final int seed, final BackroomsGen gen_below) {
		super(backlevel, gen_below, seed);
		final int level_number = this.getLevelNumber();
		final ConfigurationSection cfgParams = this.plugin.getConfigLevelParams(level_number);
		final ConfigurationSection cfgBlocks = this.plugin.getConfigLevelBlocks(level_number);
		// params
		this.enable_gen      = cfgParams.getBoolean("Enable-Gen"     );
		this.enable_top      = cfgParams.getBoolean("Enable-Top"     );
		this.level_y         = cfgParams.getInt(    "Level-Y"        );
		this.level_h         = cfgParams.getInt(    "Level-Height"   );
		this.subfloor        = cfgParams.getInt(    "SubFloor"       );
		this.thresh_prairie  = cfgParams.getDouble( "Thresh-Prairie" );
		this.path_width      = cfgParams.getInt(    "Path-Width"     );
		this.path_clearing   = cfgParams.getInt(    "Path-Clearing"  );
		this.fence_radius    = cfgParams.getDouble( "Fence-Radius"   );
		this.fence_strength  = cfgParams.getDouble( "Fence-Strength" );
		this.fence_thickness = cfgParams.getDouble( "Fence-Thickness");
		this.special_mod_a  = cfgParams.getInt(    "Special-Mod-A" );
		this.special_mod_b  = cfgParams.getInt(    "Special-Mod-B" );
		// block types
		this.block_subfloor    = cfgBlocks.getString("SubFloor"   );
		this.block_dirt        = cfgBlocks.getString("Dirt"       );
		this.block_path        = cfgBlocks.getString("Path"       );
		this.block_grass       = cfgBlocks.getString("Grass"      );
		this.block_tree_trunk  = cfgBlocks.getString("Tree-Trunk" );
		this.block_tree_leaves = cfgBlocks.getString("Tree-Leaves");
		// noise
		this.noisePath    = this.register(new FastNoiseLiteD());
		this.noiseGround  = this.register(new FastNoiseLiteD());
		this.noiseTrees   = this.register(new FastNoiseLiteD());
		this.noisePrairie = this.register(new FastNoiseLiteD());
		// path locations
		this.pathTrace = new PathTracer(this.noisePath, PATH_START_X, PATH_START_Z, this.getPathCacheMap());
	}



	@Override
	public int getLevelNumber() {
		return 309;
	}

	@Override
	public int getNextY() {
		return 320;
	}



	@Override
	public void unregister() {
		super.unregister();
		this.pathCache.set(null);
	}



	public FastNoiseLiteD getTreeNoise() {
		return this.noiseTrees;
	}
	public FastNoiseLiteD getPrairieNoise() {
		return this.noisePrairie;
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
				// dirt/grass/path
				final double g = this.noiseGround.getNoise(xx, zz);
				final double ground = 1.0f + (g < 0.0f ? g * 0.6f : g);
				final int elevation = (int) (ground * 2.5f); // 0 to 5
				for (int iy=0; iy<elevation; iy++) {
					if (iy >= elevation-1) {
						if (this.pathTrace.isPath(xx, zz, this.path_width)) {
							chunk.setBlock(ix, y_floor+iy, iz, block_path);
						} else {
							chunk.setBlock(ix, y_floor+iy, iz, block_grass);
						}
					} else {
						chunk.setBlock(ix, y_floor+iy, iz, block_dirt);
					}
				}
			} // end ix
		} // end iz
	}



	public int getPathX(final int z) {
		if (z < 0) return 0;
		return this.pathTrace.getPathX(z);
	}



	public ConcurrentHashMap<Integer, Double> getPathCacheMap() {
		// existing
		{
			final ConcurrentHashMap<Integer, Double> cache = this.pathCache.get();
			if (cache != null)
				return cache;
		}
		// new instance
		{
			final ConcurrentHashMap<Integer, Double> cache = new ConcurrentHashMap<Integer, Double>();
			if (this.pathCache.compareAndSet(null, cache))
				return cache;
		}
		return this.getPathCacheMap();
	}



	public double getCenterClearingDistance(final int x, final int z, final double strength) {
		if (Math.abs(x) > 100 || Math.abs(z) > 100)
			return Double.MAX_VALUE;
		return Math.sqrt( Math.pow((double)x, 2.0) + Math.pow((double)z, 2.0) )
			+ (this.noisePath.getNoise(x*5, z*5) * strength);
	}



	// -------------------------------------------------------------------------------
	// configs



	@Override
	protected void initNoise() {
		super.initNoise();
		final ConfigurationSection cfgParams = this.plugin.getConfigLevelParams(this.getLevelNumber());
		// path
		this.noisePath.setFrequency( cfgParams.getDouble("Noise-Path-Freq") );
		// path ground
		this.noiseGround.setFrequency(         cfgParams.getDouble("Noise-Ground-Freq"  ) );
		this.noiseGround.setFractalOctaves(    cfgParams.getInt(   "Noise-Ground-Octave") );
		this.noiseGround.setFractalGain(       cfgParams.getDouble("Noise-Ground-Gain"  ) );
		this.noiseGround.setFractalLacunarity( cfgParams.getDouble("Noise-Ground-Lacun" ) );
		this.noiseGround.setFractalType(       FractalType.Ridged                         );
		// tree noise
		this.noiseTrees.setFrequency( cfgParams.getDouble("Noise-Trees-Freq") );
		// prairie noise (doors, stairs, hatches)
		this.noisePrairie.setFrequency(               cfgParams.getDouble("Noise-Prairie-Freq"  ) );
		this.noisePrairie.setFractalOctaves(          cfgParams.getInt(   "Noise-Prairie-Octave") );
		this.noisePrairie.setFractalType(             FractalType.Ridged                          );
		this.noisePrairie.setFractalWeightedStrength( cfgParams.getDouble("Noise-Prairie-Weight") );
		this.noisePrairie.setFractalLacunarity(       cfgParams.getDouble("Noise-Prairie-Lacun" ) );
	}



	@Override
	protected void configDefaults(final ConfigurationSection cfgParams, final ConfigurationSection cfgBlocks) {
		// params
		cfgParams.addDefault("Enable-Gen",           Boolean.TRUE                                 );
		cfgParams.addDefault("Enable-Top",           Boolean.TRUE                                 );
		cfgParams.addDefault("Level-Y",              Integer.valueOf(this.getDefaultY()          ));
		cfgParams.addDefault("Level-Height",         Integer.valueOf(DEFAULT_LEVEL_H             ));
		cfgParams.addDefault("SubFloor",             Integer.valueOf(DEFAULT_SUBFLOOR            ));
		cfgParams.addDefault("Thresh-Prairie",       Double .valueOf(DEFAULT_THRESH_PRAIRIE      ));
		cfgParams.addDefault("Path-Width",           Integer.valueOf(DEFAULT_PATH_WIDTH          ));
		cfgParams.addDefault("Path-Clearing",        Integer.valueOf(DEFAULT_PATH_CLEARING       ));
		cfgParams.addDefault("Fence-Radius",         Double .valueOf(DEFAULT_FENCE_RADIUS        ));
		cfgParams.addDefault("Fence-Strength",       Double .valueOf(DEFAULT_FENCE_STRENGTH      ));
		cfgParams.addDefault("Fence-Thickness",      Double .valueOf(DEFAULT_FENCE_THICKNESS     ));
		cfgParams.addDefault("Special-Mod-A",        Integer.valueOf(DEFAULT_SPECIAL_MOD_A       ));
		cfgParams.addDefault("Special-Mod-B",        Integer.valueOf(DEFAULT_SPECIAL_MOD_B       ));
		cfgParams.addDefault("Noise-Path-Freq",      Double .valueOf(DEFAULT_NOISE_PATH_FREQ     ));
		cfgParams.addDefault("Noise-Ground-Freq",    Double .valueOf(DEFAULT_NOISE_GROUND_FREQ   ));
		cfgParams.addDefault("Noise-Ground-Octave",  Integer.valueOf(DEFAULT_NOISE_GROUND_OCTAVE ));
		cfgParams.addDefault("Noise-Ground-Gain",    Double .valueOf(DEFAULT_NOISE_GROUND_GAIN   ));
		cfgParams.addDefault("Noise-Ground-Lacun",   Double .valueOf(DEFAULT_NOISE_GROUND_LACUN  ));
		cfgParams.addDefault("Noise-Trees-Freq",     Double .valueOf(DEFAULT_NOISE_TREES_FREQ    ));
		cfgParams.addDefault("Noise-Prairie-Freq",   Double .valueOf(DEFAULT_NOISE_PRAIRIE_FREQ  ));
		cfgParams.addDefault("Noise-Prairie-Octave", Integer.valueOf(DEFAULT_NOISE_PRAIRIE_OCTAVE));
		cfgParams.addDefault("Noise-Prairie-Weight", Double .valueOf(DEFAULT_NOISE_PRAIRIE_WEIGHT));
		cfgParams.addDefault("Noise-Prairie-Lacun",  Double .valueOf(DEFAULT_NOISE_PRAIRIE_LACUN ));
		// block types
		cfgBlocks.addDefault("SubFloor",    DEFAULT_BLOCK_SUBFLOOR   );
		cfgBlocks.addDefault("Dirt",        DEFAULT_BLOCK_DIRT       );
		cfgBlocks.addDefault("Path",        DEFAULT_BLOCK_PATH       );
		cfgBlocks.addDefault("Grass",       DEFAULT_BLOCK_GRASS      );
		cfgBlocks.addDefault("Tree-Trunk",  DEFAULT_BLOCK_TREE_TRUNK );
		cfgBlocks.addDefault("Tree-Leaves", DEFAULT_BLOCK_TREE_LEAVES);
	}



}
