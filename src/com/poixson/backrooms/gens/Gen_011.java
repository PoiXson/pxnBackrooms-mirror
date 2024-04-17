package com.poixson.backrooms.gens;

import static com.poixson.utils.BlockUtils.StringToBlockDataDef;

import java.util.LinkedList;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.backrooms.BackroomsGen;
import com.poixson.backrooms.BackroomsLevel;
import com.poixson.backrooms.PreGenData;
import com.poixson.backrooms.gens.DataHolder_City.CityData;
import com.poixson.backrooms.worlds.Level_011.PregenLevel11;
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
	public static final int    DEFAULT_LEVEL_H                    = 50;
	public static final int    DEFAULT_SUBFLOOR                   = 3;
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

	// params
	public final boolean enable_gen;
	public final boolean enable_top;
	public final int     level_y;
	public final int     level_h;
	public final int     subfloor;
	public final double  thresh_road;
	public final double  thresh_alley;

	// blocks
	public final String block_subfloor;
	public final String block_road;
	public final String block_alley;

	// noise
	public final FastNoiseLiteD noiseRoad;
	public final FastNoiseLiteD noiseAlley;
	public final FastNoiseLiteD noiseHeight;
	public final FastNoiseLiteD noiseBuildingJitter;



	public Gen_011(final BackroomsLevel backlevel, final int seed, final BackroomsGen gen_below) {
		super(backlevel, gen_below, seed);
		final int level_number = this.getLevelNumber();
		final ConfigurationSection cfgParams = this.plugin.getConfigLevelParams(level_number);
		final ConfigurationSection cfgBlocks = this.plugin.getConfigLevelBlocks(level_number);
		// params
		this.enable_gen             = cfgParams.getBoolean("Enable-Gen"  );
		this.enable_top             = cfgParams.getBoolean("Enable-Top"  );
		this.level_y                = cfgParams.getInt(    "Level-Y"     );
		this.level_h                = cfgParams.getInt(    "Level-Height");
		this.subfloor               = cfgParams.getInt(    "SubFloor"    );
		this.thresh_road            = cfgParams.getDouble( "Thresh-Road" );
		this.thresh_alley           = cfgParams.getDouble( "Thresh-Alley");
		// block types
		this.block_subfloor = cfgBlocks.getString("SubFloor");
		this.block_road     = cfgBlocks.getString("Road"    );
		this.block_alley    = cfgBlocks.getString("Alley"   );
		// noise
		this.noiseRoad           = this.register(new FastNoiseLiteD());
		this.noiseAlley          = this.register(new FastNoiseLiteD());
		this.noiseHeight         = this.register(new FastNoiseLiteD());
		this.noiseBuildingJitter = this.register(new FastNoiseLiteD());
	}



	@Override
	public int getLevelNumber() {
		return 11;
	}

	@Override
	public int getNextY() {
		return this.level_y + this.bedrock_barrier + this.subfloor + this.level_h + 1;
	}



	@Override
	public void generate(final PreGenData pregen,
			final LinkedList<Tuple<BlockPlotter, StringBuilder[][]>> plots,
			final ChunkData chunk, final int chunkX, final int chunkZ) {
		if (!this.enable_gen) return;
		final BlockData block_subfloor = StringToBlockDataDef(this.block_subfloor, DEFAULT_BLOCK_SUBFLOOR);
		final BlockData block_road     = StringToBlockDataDef(this.block_road,     DEFAULT_BLOCK_ROAD    );
		final BlockData block_alley    = StringToBlockDataDef(this.block_alley,    DEFAULT_BLOCK_ALLEY   );
		if (block_subfloor == null) throw new RuntimeException("Invalid block type for level 11 SubFloor");
		if (block_road     == null) throw new RuntimeException("Invalid block type for level 11 Road"    );
		if (block_alley    == null) throw new RuntimeException("Invalid block type for level 11 Alley"   );
		final DataHolder_City data_city = ((PregenLevel11) pregen).city;
		final int y_base  = this.level_y + this.bedrock_barrier;
		final int y_floor = y_base + this.subfloor;
		for (int iz=0; iz<16; iz++) {
			for (int ix=0; ix<16; ix++) {
				final CityData dao_city = data_city.data[iz+16][ix+16];
				// subfloor
				chunk.setBlock(ix, this.level_y, iz, Material.BEDROCK);
				for (int iy=0; iy<this.subfloor; iy++)
					chunk.setBlock(ix, this.level_y+iy+1, iz, block_subfloor);
				// road
				if (dao_city.isRoad) {
					chunk.setBlock(ix, y_floor, iz, block_road);
					// sidewalk
					if (dao_city.edge_road < 5)
						chunk.setBlock(ix, y_floor+1, iz, Material.POLISHED_DIORITE_SLAB);
				} else
				// alley
				if (dao_city.isAlley) {
					chunk.setBlock(ix, y_floor,   iz, block_alley     );
				// building
				} else {
//TODO
if (dao_city.isEdgeMain) chunk.setBlock(ix, y_floor+3, iz, Material.BLUE_WOOL );
if (dao_city.isEdgeBack) chunk.setBlock(ix, y_floor+4, iz, Material.GREEN_WOOL);
					if (!dao_city.isRoadOrAlley()) {
						final Material block_building;
						final int mod = dao_city.building_height_int % 5;
						switch (mod) {
						case 0:  block_building = Material.OAK_PLANKS;      break;
						case 1:  block_building = Material.DARK_OAK_PLANKS; break;
						case 2:  block_building = Material.BRICKS;          break;
						case 3:  block_building = Material.STONE_BRICKS;    break;
						default: block_building = Material.STONE;           break;
						}
						final int h_building = dao_city.building_height_int;
						// building roof
						if (this.enable_top)
							chunk.setBlock(ix, y_floor+h_building, iz, Material.STONE_SLAB);
						// building walls
						if (dao_city.isEdge()) {
							for (int iy=0; iy<h_building+1; iy++)
								chunk.setBlock(ix, y_floor+iy, iz, block_building);
						}
					}
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
		// roads
		this.noiseRoad.setFrequency(               cfgParams.getDouble("Noise-Road-Freq"  ));
		this.noiseRoad.setCellularJitter(          cfgParams.getDouble("Noise-Road-Jitter"));
		this.noiseRoad.setNoiseType(               NoiseType.Cellular                      );
		this.noiseRoad.setFractalType(             FractalType.PingPong                    );
		this.noiseRoad.setCellularDistanceFunction(CellularDistanceFunction.Manhattan      );
		// alleys
		this.noiseAlley.setFrequency(               cfgParams.getDouble("Noise-Alley-Freq"  ));
		this.noiseAlley.setCellularJitter(          cfgParams.getDouble("Noise-Alley-Jitter"));
		this.noiseAlley.setNoiseType(               NoiseType.Cellular                       );
		this.noiseAlley.setFractalType(             FractalType.PingPong                     );
		this.noiseAlley.setCellularDistanceFunction(CellularDistanceFunction.Manhattan       );
		// building height
		this.noiseHeight.setFrequency(         cfgParams.getDouble("Noise-Building-Freq"));
		this.noiseHeight.setNoiseType(         NoiseType.Cellular                        );
		this.noiseHeight.setCellularReturnType(CellularReturnType.CellValue              );
		// building height jitter
		this.noiseBuildingJitter.setFrequency(cfgParams.getDouble("Noise-Building-Jitter-Freq"));
	}



	@Override
	protected void configDefaults(final ConfigurationSection cfgParams, final ConfigurationSection cfgBlocks) {
		// params
		cfgParams.addDefault("Enable-Gen",                 Boolean.TRUE                                       );
		cfgParams.addDefault("Enable-Top",                 Boolean.TRUE                                       );
		cfgParams.addDefault("Level-Y",                    Integer.valueOf(this.getDefaultY()                ));
		cfgParams.addDefault("Level-Height",               Integer.valueOf(DEFAULT_LEVEL_H                   ));
		cfgParams.addDefault("SubFloor",                   Integer.valueOf(DEFAULT_SUBFLOOR                  ));
		cfgParams.addDefault("Noise-Road-Freq",            Double .valueOf(DEFAULT_NOISE_ROAD_FREQ           ));
		cfgParams.addDefault("Noise-Road-Jitter",          Double .valueOf(DEFAULT_NOISE_ROAD_JITTER         ));
		cfgParams.addDefault("Noise-Alley-Freq",           Double .valueOf(DEFAULT_NOISE_ALLEY_FREQ          ));
		cfgParams.addDefault("Noise-Alley-Jitter",         Double .valueOf(DEFAULT_NOISE_ALLEY_JITTER        ));
		cfgParams.addDefault("Noise-Building-Freq",        Double .valueOf(DEFAULT_NOISE_BUILDING_FREQ       ));
		cfgParams.addDefault("Noise-Building-Jitter-Freq", Double .valueOf(DEFAULT_NOISE_BUILDING_JITTER_FREQ));
		cfgParams.addDefault("Thresh-Road",                Double .valueOf(DEFAULT_THRESH_ROAD               ));
		cfgParams.addDefault("Thresh-Alley",               Double .valueOf(DEFAULT_THRESH_ALLEY              ));
		// block types
		cfgBlocks.addDefault("SubFloor", DEFAULT_BLOCK_SUBFLOOR);
		cfgBlocks.addDefault("Road",     DEFAULT_BLOCK_ROAD    );
		cfgBlocks.addDefault("Alley",    DEFAULT_BLOCK_ALLEY   );
	}



}
