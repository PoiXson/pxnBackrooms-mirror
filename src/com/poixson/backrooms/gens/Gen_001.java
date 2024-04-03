package com.poixson.backrooms.gens;

import static com.poixson.utils.BlockUtils.StringToBlockData;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.backrooms.BackroomsGen;
import com.poixson.backrooms.BackroomsLevel;
import com.poixson.backrooms.PreGenData;
import com.poixson.backrooms.worlds.Level_000.PregenLevel0;
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
	public static final String DEFAULT_BLOCK_WALL      = "minecraft:mud_bricks";
	public static final String DEFAULT_BLOCK_SUBFLOOR  = "minecraft:dirt";
	public static final String DEFAULT_BLOCK_FLOOR_DRY = "minecraft:brown_concrete_powder";
	public static final String DEFAULT_BLOCK_FLOOR_WET = "minecraft:brown_concrete";

	// noise
	public final FastNoiseLiteD noiseBasementWalls;
	public final FastNoiseLiteD noiseMoist;
	public final FastNoiseLiteD noiseWell;

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
	public final AtomicReference<String> block_wall      = new AtomicReference<String>(null);
	public final AtomicReference<String> block_subfloor  = new AtomicReference<String>(null);
	public final AtomicReference<String> block_floor_dry = new AtomicReference<String>(null);
	public final AtomicReference<String> block_floor_wet = new AtomicReference<String>(null);



	public Gen_001(final BackroomsLevel backlevel, final int seed) {
		super(backlevel, null, seed);
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
		// noise
		this.noiseBasementWalls = this.register(new FastNoiseLiteD());
		this.noiseMoist         = this.register(new FastNoiseLiteD());
		this.noiseWell          = this.register(new FastNoiseLiteD());
	}



	@Override
	public int getLevelNumber() {
		return 1;
	}

	@Override
	public int getNextY() {
		return this.level_y + this.bedrock_barrier + this.subfloor + this.level_h + 3;
	}



	public class BasementData implements PreGenData {

		public final double valueWall;
		public final double valueMoistA;
		public final double valueMoistB;
		public boolean isWall;
		public boolean isWet;

		public BasementData(final double valueWall, final double valueMoistA, final double valueMoistB) {
			this.valueWall   = valueWall;
			this.valueMoistA = valueMoistA;
			this.valueMoistB = valueMoistB;
			this.isWall = (valueWall > Gen_001.this.thresh_wall);
			final double thresh_moist = Gen_001.this.thresh_moist;
			this.isWet = (
				valueMoistA > thresh_moist ||
				valueMoistB > thresh_moist
			);
		}

	}



	public void pregenerate(Map<Iab, BasementData> data,
			final int chunkX, final int chunkZ) {
		BasementData dao;
		int xx, zz;
		double valueWall, valueMoistA, valueMoistB;
		for (int iz=0; iz<16; iz++) {
			zz = (chunkZ * 16) + iz;
			for (int ix=0; ix<16; ix++) {
				xx = (chunkX * 16) + ix;
				valueWall   = this.noiseBasementWalls.getNoiseRot(xx, zz, 0.25);
				valueMoistA = this.noiseMoist.getNoise(xx, zz);
				valueMoistB = this.noiseMoist.getNoise(zz, xx);
				dao = new BasementData(valueWall, valueMoistA, valueMoistB);
				data.put(new Iab(ix, iz), dao);
			}
		}
	}



	@Override
	public void generate(final PreGenData pregen,
			final LinkedList<Tuple<BlockPlotter, StringBuilder[][]>> plots,
			final ChunkData chunk, final int chunkX, final int chunkZ) {
		if (!this.enable_gen) return;
		final BlockData block_wall      = StringToBlockData(this.block_wall,      DEFAULT_BLOCK_WALL     );
		final BlockData block_subfloor  = StringToBlockData(this.block_subfloor,  DEFAULT_BLOCK_SUBFLOOR );
		final BlockData block_floor_dry = StringToBlockData(this.block_floor_dry, DEFAULT_BLOCK_FLOOR_DRY);
		final BlockData block_floor_wet = StringToBlockData(this.block_floor_wet, DEFAULT_BLOCK_FLOOR_WET);
		if (block_wall      == null) throw new RuntimeException("Invalid block type for level 1 Wall"     );
		if (block_subfloor  == null) throw new RuntimeException("Invalid block type for level 1 SubFloor" );
		if (block_floor_dry == null) throw new RuntimeException("Invalid block type for level 1 Floor-Dry");
		if (block_floor_wet == null) throw new RuntimeException("Invalid block type for level 1 Floor-Wet");
		final HashMap<Iab, BasementData> basementData = ((PregenLevel0)pregen).basement;
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
							LAMP_SWITCH:
							switch (mod_x) {
							case 0: chunk.setBlock(ix, y_floor+this.lamp_y+1, iz, Material.BEDROCK);       break LAMP_SWITCH;
							case 1:
							case 9: chunk.setBlock(ix, y_floor+this.lamp_y+1, iz, Material.REDSTONE_WIRE); break LAMP_SWITCH;
							case 2:
							case 8:
								for (int iy=0; iy<5; iy++)
									chunk.setBlock(ix, y_floor+iy+this.lamp_y+1, iz, Material.CHAIN);
								break LAMP_SWITCH;
							default: break LAMP_SWITCH;
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
	protected void initNoise(final ConfigurationSection cfgParams) {
		super.initNoise(cfgParams);
		// params
		// basement wall noise
		this.noiseWalls.setFrequency(                cfgParams.getDouble("Noise-Wall-Freq"    ) );
		this.noiseWalls.setFractalOctaves(           cfgParams.getInt(   "Noise-Wall-Octave"  ) );
		this.noiseWalls.setFractalGain(              cfgParams.getDouble("Noise-Wall-Gain"    ) );
		this.noiseWalls.setFractalPingPongStrength(  cfgParams.getDouble("Noise-Wall-Strength") );
		this.noiseWalls.setNoiseType(                NoiseType.Cellular                         );
		this.noiseWalls.setFractalType(              FractalType.PingPong                       );
		this.noiseWalls.setCellularDistanceFunction( CellularDistanceFunction.Manhattan         );
		this.noiseWalls.setCellularReturnType(       CellularReturnType.Distance                );
		// moist noise
		this.noiseMoist.setFrequency(      cfgParams.getDouble("Noise-Moist-Freq"  ) );
		this.noiseMoist.setFractalOctaves( cfgParams.getInt(   "Noise-Moist-Octave") );
		this.noiseMoist.setFractalGain(    cfgParams.getDouble("Noise-Moist-Gain"  ) );
		// well noise
		this.noiseWell.setFrequency( cfgParams.getDouble("Noise-Well-Freq") );
	}



	@Override
	protected void loadConfig(final ConfigurationSection cfgParams, final ConfigurationSection cfgBlocks) {
		// block types
		this.block_wall     .set(cfgBlocks.getString("Wall"     ));
		this.block_subfloor .set(cfgBlocks.getString("SubFloor" ));
		this.block_floor_dry.set(cfgBlocks.getString("Floor-Dry"));
		this.block_floor_wet.set(cfgBlocks.getString("Floor-Wet"));
	}
	@Override
	protected void configDefaults(final ConfigurationSection cfgParams, final ConfigurationSection cfgBlocks) {
		// params
		cfgParams.addDefault("Enable-Gen",          Boolean.TRUE                                );
		cfgParams.addDefault("Enable-Top",          Boolean.TRUE                                );
		cfgParams.addDefault("Noise-Wall-Freq",     DEFAULT_NOISE_WALL_FREQ    );
		cfgParams.addDefault("Noise-Wall-Octave",   DEFAULT_NOISE_WALL_OCTAVE  );
		cfgParams.addDefault("Noise-Wall-Gain",     DEFAULT_NOISE_WALL_GAIN    );
		cfgParams.addDefault("Noise-Wall-Strength", DEFAULT_NOISE_WALL_STRENGTH);
		cfgParams.addDefault("Noise-Moist-Freq",    DEFAULT_NOISE_MOIST_FREQ   );
		cfgParams.addDefault("Noise-Moist-Octave",  DEFAULT_NOISE_MOIST_OCTAVE );
		cfgParams.addDefault("Noise-Moist-Gain",    DEFAULT_NOISE_MOIST_GAIN   );
		cfgParams.addDefault("Noise-Well-Freq",     DEFAULT_NOISE_WELL_FREQ    );
		cfgParams.addDefault("Level-Y",             Integer.valueOf(DEFAULT_LEVEL_Y            ));
		cfgParams.addDefault("Level-Height",        Integer.valueOf(DEFAULT_LEVEL_H            ));
		cfgParams.addDefault("SubFloor",            Integer.valueOf(DEFAULT_SUBFLOOR           ));
		cfgParams.addDefault("Wall-Height",         Integer.valueOf(DEFAULT_WALL_HEIGHT        ));
		cfgParams.addDefault("Lamp-Y",              Integer.valueOf(DEFAULT_LAMP_Y             ));
		cfgParams.addDefault("Well-Size",           Integer.valueOf(DEFAULT_WELL_SIZE          ));
		cfgParams.addDefault("Well-Height",         Integer.valueOf(DEFAULT_WELL_HEIGHT        ));
		cfgParams.addDefault("Thresh-Wall",         Double .valueOf(DEFAULT_THRESH_WALL        ));
		cfgParams.addDefault("Thresh-Moist",        Double .valueOf(DEFAULT_THRESH_MOIST       ));
		// block types
		cfgBlocks.addDefault("Wall",      DEFAULT_BLOCK_WALL     );
		cfgBlocks.addDefault("SubFloor",  DEFAULT_BLOCK_SUBFLOOR );
		cfgBlocks.addDefault("Floor-Dry", DEFAULT_BLOCK_FLOOR_DRY);
		cfgBlocks.addDefault("Floor-Wet", DEFAULT_BLOCK_FLOOR_WET);
	}



}
