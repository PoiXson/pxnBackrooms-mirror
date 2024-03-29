package com.poixson.backrooms.gens;

import static com.poixson.backrooms.worlds.Level_033.ENABLE_GEN_033;
import static com.poixson.backrooms.worlds.Level_033.ENABLE_TOP_033;
import static com.poixson.utils.BlockUtils.StringToBlockData;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

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


// 33 | Run For Your Life!
public class Gen_033 extends BackroomsGen {

	// default params
	public static final double DEFAULT_NOISE_FLOOR_FREQ   = 0.1;
	public static final int    DEFAULT_NOISE_FLOOR_OCTAVE = 2;
	public static final double DEFAULT_NOISE_FLOOR_GAIN   = 2.0;
	public static final double DEFAULT_THRESH_FLOOR       =-0.2;
	public static final double DEFAULT_THRESH_HAZARD      = 0.7;
	public static final int    DEFAULT_DANGER_CHUNKS      = 3;

	// default blocks
	public static final String DEFAULT_BLOCK_WALL       = "minecraft:blackstone";
	public static final String DEFAULT_BLOCK_CEILING    = "minecraft:glowstone";
	public static final String DEFAULT_BLOCK_FLOOR      = "minecraft:polished_deepslate";
	public static final String DEFAULT_BLOCK_FLOOR_SAFE = "minecraft:cracked_deepslate_tiles";
	public static final String DEFAULT_BLOCK_SUBFLOOR   = "minecraft:dark_oak_slab[type=top]";
	public static final String DEFAULT_BLOCK_PLATE      = "minecraft:stone_pressure_plate";
	public static final String DEFAULT_BLOCK_HAZARD     = "minecraft:bricks";

	// noise
	public final FastNoiseLiteD noiseFloor;

	// params
	public final AtomicDouble  noise_floor_freq   = new AtomicDouble( DEFAULT_NOISE_FLOOR_FREQ  );
	public final AtomicInteger noise_floor_octave = new AtomicInteger(DEFAULT_NOISE_FLOOR_OCTAVE);
	public final AtomicDouble  noise_floor_gain   = new AtomicDouble( DEFAULT_NOISE_FLOOR_GAIN  );
	public final AtomicDouble thresh_floor        = new AtomicDouble(DEFAULT_THRESH_FLOOR       );
	public final AtomicDouble thresh_hazard       = new AtomicDouble(DEFAULT_THRESH_HAZARD      );
	public final AtomicInteger danger_chunks      = new AtomicInteger(DEFAULT_DANGER_CHUNKS     );

	// blocks
	public final AtomicReference<String> block_wall       = new AtomicReference<String>(null);
	public final AtomicReference<String> block_ceiling    = new AtomicReference<String>(null);
	public final AtomicReference<String> block_floor      = new AtomicReference<String>(null);
	public final AtomicReference<String> block_floor_safe = new AtomicReference<String>(null);
	public final AtomicReference<String> block_subfloor   = new AtomicReference<String>(null);
	public final AtomicReference<String> block_plate      = new AtomicReference<String>(null);
	public final AtomicReference<String> block_hazard     = new AtomicReference<String>(null);



	public Gen_033(final BackroomsLevel backlevel, final int seed,
			final int level_y, final int level_h) {
		super(backlevel, seed, level_y, level_h);
		// noise
		this.noiseFloor = this.register(new FastNoiseLiteD());
	}



	@Override
	public void initNoise() {
		super.initNoise();
		// pool rooms
		this.noiseFloor.setFrequency(     this.noise_floor_freq  .get());
		this.noiseFloor.setFractalOctaves(this.noise_floor_octave.get());
		this.noiseFloor.setFractalGain(   this.noise_floor_gain  .get());
		this.noiseFloor.setFractalType(FastNoiseLiteD.FractalType.FBm);
	}



	@Override
	public void generate(final PreGenData pregen,
			final LinkedList<Tuple<BlockPlotter, StringBuilder[][]>> plots,
			final ChunkData chunk, final int chunkX, final int chunkZ) {
		if (!ENABLE_GEN_033) return;
		final BlockData block_wall       = StringToBlockData(this.block_wall,       DEFAULT_BLOCK_WALL      );
		final BlockData block_ceiling    = StringToBlockData(this.block_ceiling,    DEFAULT_BLOCK_CEILING   );
		final BlockData block_floor      = StringToBlockData(this.block_floor,      DEFAULT_BLOCK_FLOOR     );
		final BlockData block_floor_safe = StringToBlockData(this.block_floor_safe, DEFAULT_BLOCK_FLOOR_SAFE);
		final BlockData block_subfloor   = StringToBlockData(this.block_subfloor,   DEFAULT_BLOCK_SUBFLOOR  );
		final BlockData block_plate      = StringToBlockData(this.block_plate,      DEFAULT_BLOCK_PLATE     );
		final BlockData block_hazard     = StringToBlockData(this.block_hazard,     DEFAULT_BLOCK_HAZARD    );
		if (block_wall       == null) throw new RuntimeException("Invalid block type for level 33 Wall"      );
		if (block_ceiling    == null) throw new RuntimeException("Invalid block type for level 33 Ceiling"   );
		if (block_floor      == null) throw new RuntimeException("Invalid block type for level 33 Floor"     );
		if (block_floor_safe == null) throw new RuntimeException("Invalid block type for level 33 Floor-Safe");
		if (block_subfloor   == null) throw new RuntimeException("Invalid block type for level 33 SubFloor"  );
		if (block_plate      == null) throw new RuntimeException("Invalid block type for level 33 Plate"     );
		if (block_hazard     == null) throw new RuntimeException("Invalid block type for level 33 Hazard"    );
		final double thresh_floor  = this.thresh_floor .get();
		final double thresh_hazard = this.thresh_hazard.get();
		final int danger_chunks    = this.danger_chunks.get() + 1;
		double valueFloor;
		int xx, zz;
		boolean safe;
		for (int iz=0; iz<16; iz++) {
			zz = (chunkZ * 16) + iz;
			for (int ix=0; ix<16; ix++) {
				xx = (chunkX * 16) + ix;
				// fill bedrock sky
				if (ENABLE_TOP_033) {
					for (int iy=this.level_y+this.level_h; iy<320; iy++)
						chunk.setBlock(ix, iy, iz, Material.BEDROCK);
				}
				switch (ix) {
				// bedrock wall
				case  0: case  1:
				case 14: case 15:
					for (int iy=(ENABLE_TOP_033?-64:-2); iy<this.level_h; iy++)
						chunk.setBlock(ix, iy+this.level_y, iz, Material.BEDROCK);
					break;
				// inside wall
				case 2:
				case 13:
					for (int iy=-2; iy<this.level_h; iy++)
						chunk.setBlock(ix, iy+this.level_y, iz, block_wall);
					break;
				default: {
					valueFloor = 0.0 - this.noiseFloor.getNoise(xx, zz*2);
					safe = (chunkZ % danger_chunks == 0);
					// ceiling
					if (ENABLE_TOP_033)
						chunk.setBlock(ix, this.level_y+this.level_h-1, iz, block_ceiling);
					// floor
					if (safe) {
						if (chunkZ == 0
						||  valueFloor > thresh_floor) {
							chunk.setBlock(ix, this.level_y,   iz, block_floor_safe);
							chunk.setBlock(ix, this.level_y-1, iz, block_floor_safe);
						}
					} else {
						if (valueFloor > thresh_floor) {
							chunk.setBlock(ix, this.level_y+1, iz, block_plate );
							chunk.setBlock(ix, this.level_y,   iz, block_floor );
							final long mod_tnt = (ix+iz) % 5;
							if (mod_tnt == 0 || mod_tnt == 2) chunk.setBlock(ix, this.level_y-1, iz, Material.GLOWSTONE);
							else                              chunk.setBlock(ix, this.level_y-1, iz, Material.TNT);
							chunk.setBlock(ix, this.level_y-2, iz, block_subfloor);
							if (valueFloor > thresh_hazard) {
								chunk.setBlock(ix, this.level_y+2, iz, block_hazard);
								chunk.setBlock(ix, this.level_y+1, iz, block_hazard);
							}
						}
					}
					break;
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
			final ConfigurationSection cfg = this.plugin.getConfigLevelParams(33);
			this.noise_floor_freq  .set(cfg.getDouble("Noise-Floor-Freq"  ));
			this.noise_floor_octave.set(cfg.getInt(   "Noise-Floor-Octave"));
			this.noise_floor_gain  .set(cfg.getDouble("Noise-Floor-Gain"  ));
			this.thresh_floor      .set(cfg.getDouble("Thresh-Floor"      ));
			this.thresh_hazard     .set(cfg.getDouble("Thresh-Hazard"     ));
			this.danger_chunks     .set(cfg.getInt(   "Danger-Chunks"     ));
		}
		// block types
		{
			final ConfigurationSection cfg = this.plugin.getConfigLevelBlocks(33);
			this.block_wall      .set(cfg.getString("Wall"      ));
			this.block_ceiling   .set(cfg.getString("Ceiling"   ));
			this.block_floor     .set(cfg.getString("Floor"     ));
			this.block_floor_safe.set(cfg.getString("Floor-Safe"));
			this.block_subfloor  .set(cfg.getString("SubFloor"  ));
			this.block_plate     .set(cfg.getString("Plate"     ));
			this.block_hazard    .set(cfg.getString("Hazard"    ));
		}
	}
	@Override
	public void configDefaults() {
		// params
		{
			final ConfigurationSection cfg = this.plugin.getConfigLevelParams();
			cfg.addDefault("Level33.Noise-Floor-Freq",   DEFAULT_NOISE_FLOOR_FREQ  );
			cfg.addDefault("Level33.Noise-Floor-Octave", DEFAULT_NOISE_FLOOR_OCTAVE);
			cfg.addDefault("Level33.Noise-Floor-Gain",   DEFAULT_NOISE_FLOOR_GAIN  );
			cfg.addDefault("Level33.Thresh-Floor",       DEFAULT_THRESH_FLOOR      );
			cfg.addDefault("Level33.Thresh-Hazard",      DEFAULT_THRESH_HAZARD     );
			cfg.addDefault("Level33.Danger-Chunks",      DEFAULT_DANGER_CHUNKS     );
		}
		// block types
		{
			final ConfigurationSection cfg = this.plugin.getConfigLevelBlocks();
			cfg.addDefault("Level33.Wall",       DEFAULT_BLOCK_WALL      );
			cfg.addDefault("Level33.Ceiling",    DEFAULT_BLOCK_CEILING   );
			cfg.addDefault("Level33.Floor",      DEFAULT_BLOCK_FLOOR     );
			cfg.addDefault("Level33.Floor-Safe", DEFAULT_BLOCK_FLOOR_SAFE);
			cfg.addDefault("Level33.SubFloor",   DEFAULT_BLOCK_SUBFLOOR  );
			cfg.addDefault("Level33.Plate",      DEFAULT_BLOCK_PLATE     );
			cfg.addDefault("Level33.Hazard",     DEFAULT_BLOCK_HAZARD    );
		}
	}



}
