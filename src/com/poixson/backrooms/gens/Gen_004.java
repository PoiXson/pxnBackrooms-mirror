package com.poixson.backrooms.gens;

import static com.poixson.utils.BlockUtils.StringToBlockDataDef;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.backrooms.BackroomsGen;
import com.poixson.backrooms.BackroomsWorld;
import com.poixson.backrooms.PreGenData;
import com.poixson.backrooms.worlds.BackWorld_000.Pregen_Level_000;
import com.poixson.tools.abstractions.Tuple;
import com.poixson.tools.dao.Iab;
import com.poixson.tools.noise.FastNoiseLiteD;
import com.poixson.tools.noise.FastNoiseLiteD.CellularDistanceFunction;
import com.poixson.tools.noise.FastNoiseLiteD.FractalType;
import com.poixson.tools.noise.FastNoiseLiteD.NoiseType;
import com.poixson.tools.plotter.BlockPlotter;


// 4 | Ductwork
public class Gen_004 extends BackroomsGen {

	// default params
	public static final int    DEFAULT_LEVEL_H                  = 3;
	public static final int    DEFAULT_SUBCEILING               = 3;
	public static final int    DEFAULT_NOISE_DUCT_COUNT         = 4;
	public static final double DEFAULT_NOISE_DUCT_FREQ          = 0.01;
	public static final double DEFAULT_NOISE_DUCT_FREQ_ADJUST   = 0.015;
	public static final double DEFAULT_NOISE_DUCT_JITTER        = 0.8;
	public static final double DEFAULT_NOISE_DUCT_JITTER_ADJUST = 2.0;
	public static final double DEFAULT_THRESH_DUCT              = 0.93;

	// default blocks
	public static final String DEFAULT_BLOCK_SUBCEILING = "minecraft:oak_planks";
	public static final String DEFAULT_BLOCK_CEILING    = "minecraft:calcite";
	public static final String DEFAULT_BLOCK_FLOOR      = "minecraft:calcite";
	public static final String DEFAULT_BLOCK_WALLS      = "minecraft:calcite";
	public static final String DEFAULT_BLOCK_FILL       = "minecraft:oak_planks";

	// params
	public final boolean enable_gen;
	public final boolean enable_top;
	public final int     level_y;
	public final int     level_h;
	public final int     subceiling;
	public final int     noiseDuctCount;
	public final double  thresh_duct;

	// blocks
	public final String block_subceiling;
	public final String block_ceiling;
	public final String block_floor;
	public final String block_walls;
	public final String block_fill;

	// noise
	public final FastNoiseLiteD noiseDucts[];



	public Gen_004(final BackroomsWorld backworld, final int seed, final BackroomsGen gen_below)
			throws InvalidConfigurationException {
		super(backworld, gen_below, seed);
		final int level_number = this.getLevelNumber();
		final ConfigurationSection cfgParams = this.plugin.getConfigLevelParams(level_number);
		final ConfigurationSection cfgBlocks = this.plugin.getConfigLevelBlocks(level_number);
		// params
		this.enable_gen     = cfgParams.getBoolean("Enable-Gen"      );
		this.enable_top     = cfgParams.getBoolean("Enable-Top"      );
		this.level_y        = cfgParams.getInt(    "Level-Y"         );
		this.level_h        = cfgParams.getInt(    "Level-Height"    );
		this.subceiling     = cfgParams.getInt(    "SubCeiling"      );
		this.noiseDuctCount = cfgParams.getInt(    "Noise-Duct-Count");
		this.thresh_duct    = cfgParams.getDouble( "Thresh-Duct"     );
		// block types
		this.block_subceiling = cfgBlocks.getString("SubCeiling");
		this.block_ceiling    = cfgBlocks.getString("Ceiling"   );
		this.block_floor      = cfgBlocks.getString("Floor"     );
		this.block_walls      = cfgBlocks.getString("Walls"     );
		this.block_fill       = cfgBlocks.getString("Fill"      );
		// noise
		this.noiseDucts = new FastNoiseLiteD[this.noiseDuctCount];
		for (int i=0; i<this.noiseDuctCount; i++)
			this.noiseDucts[i] = this.register(new FastNoiseLiteD());
	}



	@Override
	public int getLevelNumber() {
		return 4;
	}



	@Override
	public int getLevelY() {
		return this.level_y;
	}
	@Override
	public int getOpenY() {
		return this.getMinY() + 1;
	}

	@Override
	public int getMinY() {
		return this.level_y;
	}
	@Override
	public int getMaxY() {
		return this.getMinY() + this.level_h + this.subceiling + 1;
	}



	// -------------------------------------------------------------------------------
	// generate



	public class DuctData implements PreGenData {

		public final double value_duct;
		public final double values_duct[];
		public final boolean isDuct;
		public       boolean isWall = false;

		public DuctData(final int x, final int z) {
			final int noiseDuctCount = Gen_004.this.noiseDuctCount;
			this.values_duct = new double[noiseDuctCount];
			double highest = -1.0;
			for (int i=0; i<noiseDuctCount; i++) {
				final double value = Gen_004.this.noiseDucts[i].getNoise(x, z);
				this.values_duct[i] = value;
				if (highest < value)
					highest = value;
			}
			this.value_duct = highest;
			this.isDuct = (this.value_duct > Gen_004.this.thresh_duct);
		}

	}



	public void pregenerate(Map<Iab, DuctData> data,
			final int chunkX, final int chunkZ) {
		// duct noise
		for (int iz=-1; iz<17; iz++) {
			final int zz = (chunkZ * 16) + iz;
			for (int ix=-1; ix<17; ix++) {
				final int xx = (chunkX * 16) + ix;
				data.put(
					new Iab(ix, iz),
					new DuctData(xx, zz)
				);
			}
		}
		// find walls
		for (int iz=0; iz<16; iz++) {
			for (int ix=0; ix<16; ix++) {
				final DuctData dao = data.get(new Iab(ix, iz));
				if (!dao.isDuct) {
					if (data.get(new Iab(ix,   iz-1)).isDuct) dao.isWall = true; else
					if (data.get(new Iab(ix,   iz+1)).isDuct) dao.isWall = true; else
					if (data.get(new Iab(ix+1, iz  )).isDuct) dao.isWall = true; else
					if (data.get(new Iab(ix-1, iz  )).isDuct) dao.isWall = true; else
					if (data.get(new Iab(ix+1, iz-1)).isDuct) dao.isWall = true; else
					if (data.get(new Iab(ix-1, iz-1)).isDuct) dao.isWall = true; else
					if (data.get(new Iab(ix+1, iz+1)).isDuct) dao.isWall = true; else
					if (data.get(new Iab(ix-1, iz+1)).isDuct) dao.isWall = true;
				}
			}
		}
	}



	@Override
	public void generate(final PreGenData pregen,
			final LinkedList<Tuple<BlockPlotter, StringBuilder[][]>> plots,
			final ChunkData chunk, final int chunkX, final int chunkZ) {
		if (!this.enable_gen) return;
		final BlockData block_subceiling = StringToBlockDataDef(this.block_subceiling, DEFAULT_BLOCK_SUBCEILING);
		final BlockData block_ceiling    = StringToBlockDataDef(this.block_ceiling,    DEFAULT_BLOCK_CEILING   );
		final BlockData block_floor      = StringToBlockDataDef(this.block_floor,      DEFAULT_BLOCK_FLOOR     );
		final BlockData block_walls      = StringToBlockDataDef(this.block_walls,      DEFAULT_BLOCK_WALLS     );
		final BlockData block_fill       = StringToBlockDataDef(this.block_fill,       DEFAULT_BLOCK_FILL      );
		if (block_subceiling == null) throw new RuntimeException("Invalid block type for level 23 SubCeiling");
		if (block_ceiling    == null) throw new RuntimeException("Invalid block type for level 23 Ceiling"   );
		if (block_floor      == null) throw new RuntimeException("Invalid block type for level 23 Floor"     );
		if (block_walls      == null) throw new RuntimeException("Invalid block type for level 23 Walls"     );
		if (block_fill       == null) throw new RuntimeException("Invalid block type for level 23 Fill"      );
		final Pregen_Level_000 pregen_000 = (Pregen_Level_000) pregen;
		final HashMap<Iab, DuctData> data_ducts = pregen_000.ducts;
		final int height  = this.level_h + (this.enable_top? 1 : 0 ) + 1;
		final int y_floor = this.level_y;
		final int y_ceil  = (y_floor + height) - 1;
		for (int iz=0; iz<16; iz++) {
			for (int ix=0; ix<16; ix++) {
				final DuctData dao_duct = data_ducts.get(new Iab(ix, iz));
				// duct wall
				if (dao_duct.isWall) {
					for (int iy=0; iy<height; iy++)
						chunk.setBlock(ix, y_floor+iy, iz, block_walls);
				} else
				// inside duct
				if (dao_duct.isDuct) {
					chunk.setBlock(ix, y_floor, iz, block_floor);
					if (this.enable_top)
						chunk.setBlock(ix, y_ceil, iz, block_ceiling);
				// fill
				} else {
					if (this.enable_top) {
						for (int iy=0; iy<height; iy++)
							chunk.setBlock(ix, y_floor+iy, iz, block_fill);
					}
				}
				// subceiling
				if (this.enable_top) {
					for (int iy=0; iy<this.subceiling; iy++)
						chunk.setBlock(ix, y_ceil+iy+1, iz, block_subceiling);
				}
			} // end ix
		} // end iz
	}



	// -------------------------------------------------------------------------------
	// configs



	@Override
	protected void initNoise() {
		final ConfigurationSection cfgParams = this.plugin.getConfigLevelParams(this.getLevelNumber());
		// ducts
		final double noiseDuctFreq         = cfgParams.getDouble("Noise-Duct-Freq"         );
		final double noiseDuctJitter       = cfgParams.getDouble("Noise-Duct-Jitter"       );
		final double noiseDuctFreqAdjust   = cfgParams.getDouble("Noise-Duct-Freq-Adjust"  );
		final double noiseDuctJitterAdjust = cfgParams.getDouble("Noise-Duct-Jitter-Adjust");
		for (int i=0; i<this.noiseDuctCount; i++) {
			final double ii = (double) i;
			final double freq_adjusted   = noiseDuctFreq   * (1.0 - (ii *noiseDuctFreqAdjust) );
			final double jitter_adjusted = noiseDuctJitter * (  (1.0+ii)*noiseDuctJitterAdjust);
			this.noiseDucts[i].setAngle(0.25);
			this.noiseDucts[i].setFrequency(               freq_adjusted                     );
			this.noiseDucts[i].setCellularJitter(          jitter_adjusted                   );
			this.noiseDucts[i].setNoiseType(               NoiseType.Cellular                );
			this.noiseDucts[i].setFractalType(             FractalType.PingPong              );
			this.noiseDucts[i].setCellularDistanceFunction(CellularDistanceFunction.Manhattan);
		}
	}



	@Override
	protected void configDefaults(final ConfigurationSection cfgParams, final ConfigurationSection cfgBlocks) {
		// params
		cfgParams.addDefault("Enable-Gen",               Boolean.TRUE                                     );
		cfgParams.addDefault("Enable-Top",               Boolean.TRUE                                     );
		cfgParams.addDefault("Level-Y",                  Integer.valueOf(this.getDefaultY()              ));
		cfgParams.addDefault("Level-Height",             Integer.valueOf(DEFAULT_LEVEL_H                 ));
		cfgParams.addDefault("SubCeiling",               Integer.valueOf(DEFAULT_SUBCEILING              ));
		cfgParams.addDefault("Noise-Duct-Count",         Integer.valueOf(DEFAULT_NOISE_DUCT_COUNT        ));
		cfgParams.addDefault("Noise-Duct-Freq",          Double .valueOf(DEFAULT_NOISE_DUCT_FREQ         ));
		cfgParams.addDefault("Noise-Duct-Jitter",        Double .valueOf(DEFAULT_NOISE_DUCT_JITTER       ));
		cfgParams.addDefault("Noise-Duct-Freq-Adjust",   Double .valueOf(DEFAULT_NOISE_DUCT_FREQ_ADJUST  ));
		cfgParams.addDefault("Noise-Duct-Jitter-Adjust", Double .valueOf(DEFAULT_NOISE_DUCT_JITTER_ADJUST));
		cfgParams.addDefault("Thresh-Duct",              Double .valueOf(DEFAULT_THRESH_DUCT             ));
		// block types
		cfgBlocks.addDefault("SubCeiling", DEFAULT_BLOCK_SUBCEILING);
		cfgBlocks.addDefault("Ceiling",    DEFAULT_BLOCK_CEILING   );
		cfgBlocks.addDefault("Floor",      DEFAULT_BLOCK_FLOOR     );
		cfgBlocks.addDefault("Walls",      DEFAULT_BLOCK_WALLS     );
		cfgBlocks.addDefault("Fill",       DEFAULT_BLOCK_FILL      );
	}



}
