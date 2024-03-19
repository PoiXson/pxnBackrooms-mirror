package com.poixson.backrooms.gens;

import static com.poixson.backrooms.worlds.Level_000.SUBFLOOR;
import static com.poixson.backrooms.worlds.Level_011.ENABLE_GEN_308;
import static com.poixson.backrooms.worlds.Level_011.ENABLE_TOP_308;
import static com.poixson.utils.BlockUtils.StringToBlockData;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicReference;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.backrooms.BackroomsGen;
import com.poixson.backrooms.BackroomsLevel;
import com.poixson.backrooms.PreGenData;
import com.poixson.tools.abstractions.AtomicDouble;
import com.poixson.tools.abstractions.Tuple;
import com.poixson.tools.plotter.BlockPlotter;
import com.poixson.utils.FastNoiseLiteD;
import com.poixson.utils.FastNoiseLiteD.CellularDistanceFunction;
import com.poixson.utils.FastNoiseLiteD.FractalType;
import com.poixson.utils.FastNoiseLiteD.NoiseType;


// 308 | Ikea
public class Gen_308 extends BackroomsGen {

	// default params
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

	// noise
	public final FastNoiseLiteD noiseIkeaWalls;

	// params
	public final AtomicDouble thresh_wall_L1 = new AtomicDouble(DEFAULT_THRESH_WALL_L1);
	public final AtomicDouble thresh_wall_H1 = new AtomicDouble(DEFAULT_THRESH_WALL_H1);
	public final AtomicDouble thresh_wall_L2 = new AtomicDouble(DEFAULT_THRESH_WALL_L2);
	public final AtomicDouble thresh_wall_H2 = new AtomicDouble(DEFAULT_THRESH_WALL_H2);

	// blocks
	public final AtomicReference<String> block_wall        = new AtomicReference<String>(null);
	public final AtomicReference<String> block_wall_stripe = new AtomicReference<String>(null);
	public final AtomicReference<String> block_subfloor    = new AtomicReference<String>(null);
	public final AtomicReference<String> block_subceiling  = new AtomicReference<String>(null);
	public final AtomicReference<String> block_floor       = new AtomicReference<String>(null);
	public final AtomicReference<String> block_ceiling     = new AtomicReference<String>(null);



	public Gen_308(final BackroomsLevel backlevel, final int seed,
			final int level_y, final int level_h) {
		super(backlevel, seed, level_y, level_h);
		// noise
		this.noiseIkeaWalls = this.register(new FastNoiseLiteD());
	}



	@Override
	public int getLevelNumber() {
		return 308;
	}



	@Override
	public void generate(final PreGenData pregen,
			final LinkedList<Tuple<BlockPlotter, StringBuilder[][]>> plots,
			final ChunkData chunk, final int chunkX, final int chunkZ) {
		if (!ENABLE_GEN_308) return;
		final BlockData block_wall        = StringToBlockData(this.block_wall,        DEFAULT_BLOCK_WALL       );
		final BlockData block_wall_stripe = StringToBlockData(this.block_wall_stripe, DEFAULT_BLOCK_WALL_STRIPE);
		final BlockData block_subfloor    = StringToBlockData(this.block_subfloor,    DEFAULT_BLOCK_SUBFLOOR   );
		final BlockData block_subceiling  = StringToBlockData(this.block_subceiling,  DEFAULT_BLOCK_SUBCEILING );
		final BlockData block_floor       = StringToBlockData(this.block_floor,       DEFAULT_BLOCK_FLOOR      );
		final BlockData block_ceiling     = StringToBlockData(this.block_ceiling,     DEFAULT_BLOCK_CEILING    );
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
		final double thresh_wall_L1 = this.thresh_wall_L1.get();
		final double thresh_wall_H1 = this.thresh_wall_H1.get();
		final double thresh_wall_L2 = this.thresh_wall_L2.get();
		final double thresh_wall_H2 = this.thresh_wall_H2.get();
		final double[][] values = new double[18][18];
		for (int iz=0; iz<18; iz++) {
			final int zz = (chunkZ * 16) + iz;
			for (int ix=0; ix<18; ix++) {
				final int xx = (chunkX * 16) + ix;
				values[iz][ix] = this.noiseIkeaWalls.getNoise(xx-1, zz-1);
			}
		}
		final int y  = this.level_y + SUBFLOOR + 1;
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
					(value > thresh_wall_L1 && value < thresh_wall_H1) ||
					(value > thresh_wall_L2 && value < thresh_wall_H2);
				// subfloor
				chunk.setBlock(ix, this.level_y, iz, Material.BEDROCK);
				for (int iy=0; iy<SUBFLOOR; iy++)
					chunk.setBlock(ix, this.level_y+iy+1, iz, block_subfloor);
				// subceiling
				if (ENABLE_TOP_308)
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
					if (ENABLE_TOP_308) {
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
	protected void initNoise(final ConfigurationSection cfgParams) {
		super.initNoise(cfgParams);
		// ikea walls
		this.noiseIkeaWalls.setFrequency(                cfgParams.getDouble("Noise-Wall-Freq"  ) );
		this.noiseIkeaWalls.setCellularJitter(           cfgParams.getDouble("Noise-Wall-Jitter") );
		this.noiseIkeaWalls.setNoiseType(                NoiseType.Cellular                       );
		this.noiseIkeaWalls.setFractalType(              FractalType.PingPong                     );
		this.noiseIkeaWalls.setCellularDistanceFunction( CellularDistanceFunction.Manhattan       );
	}



	@Override
	protected void loadConfig(final ConfigurationSection cfgParams, final ConfigurationSection cfgBlocks) {
		// params
		this.thresh_wall_L1.set(cfgParams.getDouble("Thresh-Wall-L1"));
		this.thresh_wall_H1.set(cfgParams.getDouble("Thresh-Wall-H1"));
		this.thresh_wall_L2.set(cfgParams.getDouble("Thresh-Wall-L2"));
		this.thresh_wall_H2.set(cfgParams.getDouble("Thresh-Wall-H2"));
		// block types
		this.block_wall       .set(cfgBlocks.getString("Wall"       ));
		this.block_wall_stripe.set(cfgBlocks.getString("Wall-Stripe"));
		this.block_subfloor   .set(cfgBlocks.getString("SubFloor"   ));
		this.block_subceiling .set(cfgBlocks.getString("SubCeiling" ));
		this.block_floor      .set(cfgBlocks.getString("Floor"      ));
		this.block_ceiling    .set(cfgBlocks.getString("Ceiling"    ));
	}
	@Override
	protected void configDefaults(final ConfigurationSection cfgParams, final ConfigurationSection cfgBlocks) {
		// params
		cfgParams.addDefault("Noise-Wall-Freq",   DEFAULT_NOISE_WALL_FREQ  );
		cfgParams.addDefault("Noise-Wall-Jitter", DEFAULT_NOISE_WALL_JITTER);
		cfgParams.addDefault("Thresh-Wall-L1",    DEFAULT_THRESH_WALL_L1   );
		cfgParams.addDefault("Thresh-Wall-H1",    DEFAULT_THRESH_WALL_H1   );
		cfgParams.addDefault("Thresh-Wall-L2",    DEFAULT_THRESH_WALL_L2   );
		cfgParams.addDefault("Thresh-Wall-H2",    DEFAULT_THRESH_WALL_H2   );
		// block types
		cfgBlocks.addDefault("Wall",        DEFAULT_BLOCK_WALL       );
		cfgBlocks.addDefault("Wall-Stripe", DEFAULT_BLOCK_WALL_STRIPE);
		cfgBlocks.addDefault("SubFloor",    DEFAULT_BLOCK_SUBFLOOR   );
		cfgBlocks.addDefault("SubCeiling",  DEFAULT_BLOCK_SUBCEILING );
		cfgBlocks.addDefault("Floor",       DEFAULT_BLOCK_FLOOR      );
		cfgBlocks.addDefault("Ceiling",     DEFAULT_BLOCK_CEILING    );
	}



}
