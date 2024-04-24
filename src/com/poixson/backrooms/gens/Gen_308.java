/*
package com.poixson.backrooms.gens;

import static com.poixson.utils.BlockUtils.StringToBlockDataDef;

import java.util.LinkedList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.backrooms.BackroomsGen;
import com.poixson.backrooms.BackroomsWorld;
import com.poixson.backrooms.PreGenData;
import com.poixson.tools.abstractions.Tuple;
import com.poixson.tools.plotter.BlockPlotter;
import com.poixson.utils.FastNoiseLiteD;
import com.poixson.utils.FastNoiseLiteD.CellularDistanceFunction;
import com.poixson.utils.FastNoiseLiteD.FractalType;
import com.poixson.utils.FastNoiseLiteD.NoiseType;


// 308 | Ikea
public class Gen_308 extends BackroomsGen {

	// default params
	public static final int    DEFAULT_LEVEL_H           = 9;
	public static final int    DEFAULT_SUBFLOOR          = 3;
	public static final int    DEFAULT_SUBCEILING        = 3;
	public static final double DEFAULT_NOISE_WALL_FREQ   = 0.025;
	public static final double DEFAULT_NOISE_WALL_JITTER = 0.7;
	public static final double DEFAULT_THRESH_WALL_L1    = 0.85;
	public static final double DEFAULT_THRESH_WALL_H1    = 0.92;
	public static final double DEFAULT_THRESH_WALL_L2    = 0.95;
	public static final double DEFAULT_THRESH_WALL_H2    = 1.0;

	// default blocks
	public static final String DEFAULT_BLOCK_WALL        = "minecraft:smooth_quartz";
	public static final String DEFAULT_BLOCK_WALL_STRIPE = "minecraft:quartz_block";
	public static final String DEFAULT_BLOCK_SUBFLOOR    = "minecraft:oak_planks";
	public static final String DEFAULT_BLOCK_SUBCEILING  = "minecraft:oak_planks";
	public static final String DEFAULT_BLOCK_FLOOR       = "minecraft:polished_andesite";
	public static final String DEFAULT_BLOCK_CEILING     = "minecraft:smooth_stone_slab[type=top]";

	// params
	public final boolean enable_gen;
	public final boolean enable_top;
	public final int     level_y;
	public final int     level_h;
	public final int     subfloor;
	public final int     subceiling;
	public final double  thresh_wall_L1;
	public final double  thresh_wall_H1;
	public final double  thresh_wall_L2;
	public final double  thresh_wall_H2;

	// blocks
	public final String block_wall;
	public final String block_wall_stripe;
	public final String block_subfloor;
	public final String block_subceiling;
	public final String block_floor;
	public final String block_ceiling;

	// noise
	public final FastNoiseLiteD noiseIkeaWalls;



	public Gen_308(final BackroomsWorld backworld, final int seed, final BackroomsGen gen_below) {
		super(backworld, gen_below, seed);
		final int level_number = this.getLevelNumber();
		final ConfigurationSection cfgParams = this.plugin.getConfigLevelParams(level_number);
		final ConfigurationSection cfgBlocks = this.plugin.getConfigLevelBlocks(level_number);
		// params
		this.enable_gen     = cfgParams.getBoolean("Enable-Gen"    );
		this.enable_top     = cfgParams.getBoolean("Enable-Top"    );
		this.level_y        = cfgParams.getInt(    "Level-Y"       );
		this.level_h        = cfgParams.getInt(    "Level-Height"  );
		this.subfloor       = cfgParams.getInt(    "SubFloor"      );
		this.subceiling     = cfgParams.getInt(    "SubCeiling"    );
		this.thresh_wall_L1 = cfgParams.getDouble( "Thresh-Wall-L1");
		this.thresh_wall_H1 = cfgParams.getDouble( "Thresh-Wall-H1");
		this.thresh_wall_L2 = cfgParams.getDouble( "Thresh-Wall-L2");
		this.thresh_wall_H2 = cfgParams.getDouble( "Thresh-Wall-H2");
		// block types
		this.block_wall        = cfgBlocks.getString("Wall"       );
		this.block_wall_stripe = cfgBlocks.getString("Wall-Stripe");
		this.block_subfloor    = cfgBlocks.getString("SubFloor"   );
		this.block_subceiling  = cfgBlocks.getString("SubCeiling" );
		this.block_floor       = cfgBlocks.getString("Floor"      );
		this.block_ceiling     = cfgBlocks.getString("Ceiling"    );
		// noise
		this.noiseIkeaWalls = this.register(new FastNoiseLiteD());
	}



	@Override
	public int getLevelNumber() {
		return 308;
	}

	@Override
	public int getNextY() {
		return this.level_y + this.bedrock_barrier + this.subfloor + this.level_h + this.subceiling + 2;
	}



	@Override
	public void generate(final PreGenData pregen,
			final LinkedList<Tuple<BlockPlotter, StringBuilder[][]>> plots,
			final ChunkData chunk, final int chunkX, final int chunkZ) {
		if (!this.enable_gen) return;
		final BlockData block_wall        = StringToBlockDataDef(this.block_wall,        DEFAULT_BLOCK_WALL       );
		final BlockData block_wall_stripe = StringToBlockDataDef(this.block_wall_stripe, DEFAULT_BLOCK_WALL_STRIPE);
		final BlockData block_subfloor    = StringToBlockDataDef(this.block_subfloor,    DEFAULT_BLOCK_SUBFLOOR   );
		final BlockData block_subceiling  = StringToBlockDataDef(this.block_subceiling,  DEFAULT_BLOCK_SUBCEILING );
		final BlockData block_floor       = StringToBlockDataDef(this.block_floor,       DEFAULT_BLOCK_FLOOR      );
		final BlockData block_ceiling     = StringToBlockDataDef(this.block_ceiling,     DEFAULT_BLOCK_CEILING    );
		if (block_wall        == null) throw new RuntimeException("Invalid block type for level 308 Wall"       );
		if (block_wall_stripe == null) throw new RuntimeException("Invalid block type for level 308 Wall-Stripe");
		if (block_subfloor    == null) throw new RuntimeException("Invalid block type for level 308 SubFloor"   );
		if (block_subceiling  == null) throw new RuntimeException("Invalid block type for level 308 SubCeiling" );
		if (block_floor       == null) throw new RuntimeException("Invalid block type for level 308 Floor"      );
		if (block_ceiling     == null) throw new RuntimeException("Invalid block type for level 308 Ceiling"    );
		final BlockData lamp = Bukkit.createBlockData("minecraft:redstone_lamp[lit=true]");
		final BlockData block_lamp_lever = Bukkit.createBlockData("minecraft:lever[powered=true,face=floor,facing=north]");
		final BlockData block_chain  = Bukkit.createBlockData("chain[axis=y]");
		final BlockData block_bars_n = Bukkit.createBlockData("minecraft:iron_bars[south=true]");
		final BlockData block_bars_s = Bukkit.createBlockData("minecraft:iron_bars[north=true]");
		final double[][] values = new double[18][18];
		for (int iz=0; iz<18; iz++) {
			final int zz = (chunkZ * 16) + iz;
			for (int ix=0; ix<18; ix++) {
				final int xx = (chunkX * 16) + ix;
				values[iz][ix] = this.noiseIkeaWalls.getNoise(xx-1, zz-1);
			}
		}
		final int y  = this.level_y + this.subfloor + 1;
		final int cy = this.level_h + y;
		int xx, zz;
		int modX, modZ;
		double value;
		boolean isWall;
		for (int iz=0; iz<16; iz++) {
			zz = (chunkZ * 16) + iz;
			modZ = (zz < 0 ? 0-zz : zz) % 9;
			for (int ix=0; ix<16; ix++) {
				xx = (chunkX * 16) + ix;
				modX = (xx < 0 ? 1-xx : xx) % 7;
				value = this.noiseIkeaWalls.getNoiseRot(xx, zz, 0.25);
				isWall =
					(value > this.thresh_wall_L1 && value < this.thresh_wall_H1) ||
					(value > this.thresh_wall_L2 && value < this.thresh_wall_H2);
				// subfloor
				chunk.setBlock(ix, this.level_y, iz, Material.BEDROCK);
				for (int iy=0; iy<this.subfloor; iy++)
					chunk.setBlock(ix, this.level_y+iy+1, iz, block_subfloor);
				// subceiling
				if (this.enable_top)
					chunk.setBlock(ix, cy+1, iz, block_subceiling);
				// wall
				if (isWall) {
					for (int iy=0; iy<this.level_h+1; iy++) {
						if (iy == 3 || iy == 5) chunk.setBlock(ix, y+iy, iz, block_wall_stripe);
						else                    chunk.setBlock(ix, y+iy, iz, block_wall       );
					}
				} else {
					// floor
					chunk.setBlock(ix, y, iz, block_floor);
					// ceiling
					if (this.enable_top) {
						// ceiling
						chunk.setBlock(ix, cy,   iz, block_ceiling   );
						chunk.setBlock(ix, cy+1, iz, block_subceiling);
						// ceiling lights
						if (modX == 1 && modZ < 5) {
							if (modZ == 0 || modZ == 4) {
								chunk.setBlock(ix, cy-1, iz, block_chain);
								if (modZ == 0) chunk.setBlock(ix, cy-2, iz, block_bars_n);
								else           chunk.setBlock(ix, cy-2, iz, block_bars_s);
							} else {
								chunk.setBlock(ix, cy-2, iz, lamp);
								if (modZ == 2) chunk.setBlock(ix, cy-1, iz, block_lamp_lever      );
								else           chunk.setBlock(ix, cy-1, iz, Material.REDSTONE_WIRE);
							}
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
		// ikea walls
		this.noiseIkeaWalls.setFrequency(               cfgParams.getDouble("Noise-Wall-Freq"  ));
		this.noiseIkeaWalls.setCellularJitter(          cfgParams.getDouble("Noise-Wall-Jitter"));
		this.noiseIkeaWalls.setNoiseType(               NoiseType.Cellular                      );
		this.noiseIkeaWalls.setFractalType(             FractalType.PingPong                    );
		this.noiseIkeaWalls.setCellularDistanceFunction(CellularDistanceFunction.Manhattan      );
	}



	@Override
	protected void configDefaults(final ConfigurationSection cfgParams, final ConfigurationSection cfgBlocks) {
		// params
		cfgParams.addDefault("Enable-Gen",        Boolean.TRUE                              );
		cfgParams.addDefault("Enable-Top",        Boolean.TRUE                              );
		cfgParams.addDefault("Level-Y",           Integer.valueOf(this.getDefaultY()       ));
		cfgParams.addDefault("Level-Height",      Integer.valueOf(DEFAULT_LEVEL_H          ));
		cfgParams.addDefault("SubFloor",          Integer.valueOf(DEFAULT_SUBFLOOR         ));
		cfgParams.addDefault("SubCeiling",        Integer.valueOf(DEFAULT_SUBCEILING       ));
		cfgParams.addDefault("Noise-Wall-Freq",   Double .valueOf(DEFAULT_NOISE_WALL_FREQ  ));
		cfgParams.addDefault("Noise-Wall-Jitter", Double .valueOf(DEFAULT_NOISE_WALL_JITTER));
		cfgParams.addDefault("Thresh-Wall-L1",    Double .valueOf(DEFAULT_THRESH_WALL_L1   ));
		cfgParams.addDefault("Thresh-Wall-H1",    Double .valueOf(DEFAULT_THRESH_WALL_H1   ));
		cfgParams.addDefault("Thresh-Wall-L2",    Double .valueOf(DEFAULT_THRESH_WALL_L2   ));
		cfgParams.addDefault("Thresh-Wall-H2",    Double .valueOf(DEFAULT_THRESH_WALL_H2   ));
		// block types
		cfgBlocks.addDefault("Wall",        DEFAULT_BLOCK_WALL       );
		cfgBlocks.addDefault("Wall-Stripe", DEFAULT_BLOCK_WALL_STRIPE);
		cfgBlocks.addDefault("SubFloor",    DEFAULT_BLOCK_SUBFLOOR   );
		cfgBlocks.addDefault("SubCeiling",  DEFAULT_BLOCK_SUBCEILING );
		cfgBlocks.addDefault("Floor",       DEFAULT_BLOCK_FLOOR      );
		cfgBlocks.addDefault("Ceiling",     DEFAULT_BLOCK_CEILING    );
	}



}
*/
