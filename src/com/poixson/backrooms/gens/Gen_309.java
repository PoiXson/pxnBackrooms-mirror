package com.poixson.backrooms.gens;

import static com.poixson.utils.BlockUtils.StringToBlockDataDef;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.backrooms.BackroomsGen;
import com.poixson.backrooms.BackroomsWorld;
import com.poixson.backrooms.PreGenData;
import com.poixson.backrooms.worlds.Level_000.Pregen_Level_000;
import com.poixson.tools.FastNoiseLiteD;
import com.poixson.tools.FastNoiseLiteD.FractalType;
import com.poixson.tools.xRand;
import com.poixson.tools.abstractions.Tuple;
import com.poixson.tools.dao.Bab;
import com.poixson.tools.dao.Iab;
import com.poixson.tools.plotter.BlockPlotter;


// 309 | Radio Station
public class Gen_309 extends BackroomsGen {
	public static final boolean GLASS_GRID = false;

	// default params
	public static final int    DEFAULT_LEVEL_H                  = 8;
	public static final int    DEFAULT_SUBFLOOR                 = 3;

	public static final int    DEFAULT_REGION_SIZE              = 512;
	public static final double DEFAULT_PATH_CHANCE              = 4.97;
	public static final double DEFAULT_PATH_WIDTH               = 4.6;
	public static final double DEFAULT_PATH_WONDER              = 6.0;//18.0;
	public static final int    DEFAULT_PATH_CLEARING            = 24;
	// path
	public static final double DEFAULT_NOISE_PATH_FREQ          = 0.009;
	public static final int    DEFAULT_NOISE_PATH_OCTAVE        = 3;
	public static final double DEFAULT_NOISE_PATH_GAIN          = 8.0;
	public static final double DEFAULT_NOISE_PATH_LACUN         = 1.8;
	// path center (radio station)
	public static final double DEFAULT_NOISE_PATH_CENTER_FREQ   = 0.018;
	public static final int    DEFAULT_NOISE_PATH_CENTER_OCTAVE = 2;
	public static final double DEFAULT_NOISE_PATH_CENTER_GAIN   = 3.3;
	public static final double DEFAULT_NOISE_PATH_CENTER_LACUN  = 2.2;
	// ground
	public static final double DEFAULT_NOISE_GROUND_FREQ        = 0.002;
	public static final int    DEFAULT_NOISE_GROUND_OCTAVE      = 3;
	public static final double DEFAULT_NOISE_GROUND_GAIN        = 0.5;
	public static final double DEFAULT_NOISE_GROUND_LACUN       = 2.0;

	// default blocks
	public static final String DEFAULT_BLOCK_SUBFLOOR = "minecraft:stone";
	public static final String DEFAULT_BLOCK_DIRT     = "minecraft:dirt";
	public static final String DEFAULT_BLOCK_PATH     = "minecraft:dirt_path";
	public static final String DEFAULT_BLOCK_GRASS    = "minecraft:grass_block";

	// params
	public final boolean enable_gen;
	public final boolean enable_top;
	public final int     level_y;
	public final int     subfloor;
	public final int     region_size;
	public final int     region_half;
	public final double  path_chance;
	public final double  path_width;
	public final double  path_wonder;
	public final int     path_clearing;
	public final int     data_extra_size;

	// blocks
	public final String block_subfloor;
	public final String block_dirt;
	public final String block_path;
	public final String block_grass;

	// noise
	public final FastNoiseLiteD noisePath;
	public final FastNoiseLiteD noisePathCenter;
	public final FastNoiseLiteD noiseGround;

	public final xRand random = (new xRand()).seed_time();

	public final PathMaze maze;



	public Gen_309(final BackroomsWorld backworld, final int seed, final BackroomsGen gen_below) {
		super(backworld, gen_below, seed);
		final int level_number = this.getLevelNumber();
		final ConfigurationSection cfgParams = this.plugin.getConfigLevelParams(level_number);
		final ConfigurationSection cfgBlocks = this.plugin.getConfigLevelBlocks(level_number);
		// params
		this.enable_gen      = cfgParams.getBoolean("Enable-Gen"        );
		this.enable_top      = cfgParams.getBoolean("Enable-Top"        );
		this.level_y         = cfgParams.getInt(    "Level-Y"           );
		this.subfloor        = cfgParams.getInt(    "SubFloor"          );
		this.region_size     = cfgParams.getInt(    "Region-Size"       );
		this.path_chance     = cfgParams.getDouble( "Path-Chance"       );
		this.path_width      = cfgParams.getDouble( "Path-Width"        );
		this.path_wonder     = cfgParams.getDouble( "Path-Wonder-Factor");
		this.path_clearing   = cfgParams.getInt(    "Path-Clearing"     );
		this.region_half     = Math.floorDiv(this.region_size, 2);
		this.data_extra_size = this.path_clearing;
		// block types
		this.block_subfloor = cfgBlocks.getString("SubFloor");
		this.block_dirt     = cfgBlocks.getString("Dirt"    );
		this.block_path     = cfgBlocks.getString("Path"    );
		this.block_grass    = cfgBlocks.getString("Grass"   );
		// noise
		this.noisePath       = this.register(new FastNoiseLiteD());
		this.noisePathCenter = this.register(new FastNoiseLiteD());
		this.noiseGround     = this.register(new FastNoiseLiteD());
		// path maze
		this.maze = new PathMaze(this.path_chance);
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



	@Override
	public void register() {
		super.register();
		this.maze.load();
		this.maze.register(this.plugin);
	}
	@Override
	public void unregister() {
		super.unregister();
		this.maze.unregister();
		this.maze.save();
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
		final HashMap<Iab, RadioPathData> data_paths = ((Pregen_Level_000)pregen).paths;
		final int y_base  = this.level_y + this.bedrock_barrier;
		final int y_floor = y_base + this.subfloor;
		for (int iz=0; iz<16; iz++) {
			final int zz = (chunkZ * 16) + iz;
			for (int ix=0; ix<16; ix++) {
				final int xx = (chunkX * 16) + ix;
				final RadioPathData dao_path = data_paths.get(new Iab(ix, iz));
				// barrier
				for (int iy=0; iy<this.bedrock_barrier; iy++)
					chunk.setBlock(ix, this.level_y+iy, iz, Material.BEDROCK);
				// subfloor
				for (int iy=0; iy<this.subfloor; iy++)
					chunk.setBlock(ix, y_base+iy, iz, block_subfloor);
				// ground
				final double g1 = this.noiseGround.getNoise(xx, zz);
				final double g2 = (g1 < 0.0 ? g1 * 0.6 : g1) + 1.0;
				final int elevation = (int) (g2 * 2.5); // 0 to 5
				for (int iy=0; iy<elevation; iy++)
					chunk.setBlock(ix, y_floor+iy, iz, block_dirt);
				if (dao_path.is_path) chunk.setBlock(ix, y_floor+elevation, iz, block_path );
				else                  chunk.setBlock(ix, y_floor+elevation, iz, block_grass);
				if (GLASS_GRID) {
					if (xx % this.region_size == 0 || zz % this.region_size == 0)
						chunk.setBlock(ix, y_floor+elevation+2, iz, Material.GLASS);
				}
			} // end ix
		} // end iz
	}



	// -------------------------------------------------------------------------------
	// configs



	@Override
	protected void initNoise() {
		super.initNoise();
		final ConfigurationSection cfgParams = this.plugin.getConfigLevelParams(this.getLevelNumber());
		// path
		this.noisePath.setFrequency(         cfgParams.getDouble("Noise-Path-Freq"  ) );
		this.noisePath.setFractalOctaves(    cfgParams.getInt(   "Noise-Path-Octave") );
		this.noisePath.setFractalGain(       cfgParams.getDouble("Noise-Path-Gain"  ) );
		this.noisePath.setFractalLacunarity( cfgParams.getDouble("Noise-Path-Lacun" ) );
		// path center (radio station)
		this.noisePathCenter.setFrequency(         cfgParams.getDouble("Noise-Path-Center-Freq"  ) );
		this.noisePathCenter.setFractalOctaves(    cfgParams.getInt(   "Noise-Path-Center-Octave") );
		this.noisePathCenter.setFractalGain(       cfgParams.getDouble("Noise-Path-Center-Gain"  ) );
		this.noisePathCenter.setFractalLacunarity( cfgParams.getDouble("Noise-Path-Center-Lacun" ) );
		// path ground
		this.noiseGround.setFrequency(         cfgParams.getDouble("Noise-Ground-Freq"  ) );
		this.noiseGround.setFractalOctaves(    cfgParams.getInt(   "Noise-Ground-Octave") );
		this.noiseGround.setFractalGain(       cfgParams.getDouble("Noise-Ground-Gain"  ) );
		this.noiseGround.setFractalLacunarity( cfgParams.getDouble("Noise-Ground-Lacun" ) );
		this.noiseGround.setFractalType(       FractalType.Ridged                         );
	}



	@Override
	protected void configDefaults(final ConfigurationSection cfgParams, final ConfigurationSection cfgBlocks) {
		// params
		cfgParams.addDefault("Enable-Gen",               Boolean.TRUE                                     );
		cfgParams.addDefault("Enable-Top",               Boolean.TRUE                                     );
		cfgParams.addDefault("Level-Y",                  Integer.valueOf(this.getDefaultY()              ));
		cfgParams.addDefault("SubFloor",                 Integer.valueOf(DEFAULT_SUBFLOOR                ));
		cfgParams.addDefault("Region-Size",              Integer.valueOf(DEFAULT_REGION_SIZE             ));
		cfgParams.addDefault("Path-Chance",              Double.valueOf( DEFAULT_PATH_CHANCE             ));
		cfgParams.addDefault("Path-Width",               Double.valueOf( DEFAULT_PATH_WIDTH              ));
		cfgParams.addDefault("Path-Wonder-Factor",       Double.valueOf( DEFAULT_PATH_WONDER             ));
		cfgParams.addDefault("Path-Clearing",            Integer.valueOf(DEFAULT_PATH_CLEARING           ));
		cfgParams.addDefault("Noise-Path-Freq",          Double .valueOf(DEFAULT_NOISE_PATH_FREQ         ));
		cfgParams.addDefault("Noise-Path-Octave",        Double .valueOf(DEFAULT_NOISE_PATH_OCTAVE       ));
		cfgParams.addDefault("Noise-Path-Lacun",         Double .valueOf(DEFAULT_NOISE_PATH_LACUN        ));
		cfgParams.addDefault("Noise-Path-Center-Freq",   Double .valueOf(DEFAULT_NOISE_PATH_CENTER_FREQ  ));
		cfgParams.addDefault("Noise-Path-Center-Octave", Double .valueOf(DEFAULT_NOISE_PATH_CENTER_OCTAVE));
		cfgParams.addDefault("Noise-Path-Center-Gain",   Double .valueOf(DEFAULT_NOISE_PATH_CENTER_GAIN  ));
		cfgParams.addDefault("Noise-Path-Center-Lacun",  Double .valueOf(DEFAULT_NOISE_PATH_CENTER_LACUN ));
		cfgParams.addDefault("Noise-Ground-Freq",        Double .valueOf(DEFAULT_NOISE_GROUND_FREQ       ));
		cfgParams.addDefault("Noise-Ground-Octave",      Integer.valueOf(DEFAULT_NOISE_GROUND_OCTAVE     ));
		cfgParams.addDefault("Noise-Ground-Gain",        Double .valueOf(DEFAULT_NOISE_GROUND_GAIN       ));
		cfgParams.addDefault("Noise-Ground-Lacun",       Double .valueOf(DEFAULT_NOISE_GROUND_LACUN      ));
		// block types
		cfgBlocks.addDefault("SubFloor",    DEFAULT_BLOCK_SUBFLOOR   );
		cfgBlocks.addDefault("Dirt",        DEFAULT_BLOCK_DIRT       );
		cfgBlocks.addDefault("Path",        DEFAULT_BLOCK_PATH       );
		cfgBlocks.addDefault("Grass",       DEFAULT_BLOCK_GRASS      );
	}



}
