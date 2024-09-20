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
import com.poixson.tools.abstractions.Tuple;
import com.poixson.tools.dao.Iab;
import com.poixson.tools.plotter.BlockPlotter;
import com.poixson.utils.FastNoiseLiteD;
import com.poixson.utils.FastNoiseLiteD.CellularDistanceFunction;
import com.poixson.utils.FastNoiseLiteD.CellularReturnType;
import com.poixson.utils.FastNoiseLiteD.FractalType;
import com.poixson.utils.FastNoiseLiteD.NoiseType;


// 1 | Basement
public class Gen_001 extends BackroomsGen {

	// default params
	public static final int    DEFAULT_LEVEL_Y             = -20;
	public static final int    DEFAULT_LEVEL_H             = 24;
	public static final int    DEFAULT_SUBFLOOR            = 3;
	public static final int    DEFAULT_WALL_HEIGHT         = 7;
	public static final int    DEFAULT_LAMP_Y              = 6;
	public static final int    DEFAULT_WELL_SIZE           = 5;
	public static final int    DEFAULT_WELL_HEIGHT         = 2;
	public static final double DEFAULT_NOISE_WALL_FREQ     = 0.033;
	public static final int    DEFAULT_NOISE_WALL_OCTAVE   = 2;
	public static final double DEFAULT_NOISE_WALL_GAIN     = 0.03;
	public static final double DEFAULT_NOISE_WALL_STRENGTH = 1.2;
	public static final double DEFAULT_NOISE_MOIST_FREQ    = 0.015;
	public static final int    DEFAULT_NOISE_MOIST_OCTAVE  = 2;
	public static final double DEFAULT_NOISE_MOIST_GAIN    = 2.0;
	public static final double DEFAULT_NOISE_WELL_FREQ     = 0.0028;
	public static final double DEFAULT_THRESH_WALL         = 0.9;
	public static final double DEFAULT_THRESH_MOIST        = 0.4;

	// default blocks
	public static final String DEFAULT_BLOCK_SUBFLOOR_WET = "minecraft:mud";
	public static final String DEFAULT_BLOCK_SUBFLOOR_DRY = "minecraft:dirt";
	public static final String DEFAULT_BLOCK_FLOOR_DRY    = "minecraft:brown_concrete_powder";
	public static final String DEFAULT_BLOCK_FLOOR_WET    = "minecraft:brown_concrete";
	public static final String DEFAULT_BLOCK_WALL         = "minecraft:mud_bricks";

	// params
	public final boolean enable_gen;
	public final boolean enable_top;
	public final int     level_y;
	public final int     level_h;
	public final int     subfloor;
	public final int     wall_height;
	public final int     lamp_y;
	public final int     well_size;
	public final int     well_height;
	public final double  thresh_wall;
	public final double  thresh_moist;

	// blocks
	public final String block_subfloor_wet;
	public final String block_subfloor_dry;
	public final String block_floor_dry;
	public final String block_floor_wet;
	public final String block_wall;

	// noise
	public final FastNoiseLiteD noiseWalls;
	public final FastNoiseLiteD noiseMoist;
	public final FastNoiseLiteD noiseWell;



	public Gen_001(final BackroomsWorld backworld, final int seed) {
		super(backworld, null, seed);
		final int level_number = this.getLevelNumber();
		final ConfigurationSection cfgParams = this.plugin.getConfigLevelParams(level_number);
		final ConfigurationSection cfgBlocks = this.plugin.getConfigLevelBlocks(level_number);
		// params
		this.enable_gen   = cfgParams.getBoolean("Enable-Gen"  );
		this.enable_top   = cfgParams.getBoolean("Enable-Top"  );
		this.level_y      = cfgParams.getInt(    "Level-Y"     );
		this.level_h      = cfgParams.getInt(    "Level-Height");
		this.subfloor     = cfgParams.getInt(    "SubFloor"    );
		this.wall_height  = cfgParams.getInt(    "Wall-Height" );
		this.lamp_y       = cfgParams.getInt(    "Lamp-Y"      );
		this.well_size    = cfgParams.getInt(    "Well-Size"   );
		this.well_height  = cfgParams.getInt(    "Well-Height" );
		this.thresh_wall  = cfgParams.getDouble( "Thresh-Wall" );
		this.thresh_moist = cfgParams.getDouble( "Thresh-Moist");
		// block types
		this.block_subfloor_wet = cfgBlocks.getString("SubFloor-Wet");
		this.block_subfloor_dry = cfgBlocks.getString("SubFloor-Dry");
		this.block_floor_dry    = cfgBlocks.getString("Floor-Dry"   );
		this.block_floor_wet    = cfgBlocks.getString("Floor-Wet"   );
		this.block_wall         = cfgBlocks.getString("Wall"        );
		// noise
		this.noiseWalls = this.register(new FastNoiseLiteD());
		this.noiseMoist = this.register(new FastNoiseLiteD());
		this.noiseWell  = this.register(new FastNoiseLiteD());
	}



	@Override
	public int getLevelNumber() {
		return 1;
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
	public int getMaxY() {
		return this.getMinY() + this.subfloor + this.level_h + 2;
	}

	public int getLampY() {
		return this.lamp_y;
	}


	public class BasementData implements PreGenData {

		public final double value_wall;
		public final double value_moistA;
		public final double value_moistB;
		public boolean isWall;
		public boolean isWet;

		public BasementData(final int x, final int z) {
			this.value_wall   = Gen_001.this.noiseWalls.getNoise(x, z);
			this.value_moistA = Gen_001.this.noiseMoist.getNoise(x, z);
			this.value_moistB = Gen_001.this.noiseMoist.getNoise(z, x);
			this.isWall = (this.value_wall > Gen_001.this.thresh_wall);
			final double thresh_moist = Gen_001.this.thresh_moist;
			this.isWet = (
				this.value_moistA > thresh_moist ||
				this.value_moistB > thresh_moist
			);
		}

	}



	public void pregenerate(Map<Iab, BasementData> data,
			final int chunkX, final int chunkZ) {
		for (int iz=0; iz<16; iz++) {
			final int zz = (chunkZ * 16) + iz;
			for (int ix=0; ix<16; ix++) {
				final int xx = (chunkX * 16) + ix;
				data.put(
					new Iab(ix, iz),
					new BasementData(xx, zz)
				);
			}
		}
	}



	@Override
	public void generate(final PreGenData pregen,
			final LinkedList<Tuple<BlockPlotter, StringBuilder[][]>> plots,
			final ChunkData chunk, final int chunkX, final int chunkZ) {
		if (!this.enable_gen) return;
		final BlockData block_subfloor_wet = StringToBlockDataDef(this.block_subfloor_wet, DEFAULT_BLOCK_SUBFLOOR_WET);
		final BlockData block_subfloor_dry = StringToBlockDataDef(this.block_subfloor_dry, DEFAULT_BLOCK_SUBFLOOR_DRY);
		final BlockData block_floor_dry    = StringToBlockDataDef(this.block_floor_dry,    DEFAULT_BLOCK_FLOOR_DRY   );
		final BlockData block_floor_wet    = StringToBlockDataDef(this.block_floor_wet,    DEFAULT_BLOCK_FLOOR_WET   );
		final BlockData block_wall         = StringToBlockDataDef(this.block_wall,         DEFAULT_BLOCK_WALL        );
		if (block_subfloor_wet == null) throw new RuntimeException("Invalid block type for level 1 SubFloor-Wet");
		if (block_subfloor_dry == null) throw new RuntimeException("Invalid block type for level 1 SubFloor-Dry");
		if (block_floor_dry    == null) throw new RuntimeException("Invalid block type for level 1 Floor-Dry"   );
		if (block_floor_wet    == null) throw new RuntimeException("Invalid block type for level 1 Floor-Wet"   );
		if (block_wall         == null) throw new RuntimeException("Invalid block type for level 1 Wall"        );
		final HashMap<Iab, BasementData> data_basement = ((Pregen_Level_000)pregen).basement;
		final int h_walls = this.level_h + 1;
		final int y_base  = this.level_y + this.bedrock_barrier;
		final int y_floor = y_base + this.subfloor;
		final int y_ceil  = y_floor + h_walls + 1;
		for (int iz=0; iz<16; iz++) {
			final int zz = (chunkZ * 16) + iz;
			final int mod_z = (zz < 0 ? 0-zz : zz) % 10;
			for (int ix=0; ix<16; ix++) {
				final int xx = (chunkX * 16) + ix;
				final int mod_x = (xx < 0 ? 0-xx : xx) % 10;
				final BasementData dao_basement = data_basement.get(new Iab(ix, iz));
				// barrier
				for (int iy=0; iy<this.bedrock_barrier; iy++)
					chunk.setBlock(ix, this.level_y+iy, iz, Material.BEDROCK);
				// subfloor wet
				if (dao_basement.isWet) {
					for (int iy=0; iy<this.subfloor; iy++)
						chunk.setBlock(ix, y_base+iy, iz, block_subfloor_wet);
				// subfloor dry
				} else {
					for (int iy=0; iy<this.subfloor; iy++)
						chunk.setBlock(ix, y_base+iy, iz, block_subfloor_dry);
				}
				// wall
				if (dao_basement.isWall) {
					for (int iy=0; iy<h_walls; iy++) {
						if (iy > this.wall_height) chunk.setBlock(ix, y_floor+iy, iz, Material.BEDROCK);
						else                       chunk.setBlock(ix, y_floor+iy, iz, block_wall      );
					}
				// room
				} else {
					// floor
					if (dao_basement.isWet) chunk.setBlock(ix, y_floor, iz, block_floor_wet);
					else                    chunk.setBlock(ix, y_floor, iz, block_floor_dry);
					// basement lights
					if (mod_z == 0) {
						if (mod_x < 3 || mod_x > 7) {
							chunk.setBlock(ix, y_floor+this.lamp_y, iz, Material.REDSTONE_LAMP);
							SWITCH_LAMP:
							switch (mod_x) {
							case 0: chunk.setBlock(ix, y_floor+this.lamp_y+1, iz, Material.BEDROCK);       break SWITCH_LAMP;
							case 1:
							case 9: chunk.setBlock(ix, y_floor+this.lamp_y+1, iz, Material.REDSTONE_WIRE); break SWITCH_LAMP;
							case 2:
							case 8:
								for (int iy=0; iy<5; iy++)
									chunk.setBlock(ix, y_floor+iy+this.lamp_y+1, iz, Material.CHAIN);
								break SWITCH_LAMP;
							default: break SWITCH_LAMP;
							}
						}
					}
				} // end wall/room
				// ceiling
				if (this.enable_top) {
					chunk.setBlock(ix, y_ceil-1, iz, Material.BEDROCK);
					if (dao_basement.isWet) chunk.setBlock(ix, y_ceil, iz, Material.WATER  );
					else                    chunk.setBlock(ix, y_ceil, iz, Material.BEDROCK);
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
		// basement wall noise
		this.noiseWalls.setAngle(0.25);
		this.noiseWalls.setFrequency(               cfgParams.getDouble("Noise-Wall-Freq"    ));
		this.noiseWalls.setFractalOctaves(          cfgParams.getInt(   "Noise-Wall-Octave"  ));
		this.noiseWalls.setFractalGain(             cfgParams.getDouble("Noise-Wall-Gain"    ));
		this.noiseWalls.setFractalPingPongStrength( cfgParams.getDouble("Noise-Wall-Strength"));
		this.noiseWalls.setNoiseType(               NoiseType.Cellular                        );
		this.noiseWalls.setFractalType(             FractalType.PingPong                      );
		this.noiseWalls.setCellularDistanceFunction(CellularDistanceFunction.Manhattan        );
		this.noiseWalls.setCellularReturnType(      CellularReturnType.Distance               );
		// moist noise
		this.noiseMoist.setFrequency(     cfgParams.getDouble("Noise-Moist-Freq"  ));
		this.noiseMoist.setFractalOctaves(cfgParams.getInt(   "Noise-Moist-Octave"));
		this.noiseMoist.setFractalGain(   cfgParams.getDouble("Noise-Moist-Gain"  ));
		// well noise
		this.noiseWell.setFrequency(cfgParams.getDouble("Noise-Well-Freq"));
	}



	@Override
	protected void configDefaults(final ConfigurationSection cfgParams, final ConfigurationSection cfgBlocks) {
		// params
		cfgParams.addDefault("Enable-Gen",          Boolean.TRUE                                );
		cfgParams.addDefault("Enable-Top",          Boolean.TRUE                                );
		cfgParams.addDefault("Level-Y",             Integer.valueOf(DEFAULT_LEVEL_Y            ));
		cfgParams.addDefault("Level-Height",        Integer.valueOf(DEFAULT_LEVEL_H            ));
		cfgParams.addDefault("SubFloor",            Integer.valueOf(DEFAULT_SUBFLOOR           ));
		cfgParams.addDefault("Wall-Height",         Integer.valueOf(DEFAULT_WALL_HEIGHT        ));
		cfgParams.addDefault("Lamp-Y",              Integer.valueOf(DEFAULT_LAMP_Y             ));
		cfgParams.addDefault("Well-Size",           Integer.valueOf(DEFAULT_WELL_SIZE          ));
		cfgParams.addDefault("Well-Height",         Integer.valueOf(DEFAULT_WELL_HEIGHT        ));
		cfgParams.addDefault("Noise-Wall-Freq",     Double .valueOf(DEFAULT_NOISE_WALL_FREQ    ));
		cfgParams.addDefault("Noise-Wall-Octave",   Integer.valueOf(DEFAULT_NOISE_WALL_OCTAVE  ));
		cfgParams.addDefault("Noise-Wall-Gain",     Double .valueOf(DEFAULT_NOISE_WALL_GAIN    ));
		cfgParams.addDefault("Noise-Wall-Strength", Double .valueOf(DEFAULT_NOISE_WALL_STRENGTH));
		cfgParams.addDefault("Noise-Moist-Freq",    Double .valueOf(DEFAULT_NOISE_MOIST_FREQ   ));
		cfgParams.addDefault("Noise-Moist-Octave",  Integer.valueOf(DEFAULT_NOISE_MOIST_OCTAVE ));
		cfgParams.addDefault("Noise-Moist-Gain",    Double .valueOf(DEFAULT_NOISE_MOIST_GAIN   ));
		cfgParams.addDefault("Noise-Well-Freq",     Double .valueOf(DEFAULT_NOISE_WELL_FREQ    ));
		cfgParams.addDefault("Thresh-Wall",         Double .valueOf(DEFAULT_THRESH_WALL        ));
		cfgParams.addDefault("Thresh-Moist",        Double .valueOf(DEFAULT_THRESH_MOIST       ));
		// block types
		cfgBlocks.addDefault("SubFloor-Wet", DEFAULT_BLOCK_SUBFLOOR_WET);
		cfgBlocks.addDefault("SubFloor-Dry", DEFAULT_BLOCK_SUBFLOOR_DRY);
		cfgBlocks.addDefault("Floor-Dry",    DEFAULT_BLOCK_FLOOR_DRY   );
		cfgBlocks.addDefault("Floor-Wet",    DEFAULT_BLOCK_FLOOR_WET   );
		cfgBlocks.addDefault("Wall",         DEFAULT_BLOCK_WALL        );
	}



}
