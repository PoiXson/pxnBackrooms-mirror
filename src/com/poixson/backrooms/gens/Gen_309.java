package com.poixson.backrooms.gens;

import static com.poixson.backrooms.worlds.Level_000.SUBFLOOR;
import static com.poixson.utils.BlockUtils.StringToBlockData;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.backrooms.BackroomsGen;
import com.poixson.backrooms.BackroomsLevel;
import com.poixson.backrooms.PreGenData;
import com.poixson.tools.PathTracer;
import com.poixson.tools.abstractions.AtomicDouble;
import com.poixson.tools.abstractions.Tuple;
import com.poixson.tools.plotter.BlockPlotter;
import com.poixson.utils.FastNoiseLiteD;
import com.poixson.utils.FastNoiseLiteD.FractalType;


// 309 | Radio Station
public class Gen_309 extends BackroomsGen {

	// default params
	public static final double DEFAULT_NOISE_PATH_FREQ     = 0.01;
	public static final double DEFAULT_NOISE_GROUND_FREQ   = 0.002;
	public static final int    DEFAULT_NOISE_GROUND_OCTAVE = 3;
	public static final double DEFAULT_NOISE_GROUND_GAIN   = 0.5;
	public static final double DEFAULT_NOISE_GROUND_LACUN  = 2.0;
	public static final double DEFAULT_NOISE_TREES_FREQ    = 0.2;
	public static final double DEFAULT_NOISE_PRAIRIE_FREQ  = 0.004;
	public static final double DEFAULT_THRESH_PRAIRIE      = 0.35;
	public static final int    DEFAULT_PATH_WIDTH          = 3;
	public static final int    DEFAULT_PATH_CLEARING       = 15;
	public static final int    PATH_START_X                = 14;
	public static final int    PATH_START_Z                = 32;
	public static final int    DEFAULT_SPECIAL_MOD_A       = 19;
	public static final int    DEFAULT_SPECIAL_MOD_B       = 15;

	// default blocks
	public static final String DEFAULT_BLOCK_DIRT        = "minecraft:dirt";
	public static final String DEFAULT_BLOCK_PATH        = "minecraft:dirt_path";
	public static final String DEFAULT_BLOCK_GRASS       = "minecraft:grass_block";
	public static final String DEFAULT_BLOCK_SUBFLOOR    = "minecraft:stone";
	public static final String DEFAULT_BLOCK_TREE_TRUNK  = "minecraft:birch_log";
	public static final String DEFAULT_BLOCK_TREE_LEAVES = "minecraft:birch_leaves";

	// params
	public final boolean enable_gen;
	public final boolean enable_top;
	// noise
	public final FastNoiseLiteD noisePath;
	public final FastNoiseLiteD noiseGround;
	public final FastNoiseLiteD noiseTrees;
	public final FastNoiseLiteD noisePrairie;

	// params
	public final AtomicDouble  thresh_prairie = new AtomicDouble( DEFAULT_THRESH_PRAIRIE);
	public final AtomicInteger path_width     = new AtomicInteger(DEFAULT_PATH_WIDTH    );
	public final AtomicInteger path_clearing  = new AtomicInteger(DEFAULT_PATH_CLEARING );
	public final AtomicInteger special_mod_a  = new AtomicInteger(DEFAULT_SPECIAL_MOD_A );
	public final AtomicInteger special_mod_b  = new AtomicInteger(DEFAULT_SPECIAL_MOD_B );

	// path locations
	protected final PathTracer pathTrace;
	protected final AtomicReference<ConcurrentHashMap<Integer, Double>> pathCache =
			new AtomicReference<ConcurrentHashMap<Integer, Double>>(null);

	// blocks
	public final AtomicReference<String> block_dirt        = new AtomicReference<String>(null);
	public final AtomicReference<String> block_path        = new AtomicReference<String>(null);
	public final AtomicReference<String> block_grass       = new AtomicReference<String>(null);
	public final AtomicReference<String> block_subfloor    = new AtomicReference<String>(null);
	public final AtomicReference<String> block_tree_trunk  = new AtomicReference<String>(null);
	public final AtomicReference<String> block_tree_leaves = new AtomicReference<String>(null);



	public Gen_309(final BackroomsLevel backlevel, final int seed,
			final int level_y, final int level_h) {
		super(backlevel, seed, level_y, level_h);
		// params
		this.enable_gen      = cfgParams.getBoolean("Enable-Gen"     );
		this.enable_top      = cfgParams.getBoolean("Enable-Top"     );
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
		final int path_width = this.path_width.get();
		final BlockData block_dirt     = StringToBlockData(this.block_dirt,     DEFAULT_BLOCK_DIRT    );
		final BlockData block_path     = StringToBlockData(this.block_path,     DEFAULT_BLOCK_PATH    );
		final BlockData block_grass    = StringToBlockData(this.block_grass,    DEFAULT_BLOCK_GRASS   );
		final BlockData block_subfloor = StringToBlockData(this.block_subfloor, DEFAULT_BLOCK_SUBFLOOR);
		if (block_dirt     == null) throw new RuntimeException("Invalid block type for level 309 Dirt"    );
		if (block_path     == null) throw new RuntimeException("Invalid block type for level 309 Path"    );
		if (block_grass    == null) throw new RuntimeException("Invalid block type for level 309 Grass"   );
		if (block_subfloor == null) throw new RuntimeException("Invalid block type for level 309 SubFloor");
		final int y = this.level_y + SUBFLOOR + 1;
		for (int iz=0; iz<16; iz++) {
			for (int ix=0; ix<16; ix++) {
				final int xx = (chunkX * 16) + ix;
				final int zz = (chunkZ * 16) + iz;
				chunk.setBlock(ix, this.level_y, iz, Material.BEDROCK);
				for (int iy=0; iy<SUBFLOOR; iy++)
					chunk.setBlock(ix, this.level_y+iy+1, iz, block_subfloor);
				final double ground;
				{
					final double g = this.noiseGround.getNoise(xx, zz);
					ground = 1.0f + (g < 0.0f ? g * 0.6f : g);
				}
				// dirt
				final int elevation = (int) (ground * 2.5f); // 0 to 5
				for (int i=0; i<elevation; i++) {
					if (i >= elevation-1) {
						if (this.pathTrace.isPath(xx, zz, path_width)) {
							chunk.setBlock(ix, y+i, iz, block_path);
						} else {
							chunk.setBlock(ix, y+i, iz, block_grass);
						}
					} else {
						chunk.setBlock(ix, y+i, iz, block_dirt);
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
	protected void initNoise(final ConfigurationSection cfgParams) {
		super.initNoise(cfgParams);
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
		this.noisePrairie.setFractalLacunarity(       cfgParams.getDouble("Noise-Prairie-Lac"   ) );
	}



	@Override
	protected void loadConfig(final ConfigurationSection cfgParams, final ConfigurationSection cfgBlocks) {
		// params
		this.thresh_prairie.set(cfgParams.getDouble("Thresh-Prairie"));
		this.path_width    .set(cfgParams.getInt(   "Path-Width"    ));
		this.path_clearing .set(cfgParams.getInt(   "Path-Clearing" ));
		this.special_mod_a .set(cfgParams.getInt(   "Special-Mod-A" ));
		this.special_mod_b .set(cfgParams.getInt(   "Special-Mod-B" ));
		// block types
		this.block_tree_trunk .set(cfgBlocks.getString("Tree-Trunk" ));
		this.block_tree_leaves.set(cfgBlocks.getString("Tree-Leaves"));
	}
	@Override
	protected void configDefaults(final ConfigurationSection cfgParams, final ConfigurationSection cfgBlocks) {
		// params
		cfgParams.addDefault("Enable-Gen",           Boolean.TRUE                                 );
		cfgParams.addDefault("Enable-Top",           Boolean.TRUE                                 );
		cfgParams.addDefault("Noise-Path-Freq",      DEFAULT_NOISE_PATH_FREQ      );
		cfgParams.addDefault("Noise-Ground-Freq",    DEFAULT_NOISE_GROUND_FREQ    );
		cfgParams.addDefault("Noise-Ground-Octave",  DEFAULT_NOISE_GROUND_OCTAVE  );
		cfgParams.addDefault("Noise-Ground-Gain",    DEFAULT_NOISE_GROUND_GAIN    );
		cfgParams.addDefault("Noise-Ground-Lacun",   DEFAULT_NOISE_GROUND_LACUN   );
		cfgParams.addDefault("Noise-Trees-Freq",     DEFAULT_NOISE_TREES_FREQ     );
		cfgParams.addDefault("Noise-Prairie-Freq",   DEFAULT_NOISE_PRAIRIE_FREQ   );
		cfgParams.addDefault("Noise-Prairie-Octave", DEFAULT_NOISE_PRAIRIE_OCTAVE );
		cfgParams.addDefault("Noise-Prairie-Weight", DEFAULT_NOISE_PRAIRIE_WEIGHT );
		cfgParams.addDefault("Noise-Prairie-Lac",    DEFAULT_NOISE_PRAIRIE_LAC    );
		cfgParams.addDefault("Thresh-Prairie",       DEFAULT_THRESH_PRAIRIE       );
		cfgParams.addDefault("Path-Width",           DEFAULT_PATH_WIDTH           );
		cfgParams.addDefault("Path-Clearing",        DEFAULT_PATH_CLEARING        );
		cfgParams.addDefault("Special-Mod-A",        DEFAULT_SPECIAL_MOD_A        );
		cfgParams.addDefault("Special-Mod-B",        DEFAULT_SPECIAL_MOD_B        );
		// block types
		cfgBlocks.addDefault("Dirt",        DEFAULT_BLOCK_DIRT       );
		cfgBlocks.addDefault("Path",        DEFAULT_BLOCK_PATH       );
		cfgBlocks.addDefault("Grass",       DEFAULT_BLOCK_GRASS      );
		cfgBlocks.addDefault("SubFloor",    DEFAULT_BLOCK_SUBFLOOR   );
		cfgBlocks.addDefault("Tree-Trunk",  DEFAULT_BLOCK_TREE_TRUNK );
		cfgBlocks.addDefault("Tree-Leaves", DEFAULT_BLOCK_TREE_LEAVES);
	}



}
