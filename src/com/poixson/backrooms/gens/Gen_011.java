package com.poixson.backrooms.gens;

import static com.poixson.backrooms.worlds.Level_011.ENABLE_GEN_011;
import static com.poixson.backrooms.worlds.Level_011.ENABLE_TOP_011;
import static com.poixson.backrooms.worlds.Level_011.SUBFLOOR;
import static com.poixson.utils.BlockUtils.StringToBlockData;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicReference;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.backrooms.BackroomsGen;
import com.poixson.backrooms.BackroomsLevel;
import com.poixson.backrooms.PreGenData;
import com.poixson.backrooms.gens.DataHolder_City.CityData;
import com.poixson.backrooms.worlds.Level_011.PregenLevel11;
import com.poixson.tools.abstractions.AtomicDouble;
import com.poixson.tools.abstractions.Tuple;
import com.poixson.tools.plotter.BlockPlotter;
import com.poixson.utils.FastNoiseLiteD;
import com.poixson.utils.FastNoiseLiteD.CellularDistanceFunction;
import com.poixson.utils.FastNoiseLiteD.CellularReturnType;
import com.poixson.utils.FastNoiseLiteD.FractalType;
import com.poixson.utils.FastNoiseLiteD.NoiseType;


// 11 | Concrete Jungle
public class Gen_011 extends BackroomsGen {

	// default params
	public static final double DEFAULT_NOISE_ROAD_FREQ            = 0.004;
	public static final double DEFAULT_NOISE_ROAD_JITTER          = 0.3;
	public static final double DEFAULT_NOISE_ALLEY_FREQ           = 0.016;
	public static final double DEFAULT_NOISE_ALLEY_JITTER         = 0.8;
	public static final double DEFAULT_NOISE_BUILDING_FREQ        = 0.03;
	public static final double DEFAULT_NOISE_BUILDING_JITTER_FREQ = 0.01;
	public static final double DEFAULT_THRESH_ROAD                = 0.7;
	public static final double DEFAULT_THRESH_ALLEY               = 0.7;
	public static final double DEFAULT_BUILDING_HEIGHT_BASE       = 6.0;
	public static final double DEFAULT_BUILDING_HEIGHT_FACTOR     = 40.0;

	// default blocks
	public static final String DEFAULT_BLOCK_SUBFLOOR = "minecraft:stone";
	public static final String DEFAULT_BLOCK_ROAD     = "minecraft:blackstone";
	public static final String DEFAULT_BLOCK_ALLEY    = "minecraft:cobbled_deepslate";

	// noise
	public final FastNoiseLiteD noiseRoad;
	public final FastNoiseLiteD noiseAlley;
	public final FastNoiseLiteD noiseHeight;
	public final FastNoiseLiteD noiseBuildingJitter;

	// params
	public final AtomicDouble noise_road_freq            = new AtomicDouble(DEFAULT_NOISE_ROAD_FREQ           );
	public final AtomicDouble noise_road_jitter          = new AtomicDouble(DEFAULT_NOISE_ROAD_JITTER         );
	public final AtomicDouble noise_alley_freq           = new AtomicDouble(DEFAULT_NOISE_ALLEY_FREQ          );
	public final AtomicDouble noise_alley_jitter         = new AtomicDouble(DEFAULT_NOISE_ALLEY_JITTER        );
	public final AtomicDouble noise_building_freq        = new AtomicDouble(DEFAULT_NOISE_BUILDING_FREQ       );
	public final AtomicDouble noise_building_jitter_freq = new AtomicDouble(DEFAULT_NOISE_BUILDING_JITTER_FREQ);
	public final AtomicDouble thresh_road                = new AtomicDouble(DEFAULT_THRESH_ROAD               );
	public final AtomicDouble thresh_alley               = new AtomicDouble(DEFAULT_THRESH_ALLEY              );
	public final AtomicDouble building_height_base       = new AtomicDouble(DEFAULT_BUILDING_HEIGHT_BASE      );
	public final AtomicDouble building_height_factor     = new AtomicDouble(DEFAULT_BUILDING_HEIGHT_FACTOR    );

	// blocks
	public final AtomicReference<String> block_subfloor = new AtomicReference<String>(null);
	public final AtomicReference<String> block_road     = new AtomicReference<String>(null);
	public final AtomicReference<String> block_alley    = new AtomicReference<String>(null);



	public Gen_011(final BackroomsLevel backlevel, final int seed,
			final int level_y, final int level_h) {
		super(backlevel, seed, level_y, level_h);
		// noise
		this.noiseRoad           = this.register(new FastNoiseLiteD());
		this.noiseAlley          = this.register(new FastNoiseLiteD());
		this.noiseHeight         = this.register(new FastNoiseLiteD());
		this.noiseBuildingJitter = this.register(new FastNoiseLiteD());
	}



	@Override
	public void initNoise() {
		super.initNoise();
		// roads
		this.noiseRoad.setFrequency(     this.noise_road_freq  .get());
		this.noiseRoad.setCellularJitter(this.noise_road_jitter.get());
		this.noiseRoad.setNoiseType(NoiseType.Cellular);
		this.noiseRoad.setFractalType(FractalType.PingPong);
		this.noiseRoad.setCellularDistanceFunction(CellularDistanceFunction.Manhattan);
		// alleys
		this.noiseAlley.setFrequency(     this.noise_alley_freq  .get());
		this.noiseAlley.setCellularJitter(this.noise_alley_jitter.get());
		this.noiseAlley.setNoiseType(NoiseType.Cellular);
		this.noiseAlley.setFractalType(FractalType.PingPong);
		this.noiseAlley.setCellularDistanceFunction(CellularDistanceFunction.Manhattan);
		// building height
		this.noiseHeight.setFrequency(this.noise_building_freq.get());
		this.noiseHeight.setNoiseType(NoiseType.Cellular);
		this.noiseHeight.setCellularReturnType(CellularReturnType.CellValue);
		// building height jitter
		this.noiseBuildingJitter.setFrequency(this.noise_building_jitter_freq.get());
	}



	@Override
	public void generate(final PreGenData pregen,
			final LinkedList<Tuple<BlockPlotter, StringBuilder[][]>> plots,
			final ChunkData chunk, final int chunkX, final int chunkZ) {
		if (!ENABLE_GEN_011) return;
		final BlockData block_subfloor = StringToBlockData(this.block_subfloor, DEFAULT_BLOCK_SUBFLOOR);
		final BlockData block_road     = StringToBlockData(this.block_road,     DEFAULT_BLOCK_ROAD    );
		final BlockData block_alley    = StringToBlockData(this.block_alley,    DEFAULT_BLOCK_ALLEY   );
		if (block_subfloor == null) throw new RuntimeException("Invalid block type for level 11 SubFloor");
		if (block_road     == null) throw new RuntimeException("Invalid block type for level 11 Road"    );
		if (block_alley    == null) throw new RuntimeException("Invalid block type for level 11 Alley"   );
		final DataHolder_City city = ((PregenLevel11) pregen).city;
		final int y = this.level_y + SUBFLOOR + 1;
		for (int iz=0; iz<16; iz++) {
			for (int ix=0; ix<16; ix++) {
				final CityData data = city.data[iz+16][ix+16];
				// subfloor
				chunk.setBlock(ix, this.level_y, iz, Material.BEDROCK);
				for (int iy=0; iy<SUBFLOOR; iy++)
					chunk.setBlock(ix, this.level_y+iy+1, iz, block_subfloor);
				// road
				if (data.isRoad) {
					chunk.setBlock(ix, y, iz, block_road);
					// sidewalk
					if (data.edge_road < 5)
						chunk.setBlock(ix, y+1, iz, Material.POLISHED_DIORITE_SLAB);
				} else
				// alley
				if (data.isAlley) {
					chunk.setBlock(ix, y,   iz, block_alley     );
				// building
				} else {
//TODO
if (data.isEdgeMain) chunk.setBlock(ix, y+3, iz, Material.BLUE_WOOL );
if (data.isEdgeBack) chunk.setBlock(ix, y+4, iz, Material.GREEN_WOOL);
					if (!data.isRoadOrAlley()) {
						final Material block_building;
						final int mod = data.building_height_int % 5;
						switch (mod) {
						case 0:  block_building = Material.OAK_PLANKS;      break;
						case 1:  block_building = Material.DARK_OAK_PLANKS; break;
						case 2:  block_building = Material.BRICKS;          break;
						case 3:  block_building = Material.STONE_BRICKS;    break;
						default: block_building = Material.STONE;           break;
						}
						final int h = data.building_height_int;
						// building roof
						if (ENABLE_TOP_011)
							chunk.setBlock(ix, y+h, iz, Material.STONE_SLAB);
						// building walls
						if (data.isEdge()) {
							for (int iy=0; iy<h+1; iy++)
								chunk.setBlock(ix, y+iy, iz, block_building);
						}
					}
				}
			} // end ix
		} // end iz
	}



	// -------------------------------------------------------------------------------
	// configs



	@Override
	protected void loadConfig() {
		// params
		{
			final ConfigurationSection cfg = this.plugin.getLevelParams(0);
			this.noise_road_freq       .set(cfg.getDouble("Noise-Road-Freq"       ));
			this.noise_road_jitter     .set(cfg.getDouble("Noise-Road-Jitter"     ));
			this.noise_alley_freq      .set(cfg.getDouble("Noise-Alley-Freq"      ));
			this.noise_alley_jitter    .set(cfg.getDouble("Noise-Alley-Jitter"    ));
			this.noise_building_freq   .set(cfg.getDouble("Noise-Building-Freq"   ));
			this.thresh_road           .set(cfg.getDouble("Thresh-Road"           ));
			this.thresh_alley          .set(cfg.getDouble("Thresh-Alley"          ));
			this.building_height_base  .set(cfg.getDouble("Building-Height-Base"  ));
			this.building_height_factor.set(cfg.getDouble("Building-Height-Factor"));
		}
		// block types
		{
			final ConfigurationSection cfg = this.plugin.getLevelBlocks(11);
			this.block_subfloor.set(cfg.getString("SubFloor"));
			this.block_road    .set(cfg.getString("Road"    ));
			this.block_alley   .set(cfg.getString("Alley"   ));
		}
	}
	public static void ConfigDefaults(final FileConfiguration cfg) {
		// params
		cfg.addDefault("Level0.Params.Noise-Road-Freq",            DEFAULT_NOISE_ROAD_FREQ           );
		cfg.addDefault("Level0.Params.Noise-Road-Jitter",          DEFAULT_NOISE_ROAD_JITTER         );
		cfg.addDefault("Level0.Params.Noise-Alley-Freq",           DEFAULT_NOISE_ALLEY_FREQ          );
		cfg.addDefault("Level0.Params.Noise-Alley-Jitter",         DEFAULT_NOISE_ALLEY_JITTER        );
		cfg.addDefault("Level0.Params.Noise-Building-Freq",        DEFAULT_NOISE_BUILDING_FREQ       );
		cfg.addDefault("Level0.Params.Noise-Building-Jitter-Freq", DEFAULT_NOISE_BUILDING_JITTER_FREQ);
		cfg.addDefault("Level0.Params.Thresh-Road",                DEFAULT_THRESH_ROAD               );
		cfg.addDefault("Level0.Params.Thresh-Alley",               DEFAULT_THRESH_ALLEY              );
		cfg.addDefault("Level0.Params.Building-Height-Base",       DEFAULT_BUILDING_HEIGHT_BASE      );
		cfg.addDefault("Level0.Params.Building-Height-Factor",     DEFAULT_BUILDING_HEIGHT_FACTOR    );
		// block types
		cfg.addDefault("Level11.Blocks.SubFloor", DEFAULT_BLOCK_SUBFLOOR);
		cfg.addDefault("Level11.Blocks.Road",     DEFAULT_BLOCK_ROAD    );
		cfg.addDefault("Level11.Blocks.Alley",    DEFAULT_BLOCK_ALLEY   );
	}



}
