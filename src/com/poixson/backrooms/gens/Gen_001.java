package com.poixson.backrooms.gens;

import static com.poixson.backrooms.worlds.Level_000.SUBFLOOR;
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
import com.poixson.tools.abstractions.AtomicDouble;
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

	public static final int LAMP_Y = 6;

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
	public final AtomicDouble thresh_wall  = new AtomicDouble( DEFAULT_THRESH_WALL );
	public final AtomicDouble thresh_moist = new AtomicDouble( DEFAULT_THRESH_MOIST);

	// blocks
	public final AtomicReference<String> block_wall      = new AtomicReference<String>(null);
	public final AtomicReference<String> block_subfloor  = new AtomicReference<String>(null);
	public final AtomicReference<String> block_floor_dry = new AtomicReference<String>(null);
	public final AtomicReference<String> block_floor_wet = new AtomicReference<String>(null);



	public Gen_001(final BackroomsLevel backlevel, final int seed,
			final int level_y, final int level_h) {
		super(backlevel, seed, level_y, level_h);
		this.enable_gen   = cfgParams.getBoolean("Enable-Gen"  );
		this.enable_top   = cfgParams.getBoolean("Enable-Top"  );
		// noise
		this.noiseBasementWalls = this.register(new FastNoiseLiteD());
		this.noiseMoist         = this.register(new FastNoiseLiteD());
		this.noiseWell          = this.register(new FastNoiseLiteD());
	}



	@Override
	public int getLevelNumber() {
		return 1;
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
			this.isWall = (valueWall > Gen_001.this.thresh_wall.get());
			final double thresh_moist = Gen_001.this.thresh_moist.get();
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
		BasementData dao;
		final int y = this.level_y + SUBFLOOR + 1;
		final int h = this.level_h + 1;
		int modX, modZ;
		int xx, zz;
		for (int iz=0; iz<16; iz++) {
			zz = (chunkZ * 16) + iz;
			modZ = (zz < 0 ? 0-zz : zz) % 10;
			for (int ix=0; ix<16; ix++) {
				xx = (chunkX * 16) + ix;
				modX = (xx < 0 ? 0-xx : xx) % 10;
				// basement floor
				chunk.setBlock(ix, this.level_y, iz, Material.BEDROCK);
				for (int yy=0; yy<SUBFLOOR; yy++)
					chunk.setBlock(ix, this.level_y+yy+1, iz, block_subfloor);
				dao = basementData.get(new Iab(ix, iz));
				if (dao == null) continue;
				// wall
				if (dao.isWall) {
					for (int yy=0; yy<h; yy++) {
						if (yy > 6) chunk.setBlock(ix, y+yy, iz, Material.BEDROCK);
						else        chunk.setBlock(ix, y+yy, iz, block_wall);
					}
				// room
				} else {
					if (dao.isWet) chunk.setBlock(ix, y, iz, block_floor_wet);
					else           chunk.setBlock(ix, y, iz, block_floor_dry);
					// basement lights
					if (modZ == 0) {
						if (modX < 3 || modX > 7) {
							chunk.setBlock(ix, y+LAMP_Y, iz, Material.REDSTONE_LAMP);
							switch (modX) {
							case 0: chunk.setBlock(ix, y+LAMP_Y+1, iz, Material.BEDROCK);       break;
							case 1:
							case 9: chunk.setBlock(ix, y+LAMP_Y+1, iz, Material.REDSTONE_WIRE); break;
							case 2:
							case 8:
								for (int iy=0; iy<5; iy++)
									chunk.setBlock(ix, y+iy+LAMP_Y+1, iz, Material.CHAIN);
								break;
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
		this.thresh_wall .set(cfgParams.getDouble("Thresh-Wall" ));
		this.thresh_moist.set(cfgParams.getDouble("Thresh-Moist"));
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
		cfgParams.addDefault("Thresh-Wall",         DEFAULT_THRESH_WALL        );
		cfgParams.addDefault("Thresh-Moist",        DEFAULT_THRESH_MOIST       );
		// block types
		cfgBlocks.addDefault("Wall",      DEFAULT_BLOCK_WALL     );
		cfgBlocks.addDefault("SubFloor",  DEFAULT_BLOCK_SUBFLOOR );
		cfgBlocks.addDefault("Floor-Dry", DEFAULT_BLOCK_FLOOR_DRY);
		cfgBlocks.addDefault("Floor-Wet", DEFAULT_BLOCK_FLOOR_WET);
	}



}
