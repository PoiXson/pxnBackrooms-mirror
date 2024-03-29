package com.poixson.backrooms.gens;

import static com.poixson.backrooms.worlds.Level_000.ENABLE_GEN_309;
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

	// noise
	public final FastNoiseLiteD noisePath;
	public final FastNoiseLiteD noiseGround;
	public final FastNoiseLiteD noiseTrees;
	public final FastNoiseLiteD noisePrairie;

	// params
	public final AtomicDouble  noise_path_freq     = new AtomicDouble( DEFAULT_NOISE_PATH_FREQ    );
	public final AtomicDouble  noise_ground_freq   = new AtomicDouble( DEFAULT_NOISE_GROUND_FREQ  );
	public final AtomicInteger noise_ground_octave = new AtomicInteger(DEFAULT_NOISE_GROUND_OCTAVE);
	public final AtomicDouble  noise_ground_gain   = new AtomicDouble( DEFAULT_NOISE_GROUND_GAIN  );
	public final AtomicDouble  noise_ground_lacun  = new AtomicDouble( DEFAULT_NOISE_GROUND_LACUN );
	public final AtomicDouble  noise_trees_freq    = new AtomicDouble( DEFAULT_NOISE_TREES_FREQ   );
	public final AtomicDouble  noise_prairie_freq  = new AtomicDouble( DEFAULT_NOISE_PRAIRIE_FREQ );
	public final AtomicDouble  thresh_prairie      = new AtomicDouble( DEFAULT_THRESH_PRAIRIE     );
	public final AtomicInteger path_width          = new AtomicInteger(DEFAULT_PATH_WIDTH         );
	public final AtomicInteger path_clearing       = new AtomicInteger(DEFAULT_PATH_CLEARING      );
	public final AtomicInteger special_mod_a       = new AtomicInteger(DEFAULT_SPECIAL_MOD_A      );
	public final AtomicInteger special_mod_b       = new AtomicInteger(DEFAULT_SPECIAL_MOD_B      );

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
		// noise
		this.noisePath    = this.register(new FastNoiseLiteD());
		this.noiseGround  = this.register(new FastNoiseLiteD());
		this.noiseTrees   = this.register(new FastNoiseLiteD());
		this.noisePrairie = this.register(new FastNoiseLiteD());
		// path locations
		this.pathTrace = new PathTracer(this.noisePath, PATH_START_X, PATH_START_Z, this.getPathCacheMap());
	}



	@Override
	public void initNoise() {
		super.initNoise();
		// path
		this.noisePath.setFrequency(this.noise_path_freq.get());
		// path ground
		this.noiseGround.setFrequency(        this.noise_ground_freq  .get());
		this.noiseGround.setFractalOctaves(   this.noise_ground_octave.get());
		this.noiseGround.setFractalGain(      this.noise_ground_gain  .get());
		this.noiseGround.setFractalLacunarity(this.noise_ground_lacun .get());
		this.noiseGround.setFractalType(FractalType.Ridged);
		// tree noise
		this.noiseTrees.setFrequency(this.noise_trees_freq.get());
		// prairie noise (doors, stairs, hatches)
		this.noisePrairie.setFrequency(this.noise_prairie_freq.get());
		this.noisePrairie.setFractalOctaves(2);
		this.noisePrairie.setFractalType(FractalType.Ridged);
		this.noisePrairie.setFractalWeightedStrength(2.0);
		this.noisePrairie.setFractalLacunarity(5.0);
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
		if (!ENABLE_GEN_309) return;
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
	protected void loadConfig() {
		// params
		{
			final ConfigurationSection cfg = this.plugin.getConfigLevelParams(309);
			this.noise_path_freq    .set(cfg.getDouble("Noise-Path-Freq"    ));
			this.noise_ground_freq  .set(cfg.getDouble("Noise-Ground-Freq"  ));
			this.noise_ground_octave.set(cfg.getInt(   "Noise-Ground-Octave"));
			this.noise_ground_gain  .set(cfg.getDouble("Noise-Ground-Gain"  ));
			this.noise_ground_lacun .set(cfg.getDouble("Noise-Ground-Lacun" ));
			this.noise_trees_freq   .set(cfg.getDouble("Noise-Trees-Freq"   ));
			this.noise_prairie_freq .set(cfg.getDouble("Noise-Prairie-Freq" ));
			this.thresh_prairie     .set(cfg.getDouble("Thresh-Prairie"     ));
			this.path_width         .set(cfg.getInt(   "Path-Width"         ));
			this.path_clearing      .set(cfg.getInt(   "Path-Clearing"      ));
			this.special_mod_a      .set(cfg.getInt(   "Special-Mod-A"      ));
			this.special_mod_b      .set(cfg.getInt(   "Special-Mod-B"      ));
		}
		// block types
		{
			final ConfigurationSection cfg = this.plugin.getConfigLevelBlocks(309);
			this.block_tree_trunk .set(cfg.getString("Tree-Trunk" ));
			this.block_tree_leaves.set(cfg.getString("Tree-Leaves"));
		}
	}
	@Override
	public void configDefaults() {
		// params
		{
			final ConfigurationSection cfg = this.plugin.getConfigLevelParams();
			cfg.addDefault("Level309.Noise-Path-Freq",     DEFAULT_NOISE_PATH_FREQ    );
			cfg.addDefault("Level309.Noise-Ground-Freq",   DEFAULT_NOISE_GROUND_FREQ  );
			cfg.addDefault("Level309.Noise-Ground-Octave", DEFAULT_NOISE_GROUND_OCTAVE);
			cfg.addDefault("Level309.Noise-Ground-Gain",   DEFAULT_NOISE_GROUND_GAIN  );
			cfg.addDefault("Level309.Noise-Ground-Lacun",  DEFAULT_NOISE_GROUND_LACUN );
			cfg.addDefault("Level309.Noise-Trees-Freq",    DEFAULT_NOISE_TREES_FREQ   );
			cfg.addDefault("Level309.Noise-Prairie-Freq",  DEFAULT_NOISE_PRAIRIE_FREQ );
			cfg.addDefault("Level309.Thresh-Prairie",      DEFAULT_THRESH_PRAIRIE     );
			cfg.addDefault("Level309.Path-Width",          DEFAULT_PATH_WIDTH         );
			cfg.addDefault("Level309.Path-Clearing",       DEFAULT_PATH_CLEARING      );
			cfg.addDefault("Level309.Special-Mod-A",       DEFAULT_SPECIAL_MOD_A      );
			cfg.addDefault("Level309.Special-Mod-B",       DEFAULT_SPECIAL_MOD_B      );
		}
		// block types
		{
			final ConfigurationSection cfg = this.plugin.getConfigLevelBlocks();
			cfg.addDefault("Level309.Dirt",        DEFAULT_BLOCK_DIRT       );
			cfg.addDefault("Level309.Path",        DEFAULT_BLOCK_PATH       );
			cfg.addDefault("Level309.Grass",       DEFAULT_BLOCK_GRASS      );
			cfg.addDefault("Level309.SubFloor",    DEFAULT_BLOCK_SUBFLOOR   );
			cfg.addDefault("Level309.Tree-Trunk",  DEFAULT_BLOCK_TREE_TRUNK );
			cfg.addDefault("Level309.Tree-Leaves", DEFAULT_BLOCK_TREE_LEAVES);
		}
	}



}
